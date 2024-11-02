package jhkim105.tutorials.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jhkim105.tutorials.domain.QCompany
import jhkim105.tutorials.domain.QUser
import jhkim105.tutorials.domain.User

class UserRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
): UserRepositoryCustom {
    override fun findAllByCompanyName(companyName: String): MutableList<User> {
        val company = QCompany.company
        val user = QUser.user
        val query = jpaQueryFactory.from(user).join(user.company, company).where(company.name.eq(companyName));
        return query.select(user).fetch()
    }

}