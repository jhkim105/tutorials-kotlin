package jhkim105.tutorials.batch.application.port.`in`

import jhkim105.tutorials.batch.application.domain.entity.User

interface UserSaveUseCase {

    fun saveAll(users: List<User>)
}