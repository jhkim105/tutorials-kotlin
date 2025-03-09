package jhkim105.tutorials.mongodb

import io.kotest.matchers.shouldBe
import jhkim105.tutorials.mongodb.user.User
import jhkim105.tutorials.mongodb.user.UsernameOnly
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.asType
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import kotlin.test.Test

@DataMongoTest
class MongoTemplateTest {

    @Autowired
    lateinit var template: MongoTemplate

    @BeforeEach
    fun before() {
        template.dropCollection<User>()
    }

    @Test
    fun find() {
        template.insert(User(username = "user100"))
        template.insert(User(username = "user101"))

        val users = template.query(User::class.java)
            .matching(query(where("username").isEqualTo("user100")))
            .all()

        users.size shouldBe 1
    }

    @Test
    fun findProjection() {
        template.insert(User(username = "user100"))
        template.insert(User(username = "user101"))

        val usernameOnly = template.query(User::class.java)
            .asType<UsernameOnly>()
            .matching(query(where("username").isEqualTo("user100")))
            .oneValue()

        usernameOnly?.getUsername() shouldBe "user100"
    }
}