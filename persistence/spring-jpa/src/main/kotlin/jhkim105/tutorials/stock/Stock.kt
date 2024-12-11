package jhkim105.tutorials.stock

import jakarta.persistence.*

@Entity
class Stock(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null,
    val code: String,

    @OneToMany(mappedBy = "stock", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    val stockWarnings: MutableSet<StockWarning> = mutableSetOf()
)