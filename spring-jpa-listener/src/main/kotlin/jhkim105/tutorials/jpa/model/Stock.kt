package jhkim105.tutorials.jpa.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "stock")
class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var exchangeCode: String,

    @Column(unique = true, nullable = false)
    var stockCode: String,

    @CreatedDate
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    val updatedAt: LocalDateTime? = null
) {
    override fun toString(): String {
        return "Stock(id=$id, exchangeCode='$exchangeCode', stockCode='$stockCode', createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}