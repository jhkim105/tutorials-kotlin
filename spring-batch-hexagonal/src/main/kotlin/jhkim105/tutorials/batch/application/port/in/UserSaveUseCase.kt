package jhkim105.tutorials.batch.application.port.`in`

import jhkim105.tutorials.batch.application.domain.model.User

interface UserSaveUseCase {

    fun saveAll(users: List<User>)
}