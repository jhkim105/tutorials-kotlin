package jhkim105.flexmark.research_content

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.ins.InsExtension
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import java.net.URLDecoder

class MarkdownToResearchContentConverter {

    private val parser: Parser = run {
        val opts = MutableDataSet().apply {
            set(Parser.EXTENSIONS, listOf(
                StrikethroughExtension.create(),
                SuperscriptExtension.create(),
                InsExtension.create(),
            ))
        }
        Parser.builder(opts).build()
    }

    fun convert(md: String): ResearchContentBody {
        val doc = parser.parse(md)
        val blocks = mutableListOf<Block>()

        var node: Node? = doc.firstChild
        while (node != null) {
            when (node) {
                is FencedCodeBlock -> {
                    when (bs(node.info).trim().lowercase()) {
                        "intro" -> {
                            val text = bs(node.contentChars).replace("\r\n", "\n").trimEnd()
                            blocks += IntroBlock(
                                id = genId(),
                                children = if (text.isEmpty()) emptyList() else listOf(TextInline(text = text))
                            )
                        }
                        "dropdown" -> {
                            val lines = bs(node.contentChars).replace("\r\n", "\n")
                                .lines().map { it.trim() }.filter { it.isNotEmpty() }
                            if (lines.isNotEmpty()) {
                                val summaryText = lines.first()
                                val descText = lines.drop(1).map { it.removePrefix("- ").trim() }
                                val summary = DropdownSummary(
                                    id = genId(),
                                    children = listOf(TextInline(text = summaryText))
                                )
                                val descriptions = DropdownDescriptions(
                                    id = genId(),
                                    children = descText.map { TextInline(text = it) }
                                )
                                blocks += DropdownBlock(
                                    id = genId(),
                                    children = listOf(summary, descriptions)
                                )
                            }
                        }
                        "margin" -> {
                            val h = bs(node.contentChars).trim()
                            if (h.isNotEmpty()) blocks += MarginBlock(id = genId(), height = h)
                        }
                    }
                }

                is Heading -> {
                    val inlines = collectInlines(node)
                    blocks += TitleBlock(id = genId(), children = inlines)
                }

                is BlockQuote -> {
                    val inlines = collectInlines(node)
                    blocks += CallOutBlock(id = genId(), children = inlines)
                }

                is BulletList -> {
                    val items = mutableListOf<UnorderedListItem>()
                    node.children.forEach { li ->
                        if (li is ListItem) {
                            items += UnorderedListItem(
                                id = genId(),
                                children = collectInlines(li)
                            )
                        }
                    }
                    // 리스트 다음 문단이 {caption: ...} 이면 캡션으로
                    val caption = (node.next as? Paragraph)?.let { p ->
                        val txt = bs(p.chars).trim()
                        if (txt.startsWith("{caption:") && txt.endsWith("}")) {
                            txt.removePrefix("{caption:").removeSuffix("}").trim()
                        } else null
                    }
                    blocks += UnorderedListBlock(
                        id = genId(),
                        children = items,
                        caption = caption
                    )
                }

                is Paragraph -> {
                    val images = extractImagesFromParagraph(node)
                    if (images.isNotEmpty()) {
                        blocks += ImagesBlock(id = genId(), images = images)
                    } else {
                        val inlines = collectInlines(node)
                        if (inlines.isNotEmpty()) blocks += ParagraphBlock(id = genId(), children = inlines)
                    }
                }
            }
            node = node.next
        }
        return blocks
    }

    // -------- helpers --------

    /** BasedSequence? → String */
    private fun bs(seq: CharSequence?): String = seq?.toString() ?: ""

    private fun collectInlines(container: Node): List<Inline> {
        val list = mutableListOf<Inline>()
        var n = container.firstChild
        while (n != null) {
            when (val cur = n) {
                is Text -> list += splitTextWithHighlight(bs(cur.chars))
                is SoftLineBreak, is HardLineBreak -> list += TextInline(text = "\n")
                is Emphasis -> {
                    list += collectInlines(cur)
                }
                is StrongEmphasis -> {
                    val inner = collectInlines(cur)
                    if (inner.isNotEmpty()) {
                        list += inner.map { if (it is TextInline) it.copy(weight = "bold") else it }
                    } else {
                        val t = bs(cur.text)
                        if (t.isNotBlank()) list += TextInline(text = t, weight = "bold")
                    }
                }
                is Link -> {
                    list += linkToInline(cur)
                }
                is Image -> {
                    // 문단 텍스트 사이 단일 이미지는 간단 표기로
                    list += TextInline(text = "[이미지]")
                }
                is Code -> {
                    list += TextInline(text = bs(cur.text))
                }
                else -> {
                    list += collectInlines(cur)
                }
            }
            n = n.next
        }
        return list
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
        val dest = bs(link.url)
        val children = collectInlines(link)
        fun textOfChildren() = if (children.isEmpty()) listOf(TextInline(text = dest)) else children

        return when {
            dest.startsWith("stock:", true) -> {
                // stock:AAPL?isin=US0378331005&ex=201
                val after = dest.substringAfter(":", "")
                val code = after.substringBefore("?").ifBlank { after }
                val qs = after.substringAfter("?", "")
                val params = qs.split("&").mapNotNull {
                    val kv = it.split("=")
                    if (kv.size == 2) kv[0] to URLDecoder.decode(kv[1], Charsets.UTF_8) else null
                }.toMap()
                StockLink(
                    id = genId(),
                    stockId = code,
                    isinCode = params["isin"] ?: "",
                    exchangeId = params["ex"] ?: "",
                    children = textOfChildren()
                )
            }
            dest.startsWith("news:", true) -> {
                NewsLink(id = genId(), newsId = dest.substringAfter(":"), children = textOfChildren())
            }
            dest.startsWith("contents:", true) -> {
                ContentsLink(id = genId(), url = dest.substringAfter(":"), children = textOfChildren())
            }
            dest.startsWith("scheme:", true) -> {
                SchemeLink(id = genId(), openScheme = dest.substringAfter(":"), children = textOfChildren())
            }
            dest.startsWith("http://") || dest.startsWith("https://") -> {
                ExternalLink(id = genId(), url = dest, navigationType = "default", children = textOfChildren())
            }
            else -> {
                ExternalLink(id = genId(), url = dest, navigationType = "default", children = textOfChildren())
            }
        }
    }

    private fun extractImagesFromParagraph(p: Paragraph): List<ImageItem> {
        val imgs = mutableListOf<ImageItem>()
        var onlyImages = true
        var n = p.firstChild
        while (n != null) {
            when (n) {
                is Image -> imgs += imageNodeToItem(n)
                is SoftLineBreak, is HardLineBreak -> { /* ok */ }
                is Text -> if (bs(n.chars).isNotBlank()) onlyImages = false
                else -> onlyImages = false
            }
            n = n.next
        }
        return if (onlyImages && imgs.isNotEmpty()) imgs else emptyList()
    }

    private fun imageNodeToItem(img: Image): ImageItem {
        val url = bs(img.url)
        val title = bs(img.title)
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