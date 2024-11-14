package jhkim105.tutorials.beanio.fixedlength

import jhkim105.tutorials.beanio.Contact
import org.beanio.BeanWriter
import org.beanio.StreamFactory
import org.beanio.builder.StreamBuilder
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class FixedLengthTest {

    private fun streamFactory(): StreamFactory {
        val streamFactory = StreamFactory.newInstance()
        streamFactory.loadResource("mapping.xml")
        return streamFactory
    }

    @Test
    fun write() {
        val streamFactory = StreamFactory.newInstance()
        streamFactory.loadResource("mapping.xml")

        val contacts = listOf(
            Contact(
                firstName = "John",
                lastName = "Smith",
                number = "01011111111",
            ),
            Contact(
                firstName = "John2",
                lastName = "Smith2",
                number = "01011111112",
            ),
        )

        val writer = streamFactory.createWriter("fixedlengthStream", File("contact.txt"));
        contacts.forEach {
            writer.write(it)
        }
        writer.flush()
        writer.close()
    }

    @Test
    fun read() {
        val streamFactory = StreamFactory.newInstance()
        streamFactory.loadResource("mapping.xml")

        val reader = streamFactory.createReader("fixedlengthStream", ClassPathResource("contact.txt").file)

        val list = mutableListOf<Contact>()
        while(true) {
            val record = reader.read() ?: break
            list.add(record as Contact)
        }
        println(list)
    }



    @Test
    fun marshall() {
        val contact = Contact(
            firstName = "John",
            lastName = "Smith",
            number = "01011111111",
        )

        val streamName = "fixedlengthStream"
        val streamFactory = streamFactory()
        val marshaller = streamFactory.createMarshaller(streamName)
        val result = marshaller?.marshal(contact).toString()
        println("contact-> $result")
    }

    @Test
    fun unmarshall() {
        val input = "John                Smith                         01011111111"
        val streamFactory = streamFactory()
        val unmarshaller = streamFactory.createUnmarshaller("fixedlengthStream")
        val contact = unmarshaller.unmarshal(input) as Contact
        println(contact)
    }


    @Test
    fun marshallList() {
        val contacts = listOf(
            Contact(
                firstName = "John",
                lastName = "Smith",
                number = "01011111111",
            ),
            Contact(
                firstName = "John2",
                lastName = "Smith2",
                number = "01011111112",
            ),
        )

        val streamName = "fixedlengthStream"
        val streamFactory = streamFactory()
        val marshaller = streamFactory.createMarshaller(streamName)

        val result = StringBuilder()
        contacts.forEach {
            result.append(marshaller.marshal(it)).append("\n")
        }

        println(result.toString())
    }

}