package com.example.quiz.service;

import com.example.quiz.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;



@Service
public class QuizUserDetailsService implements UserDetailsService{

    // Simula una base de datos en memoria usando un Map
    // En una aplicación real, esto sería reemplazado por un Repository que conecte con una BD real
    private final Map<String, User> users = new HashMap<>();

    // Codificador de contraseñas que usará Spring Security para verificar las contraseñas
    // BCrypt es un algoritmo de hashing seguro ampliamente utilizado
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Spring Security llama automáticamente a este método durante el proceso de login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.get(username);

        // Si no encuentra el usuario, lanza una excepción que Spring Security capturará
        // Esto resultará en un error de "credenciales inválidas" en el login
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Construye y retorna un objeto UserDetails que Spring Security entiende
        // Este objeto contiene la información que Spring Security necesita para la autenticación
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())      // Nombre de usuario para login
                .password(user.getPassword())      // Contraseña codificada (hash) para verificación
                .roles(user.getRole())             // Rol(es) del usuario para autorización (se convertirá a "ROLE_XXX" automáticamente gracuas a WebSecurityConfig)
                .build();
    }

    public void registerUser(String username, String password, String role, String email) throws Exception {
        // Verifica si el usuario ya existe para evitar duplicados. Buscamos key porque 2 usuarios pueden tener la misma contraseña ero no el mismo usuario(de no tener ID, podría ser PK)
        if(this.users.containsKey(username)) {
            throw new Exception("User already exists");
        } else {
            // Codifica la contraseña en texto plano a un hash seguro antes de almacenarla
            String encodedPassword=this.passwordEncoder.encode(password);

            // Almacena el nuevo usuario en nuestro Map

            this.users.put(username,new User(username,encodedPassword, role, email));

            // En una app real, esto guardaría en una base de datos
        }
    }

    //metodo auxiliar para obtener un objeto tipo user al indicar el nombre (lo podemos sacar con Authentication)
    public User getUserByUsername(String username) {
        return users.get(username);  // sacamos el objeto user de forma sencilla
    }
}
