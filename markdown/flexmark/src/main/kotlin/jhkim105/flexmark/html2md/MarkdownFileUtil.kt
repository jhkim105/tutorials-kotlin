package jhkim105.flexmark.html2md

import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object MarkdownFileUtil {

    fun decideBaseName(url: String, html: String): String {
        val uri = URI(url)
        val lastSeg = run {
            val raw = uri.path?.substringAfterLast('/') ?: ""
            val decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8)
            decoded.substringBeforeLast('.') // 확장자 제거
        }.trim()

        val fromPath = lastSeg.takeIf { it.isNotBlank() && it.lowercase() !in setOf("index", "default") }
        val title = extractTitle(html)?.takeIf { it.isNotBlank() }

        val base = when {
            fromPath != null -> fromPath
            title != null -> title
            uri.host != null -> uri.host
            else -> "download"
        }

        val safe = sanitizeFileName(base)
        return safe.ifBlank { timestampName() }
    }

    fun unique(target: File): File {
        if (!target.exists()) return target
        val name = target.nameWithoutExtension
        val ext = target.extension.let { if (it.isBlank()) "" else ".$it" }
        var i = 1
        while (true) {
            val f = File(target.parentFile, "$name ($i)$ext")
            if (!f.exists()) return f
            i++
        }
    }

    private fun extractTitle(html: String): String? {
        val p = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val m = p.matcher(html)
        return if (m.find()) m.group(1)?.replace(Regex("\\s+"), " ")?.trim() else null
    }

    private fun sanitizeFileName(name: String, maxLen: Int = 80): String {
        val cleaned = name
            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.take(maxLen)
    }

    private fun timestampName(): String =
        "page_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
}
