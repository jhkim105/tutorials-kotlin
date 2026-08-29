package jhkim105.tutorials.postgresql

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_sequence", allocationSize = 50)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: Long,

    /**
     * 1. PostgreSQL JSONB Mapping
     * Stores complex JSON with native indexing (GIN) support.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var attributes: Map<String, Any> = emptyMap(),

    /**
     * 2. PostgreSQL Native Array Type (text[])
     * Maps Kotlin List directly to DB array column without join tables.
     */
    @Column(columnDefinition = "text[]")
    var tags: List<String> = emptyList()
)

interface ProductRepository : JpaRepository<Product, Long> {

    // Native query searching inside JSONB using '->>' operator (extract as text)
    @Query(
        value = "SELECT * FROM products p WHERE p.attributes ->> :key = :value",
        nativeQuery = true
    )
    fun findByAttributeKeyAndValue(
        @Param("key") key: String,
        @Param("value") value: String
    ): List<Product>

    // Native query searching inside native array column using ANY()
    @Query(
        value = "SELECT * FROM products p WHERE :tag = ANY(p.tags)",
        nativeQuery = true
    )
    fun findByTag(@Param("tag") tag: String): List<Product>
}
