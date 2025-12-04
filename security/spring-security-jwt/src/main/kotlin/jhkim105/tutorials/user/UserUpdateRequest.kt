package jhkim105.tutorials.user

data class UserUpdateRequest(var nickname: String? = null) {
    fun applyTo(currentUser: User) {
        currentUser.update(nickname)
    }
}
