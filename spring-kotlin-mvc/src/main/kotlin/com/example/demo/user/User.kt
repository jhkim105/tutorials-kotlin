package com.example.springkotlinmvc.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

//@Entity
class User(
    var username: String,
    var password: String,
    var name: String,
    var description: String? = null,
    @Id @GeneratedValue var id: Long? = null)
