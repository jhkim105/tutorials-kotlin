package jhkim105.tutorials

import jhkim105.tutorials.user.entity.Company
import jhkim105.tutorials.user.entity.User
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Environment
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import org.junit.jupiter.api.Test
import java.util.*

class SchemaExportTest {
    @Test
    fun test() {
        generateSchema()
    }

    private fun generateSchema() {
        val settings = mapOf(
            Environment.DIALECT to "org.hibernate.dialect.MySQL8Dialect",
            Environment.HBM2DDL_AUTO to "none",
            Environment.SHOW_SQL to false,
            Environment.FORMAT_SQL to true
        )

        val serviceRegistry = StandardServiceRegistryBuilder()
            .applySettings(settings)
            .build()

        val metadataSources = MetadataSources(serviceRegistry)
        metadataSources.addAnnotatedClass(User::class.java)
        metadataSources.addAnnotatedClass(Company::class.java)
        val metadata = metadataSources.buildMetadata()

        val schemaExport = SchemaExport().apply {
            setOutputFile("schema.sql")
            setDelimiter(";")
            setFormat(true)
            setHaltOnError(true)
        }

        schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata)
    }
}