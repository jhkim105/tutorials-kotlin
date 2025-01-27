package jhkim105.tutorials.user.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jhkim105.tutorials.user.entity.QCompany
import jhkim105.tutorials.user.entity.QUser
import jhkim105.tutorials.user.entity.User
import org.springframework.stereotype.Repository

@Repository
class UserQueryDslRepository(
    private val jpaQueryFactory: JPAQueryFactory
) {
    fun findAllByCompanyName(companyName: String): MutableList<User> {
        val company = QCompany.company
        val user = QUser.user
        val query = jpaQueryFactory.from(user).join(user.company, company).where(company.name.eq(companyName));
        return query.select(user).fetch()
    }

}