package jhkim105.tutorials.beanio.csv

import jhkim105.tutorials.beanio.Contact
import org.beanio.StreamFactory
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class CsvTest {

    @Test
    fun read() {
        val factory = StreamFactory.newInstance()
        factory.loadResource("mapping.xml")

        val reader = factory.createReader("csvStream", ClassPathResource("contact.csv").file)

        var record: Any? = null
        while (true) {
            record = reader.read()
            if (record == null) {
                break
            }
            when (reader.recordName) {
                "header" -> {
                    val header = record as Map<String, Any>
                    println(header["fileDate"])
                }
                "contact" -> {
                    val contact = record as Map<String, Any>
                    println(contact)
                }

                "trailer" -> {
                    val recordCount = record as Int
                    print("recordCount: $recordCount")
                }
            }
        }
        reader.close()
    }
}