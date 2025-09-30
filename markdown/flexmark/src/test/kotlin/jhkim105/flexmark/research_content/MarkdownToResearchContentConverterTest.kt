package jhkim105.flexmark.research_content
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class MarkdownToResearchContentConverterTest : StringSpec({

    val converter = MarkdownToResearchContentConverter()
    val sampleMarkdown: String = javaClass.getResource("/research_content.md")!!.readText()

    "샘플 마크다운을 DTO로 변환하면 주요 블록이 생성된다" {
        val body = converter.convert(sampleMarkdown)

        // 전체 블록이 어느 정도 나와야 함
        body.size.shouldBeGreaterThan(10)

        // INTRO / TITLE / CALL_OUT / PARAGRAPH / DROPDOWN / UNORDERED_LIST / MARGIN / IMAGES 존재 확인
        body.any { it is IntroBlock } shouldBe true
        body.any { it is TitleBlock } shouldBe true
        body.any { it is CallOutBlock } shouldBe true
        body.any { it is ParagraphBlock } shouldBe true
        body.any { it is DropdownBlock } shouldBe true
        body.any { it is UnorderedListBlock } shouldBe true
        body.any { it is MarginBlock } shouldBe true
        body.any { it is ImagesBlock } shouldBe true

        // DROPDOWN 내부 구조 확인
        val dropdown = body.first { it is DropdownBlock }.shouldBeInstanceOf<DropdownBlock>()
        dropdown.children.size shouldBe 2
        dropdown.children[0].shouldBeInstanceOf<DropdownSummary>()
        dropdown.children[1].shouldBeInstanceOf<DropdownDescriptions>()

        // MARGIN 값 확인
        val margin = body.first { it is MarginBlock }.shouldBeInstanceOf<MarginBlock>()
        margin.height shouldBe "40"

        // 링크 파싱 확인: STOCK_LINK가 파라미터까지 잘 들어갔는지
        val paragraphWithStock = body.filterIsInstance<ParagraphBlock>()
            .first { p -> p.children.any { it is StockLink } }
        val stock = paragraphWithStock.children.first { it is StockLink }.shouldBeInstanceOf<StockLink>()
        stock.stockId shouldBe "AAPL"
        stock.isinCode shouldBe "US0378331005"
        stock.exchangeId shouldBe "201"
    }

    "코드펜스 info 대소문자 허용 (Intro/Dropdown/Margin)" {
        val md = """
            ```Intro
            hello
            ```
            
            ```DROPDOWN
            summary
            - a
            - b
            ```
            
            ```Margin
            24
            ```
        """.trimIndent()

        val body = converter.convert(md)
        body.shouldHaveAtLeastSize(3)
        body.any { it is IntroBlock } shouldBe true
        body.any { it is DropdownBlock } shouldBe true
        val margin = body.filterIsInstance<MarginBlock>().single()
        margin.height shouldBe "24"
    }

    "직렬화/역직렬화 라운드트립 (Jackson)" {
        val body = converter.convert(sampleMarkdown)
        val om = jacksonObjectMapper().registerKotlinModule()

        val json = om.writeValueAsString(body)
        val back: ResearchContentBody = om.readValue(json)

        back shouldBe body
    }
})