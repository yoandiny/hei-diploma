package mg.yoan.diploma.endpoint.rest.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final DatabaseAuthenticationProvider databaseAuthenticationProvider;
  private final RoleBasedAuthSuccessHandler roleBasedAuthSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final WebAwareAuthenticationEntryPoint authenticationEntryPoint;
  private final WebAwareAccessDeniedHandler accessDeniedHandler;
  private final JwtCookie jwtCookie;

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
    FilterRegistrationBean<JwtAuthenticationFilter> registration =
        new FilterRegistrationBean<>(jwtAuthenticationFilter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/auth/**", "/students/**", "/ping", "/health/**", "/admin/students")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .securityContext(
            context -> context.securityContextRepository(new NullSecurityContextRepository()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/auth/login")
                    .permitAll()
                    .requestMatchers("/ping", "/health/**")
                    .permitAll()
                    .requestMatchers("/admin/students")
                    .hasRole("ADMIN")
                    .requestMatchers("/students/**")
                    .hasRole("STUDENT")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .requestCache(cache -> cache.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .securityContext(
            context -> context.securityContextRepository(new NullSecurityContextRepository()))
        .authenticationProvider(databaseAuthenticationProvider)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/", "/error", "/login", "/logout", "/forbidden")
                    .permitAll()
                    .requestMatchers("/student/**")
                    .hasRole("STUDENT")
                    .requestMatchers("/teacher/**")
                    .hasRole("TEACHER")
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .successHandler(roleBasedAuthSuccessHandler)
                    .failureHandler(loginFailureHandler)
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .addLogoutHandler(
                        (request, response, authentication) -> jwtCookie.clear(request, response))
                    .logoutSuccessUrl("/login?logout")
                    .permitAll())
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
