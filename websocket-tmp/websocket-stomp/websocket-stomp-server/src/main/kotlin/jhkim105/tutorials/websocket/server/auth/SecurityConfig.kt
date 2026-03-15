package jhkim105.tutorials.websocket.server.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager

//@Configuration
//@EnableWebSocketSecurity
class SecurityConfig {

    @Bean
    fun authorizationManager(messages: MessageMatcherDelegatingAuthorizationManager.Builder): AuthorizationManager<Message<*>> {
        return messages.simpMessageDestMatchers("/app").authenticated()
            .simpSubscribeDestMatchers("/topic").authenticated()
            .simpTypeMatchers(SimpMessageType.CONNECT).authenticated()
            .nullDestMatcher().permitAll()
            .anyMessage().denyAll()
            .build()
    }

    /**
     * 6.1.5 기준 csrfChannelInterceptor 를 비활성화는 옵션이 없음.
     * csrfChannelInterceptor 라는 Bean 이름으로 등록시 변경되므로 임시로 추가
     * @see <a href="https://docs.spring.io/spring-security/reference/6.1/servlet/integrations/websocket.html#websocket-sameorigin-disable">Disable CSRF within WebSockets</a>
     * @see org.springframework.security.config.annotation.web.socket.WebSocketMessageBrokerSecurityConfiguration
     */
    @Bean
    fun csrfChannelInterceptor() = object : ChannelInterceptor {}
}