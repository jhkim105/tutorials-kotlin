package jhkim105.tutorials.jpa

import jhkim105.tutorials.jpa.model.CustomRevisionEntity
import jhkim105.tutorials.jpa.model.Group
import jhkim105.tutorials.jpa.model.User
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import java.io.File
import java.util.*
import kotlin.test.Test

class EnversSchemaExportTest {

    @Test
    fun `generate schema for audited User entity only`() {
        val settings = mapOf(
            AvailableSettings.DIALECT to "org.hibernate.dialect.MySQL8Dialect",
            AvailableSettings.FORMAT_SQL to true
        )

        val registry = StandardServiceRegistryBuilder()
            .applySettings(settings)
            .build()

        val metadata = MetadataSources(registry)
            .addAnnotatedClass(User::class.java)
            .addAnnotatedClass(Group::class.java)
            .addAnnotatedClass(CustomRevisionEntity::class.java)
            .buildMetadata()

        val outputFile = File("build/generated/user_aud_schema.sql")
        outputFile.parentFile.mkdirs()
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val schemaExport = SchemaExport().apply {
            setFormat(true)
            setDelimiter(";")
            setOutputFile(outputFile.absolutePath)
        }

        schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata)

        println("스키마 생성 완료: ${outputFile.absolutePath}")
    }
}