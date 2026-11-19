package jhkim105.tutorials.jpadatetime.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

@Service
class DbMetadataService(
    private val dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate
) {

    fun getDatabaseEngine(): String {
        return dataSource.connection.use { conn ->
            val meta = conn.metaData
            "${meta.databaseProductName} ${meta.databaseProductVersion}"
        }
    }

    fun getColumnTypes(tableName: String = "time_record"): Map<String, String> {
        val columnTypeMap = mutableMapOf<String, String>()
        dataSource.connection.use { conn ->
            val meta = conn.metaData
            val rs = meta.getColumns(null, null, tableName.lowercase(), null)
            val rsUpper = if (!rs.isBeforeFirst) {
                meta.getColumns(null, null, tableName.uppercase(), null)
            } else rs

            while (rsUpper.next()) {
                val columnName = rsUpper.getString("COLUMN_NAME").lowercase()
                val typeName = rsUpper.getString("TYPE_NAME")
                val columnSize = rsUpper.getInt("COLUMN_SIZE")
                val decimalDigits = rsUpper.getInt("DECIMAL_DIGITS")
                val displayType = when {
                    decimalDigits > 0 -> "$typeName($decimalDigits)"
                    columnSize > 0 && typeName in listOf("VARCHAR", "CHAR") -> "$typeName($columnSize)"
                    else -> typeName
                }
                columnTypeMap[columnName] = displayType
            }
        }
        return columnTypeMap
    }

    fun getRawRowValues(id: String, tableName: String = "time_record"): Map<String, String?> {
        val sql = "SELECT * FROM $tableName WHERE id = ?"
        return jdbcTemplate.query(
            sql,
            ResultSetExtractor { rs ->
                val meta = rs.metaData
                val rowMap = mutableMapOf<String, String?>()
                if (rs.next()) {
                    for (i in 1..meta.columnCount) {
                        val colName = meta.getColumnName(i).lowercase()
                        val rawStr = rs.getString(i)
                        rowMap[colName] = rawStr
                    }
                }
                rowMap
            },
            id
        ) ?: emptyMap()
    }
}

