package jhkim105.tutorials.batch.application.port.out

import jhkim105.tutorials.batch.application.domain.model.User

interface UserSavePort {
    fun saveAll(users: List<User>)
}