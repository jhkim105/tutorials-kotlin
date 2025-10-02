package jhkim105.flexmark.content

import jhkim105.flexmark.content.MarkdownToContentConverter.convert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownToContentConverterTest {

    @Test
    fun `H1 제목이 TitleBlock level 1로 변환된다`() {
        val md = "# Hello **World**"
        val doc = convert(md)

        assertEquals(1, doc.body.size)
        val title = doc.body.first() as TitleBlock
        assertEquals(1, title.level)
        // children: "Hello ", Strong("World")
        val expected = listOf(
            TextRun("Hello "),
            StrongInline(children = listOf(TextRun("World")))
        )
        assertEquals(expected, title.children)
    }

    @Test
    fun `문단의 강조, 기울임, 링크, 줄바꿈, 코드가 보존된다`() {
        val md = """
            안녕은 **굵게** 그리고 _기울임_
            링크는 [여기](https://example.com "샘플")
            인라인 `code` 와 **굵게 _중첩 기울임_** 테스트
        """.trimIndent()

        val doc = convert(md)
        assertEquals(1, doc.body.size)
        val p = doc.body.first() as ParagraphBlock

        // 기대 구조를 정확히 기술
        val expected = listOf<Inline>(
            TextRun("안녕은 "),
            StrongInline(listOf(TextRun("굵게"))),
            TextRun(" 그리고 "),
            EmphasisInline(listOf(TextRun("기울임"))),
            LineBreakInline(hard = false), // "두 칸 공백 + 개행" → soft break
            TextRun("링크는 "),
            LinkInline(
                destination = "https://example.com",
                title = "샘플",
                children = listOf(TextRun("여기"))
            ),
            LineBreakInline(hard = false),
            TextRun("인라인 "),
            CodeInline("code"),
            TextRun(" 와 "),
            StrongInline(
                children = listOf(
                    TextRun("굵게 "),
                    EmphasisInline(children = listOf(TextRun("중첩 기울임")))
                )
            ),
            TextRun(" 테스트")
        )
        assertEquals(expected, p.children)
    }

    @Test
    fun `이미지가 ImageBlock으로 변환되고 title은 description, alt는 holder로 간다`() {
        val md = """![CNN](https://t1.kakaocdn "미국 대통령 도널드 트럼프")"""
        val doc = convert(md)

        assertEquals(1, doc.body.size)
        val image = doc.body.first() as ImageBlock
        assertEquals(1, image.images.size)

        val item = image.images.first()
        val expected = ImageItem(
            image_url = "https://t1.kakaocdn",
            copyright = Copyright(
                description = "미국 대통령 도널드 트럼프",
                holder = "CNN"
            )
        )
        assertEquals(expected, item)
    }

    @Test
    fun `캡션 규칙 - 전체가 별 또는 언더스코어로 감싸진 단락은 CaptionBlock`() {
        val md = "*이것은 캡션입니다.*"
        val doc = convert(md)

        assertEquals(1, doc.body.size)
        val cap = doc.body.first() as CaptionBlock
        // 기본 구현은 마커 제거 후 텍스트만 TextRun 으로 넣음
        assertEquals(listOf(TextRun("이것은 캡션입니다.")), cap.children)
    }

    @Test
    fun `빈 줄 MarginBlock 삽입`() {
        val md = """
            # 제목
            첫 단락
            
            둘째 단락
        """.trimIndent()

        val doc = convert(md)

        // 예상: TitleBlock, ParagraphBlock, MarginBlock, ParagraphBlock (총 4개)
        assertEquals(4, doc.body.size)
        assertTrue(doc.body[0] is TitleBlock)
        assertTrue(doc.body[1] is ParagraphBlock)
        assertTrue(doc.body[2] is MarginBlock)
        assertTrue(doc.body[3] is ParagraphBlock)
    }

    @Test
    fun `복합 문서 스냅샷 테스트 - 구조 확인`() {
        val md = """
            # 5년 적립시 **매수 승자**
            안녕하세요. _카카오~_  
            자세한 내용은 [여기](https://example.com "샘플")를 참고하세요.

            ![CNN](https://t1.kakaocdn "미국 대통령 도널드 트럼프")

            성장주: 엔비디아, 테슬라~  
            *배당이 포함된 기록이에요.*
            인라인 `code` 와 **굵게 _중첩 기울임_** 도 됩니다.
        """.trimIndent()

        val doc = convert(md)

        // 블록 수와 각 블록 타입/일부 필드만 빠르게 검증 (세부 인라인은 개별 테스트에서 커버)
        assertEquals(6, doc.body.size)

        assertTrue(doc.body[0] is TitleBlock)
        (doc.body[0] as TitleBlock).also {
            assertEquals(1, it.level)
            // 첫 child 가 "5년 적립시 " 이어야 함
            assertTrue(it.children.first() == TextRun("5년 적립시 "))
            // 그 다음 strong("매수 승자") 확인
            assertTrue(it.children[1] == StrongInline(listOf(TextRun("매수 승자"))))
        }

        assertTrue(doc.body[1] is ParagraphBlock) // 인라인 스타일 포함 문단
        assertTrue(doc.body[2] is MarginBlock)    // 빈 줄로 인한 마진
        assertTrue(doc.body[3] is ImageBlock)     // 이미지 블록
        assertTrue(doc.body[4] is MarginBlock)    // 빈 줄로 인한 마진
        assertTrue(doc.body[5] is ParagraphBlock) // 캡션/코드 포함 문단(현재 구현은 두 줄이 하나의 문단으로 합쳐질 수 있음)
    }

    @Test
    fun `SoftLineBreak - 줄 끝 공백 없이 개행하면 Soft BR가 된다`() {
        // 줄 끝 공백 없음 → 일반 개행 → SoftLineBreak
        val md = """
        첫 줄
        둘째 줄
    """.trimIndent()

        val doc = convert(md)
        // 한 개의 ParagraphBlock 안에서 SoftLineBreak 로 이어져야 함
        assertEquals(1, doc.body.size)
        val p = doc.body.first() as ParagraphBlock

        val expected = listOf<Inline>(
            TextRun("첫 줄"),
            LineBreakInline(hard = false),
            TextRun("둘째 줄")
        )
        assertEquals(expected, p.children)
    }

    @Test
    fun `HardLineBreak - 줄 끝 두 칸 공백 또는 백슬래시로 개행하면 Hard BR가 된다`() {
        // 케이스 A: 줄 끝 두 칸 공백 + 개행 → HardLineBreak
        val mdA = "첫 줄␠␠\n둘째 줄" // ␠␠ 은 두 칸 공백을 의미(실제 코드에선 그냥 "  ")
            .replace("␠", " ")        // 가독성을 위해 치환 사용

        val docA = convert(mdA)
        val pA = docA.body.first() as ParagraphBlock
        val expectedA = listOf<Inline>(
            TextRun("첫 줄"),
            LineBreakInline(hard = true),
            TextRun("둘째 줄")
        )
        assertEquals(expectedA, pA.children)

        // 케이스 B: 줄 끝 백슬래시 + 개행 → HardLineBreak
        val mdB = """
        첫 줄\
        둘째 줄
    """.trimIndent()

        val docB = convert(mdB)
        val pB = docB.body.first() as ParagraphBlock
        val expectedB = listOf<Inline>(
            TextRun("첫 줄"),
            LineBreakInline(hard = true),
            TextRun("둘째 줄")
        )
        assertEquals(expectedB, pB.children)
    }
}