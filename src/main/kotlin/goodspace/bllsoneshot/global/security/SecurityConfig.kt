package goodspace.bllsoneshot.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val tokenProvider: TokenProvider
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/swagger-ui/**").permitAll() // swagger
                    .requestMatchers("/v3/api-docs/**").permitAll() // SpringDoc
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 허용
                    .requestMatchers("/auth/**").permitAll() // 인증 인가
                    .anyRequest().authenticated()
            }
            .cors { it.configurationSource(configurationSource()) }
            .addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun configurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "https://frontend-three-inky-vo3s6yr68l.vercel.app",
                "http://localhost:5173"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Access-Control-Allow-Credentials", "Authorization", "Set-Cookie")
            allowCredentials = true
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().also {
            it.registerCorsConfiguration("/**", config)
        }
    }
}
