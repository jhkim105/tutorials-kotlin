package jhkim105.tutorials.beanio.fixedlength

import org.beanio.StreamFactory
import org.beanio.builder.StreamBuilder
import org.junit.jupiter.api.Test

class FixedLengthAnnotationTest {
    private fun streamFactory(streamName: String): StreamFactory {
        val builder = StreamBuilder(streamName)
            .format("fixedlength")
            .strict()
            .addRecord(AnnotatedContact::class.java)

        val streamFactory = StreamFactory.newInstance()
        streamFactory.define(builder)
        return streamFactory
    }

    @Test
    fun marshall() {
        val contact = AnnotatedContact(
            firstName = "John",
            lastName = "Smith",
            number = "01011112222",
        )

        val streamName = "fixedlength"
        val streamFactory = streamFactory(streamName)
        val marshaller = streamFactory.createMarshaller(streamName)
        val result = marshaller.marshal(contact).toString()
        println("contact-> $result")
    }

    @Test
    fun unmarshall() {
        val input = "John----------------Smith                         01011112222"
        val streamName = "fixedlength"
        val streamFactory = streamFactory(streamName)
        val unmarshaller = streamFactory.createUnmarshaller(streamName)
        val contact = unmarshaller.unmarshal(input) as AnnotatedContact
        println(contact)
    }
}