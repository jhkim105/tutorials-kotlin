package jhkim105.tutorials.kotlin.spring_aop.event_logging

interface NoticeEvent {
    fun from(): String
    fun to(): String
    fun eventType(): EventType
}

enum class EventType {
    FOLLOW, UNFOLLOW
}

