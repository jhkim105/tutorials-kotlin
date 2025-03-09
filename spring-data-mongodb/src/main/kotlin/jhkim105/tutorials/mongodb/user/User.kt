package jhkim105.tutorials.mongodb.user

import org.springframework.data.annotation.Id

data class User(

    @Id
    val id: String? = null,
    val username: String,
    val enabled: Boolean = true,
    val email: EmailAddress? = null

)

