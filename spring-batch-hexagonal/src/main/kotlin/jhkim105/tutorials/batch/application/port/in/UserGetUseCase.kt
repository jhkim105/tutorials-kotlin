package jhkim105.tutorials.batch.application.port.`in`

import jhkim105.tutorials.batch.application.domain.entity.User

interface UserGetUseCase {

    fun getUsers(): List<User>
}