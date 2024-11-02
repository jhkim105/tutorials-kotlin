package jhkim105.tutorials.repository

import jhkim105.tutorials.domain.User

interface UserRepositoryCustom {

    fun findAllByCompanyName(companyName: String): MutableList<User>

}