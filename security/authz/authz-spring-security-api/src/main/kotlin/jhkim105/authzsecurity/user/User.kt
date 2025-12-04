package jhkim105.authzsecurity.user

enum class Role {
    USER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val role: Role
)
