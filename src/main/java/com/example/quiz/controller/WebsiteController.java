package com.example.quiz.controller;

import com.example.quiz.model.User;
import com.example.quiz.model.Question;
import com.example.quiz.service.QuizUserDetailsService;
import com.example.quiz.service.QuestionsService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;//SIRVE DE ALGO?
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebsiteController {
    private final QuizUserDetailsService userDetailsService;
    private final QuestionsService questionsService;
    private final AuthenticationManager authenticationManager;
    public WebsiteController(QuizUserDetailsService userDetailsService, QuestionsService questionsService, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.questionsService = questionsService;
        this.authenticationManager = authenticationManager;
    }

    /*-------ENDPOINTS-----------*/
    //este controller redirige al login  según el usuario esté logueado tras comprobarlo
    @GetMapping("/home")
    public String homepage(Model model) {
// Get the authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
// Check if the user is authenticated
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
// Redirect to the login page if the user is not authenticated
            return "redirect:/login";
        }
// Get the username
        String username = authentication.getName();


        model.addAttribute("username", username);
// Get the user's role
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER"); // Default role if no authority is found
// Redirect to the appropriate page based on the role
        model.addAttribute("role",role);

        //creacion del usuario para guardarlo ene l Model
        User user=userDetailsService.getUserByUsername(username);
        model.addAttribute("user",user);


        if (role.equals("ROLE_ADMIN")) {
            return "admin"; // Return the admin.html template, it has the quiz options for admins
        } else {
            return "quiz"; // Return the quiz.html template. for users to view and submit quiz answers right after logging in.
        }

    }




    @GetMapping("/login")
    public String login() {

        return "login"; // Returns the login.html template
    }


    @GetMapping("/register")
    public String register(Model model) {
        User user=new User();
        model.addAttribute("user", user);
        return "register"; // Returns the register.html template
    }
    // POST endpoint to handle user registration and auto-login

    @PostMapping("/register")

    public String registerUser(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {
        // 1. Verifica si hay errores de validación
        if (bindingResult.hasErrors()) {
            // Redirige o vuelve al formulario con errores
            return "register";
        }

// Register the user by storing their details in the HashMap
        String username=user.getUsername();
        String password=user.getPassword();
        String role=user.getRole();
        String email=user.getEmail();
        try {

            userDetailsService.registerUser(username, password, role, email);
        } catch (Exception userExistsAlready) {
// Redirect to the /register endpoint
            return "redirect:/register?error";
        }
// Authenticate the user programmatically
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
// Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
// Redirect to the /login endpoint
        return "redirect:/login?success";
    }


//    TODO: ENDPOINTS PENDIENTES
    /*
    @GetMapping("/admin/add")
    public String addQuizForm() {

        return "addQuiz"; // Returns the addQuiz.html template
    }

    @PostMapping("/admin/add")
    public String addQuiz() {

        return "addQuiz"; // Returns the addQuiz.html template
    }

    @GetMapping("/admin/edit")
    public String editQuizForm() {

        return "editQuiz"; // Returns the addQuiz.html template
    }

    @PutMapping("/admin/edit")
    public String editQuiz() {

        return "editQuiz"; // Returns the addQuiz.html template
    }

    @DeleteMapping("/admin/edit")
    public String deleteQuiz() {

        return "editQuiz"; // Returns the addQuiz.html template
    }



    @GetMapping("/result")
    public String result() {

        return "quiz"; // Returns the addQuiz.html template
    }


    @PostMapping("/quiz")
    public String submitAnswers(Model model) {



    }*/


}