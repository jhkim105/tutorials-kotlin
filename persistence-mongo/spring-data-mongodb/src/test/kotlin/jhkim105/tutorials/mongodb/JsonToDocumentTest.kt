package jhkim105.tutorials.mongodb

import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import kotlin.test.Test

@SpringBootTest
class JsonToDocumentTest @Autowired constructor(
    private val mongoTemplate: MongoTemplate
) {

    @Test
    fun saveTo() {
        val json = "{\"name\":\"Book A\", \"genre\": \"Comedy\"}"
        val doc: Document = Document.parse(json)
        mongoTemplate.insert(doc, "book")
    }
}