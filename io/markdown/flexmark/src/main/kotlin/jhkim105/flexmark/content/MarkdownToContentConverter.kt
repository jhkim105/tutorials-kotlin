package jhkim105.flexmark.content

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet

object MarkdownToContentConverter {
    private const val DEFAULT_MARGIN_HEIGHT = "40"

    fun convert(markdown: String): ContentDocument {
        val options = MutableDataSet()
        val parser = Parser.builder(options).build()
        val doc: Node = parser.parse(markdown)

        val blocks = mutableListOf<Block>()

        val marginSpots = detectMarginSpots(markdown)

        var node: Node? = doc.firstChild
        var passedLines = 0
        var marginIdx = 0

        while (node != null) {
            // 마진 후보가 현재 블록 이전에 있으면 먼저 반영
            while (marginIdx < marginSpots.size && marginSpots[marginIdx] <= passedLines) {
                blocks.add(MarginBlock(height = DEFAULT_MARGIN_HEIGHT))
                marginIdx++
            }

            when (node) {
                is Heading -> {
                    blocks.add(
                        TitleBlock(
                            level = node.level,
                            children = extractInlines(node) // 인라인 스타일 보존
                        )
                    )
                    passedLines += node.chars.lines().size
                }

                is Paragraph -> {
                    // 1) Paragraph가 "이미지 단독 문단"인지 먼저 체크
                    val onlyImage = extractSoleImageIfParagraph(node)
                    if (onlyImage != null) {
                        // 이미지 단독 문단 → ImageBlock으로 승격
                        blocks.add(imageBlockFrom(onlyImage))
                    } else {
                        // 2) 일반 문단 처리 (인라인 보존 + 캡션 규칙 적용)
                        val inlines = extractInlines(node)
                        if (isWholeEmphasis(inlines)) {
                            blocks.add(CaptionBlock(children = unwrapEmphasis(inlines)))
                        } else {
                            blocks.add(ParagraphBlock(children = inlines))
                        }
                    }
                    passedLines += node.chars.lines().size
                }

                is Image -> {
                    blocks.add(imageBlockFrom(node))
                    passedLines += node.chars.lines().size
                }

                is ThematicBreak -> {
                    // 필요시 구분선을 Margin 등으로 매핑 가능
                    passedLines += node.chars.lines().size
                }

                is BulletList, is OrderedList, is BlockQuote -> {
                    // 목록/인용을 원하시면 별도 DTO로 확장 가능
                    passedLines += node.chars.lines().size
                }

                else -> {
                    // 기타 노드는 우선 스킵
                    passedLines += node.chars.lines().size
                }
            }

            node = node.next
        }

        // 남은 마진 후보 반영
        while (marginIdx < marginSpots.size) {
            blocks.add(MarginBlock(height = DEFAULT_MARGIN_HEIGHT))
            marginIdx++
        }

        return ContentDocument(blocks)
    }

    // ===== 인라인 추출 =====

    /** Heading, Paragraph 등의 자식 인라인 노드를 트리로 변환 */
    private fun extractInlines(container: Node): List<Inline> {
        val res = mutableListOf<Inline>()
        var child = container.firstChild
        while (child != null) {
            res += mapInlineNode(child)
            child = child.next
        }
        return res
    }

    /**
     * Paragraph가 "이미지 한 개만 포함"하는지 검사하고,
     * 그 경우 해당 Image 노드를 반환. 아니면 null.
     * (공백 Text, Soft/Hard BR 등은 무시 대상으로 간주)
     */
    private fun extractSoleImageIfParagraph(p: Paragraph): Image? {
        val meaningfulNodes = generateSequence(p.firstChild) { it.next }
            .filterNot {
                (it is Text && it.chars.toString().isBlank()) ||
                        it is SoftLineBreak || it is HardLineBreak
            }
            .toList()

        return if (meaningfulNodes.size == 1 && meaningfulNodes[0] is Image) {
            meaningfulNodes[0] as Image
        } else null
    }

