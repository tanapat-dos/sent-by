package com.reelshelf.app.metadata

import com.reelshelf.app.urls.Platform
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun interface SimpleHttpGet {
    fun get(url: String): SimpleHttpResponse
}

data class SimpleHttpResponse(
    val code: Int,
    val body: String?,
)

class UrlSimpleHttpGet : SimpleHttpGet {
    override fun get(url: String): SimpleHttpResponse {
        val connection =
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SentBy/0.2")
            }
        return try {
            val code = connection.responseCode
            val stream =
                if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }
            SimpleHttpResponse(code, body)
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Standards-based YouTube oEmbed fetcher (no HTML scraping).
 */
class YoutubeOEmbedFetcher(
    private val http: SimpleHttpGet = UrlSimpleHttpGet(),
) : MetadataFetcher {
    override val platform: Platform = Platform.YOUTUBE

    override fun fetch(canonicalUrl: String, platformContentId: String?): MetadataResult {
        return try {
            val endpoint =
                "https://www.youtube.com/oembed?format=json&url=" +
                    URLEncoder.encode(canonicalUrl, Charsets.UTF_8.name())
            val response = http.get(endpoint)
            when (response.code) {
                200 -> {
                    val body = response.body.orEmpty()
                    MetadataResult(
                        platform = platform,
                        title = jsonStringField(body, "title"),
                        creatorName = jsonStringField(body, "author_name"),
                        thumbnailUrl = jsonStringField(body, "thumbnail_url"),
                    )
                }
                401, 403 ->
                    MetadataResult(
                        platform = platform,
                        failure = MetadataFailure.LoginRequired("HTTP ${response.code}"),
                    )
                404 ->
                    MetadataResult(
                        platform = platform,
                        failure = MetadataFailure.Permanent("HTTP 404"),
                    )
                else ->
                    MetadataResult(
                        platform = platform,
                        failure = MetadataFailure.Retryable("HTTP ${response.code}"),
                    )
            }
        } catch (e: java.net.SocketTimeoutException) {
            MetadataResult(platform = platform, failure = MetadataFailure.Retryable(e.message))
        } catch (e: Exception) {
            MetadataResult(platform = platform, failure = MetadataFailure.Retryable(e.message))
        }
    }

    companion object {
        private fun jsonStringField(json: String, key: String): String? {
            val regex = Regex(""""$key"\s*:\s*"((?:\\.|[^"\\])*)"""")
            val match = regex.find(json) ?: return null
            return match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .ifBlank { null }
        }
    }
}
