package jhkim105.tutorials.mongodb.user

import com.querydsl.core.annotations.QueryEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
@QueryEntity
class User(

    @Id
    val id: String? = null,
    val username: String,
    val enabled: Boolean = true,
    val email: EmailAddress? = null

)


