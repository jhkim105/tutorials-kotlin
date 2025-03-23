package jhkim105.tutorials.redis.pubsub

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic

class RedisMessageSubscriber: MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        println("Message received: ${String(message.body)}")
    }
}