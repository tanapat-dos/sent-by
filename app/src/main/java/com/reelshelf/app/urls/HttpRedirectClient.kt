package com.reelshelf.app.urls

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Production redirect client. Prefer injecting fakes in unit tests.
 */
class HttpRedirectClient : RedirectClient {
    override fun resolve(url: String, maxRedirects: Int, timeoutMs: Int): RedirectResolution {
        var current = url
        val hops = mutableListOf<String>()
        var redirects = 0
        return try {
            while (redirects <= maxRedirects) {
                hops.add(current)
                val connection =
                    (URL(current).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = timeoutMs
                        readTimeout = timeoutMs
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", USER_AGENT)
                    }
                try {
                    val code = connection.responseCode
                    when (code) {
                        in 300..399 -> {
                            val location = connection.getHeaderField("Location")
                            if (location.isNullOrBlank()) {
                                return RedirectResolution.Failed(
                                    reason = RedirectResolution.FailureReason.HTTP_ERROR,
                                    lastUrl = current,
                                    redirectCount = redirects,
                                    message = "Redirect without Location ($code)",
                                )
                            }
                            current = resolveLocation(current, location)
                            redirects++
                            if (redirects > maxRedirects) {
                                return RedirectResolution.Failed(
                                    reason = RedirectResolution.FailureReason.TOO_MANY_REDIRECTS,
                                    lastUrl = current,
                                    redirectCount = redirects,
                                )
                            }
                        }
                        in 200..299 -> {
                            return RedirectResolution.Success(
                                finalUrl = current,
                                redirectCount = redirects,
                                hopUrls = hops.toList(),
                            )
                        }
                        401, 403 -> {
                            return RedirectResolution.Failed(
                                reason = RedirectResolution.FailureReason.LOGIN_OR_COOKIE_WALL,
                                lastUrl = current,
                                redirectCount = redirects,
                                message = "HTTP $code",
                            )
                        }
                        else -> {
                            return RedirectResolution.Failed(
                                reason = RedirectResolution.FailureReason.HTTP_ERROR,
                                lastUrl = current,
                                redirectCount = redirects,
                                message = "HTTP $code",
                            )
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            }
            RedirectResolution.Failed(
                reason = RedirectResolution.FailureReason.TOO_MANY_REDIRECTS,
                lastUrl = current,
                redirectCount = redirects,
            )
        } catch (_: java.net.SocketTimeoutException) {
            RedirectResolution.Failed(
                reason = RedirectResolution.FailureReason.TIMEOUT,
                lastUrl = current,
                redirectCount = redirects,
            )
        } catch (e: IOException) {
            RedirectResolution.Failed(
                reason = RedirectResolution.FailureReason.NETWORK,
                lastUrl = current,
                redirectCount = redirects,
                message = e.message,
            )
        }
    }

    private fun resolveLocation(current: String, location: String): String {
        return if (location.startsWith("http://") || location.startsWith("https://")) {
            location
        } else {
            URL(URL(current), location).toString()
        }
    }

    companion object {
        private const val USER_AGENT = "SentBy/0.1 (+phase0-feasibility; mailto:dev@sentby.local)"
    }
}
