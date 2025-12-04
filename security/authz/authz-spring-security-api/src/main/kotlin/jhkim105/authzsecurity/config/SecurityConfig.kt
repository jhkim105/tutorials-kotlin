package jhkim105.authzsecurity.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val headerAuthenticationFilter: HeaderAuthenticationFilter,
    private val errorResponseWriter: ErrorResponseWriter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, authException ->
                        errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, authException.message)
                    }
                    .accessDeniedHandler { _, response, accessDeniedException ->
                        errorResponseWriter.write(response, HttpStatus.FORBIDDEN, accessDeniedException.message)
                    }
            }
        return http.build()
    }
}
