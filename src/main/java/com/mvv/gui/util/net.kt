package com.mvv.gui.util

import java.net.URI
import java.net.URL
import java.net.URLEncoder


fun downloadUrl(url: String, timeout: Long = 10_000): ByteArray = downloadUrl(URL(url), timeout)

fun downloadUrl(url: URI, timeout: Long = 10_000): ByteArray = downloadUrl(url.toURL(), timeout)

fun downloadUrl(url: URL, timeout: Long = 10_000): ByteArray =
    url.openConnection()
        .also { it.connectTimeout = timeout.toInt(); it.readTimeout = timeout.toInt() }
        .getInputStream()
        .use { it.readAllBytes() }

fun urlEncode(text: String): String = URLEncoder.encode(text, Charsets.UTF_8)
