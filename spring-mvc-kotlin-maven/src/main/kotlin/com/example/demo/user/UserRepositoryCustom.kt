package com.example.demo.user

interface UserRepositoryCustom {

    fun findAllByCompanyName(companyName: String): MutableList<User>

}