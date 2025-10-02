package jhkim105.flexmark.research_content

import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet

class MarkdownToResearchContentConverter {

    private val parser: Parser = run {
        val opts = MutableDataSet()
        Parser.builder(opts).build()
    }

    fun convert(markdown: String): ResearchContentBody {
        val doc = parser.parse(markdown)
        val blocks = mutableListOf<Block>()

        val marginSpots = detectMarginSpots(markdown)

        var node: Node? = doc.firstChild
        var passedLines = 0
        var marginIdx = 0

        while (node != null) {
            // 마진 후보가 현재 블록 이전에 있으면 먼저 반영
            while (marginIdx < marginSpots.size && marginSpots[marginIdx] <= passedLines) {
                blocks.add(MarginBlock())
                marginIdx++
            }
            when (node) {
                is Heading -> {
                    val inlines = extractInlines(node)
                    blocks += TitleBlock(id = genId(), children = inlines)
                    passedLines += node.chars.lines().size
                }

                is BlockQuote -> {
                    val inlines = extractInlines(node)
                    blocks += CallOutBlock(id = genId(), children = inlines)
                    passedLines += node.chars.lines().size
                }

                is BulletList -> {
                    val items = mutableListOf<UnorderedListItem>()
                    node.children.forEach { li ->
                        if (li is ListItem) {
                            items += UnorderedListItem(
                                id = genId(),
                                children = extractInlines(li)
                            )
                        }
                    }

                    blocks += UnorderedListBlock(
                        id = genId(),
                        children = items
                    )
                    passedLines += node.chars.lines().size
                }

//                is Paragraph -> {
//                    val images = extractImagesFromParagraph(node)
//                    val inlines = extractInlines(node)
//                    // 텍스트가 있으면 ParagraphBlock 먼저 추가
//                    if (inlines.isNotEmpty()) {
//                        blocks += ParagraphBlock(id = genId(), children = inlines)
//                    }
//                    // 이미지가 있으면 ImagesBlock 추가 (문단 내 위치상 텍스트 다음에 나오도록)
//                    if (images.isNotEmpty()) {
//                        blocks += ImagesBlock(id = genId(), images = images)
//                    }
//                    passedLines += node.chars.lines().size
//                }

                is Paragraph -> {
                    val exploded = splitParagraphByOrder(node)
                    blocks.addAll(exploded)
                    passedLines += node.chars.lines().size
                }

                else -> {
                    passedLines += node.chars.lines().size
                }
            }

            node = node.next
        }

        // 남은 마진 후보 반영
        while (marginIdx < marginSpots.size) {
            blocks.add(MarginBlock())
            marginIdx++
        }

        return blocks
    }

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

    private fun toString(seq: CharSequence?): String = seq?.toString() ?: ""

    private fun extractInlines(container: Node): List<Inline> {
        val list = mutableListOf<Inline>()
        var child = container.firstChild
        while (child != null) {
            list += extractInline(child)
            child = child.next
        }
        return list
    }

    private fun extractInline(
        child: Node
    ): List<Inline> =
        when (child) {
            is Text -> splitTextWithHighlight(toString(child.chars))
            is SoftLineBreak, is HardLineBreak -> listOf(TextInline(text = "\n"))
            is Emphasis -> {
                extractInlines(child)
            }

            is StrongEmphasis -> {
                val inner = extractInlines(child)
                if (inner.isNotEmpty()) {
                    inner.map { if (it is TextInline) it.copy(weight = "bold") else it }.toList()
                } else {
                    val t = toString(child.text)
                    if (t.isNotBlank()) listOf(TextInline(text = t, weight = "bold")) else {
                        println("t: $t")
                        emptyList()
                    }
                }
            }

            is Link -> {
                listOf(linkToInline(child))
            }

            is Image -> {
                // 인라인 이미지 무시
                emptyList()
            }

            is Code -> {
                listOf(TextInline(text = toString(child.text)))
            }

            else -> {
                if (child.firstChild != null) extractInlines(child) else emptyList()
            }
        }

