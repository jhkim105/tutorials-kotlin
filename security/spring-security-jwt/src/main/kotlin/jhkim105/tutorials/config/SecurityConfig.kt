package jhkim105.tutorials.config

import jhkim105.tutorials.jwt.JwtService
import jhkim105.tutorials.security.JwtAuthenticationFilter
import jhkim105.tutorials.security.JwtAuthenticationProvider
import jhkim105.tutorials.security.SecurityErrorHandler
import jhkim105.tutorials.security.TokenAuthenticationEntryPoint
import jhkim105.tutorials.security.UserDetailsServiceImpl
import jhkim105.tutorials.user.UserRepository
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityErrorHandler: SecurityErrorHandler,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) {

    companion object {
        private val IGNORE_URI_PATTERNS = arrayOf("/version", "/error")
    }

    @Bean
    @Order(1)
    fun ignoredPatternFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatchers { matchers ->
                matchers
                    .requestMatchers(*IGNORE_URI_PATTERNS)
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            }
            .authorizeHttpRequests { authorize -> authorize.anyRequest().permitAll() }
            .requestCache { it.disable() }
            .securityContext { it.disable() }
            .sessionManagement { it.disable() }
            .build()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login", "/jwks/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), BasicAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(tokenAuthenticationEntryPoint()) }

        return http.build()
    }

    private fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        val jwtAuthenticationFilter = JwtAuthenticationFilter("/**", jwtService, securityErrorHandler)
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager())
        jwtAuthenticationFilter.afterPropertiesSet()
        return jwtAuthenticationFilter
    }

    @Bean
    fun userDetailsService(): UserDetailsService = UserDetailsServiceImpl(userRepository)

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun tokenAuthenticationEntryPoint() = TokenAuthenticationEntryPoint(securityErrorHandler)

    @Bean
    fun authenticationManager(): AuthenticationManager =
        ProviderManager(daoAuthenticationProvider(), jwtAuthenticationProvider())

    private fun jwtAuthenticationProvider() = JwtAuthenticationProvider()

    private fun daoAuthenticationProvider(): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService())
            setPasswordEncoder(passwordEncoder())
        }
}
