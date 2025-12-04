package jhkim105.tutorials.config

import jhkim105.tutorials.security.SecurityUtils
import jhkim105.tutorials.security.UserPrincipal
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

@Configuration
@EnableJpaAuditing
class JpaConfig {

    @Bean
    fun auditorAware(): AuditorAware<String> = AuditorAware {
        Optional.ofNullable(SecurityUtils.getAuthUserSilently())
            .map(UserPrincipal::id)
    }
}
