package jhkim105.tutorials.batch.adapter.out.file

import jhkim105.tutorials.batch.application.domain.entity.User
import org.beanio.StreamFactory
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component


@Component
class UserFileReader {

    fun read(resource: Resource): List<User> {
        val streamFactory = StreamFactory.newInstance()
        streamFactory.loadResource("beanio-mapping.xml")

        val reader = streamFactory.createReader("userFile", resource.file)

        val users = mutableListOf<User>()
        reader.use {
            while (true) {
                val record = reader.read() ?: break
                if (record is Map<*, *>) {
                    val username = record["username"] as String
                    val name = record["name"] as String
                    users.add(User(username = username, name = name))
                }
            }
        }
        return users
    }
}