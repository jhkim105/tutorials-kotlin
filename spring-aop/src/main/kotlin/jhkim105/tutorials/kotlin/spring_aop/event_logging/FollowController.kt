package jhkim105.tutorials.kotlin.spring_aop.event_logging

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/follow")
class FollowController {

    @PostMapping
    fun follow(@RequestBody followRequest: FollowRequest) {

    }
}

data class FollowRequest(val from: String, val to: String) : NoticeEvent {
    override fun from(): String {
        return from
    }

    override fun to(): String {
        return to
    }

    override fun eventType(): EventType {
        return EventType.FOLLOW
    }


}