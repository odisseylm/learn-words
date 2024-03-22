package com.mvv.gui.memoword

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import java.net.CookieManager
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists



class PersistentCookieStore(
    private val file: Path,
    private val cookieStore: CookieStore = CookieManager().cookieStore,
) : CookieStore by cookieStore {

    private val log = mu.KotlinLogging.logger {}

    private val objectMapper = createDefaultObjectMapper().also {
        it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun load() {
        try {
            if (file.exists() && Files.size(file) > 0) {
                val cookies = objectMapper.readValue(file.toFile(), object : TypeReference<List<CookieEntry>>() {})
                cookies.forEach { cookieStore.add(it.uri, it.toHttpCookie()) }
            }
        }
        catch (ex: Exception) {
            log.warn { "Error of restoring cookies." }
        }
    }

    fun save() {
        val asEntries: List<CookieEntry> = cookieStore.urIs.flatMap { uri ->
            cookieStore.get(uri).map { it.toCookieEntry(uri) } }

        file.parent.createDirectories()
        objectMapper.writeValue(file.toFile(), asEntries)
    }
}

internal data class CookieEntry (
    val uri: URI,
    val name: String,
    val value: String,
    val comment: String?,
    val commentURL: String?,
    val discard: Boolean?,
    val domain: String?,
    val httpOnly: Boolean?,
    val maxAge: Long?,
    val path: String?,
    val portlist: String?,
    val secure: Boolean?,
    val version: Int?,
) {
    @Suppress("DuplicatedCode")
    fun toHttpCookie(): HttpCookie {
        val c = HttpCookie(name, value)
        comment   ?.also { c.comment    = it }
        commentURL?.also { c.commentURL = it }
        discard   ?.also { c.discard    = it }
        domain    ?.also { c.domain     = it }
        httpOnly  ?.also { c.isHttpOnly = it }
        maxAge    ?.also { c.maxAge     = it }
        path      ?.also { c.path       = it }
        portlist  ?.also { c.portlist   = it }
        secure    ?.also { c.secure     = it }
        version   ?.also { c.version    = it }

        return c
    }
}

internal fun HttpCookie.toCookieEntry(uri: URI): CookieEntry = CookieEntry(
    uri      = uri,
    name     = this.name,
    value    = this.value,
    comment  = this.comment,
    commentURL = this.commentURL,
    discard  = this.discard,
    domain   = this.domain,
    httpOnly = this.isHttpOnly,
    maxAge   = this.maxAge,
    path     = this.path,
    portlist = this.portlist,
    secure   = this.secure,
    version  = this.version,
)
