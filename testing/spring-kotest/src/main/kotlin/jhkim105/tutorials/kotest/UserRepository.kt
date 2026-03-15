package jhkim105.tutorials.kotest

import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository : JpaRepository<User, Long>