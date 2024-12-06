package jhkim105.tutorials.batch.adapter.out.persistence

import jakarta.persistence.*

@Entity
@Table(name = "user")
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var username: String
) {


}