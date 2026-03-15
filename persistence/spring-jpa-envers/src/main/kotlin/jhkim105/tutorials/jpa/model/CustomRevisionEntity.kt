package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp

@Entity
@RevisionEntity
@Table(name = "REVINFO")
class CustomRevisionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    var id: Long? = null,

    @RevisionTimestamp
    var timestamp: Long? = null
)