package jhkim105.flexmark.research_content

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class MarkdownToResearchContentConverterTest : StringSpec({

 val converter = MarkdownToResearchContentConverter()
 val sampleMarkdown: String = javaClass.getResource("/research_content.md")!!.readText()

 "마크 다운을 ResearchContentBody 로 변환" {
  val body = converter.convert(sampleMarkdown)

  body.size.shouldBe(14)

  body.any { it is TitleBlock } shouldBe true
  body.any { it is ParagraphBlock } shouldBe true
  body.any { it is MarginBlock } shouldBe true
  body.any { it is CallOutBlock } shouldBe true
  body.any { it is UnorderedListBlock } shouldBe true
  body.any { it is ImagesBlock } shouldBe true

  // Block 확인
  body[0].shouldBeInstanceOf<TitleBlock>()
  body[1].shouldBeInstanceOf<ParagraphBlock>()
  body[2].shouldBeInstanceOf<MarginBlock>()
  body[3].shouldBeInstanceOf<ParagraphBlock>()
  body[4].shouldBeInstanceOf<MarginBlock>()
  body[5].shouldBeInstanceOf<ParagraphBlock>()
  body[6].shouldBeInstanceOf<MarginBlock>()
  body[7].shouldBeInstanceOf<CallOutBlock>()
  body[8].shouldBeInstanceOf<MarginBlock>()
  body[9].shouldBeInstanceOf<TitleBlock>()
  body[10].shouldBeInstanceOf<ParagraphBlock>()
  body[11].shouldBeInstanceOf<ImagesBlock>()
  body[12].shouldBeInstanceOf<MarginBlock>()
  body[13].shouldBeInstanceOf<UnorderedListBlock>()
 }
})
