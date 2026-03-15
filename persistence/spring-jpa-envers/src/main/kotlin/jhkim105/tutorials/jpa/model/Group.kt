package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.hibernate.envers.Audited

@Entity
@Table(name = "t_group")
@Audited
class Group(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    var users: MutableList<User> = mutableListOf()
) {

    fun addUser(user: User) {
        users.add(user)
        user.group = this
    }

    fun removeUser(user: User) {
        users.remove(user)
        user.group = null
    }
}