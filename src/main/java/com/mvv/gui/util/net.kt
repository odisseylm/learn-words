package com.mvv.gui.util

import java.net.URI
import java.net.URL


fun downloadUrl(url: String, timeout: Long = 10_000): ByteArray = downloadUrl(URL(url), timeout)

fun downloadUrl(url: URI, timeout: Long = 10_000): ByteArray = downloadUrl(url.toURL(), timeout)

fun downloadUrl(url: URL, timeout: Long = 10_000): ByteArray =
    url.openConnection()
        .also { it.connectTimeout = timeout.toInt(); it.readTimeout = timeout.toInt() }
        .getInputStream()
        .use { it.readAllBytes() }
