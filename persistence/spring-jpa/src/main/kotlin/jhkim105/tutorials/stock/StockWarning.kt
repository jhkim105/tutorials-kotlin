package jhkim105.tutorials.stock

import jakarta.persistence.*

@Entity
class StockWarning(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null,

    @ManyToOne
    val stock: Stock,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val warningType: StockWarningType
)

enum class StockWarningType {
    MANGED_STOCK,
    INSUFFICIENT_DISCLOSURE,
    TRADING_HALT,
    LIQUIDATION
}