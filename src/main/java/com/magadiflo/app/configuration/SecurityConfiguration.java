package com.magadiflo.app.configuration;

import com.magadiflo.app.constant.SecurityConstant;
import com.magadiflo.app.filter.JwtAccessDeniedHandler;
import com.magadiflo.app.filter.JwtAuthenticationEntryPoint;
import com.magadiflo.app.filter.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //Nos permitirá establecer la seguridad al nivel de método
public class SecurityConfiguration {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public SecurityConfiguration(JwtAuthorizationFilter jwtAuthorizationFilter,
                                 JwtAccessDeniedHandler jwtAccessDeniedHandler,
                                 JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors()//Si alguien no se especificó como un dominio que pueda acceder a esta aplicación, será rechazado
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(SecurityConstant.PUBLIC_URLS).permitAll() //Todos pueden acceder a estas urls
                .anyRequest().authenticated() //Cualquier otra solicitud debe estar autenticado
                .and()
                .exceptionHandling().accessDeniedHandler(this.jwtAccessDeniedHandler)
                .authenticationEntryPoint(this.jwtAuthenticationEntryPoint)
                .and()
                .addFilterBefore(this.jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
/***
 * NOTA: Nueva configuración para la clase SecurityConfiguration
 * En la clase antigua (la que hereda de WebSecurityConfigurerAdapter) sobreescribía tres métodos:
 * 1°-----------
 * @Override
 * protected void configure(AuthenticationManagerBuilder auth) throws Exception {
 *    auth.userDetailsService(this.userDetailsService).passwordEncoder(this.bCryptPasswordEncoder);
 * }
 *
 * 2°-----------
 * @Override
 * protected void configure(HttpSecurity http) throws Exception {
 *    http.csrf().disable().cors().....
 *    .......
 * }
 *
 * 3°-----------
 * @Bean
 * @Override
 * public AuthenticationManager authenticationManagerBean() throws Exception {
 *    return super.authenticationManagerBean();
 * }
 *
 * Y en la clase principal del proyecto (SpringSecurityJwtBackendApplication) se colocó el bean del passwordEncoder
 * @Bean
 * public BCryptPasswordEncoder bCryptPasswordEncoder() {
 * 	 return new BCryptPasswordEncoder();
 * }
 *
 * Los cambios realizados fueron:
 * UNO) Crear un bean del SecurityFilterChain que reemplaza al método 2 sobreescrito
 * @Bean
 * protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
 *  -----
 *  -----
 * }
 * DOS) El método 3° sobreescrito, sigue siendo un bean, pero ya no se obtiene de la clase
 * que extendíamos, porque está deprecado y se eliminó. En este caso, se obtiene de otra
 * manera el AuthenticationManager
 * @Bean
 * public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration)
 *    throws Exception {
 *    return authenticationConfiguration.getAuthenticationManager();
 * }
 * TRES) El método 1° sobreescrito:
 * protected void configure(AuthenticationManagerBuilder auth) throws Exception {...
 * fue eliminado ya según los comentarios de otros desarrolladores en el doc. oficial de spring
 * mencionan: "Configurar UserDetailsService y PasswordEncoder en AuthenticationManagerBuilder es redundante.
 * Si son frijoles, Spring Security los recogerá automáticamente." Por tal motivo fueron eliminados y
 * únicamente nos aseguramos de declarar un bean del tipo PasswordEncoder, mismo que ya lo tenemos
 * declarado en la clase principal del proyecto (SpringSecurityJwtBackendApplication)
 *
 * Referencias de las fuentes consultadas:
 * https://www.youtube.com/watch?v=7HQ-x9aoZx8
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 * https://www.codejava.net/frameworks/spring-boot/fix-websecurityconfigureradapter-deprecated
 */