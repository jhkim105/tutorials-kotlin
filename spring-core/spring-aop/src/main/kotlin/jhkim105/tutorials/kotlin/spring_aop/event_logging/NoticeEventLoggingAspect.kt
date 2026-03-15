package jhkim105.tutorials.kotlin.spring_aop.event_logging

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
@Aspect
class NoticeEventLoggingAspect {

    private val log = LoggerFactory.getLogger(javaClass)

    @Before("execution(* jhkim105.tutorials.kotlin..*.*Controller.*(..)) && args(noticeEvent)")
    fun logLoggableProperty(joinPoint: JoinPoint, noticeEvent: NoticeEvent) {
        log.info("$noticeEvent")
    }


}