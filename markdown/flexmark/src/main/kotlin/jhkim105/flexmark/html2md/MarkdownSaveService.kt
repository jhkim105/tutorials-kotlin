package jhkim105.flexmark.html2md

import org.springframework.stereotype.Service
import java.io.File

@Service
class MarkdownSaveService(
    private val htmlDownloader: HtmlDownloader,
    private val converter: HtmlToMarkdownConverter
) {
    /**
     * 1) HTML 다운로드
     * 2) Markdown 변환
     * 3) 파일 저장 (파일명 결정은 유틸에 위임)
     */
    fun downloadAndSaveAsMarkdown(url: String, outputDir: File = File(".")): File {
        val html = htmlDownloader.fetch(url)
        val md = converter.convert(html)

        val base = MarkdownFileUtil.decideBaseName(url, html)
        val out = MarkdownFileUtil.unique(File(outputDir, "$base.md"))

        out.parentFile?.mkdirs()
        out.writeText(md, Charsets.UTF_8)
        return out
    }
}
