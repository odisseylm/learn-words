package com.mvv.gui.words

import com.mvv.gui.dictionary.getProjectDirectory
import com.mvv.gui.memoword.parseAsHtml
import com.mvv.gui.util.filterNotBlank
import com.mvv.gui.util.filterNotEmpty
import com.mvv.gui.util.safeSubstring
import org.jsoup.nodes.Document
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Path
import java.sql.Blob
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists


private val log = mu.KotlinLogging.logger {}


@Suppress("SqlDialectInspection", "SqlNoDataSourceInspection")
class EnglishSynonyms : AutoCloseable {

    init { Class.forName("org.sqlite.JDBC")  }

    private val toCacheInMemory = true
    private val cacheMap = ConcurrentHashMap<String, List<String>>()

    private val dbFile: Path = run {
        val projectDirectory = getProjectDirectory(javaClass)
        val possibleLocations = listOf(
            projectDirectory.resolve("temp/english-synonyms-newdatabase.db"),
        )
        possibleLocations.find { it.exists() }
            ?: throw UncheckedIOException(IOException("english-synonyms-newdatabase.db is not found."))
    }

    private val connection = DriverManager.getConnection("jdbc:sqlite:$dbFile")

    override fun close() { connection.close() }

    fun getSynonymsOf(wordOrPhrase: String): List<String> =
               if (toCacheInMemory) cacheMap.computeIfAbsent(wordOrPhrase) { getSynonymsOfImpl(it) }
               else getSynonymsOfImpl(wordOrPhrase)

    private fun getSynonymsOfImpl(wordOrPhrase: String): List<String> {
        connection.prepareStatement(" select result from table_english_synonyms where word = ? ").use { st ->
            st.setString(1, wordOrPhrase)

            st.executeQuery().use { rs ->
                val strResults = rs.asIterable()
                    .map { it.getString(1) }
                    .filterNotBlank()
                    .toList()

                return strResults.flatMap { it.parseSynonymsFromEnglishResultColumn() }
            }
        }
    }

    /*
    fun aaa() {

        connection.createStatement().use { st ->

            val schemas = mutableListOf<String>()
            connection.metaData.schemas.use { rs -> while (rs.next()) { schemas.add(rs.getString(1)!!) } }
            println("schemas: $schemas")

            val catalogs = mutableListOf<String>()
            connection.metaData.catalogs.use { rs -> while (rs.next()) { catalogs.add(rs.getString(1)!!) } }
            println("catalogs: $catalogs")

            val tables = mutableListOf<String>()
            for (catalog in catalogs) {
                connection.metaData.getTables(catalog, "", "", arrayOf<String>()).use {
                    rs -> while (rs.next()) { tables.add(catalog + " => " + rs.getString(1)!!) } }
            }

            println("tables: $tables")


            val tables22 = listOf(
                "Proverbs",
                    //  "_id"   INTEGER,
                    // "proverbs"      TEXT,
                    // "meanings"      TEXT,
                    // "fav"   TEXT DEFAULT 0
                "Idioms",
                    // "_id"   INTEGER,
                    //"Idiom" TEXT,
                    //"Define"        TEXT,
                    //"Ex"    TEXT,
                    //"fav"   TEXT
                "table_english_synonyms",
                    // "edited"        TEXT,
                    //"favorites"     TEXT,
                    //"_id"   integer PRIMARY KEY AUTOINCREMENT,
                    //"word"  TEXT,
                    //"result"        TEXT
                )

            val rowCount = st.executeQuery(" select count(*) from table_english_synonyms ").use { rs ->
                rs.next()
                rs.getInt(1)
            } //as Number
            println("rowCount: $rowCount")

            /*
            st.executeQuery(" select * from table_english_synonyms ").use { rs ->
                rs.next()
                rs.getInt(1)
            } //as Number
            println("rowCount: $rowCount")
            */

            //st.executeQuery(" select * from table_english_synonyms ").use { rs: ResultSet ->
            //    while (rs.next()) {
            //        ;
            //    }
            //}

            tables22.forEach {
                printRows(connection, it, 10)
            }

            st.executeQuery("select * from table_english_synonyms where word = 'kill'").use { rs ->
                printTableQuery(rs, 10)
            }

            st.executeQuery("select * from table_english_synonyms where word = 'terrible'").use { rs ->
                printTableQuery(rs, 10)
            }
        }
    }
    */
}

