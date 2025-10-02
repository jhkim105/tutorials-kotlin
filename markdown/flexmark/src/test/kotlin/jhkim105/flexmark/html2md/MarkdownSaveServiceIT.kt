// src/test/kotlin/com/example/MarkdownSaveServiceIT.kt
package jhkim105.flexmark.html2md

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.client.RestClient
import java.io.File
import java.nio.file.Path

/**
 * 실제 네트워크를 사용해 example.com HTML을 내려받고
 * Markdown으로 변환하여 파일로 저장하는 통합 테스트.
 */
class MarkdownSaveServiceIT {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `example_com 페이지를 내려받아 Markdown으로 저장한다`() {
        // given
        val url = "https://example.com/"
        // RestClient 는 별도 Apache HC 의존성 없이 기본 빌더로 생성
        val restClient = RestClient.builder()
            .defaultHeader("User-Agent", "MarkdownFetcher/1.0 (test)")
            .build()

        val downloader = HtmlDownloader(restClient)            // 실제 다운로드 사용
        val converter = HtmlToMarkdownConverter()              // 실제 변환기 사용
        val service = MarkdownSaveService(downloader, converter)

        val outDir = tempDir.toFile()

        // when
        val saved: File = service.downloadAndSaveAsMarkdown(url, outDir)

        // then
        assertTrue(saved.exists(), "파일이 생성되어야 합니다")
        val md = saved.readText()

        // example.com 은 <title>Example Domain</title> 를 제공
        // 경로 세그먼트가 비어 있으므로 파일명은 제목 기반 → "Example Domain.md" (환경에 따라 (1) 등이 붙을 수 있어 name 검사 완화)
        assertTrue(saved.name.endsWith(".md"), "확장자는 .md 여야 합니다")
        assertTrue(saved.name.contains("Example Domain"), "파일명에 제목이 반영되어야 합니다")

        // flexmark 변환 결과에 핵심 텍스트가 포함되는지만 확인(개행/서식은 구현체에 따라 다를 수 있음)
        assertTrue(
            md.contains("Example Domain"),
            "변환된 Markdown에 페이지 제목이 포함되어야 합니다"
        )
        assertTrue(
            md.contains("illustrative examples") || md.contains("illustrative"),
            "본문 주요 문구가 포함되어야 합니다"
        )
    }
}
