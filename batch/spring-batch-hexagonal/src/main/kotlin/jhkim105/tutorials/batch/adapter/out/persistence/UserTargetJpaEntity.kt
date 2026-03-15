package jhkim105.tutorials.batch.adapter.out.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_target")
class UserTargetJpaEntity(
    @Id
    val id: Long,
    var username: String
) {


}