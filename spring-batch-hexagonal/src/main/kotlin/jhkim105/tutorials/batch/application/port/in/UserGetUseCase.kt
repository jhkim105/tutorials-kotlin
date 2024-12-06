package jhkim105.tutorials.batch.application.port.`in`

import jhkim105.tutorials.batch.application.domain.model.User

interface UserGetUseCase {

    fun getUsers(): List<User>
}