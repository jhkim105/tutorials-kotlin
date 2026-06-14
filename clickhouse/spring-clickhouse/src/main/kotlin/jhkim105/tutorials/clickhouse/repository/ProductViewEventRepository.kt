package jhkim105.tutorials.clickhouse.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import jhkim105.tutorials.clickhouse.domain.ProductViewEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Repository
class ProductViewEventRepository(private val jdbcTemplate: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        ProductViewEvent(
            id = UUID.fromString(rs.getString("id")),
            productId = rs.getString("product_id"),
            userId = rs.getString("user_id"),
            price = rs.getBigDecimal("price"),
            urlPath = rs.getString("url_path"),
            referrer = rs.getString("referrer"),
            createdAt = rs.getObject("created_at", LocalDateTime::class.java)
        )
    }

    fun save(event: ProductViewEvent) {
        val sql = """
            INSERT INTO product_view_events (id, product_id, user_id, price, url_path, referrer)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            event.id.toString(),
            event.productId,
            event.userId,
            event.price,
            event.urlPath,
            event.referrer
        )
        logger.info { "이커머스 조회 이벤트 저장 성공: $event" }
    }

    fun findById(id: UUID): ProductViewEvent? {
        val sql = "SELECT * FROM product_view_events WHERE id = ?"
        return jdbcTemplate.query(sql, rowMapper, id.toString()).firstOrNull()
    }

    fun findAll(): List<ProductViewEvent> {
        val sql = "SELECT * FROM product_view_events"
        return jdbcTemplate.query(sql, rowMapper)
    }

    fun delete(id: UUID) {
        val sql = "DELETE FROM product_view_events WHERE id = ?"
        jdbcTemplate.update(sql, id.toString())
        logger.info { "이커머스 조회 이벤트 삭제 완료 (ID: $id)" }
    }

    /**
     * ClickHouse의 OLAP 강점을 보여주는 분석용 메서드
     * 상품별 조회 빈도 집계를 수행하여 가장 많이 조회된 상위 N개 상품 정보를 반환합니다.
     */
    fun getTopViewedProducts(limit: Int): List<Pair<String, Long>> {
        val sql = """
            SELECT product_id, count() as view_count 
            FROM product_view_events 
            GROUP BY product_id 
            ORDER BY view_count DESC 
            LIMIT ?
        """.trimIndent()
        
        logger.info { "가장 많이 조회된 상위 $limit 상품 조회 수행" }
        return jdbcTemplate.query(sql, { rs, _ ->
            rs.getString("product_id") to rs.getLong("view_count")
        }, limit)
    }
}
