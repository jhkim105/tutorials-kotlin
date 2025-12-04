package jhkim105.tutorials.jwt

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.hibernate.Length
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(name = "jwk")
class Jwk(
    @Id
    @Column(length = 50)
    var id: String,

    @Lob
    @Column(nullable = false, length = Length.LONG32)
    var keyData: String,

    @CreatedDate
    var createdAt: Instant? = null,
)