    /** 개별 인라인 노드를 DTO로 변환 (재귀) */
    private fun mapInlineNode(n: Node): List<Inline> = when (n) {
        is Text -> listOf(TextRun(n.chars.toString()))
        is StrongEmphasis -> listOf(StrongInline(children = extractInlines(n)))
        is Emphasis -> listOf(EmphasisInline(children = extractInlines(n)))
        is Code -> listOf(CodeInline(code = n.text.toString()))
        is SoftLineBreak -> listOf(LineBreakInline(hard = false))
        is HardLineBreak -> listOf(LineBreakInline(hard = true))
        is Link -> listOf(
            LinkInline(
                destination = n.url.toString(),
                title = n.title?.toString(),
                children = extractInlines(n)
            )
        )
        // 자동 링크(메일/URL)는 일반 Link으로 통일
        is AutoLink -> listOf(
            LinkInline(
                destination = n.text.toString(),
                title = null,
                children = listOf(TextRun(n.text.toString()))
            )
        )
        // Paragraph 안의 Image 등: 블록으로 뺄지, 인라인으로 둘지 정책 선택
        is Image -> {
            // 여기서는 "인라인 이미지 무시(별도 블록으로만 처리)" 정책.
            // 필요하면 InlineImage DTO를 추가해 반환하세요.
            emptyList()
        }
        else -> {
            // 기타 인라인은 자식이 있으면 재귀 추출, 없으면 무시
            if (n.firstChild != null) extractInlines(n) else emptyList()
        }
    }

    // ===== 유틸 =====

    /** 인라인 트리에서 plain text만 연결해서 캡션 여부 판단에 사용 */
    private fun inlinesToPlain(inlines: List<Inline>): String {
        val sb = StringBuilder()
        fun walk(n: Inline) {
            when (n) {
                is TextRun -> sb.append(n.text)
                is StrongInline -> n.children.forEach(::walk)
                is EmphasisInline -> n.children.forEach(::walk)
                is CodeInline -> sb.append(n.code)
                is LinkInline -> n.children.forEach(::walk)
                is LineBreakInline -> sb.append('\n')
            }
        }
        inlines.forEach(::walk)
        return sb.toString()
    }

    /** *...* 또는 _..._ 패턴을 인라인 구조에서도 제거 (겉표식만 제거, 내부 스타일은 유지) */
    private fun stripCaptionMarkersInline(inlines: List<Inline>): List<Inline> {
        val plain = inlinesToPlain(inlines).trim()
        val stripped = when {
            plain.startsWith("*") && plain.endsWith("*") && plain.length >= 2 ->
                plain.removePrefix("*").removeSuffix("*").trim()
            plain.startsWith("_") && plain.endsWith("_") && plain.length >= 2 ->
                plain.removePrefix("_").removeSuffix("_").trim()
            else -> plain
        }
        return listOf(TextRun(stripped)) // 간단화: 캡션 내부에 복합 스타일이 필요하면 규칙을 더 정교화하세요.
    }

    /** 캡션 규칙: 전체가 *…* 또는 _…_ 로 둘러싸인 한 단락 */
    private fun isCaptionParagraph(plain: String): Boolean {
        val t = plain.trim()
        return (t.startsWith("*") && t.endsWith("*") && t.length >= 2) ||
                (t.startsWith("_") && t.endsWith("_") && t.length >= 2)
    }

    private fun isWholeEmphasis(inlines: List<Inline>): Boolean =
        inlines.size == 1 && (inlines[0] is EmphasisInline || inlines[0] is StrongInline)

    private fun unwrapEmphasis(inlines: List<Inline>): List<Inline> {
        val children = when (val only = inlines[0]) {
            is EmphasisInline -> only.children
            is StrongInline   -> only.children
            else              -> inlines
        }
        // 단순화: 캡션 내부 인라인을 모두 평문화해 텍스트 하나로
        val plain = inlinesToPlain(children).trim()
        return listOf(TextRun(plain))
    }


    private fun imageBlockFrom(img: Image): ImageBlock {
        val url = img.url.toString()
        val title = img.title?.toString()?.takeIf { it.isNotBlank() }
        val alt = img.text?.toString()?.takeIf { it.isNotBlank() }
        val copyright = if (!title.isNullOrBlank() || !alt.isNullOrBlank()) {
            Copyright(description = title, holder = alt)
        } else null
        return ImageBlock(images = listOf(ImageItem(image_url = url, copyright = copyright)))
    }

    /** 원문에서 빈 줄 2개 이상이 연속되는 지점을 margin 후보로 식별 */
    private fun detectDoubleBlankLineSpots(md: String): List<Int> {
        val lines = md.split('\n')
        val spots = mutableListOf<Int>()
        var blanks = 0
        lines.forEachIndexed { idx, line ->
            if (line.isBlank()) {
                blanks++
                if (blanks == 2) spots += idx
            } else {
                blanks = 0
            }
        }
        return spots
    }

    /**
     * 원문에서 빈 줄이 1개 이상 나오면 margin 후보로 잡는다.
     */
    private fun detectMarginSpots(md: String): List<Int> {
        val lines = md.split('\n')
        val spots = mutableListOf<Int>()
        lines.forEachIndexed { idx, line ->
            if (line.isBlank()) {
                spots += idx
            }
        }
        return spots
    }
}
