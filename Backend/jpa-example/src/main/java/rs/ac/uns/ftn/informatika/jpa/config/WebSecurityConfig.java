package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import rs.ac.uns.ftn.informatika.jpa.service.CustomUserDetailsService;
import rs.ac.uns.ftn.informatika.jpa.util.JwtAuthenticationFilter;
import rs.ac.uns.ftn.informatika.jpa.metrics.ActiveUserTrackingFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ActiveUserTrackingFilter activeUserTrackingFilter;

    public WebSecurityConfig(CustomUserDetailsService userDetailsService,
                             JwtAuthenticationFilter jwtAuthenticationFilter,
                             ActiveUserTrackingFilter activeUserTrackingFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.activeUserTrackingFilter = activeUserTrackingFilter;
    }

    /* ===== Auth beans ===== */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider direktno koristi tvoj CustomUserDetailsService
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    // AuthenticationManager za AuthController (idiomatski u 2.7)
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /* ===== Glavni Security lanac ===== */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // admin: menjanje ad-eligible
                        .antMatchers(org.springframework.http.HttpMethod.PUT, "/api/posts/*/ad-eligible").hasRole("ADMIN")

                        // otvoreni endpointi (ostavljeno kako imaš)
                        .antMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/activate",
                                "/api/posts/all",
                                "/api/posts/all?userId=*",
                                "/api/posts/all-comments/{id}",
                                "/api/posts/{id}",
                                "/api/posts/add-comment",
                                "/api/locations/",
                                "/api/locations/{id}",
                                "/api/posts/like/{postId}",
                                "/api/posts",
                                "/api/users/all/{id}",
                                "/api/users/filter/{id}",
                                "/images/**",
                                "/api/users/{id}",
                                "/api/users/show/{id}",
                                "/api/users/followUser/{id}",
                                "/api/users/followUserId/{id}",
                                "/api/users/sendWeeklySummaries/",
                                "/api/posts/getPost/{id}",
                                "/api/analytics",
                                "/api/posts/analytics/top-likers-last7days",
                                "/api/users/me",
                                "/api/care-locations",
                                "/api/posts/nearby",
                                "/api/posts/analytics/top-posts-week",
                                "/api/posts/analytics/top-posts-alltime",
                                "/api/users/profile/{id}",
                                "/api/users/change-password",
                                "/api/chats/**",
                                "/ws-chat/**",
                                "/ws-chat",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        // redosled filtera
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(activeUserTrackingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ===== Globalni CORS ===== */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
