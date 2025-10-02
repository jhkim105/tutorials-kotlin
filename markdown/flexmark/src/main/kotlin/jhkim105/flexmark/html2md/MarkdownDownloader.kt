package jhkim105.flexmark.html2md

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class MarkdownDownloader(
    private val restClient: RestClient
) {

    fun downloadAsMarkdown(url: String, outputDir: File = File(".")): File {
        val html = fetchHtml(url)
        val markdown = FlexmarkHtmlConverter.builder().build().convert(html)

        val fileName = buildFileName(url, html)
        val outFile = uniqueFile(File(outputDir, "$fileName.md"))

        outFile.parentFile?.mkdirs()
        outFile.writeText(markdown, Charsets.UTF_8)
        return outFile
    }

    private fun fetchHtml(url: String): String {
        val response = restClient.get()
            .uri(url)
            .retrieve()
            .toEntity(String::class.java)

        if (!response.statusCode.is2xxSuccessful) {
            throw IllegalStateException("HTTP ${response.statusCode} from $url")
        }

        return response.body ?: error("Empty body from $url")
    }

    /** 파일명 생성: path 마지막 세그먼트 → <title> → host → timestamp */
    private fun buildFileName(url: String, html: String): String {
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
        return safe.ifBlank {
            val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            "page_$ts"
        }
    }

    private fun extractTitle(html: String): String? {
        val p = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val m = p.matcher(html)
        return if (m.find()) {
            m.group(1)?.replace(Regex("\\s+"), " ")?.trim()
        } else null
    }

    private fun sanitizeFileName(name: String, maxLen: Int = 80): String {
        val cleaned = name
            .replace(Regex("[\\\\/:*?\"<>|]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.take(maxLen)
    }

    private fun uniqueFile(base: File): File {
        if (!base.exists()) return base
        val name = base.nameWithoutExtension
        val ext = base.extension.let { if (it.isBlank()) "" else ".$it" }
        var i = 1
        while (true) {
            val f = File(base.parentFile, "$name ($i)$ext")
            if (!f.exists()) return f
            i++
        }
    }
}
