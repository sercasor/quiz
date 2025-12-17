package com.example.quiz.controller;

import com.example.quiz.model.User;
import com.example.quiz.model.Question;
import com.example.quiz.service.QuizUserDetailsService;
import com.example.quiz.service.QuestionsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

@Controller
public class WebsiteController {
    private final QuizUserDetailsService userDetailsService;
    private final QuestionsService questionsService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(WebsiteController.class); //logger para ir viendo por la terminal info interesante para debuggear
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
        model.addAttribute("role",role);

        //creacion del usuario para guardarlo ene l Model
        User user=userDetailsService.getUserByUsername(username);
        model.addAttribute("user",user);

        // Redirect to the appropriate page based on the role
        if (role.equals("ROLE_ADMIN")) {
            model.addAttribute("listaQuizzes",questionsService.loadQuizzes());
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
        logger.info("Se ha creado un nuevo usuario correctamente");
        return "redirect:/login?success";
    }


    @GetMapping("/admin/add")
    public String addQuizForm(Model model) {
        Question question=new Question();

        //TODO: pensar si esto realmente vale la pena más allá de ver los valores que se van pasando
        if (!model.containsAttribute("question")){
            System.out.println("No existía el atributo question. Va a añadirse al modelo al final ");

        }else {
            System.out.println("Ya existe el atributo question. Su valor es: ");
            System.out.println(model.getAttribute("question")); //DEBUG
        }
        //TODO: add attribute al Model??
        model.addAttribute("question", question);


        return "addQuiz"; // Returns the addQuiz.html template

    }

    @PostMapping("/admin/add")
    //DEBUG: @ModelAttribute Question question //lo usábamos al principio pero el formulario presenta varias dificultades: ArrayList no es un tipo de html input y tenemos que asegurarnos que cada respuesta correcta corresponda exactamente con las opciones introducidas. Por eso hemos optado por pasar un objeto vacío y varios RequestParam para asignarle atributos en el endpoint del POST
    public String addQuiz(@Valid @ModelAttribute Question question,
                          BindingResult bindingResult,
                          Model model) {
    //TODO: revisar si ponemos validaciones, revisar si todo esto funciona
        // 1. Verifica si hay errores de validación
        if (bindingResult.hasErrors()) {
            // Redirige o vuelve al formulario con errores. Importate que sea la misma vista o Thymeleaf no mostrará los errores
            return "/admin/add";
        }
            //añadimos al objeto el resto de atributos que nos faltan gracias a los atributos auxiliares tomados del form (opciones, enunciado y respuesta correcta)
            question.setId(questionsService.getNextId());
            Question.setQuizesCreated(questionsService.getNextId()+1);
            ArrayList<String> listaOpciones=new ArrayList<>(Arrays.asList(question.getInputOpcionesString()));
            question.setOptions(listaOpciones);

            question.setCorrectAnswer(question.getOptions().get(Integer.parseInt(question.getInputCorrectAnswer()))); //el select nos devuelve un String --> lo parseamos a int y lo usamos para el get() del ArrayList como parámetro del setter

            //pasamos el objeto acabado al modelo
            model.addAttribute("question",question);

        //actualizamos nuestra BD local con la nueva pregunta
        try {
            questionsService.addQuiz(question);
        } catch (Exception e) {
            System.out.println(e.getMessage());
// Redirect to the /add endpoint
            return "redirect:/admin/addQuiz?error";//DEBUG: podríamos usar @PathVariable para darle capacidades extra y más info al usuario
        }


// Redirect to the /add endpoint
        logger.info("Se ha creado una nueva pregunta correctamente con el valor: "+ model.getAttribute("question"));
        return "redirect:/admin/add?success";





    }

//    TODO: ENDPOINTS PENDIENTES
    /*


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