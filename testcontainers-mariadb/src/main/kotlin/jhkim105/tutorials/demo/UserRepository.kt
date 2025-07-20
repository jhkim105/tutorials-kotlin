package jhkim105.tutorials.demo

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
