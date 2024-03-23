package com.mvv.gui.memoword

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset


@Suppress("FunctionName")
internal fun GET(uri: String): HttpRequest =
    HttpRequest.newBuilder(URI(uri)).GET().build()

internal fun HttpClient.sendGet(uri: String): HttpResponse<String> =
    this.send(GET(uri), HttpResponse.BodyHandlers.ofString())

internal inline fun <reified T> HttpClient.sendJsonGet(uri: String, objectMapper: ObjectMapper): HttpResponse<T> =
    this.send(
        GET(uri),
        jsonBodyHandler<T>(objectMapper)
    )


fun String.urlEncode(charset: Charset = Charsets.UTF_8): String = URLEncoder.encode(this, charset)


fun createDefaultObjectMapper(): ObjectMapper = ObjectMapper().also {
    it.registerModule(KotlinModule.Builder().build())
}


inline fun <reified T> jsonBodyHandler(objectMapper: ObjectMapper): HttpResponse.BodyHandler<T> =
    jsonBodyHandler(object : TypeReference<T>() {}, objectMapper)
fun <T> jsonBodyHandler(valueTypeRef: TypeReference<T>, objectMapper: ObjectMapper): HttpResponse.BodyHandler<T> =
    HttpResponse.BodyHandler {
        HttpResponse.BodySubscribers.mapping( HttpResponse.BodySubscribers.ofByteArray() ) {
            objectMapper.readValue(it, valueTypeRef)
        }
    }


fun ofFormData(data: Map<Any, Any>): HttpRequest.BodyPublisher {
    val str = StringBuilder()
    for ((key, value) in data) {
        if (str.isNotEmpty()) str.append("&")

        str.append(key.toString().urlEncode())
        str.append("=")
        str.append(value.toString().urlEncode())
    }
    return HttpRequest.BodyPublishers.ofString(str.toString())
}


fun Element.attributeValue(attrName: String): String? {
    @Suppress("UNNECESSARY_SAFE_CALL") // according to code/javadoc, value CAN be null!
    return this.attribute(attrName)?.value
}
