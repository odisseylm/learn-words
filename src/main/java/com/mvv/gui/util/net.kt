package com.mvv.gui.util

import org.apache.commons.io.FileUtils
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


data class NetSettings (
    val timeout: Duration = Duration.ofSeconds(10),
    val ignoreCertificate: Boolean = false
)


private val defaultNetSettings = NetSettings()

fun downloadUrl(url: String, settings: NetSettings = defaultNetSettings): ByteArray =
    downloadUrl(URL(url), settings)

fun downloadUrl(url: URI, settings: NetSettings = defaultNetSettings): ByteArray =
    downloadUrl(url.toURL(), settings)

/*
fun downloadUrl_old(url: URL, timeout: Long): ByteArray =
    url.openConnection()
        .also { it.connectTimeout = timeout.toInt(); it.readTimeout = timeout.toInt() }
        .getInputStream()
        .use { it.readAllBytes() }
*/

fun downloadUrl(url: URL, settings: NetSettings = defaultNetSettings): ByteArray {

    if (url.protocol == "file")
        return Files.readAllBytes(FileUtils.toFile(url).toPath())

    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(url.toURI())
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

    return httpResponse.body()
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