internal fun String.parseSynonymsFromEnglishResultColumn(): List<String> {

    val document: Document = this.parseAsHtml()
    val body = document.body()

    var synonymsEls = body.getElementsContainingOwnText("Synonyms:")
    if (synonymsEls.isEmpty()) synonymsEls = body.getElementsContainingOwnText("synonyms")

    if(synonymsEls.isEmpty()) return emptyList()

    val synonymsText = synonymsEls[0].parent()?.ownText()

    return synonymsText?.split(',')?.map { it.trim() }?.filterNotEmpty() ?: emptyList()
}


fun ResultSet.asIterable(): Iterable<ResultSet> = Iterable { ResultSetAsIterator(this) }

private class ResultSetAsIterator(val resultSet: ResultSet) : Iterator<ResultSet> {
    override fun hasNext(): Boolean = resultSet.next()
    override fun next(): ResultSet = resultSet
}


/*
import com.mvv.gui.util.addNotNull
import org.jsoup.nodes.Node


private fun Node.nextSiblings(): List<Node> {
    val nextSiblings = mutableListOf<Node>()

    var next: Node? = this
    do {
        next = next?.nextSibling()
        nextSiblings.addNotNull(next)
    }
    while (next != null)

    return nextSiblings
}
*/


fun printAllSqlLiteTables(dbFile: Path, maxRowCount: Int = 10) {

    Class.forName("org.sqlite.JDBC")

    DriverManager.getConnection("jdbc:sqlite:$dbFile").use { con ->

        val tables: List<String> = con.metaData.getTables(null, null, null, null).use { rs ->
            rs.asIterable().map { it.getString("TABLE_NAME") }
        }

        tables.forEach { table ->
            try { printRowCount(con, table) } catch(_: Exception) { }
            try { printRows(con, table, maxRowCount) }
            catch (ex: Exception) { log.error { "Error of printing table [$table] ($ex)." } }
        }

        if ("items" in tables)
            printRows(con, "items", maxRowCount)
    }
}

private fun printRowCount(con: Connection, table: String) {

    println("\n\n\n--------- TABLE $table -----------------------")

    con.createStatement().use { st ->
        @Suppress("SqlNoDataSourceInspection", "SqlDialectInspection")
        st.executeQuery(" select count(*) from $table ").use { rs -> printTableQuery(rs, 1) }
    }

}

private fun printRows(con: Connection, table: String, maxRowCount: Int) {
    println("\n\n\n--------- TABLE $table -----------------------")
    con.createStatement().use { st ->
        @Suppress("SqlNoDataSourceInspection", "SqlDialectInspection")
        st.executeQuery(" select * from $table ").use { rs -> printTableQuery(rs, maxRowCount) }
    }

}

private fun printTableQuery(rs: ResultSet, maxRowCount: Int) {
    val columnNames = mutableListOf<String>()
    for (i in 1..rs.metaData.columnCount) {
        columnNames.add(rs.metaData.getColumnName(i))
    }

    for (i in 1..maxRowCount) {
        if (!rs.next()) break

        //val columns = mutableListOf<String>()
        //for (j in 1..rs.metaData.columnCount) columns.add(rs.metaData.getColumnName(j))

        println("-------------------------------------------------")
        for (cn in columnNames) {
            val v = rs.getObject(cn)
            println("$cn: $v")

            //if (rs.metaData.getColumnType(columnNames.indexOf(cn) + 1) == Types.BLOB) {
            //    val rs.getBlob(cn)
            //}
            when (v) {
                is String, is Int -> { }

                is Blob ->
                    try {
                        val bytes = v.getBytes(0, v.length().toInt())
                        println(String(bytes, Charsets.UTF_8).safeSubstring(0, 250))
                    } catch (ex: Exception) { ex.printStackTrace() }

                is ByteArray ->
                    try {
                        //println(String(v, Charsets.UTF_8).safeSubstring(0, 250))
                        //println(String(v, Charsets.ISO_8859_1).safeSubstring(0, 250))
                        //println(String(v, java.nio.charset.Charset.forName("CP866")).safeSubstring(0, 250))
                        //println(String(Base64.getDecoder().decode(v)).safeSubstring(0, 250))
                    } catch (ex: Exception) { ex.printStackTrace() }

                else -> println(v.javaClass.name)
            }
        }
    }
}
