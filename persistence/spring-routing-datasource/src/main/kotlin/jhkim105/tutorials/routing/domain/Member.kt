package jhkim105.tutorials.routing.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var username: String,

    @Column(nullable = false)
    var email: String,
) {
    override fun toString(): String = "Member(id=$id, username='$username', email='$email')"
}
