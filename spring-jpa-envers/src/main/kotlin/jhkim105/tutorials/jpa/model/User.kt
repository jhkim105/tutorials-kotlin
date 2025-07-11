package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.hibernate.envers.Audited
import javax.management.relation.Role

@Entity
@Table(name = "t_user")
@Audited
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var username: String,

    @ManyToOne
    @JoinColumn(name = "group_id")
    var group: Group? = null,

    val email: String? = null


)


