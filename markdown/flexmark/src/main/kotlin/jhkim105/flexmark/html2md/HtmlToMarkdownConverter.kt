package jhkim105.flexmark.html2md

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.springframework.stereotype.Component

@Component
class HtmlToMarkdownConverter {
    private val converter = FlexmarkHtmlConverter.builder().build()

    fun convert(html: String): String = converter.convert(html)
}