    private fun splitParagraphByOrder(p: Paragraph): List<Block> {
        val out = mutableListOf<Block>()
        val textBuf = mutableListOf<Inline>()
        val imgBuf = mutableListOf<ImageItem>()

        fun flushText() {
            if (textBuf.isNotEmpty()) {
                out += ParagraphBlock(id = genId(), children = textBuf.toList())
                textBuf.clear()
            }
        }

        fun flushImages() {
            if (imgBuf.isNotEmpty()) {
                out += ImagesBlock(id = genId(), images = imgBuf.toList())
                imgBuf.clear()
            }
        }

        var child: Node? = p.firstChild
        while (child != null) {
            when (child) {
                is Image -> {
                    // 이미지가 나오면 이전 텍스트 문단을 먼저 마감
                    flushText()
                    imgBuf += imageNodeToItem(child)
                }

                is SoftLineBreak, is HardLineBreak -> {
                    // 줄바꿈은 텍스트 버퍼에 개행으로 반영
                    textBuf += TextInline(text = "\n")
                    // 만약 직전에 이미지를 모으고 있었다면 먼저 마감
                    flushImages()
                }

                is Text -> {
                    val s = child.chars.toString()
                    if (s.isNotBlank()) {
                        textBuf += splitTextWithHighlight(s)
                    }
                    // 텍스트가 시작되면 이미지 묶음은 마감
                    flushImages()
                }

                else -> {
                    // 굵게/기울임/링크/인라인 코드 등은 기존 로직 재사용
                    val inlines = extractInline(child)
                    if (inlines.isNotEmpty()) {
                        textBuf += inlines
                    }
                    flushImages()
                }
            }

            // 다음 노드로
            child = child.next
        }

        // 남은 것들 마감
        flushImages()
        flushText()

        return out
    }

    private fun splitTextWithHighlight(src: String): List<TextInline> {
        if (!src.contains("==")) return listOf(TextInline(text = src))
        val out = mutableListOf<TextInline>()
        var i = 0
        while (i < src.length) {
            val start = src.indexOf("==", i)
            if (start < 0) {
                out += TextInline(text = src.substring(i))
                break
            }
            if (start > i) out += TextInline(text = src.substring(i, start))
            val end = src.indexOf("==", start + 2)
            if (end < 0) {
                out += TextInline(text = src.substring(start))
                break
            }
            val hi = src.substring(start + 2, end)
            if (hi.isNotEmpty()) out += TextInline(text = hi, highlight = "highlight-yellow")
            i = end + 2
        }
        return out.filter { it.text.isNotEmpty() }
    }

    private fun linkToInline(link: Link): Inline {
        val dest = toString(link.url)
        val children = extractInlines(link)
        fun textOfChildren() = if (children.isEmpty()) listOf(TextInline(text = dest)) else children
        return ExternalLink(id = genId(), url = dest, navigationType = "default", children = textOfChildren())
    }


    private fun extractImagesFromParagraph(p: Paragraph): List<ImageItem> {
        val images = mutableListOf<ImageItem>()
        var child = p.firstChild
        while (child != null) {
            if (child is Image) {
                images += imageNodeToItem(child)
            }
            child = child.next
        }
        return images
    }

    private fun extractOnlyImageIfParagraph(p: Paragraph): List<ImageItem>{
        val meaningfulNodes = generateSequence(p.firstChild) { it.next }
            .filterNot {
                (it is Text && it.chars.toString().isBlank()) ||
                        it is SoftLineBreak || it is HardLineBreak
            }
            .toList()

        return if (meaningfulNodes.size == 1 && meaningfulNodes[0] is Image) {
            val image = meaningfulNodes[0] as Image
            listOf(imageNodeToItem(image))
        } else emptyList()
    }

    private fun imageNodeToItem(img: Image): ImageItem {
        val url = toString(img.url)
        val title = toString(img.title)
        val (desc, holder) = splitDescHolder(title)
        return ImageItem(
            id = genId(),
            imageUrl = url,
            copyright = if (desc.isBlank() && holder.isBlank()) null else Copyright(
                description = desc.ifBlank { null },
                holder = holder.ifBlank { null }
            )
        )
    }

    private fun splitDescHolder(title: String): Pair<String, String> {
        val idx = title.indexOf('©')
        return if (idx >= 0) {
            val d = title.substring(0, idx).trim()
            val h = title.substring(idx + 1).trim()
            d to h
        } else {
            title.trim() to ""
        }
    }

    private fun genId(): String = java.util.UUID.randomUUID().toString()
}

