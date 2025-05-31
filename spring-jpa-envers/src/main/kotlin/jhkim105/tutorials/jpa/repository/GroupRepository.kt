package jhkim105.tutorials.jpa.repository

import jhkim105.tutorials.jpa.model.Group
import org.springframework.data.jpa.repository.JpaRepository


interface GroupRepository : JpaRepository<Group, Long>