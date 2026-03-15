package jhkim105.tutorials.jpa.repository

import jhkim105.tutorials.jpa.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.history.RevisionRepository

interface UserRepository : JpaRepository<User, Long>, RevisionRepository<User, Long, Long>

