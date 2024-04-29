package com.mvv.gui.util

import org.apache.commons.io.FileUtils
import java.io.IOException
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


// to avoid deprecation warnings
fun url(url: String): URL = URI(url).toURL()


data class NetSettings (
    val timeout: Duration = Duration.ofSeconds(10),
    val ignoreCertificate: Boolean = false
)


private val defaultNetSettings = NetSettings()

fun downloadUrl(url: String, settings: NetSettings = defaultNetSettings): ByteArray =
    downloadUrl(url(url), settings)

fun downloadUrl(url: URI, settings: NetSettings = defaultNetSettings): ByteArray =
    downloadUrl(url.toURL(), settings)

/*
fun downloadUrl_old(url: URL, timeout: Long): ByteArray =
    url.openConnection()
        .also { it.connectTimeout = timeout.toInt(); it.readTimeout = timeout.toInt() }
        .getInputStream()
        .use { it.readAllBytes() }
*/

fun downloadUrl(url: URL, settings: NetSettings = defaultNetSettings): ByteArray =
    downloadUrlContent(url, settings).bytes


fun downloadUrlText(url: String, settings: NetSettings = defaultNetSettings): String =
    downloadUrlText(url(url), settings)
fun downloadUrlText(url: URI, settings: NetSettings = defaultNetSettings): String =
    downloadUrlText(url.toURL(), settings)
fun downloadUrlText(url: URL, settings: NetSettings = defaultNetSettings): String {

    val content = downloadUrlContent(url, settings)
    val contentTypeStr: String = (content.headers["Content-Type"] ?: content.headers["content-type"])
        ?.first()?.lowercase()
        ?: throw IOException("No Content-Type header.")

    if (!contentTypeStr.startsWith("text/")) throw IOException("Content is not text (content type is [$contentTypeStr]).")

    val contentCharSet = contentTypeStr.substringAfter("charset=", "UTF-8").trim()

    return String(content.bytes, Charset.forName(contentCharSet))
}



class Content (
    val bytes: ByteArray,
    val headers: Map<String, List<String>>,
)

fun downloadUrlContent(url: URL, settings: NetSettings = defaultNetSettings): Content {

    if (url.protocol == "file") {
        val ext = url.file.substringAfter(".", "").lowercase()
        val headers: Map<String, List<String>> = when (ext) {
            "txt" -> mapOf("Content-Type" to listOf("text/plain"))
            "htm", "html" -> mapOf("Content-Type" to listOf("text/html"))
            else -> emptyMap()
        }

        return Content(Files.readAllBytes(FileUtils.toFile(url).toPath()), headers)
    }

    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(url.toURI())
        .headers("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0")
        .timeout(settings.timeout)
        .GET()
        .build()

    val clientBuilder = HttpClient.newBuilder()
        .connectTimeout(settings.timeout)
        //.followRedirects(HttpClient.Redirect.NEVER)

    if (settings.ignoreCertificate)
        clientBuilder.sslContext(createUnsecureSSLContext())

    val httpResponse = clientBuilder
        .build()
        .send(request, HttpResponse.BodyHandlers.ofByteArray())

    return Content(httpResponse.body(), httpResponse.headers().map())
}

fun urlEncode(text: String): String = URLEncoder.encode(text, Charsets.UTF_8)


fun createUnsecureSSLContext(): SSLContext {

    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
    })

    // val allHostsValid = HostnameVerifier { _, _ -> true }

    val sc: SSLContext = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, SecureRandom())
    return sc
}


/*
// singleton
internal enum class WhitelistHostnameVerifier(vararg hostnames: String) : HostnameVerifier {
    //INSTANCE("localhost", "sub.domain.com");
    INSTANCE("localhost");

    val whitelist: MutableSet<String> = synchronizedSet(HashSet(hostnames.toList()))
    private val defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()

    override fun verify(host: String, session: SSLSession): Boolean =
        if (host in whitelist) true
        // !!! important: use default verifier for all other hosts !!!
        else defaultHostnameVerifier.verify(host, session)
}
*/
