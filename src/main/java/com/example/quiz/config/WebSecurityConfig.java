package com.example.quiz.config;
import com.example.quiz.service.QuizUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// Indica que esta clase es una clase de configuración de Spring
@Configuration
// Habilita la seguridad web de Spring Security y permite la configuración personalizada
@EnableWebSecurity
public class WebSecurityConfig {
    // Servicio personalizado que carga los detalles del usuario desde la base de datos
    private final QuizUserDetailsService userDetailsService;

    // Constructor que recibe el servicio por inyección de dependencias
    public WebSecurityConfig(QuizUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Bean principal que define la cadena de filtros de seguridad (el corazón de Spring Security)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize                  // Configuración de autorización de solicitudes HTTP
                        //Rutas públicas - acceso sin autenticación
                        .requestMatchers("/register", "/login").permitAll() // Allow access to registration and login pages //
                        // Rutas restringidas por roles
                        .requestMatchers("/admin*").hasRole("ADMIN") // Restrict /admin y subfolders to users with the ADMIN role
                        .requestMatchers("/viewer").hasRole("USER") // Restrict /viewer to users with the USER role
                        // Regla por defecto para cualquier otra ruta
                        .anyRequest().authenticated() // Require authentication for all other endpoints
                )
                // Configuración del formulario de login personalizado
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .defaultSuccessUrl("/home", true) // Redirect to /homeafter successful login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/home")//PRUEBAS de redirecciones de logout
                        .permitAll()
                );
        // Construye y retorna la cadena de filtros configurada
        return http.build();
    }
    // Bean que configura el AuthenticationManager (gestor de autenticación)
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // Obtiene el builder para construir el AuthenticationManager
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        // Configura el AuthenticationManager
        authenticationManagerBuilder
                .userDetailsService(userDetailsService) // mi UserDetailsService personalizado (atributo)
                .passwordEncoder(passwordEncoder()); // el codificador de contraseñas (BCryptPasswordEncoder, el metodo que implementa su interfaz está abajo)
        // Construye y retorna el AuthenticationManager configurado
        return authenticationManagerBuilder.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Retorna un codificador BCrypt (algoritmo de hashing seguro para contraseñas)
        return new BCryptPasswordEncoder();
    }
}
