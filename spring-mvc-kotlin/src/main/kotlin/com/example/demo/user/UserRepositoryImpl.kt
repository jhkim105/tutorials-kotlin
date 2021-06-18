package com.example.demo.user

import com.querydsl.jpa.impl.JPAQueryFactory

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