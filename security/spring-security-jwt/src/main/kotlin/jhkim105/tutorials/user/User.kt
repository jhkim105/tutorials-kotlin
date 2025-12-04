package jhkim105.tutorials.user

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "tu_user")
class User {

    @Id
    @Column(length = 50)
    @UuidGenerator
    var id: String? = null

    var username: String? = null

    var password: String? = null

    var nickname: String? = null

    @ElementCollection(targetClass = Role::class, fetch = FetchType.EAGER)
    @JoinTable(name = "tu_user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = HashSet()

    fun update(nickname: String?) {
        this.nickname = nickname
    }
}
