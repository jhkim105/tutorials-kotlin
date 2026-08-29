package jhkim105.tutorials.mongodb

import io.kotest.matchers.shouldBe
import jhkim105.tutorials.mongodb.user.User
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test


@DataMongoTest
@ActiveProfiles("cluster")
class MongoDslTest {


    @Autowired
    lateinit var operations: MongoOperations

    @BeforeEach
    fun before() {
        operations.dropCollection<User>()
    }

    @Test
    fun find() {
        operations.insert(User(username = "user100"))
        operations.insert(User(username = "user101"))

        val users = operations.find(Query(User::username isEqualTo "user100"), User::class.java)
        users.size shouldBe 1
    }

    @Test
    fun findByCriteria() {
        operations.insert(User(username = "user100"))
        operations.insert(User(username = "user101"))

        val users = operations.find(
            Query(
                Criteria().andOperator(
                    User::username isEqualTo "user100",
                    User::enabled isEqualTo true
                )
            ), User::class.java
        )
        users.size shouldBe 1
    }
}