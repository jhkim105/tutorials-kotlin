package jhkim105.tutorials.batch.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserTargetJpaRepository : JpaRepository<UserTargetJpaEntity, Long> {
}