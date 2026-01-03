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

import java.util.*;

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

        //question list to display in admin or user dashboard respectively
        List<Question> listaQuizzes=new ArrayList<>(questionsService.loadQuizzes());
        model.addAttribute("listaQuizzes",listaQuizzes);
        logger.info("En el model se ha añadido el atributo listaQuizzes con los siguientes valores: "+listaQuizzes);
        // Redirect to the appropriate page based on the role
        if (role.equals("ROLE_ADMIN")) {
            return "admin"; // Return the admin.html template, it has the quiz options for admins
        } else {
//            //DEBUG, NO PARECE NECESARIO Y ES MÁS BIEN COMPLICADO DE IMPLEMENTAR CON THYMELEAF
//            Map<Integer,String> respuestasUsuario=new HashMap<>(); //List idéntica que modificamos en la vista y luego usaremos en el endpoint para comparar
//            model.addAttribute("respuestasUsuario",respuestasUsuario);
//            logger.info("En el model se ha añadido un Map vacío llamado respuestasUsuario");
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

    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               Model model) {
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

    /*----------------------------------------------------ENDPOINTS PARA AÑADIR PREGUNTAS----------------------------------------------------*/

    @GetMapping("/admin/add")
    public String addQuizForm(Model model) {
        Question question=new Question();

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
            logger.warn(e.getMessage());
// Redirect to the /add endpoint
            return "redirect:/admin/addQuiz?error";//DEBUG: podríamos usar @PathVariable para darle capacidades extra y más info al usuario
        }


// Redirect to the /add endpoint
        logger.info("Se ha creado una nueva pregunta correctamente con el valor: "+ model.getAttribute("question"));
        return "redirect:/admin/add?success";





    }

    /*----------------------------------------------------ENDPOINTS PARA EDITAR PREGUNTAS----------------------------------------------------*/



        @GetMapping("/admin/edit/{id}")
    public String editQuizForm(
        @PathVariable  int id,
        Model model
                ) {

            Question question=new Question(); //question que bindeamos con el formulario para editar
            model.addAttribute("question",question);

            //son los datos antiguos simplemente para display
            Question oldQuestion=this.questionsService.getQuestions().get(id);
            String oldQuestionString=oldQuestion.toString();
            model.addAttribute("oldQuestionString",oldQuestionString);

            int oldQuestionID=oldQuestion.getId();
            model.addAttribute("oldQuestionID",oldQuestionID);

        return "editQuiz"; // Returns the addQuiz.html template
    }

    @PostMapping("/admin/edit/{id}")
    public String editQuiz(
            @Valid @ModelAttribute Question question,
            BindingResult bindingResult,
            @PathVariable  int id,
            Model model) {
        if (bindingResult.hasErrors()) {
            // Redirige o vuelve al formulario con errores. Importate que sea la misma vista o Thymeleaf no mostrará los errores
            return "editQuiz";
        }

        if(question!=null){
            model.addAttribute("question",question);
        }

        Question oldQuestion=this.questionsService.getQuestions().get(id);

        //añadimos al objeto el resto de atributos que nos faltan gracias a los atributos auxiliares tomados del form (opciones, enunciado y respuesta correcta)

        try {
            ArrayList<String> listaOpciones=new ArrayList<>(Arrays.asList(question.getInputOpcionesString()));

            question.setOptions(listaOpciones);

            question.setCorrectAnswer(question.getOptions().get(Integer.parseInt(question.getInputCorrectAnswer()))); //el select nos devuelve un String --> lo parseamos a int y lo usamos para el get() del ArrayList como parámetro del setter

            //pasamos el objeto acabado al modelo
            model.addAttribute("question",question);

            //actualizamos nuestra BD local con la nueva pregunta
            questionsService.editQuiz(question);

            logger.info("Se ha recibido con éxito la petición de cambio de una pregunta. Los nuevos datos son: "+ question.toString());

        } catch (NullPointerException e) {
            logger.info("El input de las opciones ha dado un null. Mensaje error: "+ e.getMessage());
        }catch (Exception e) {
            logger.info("Se ha producido un error:  "+ e.getMessage());
            return "redirect:/admin/edit?error";//DEBUG: tal vez mejor sin UTMs
        }

        return "redirect:/admin/edit/{id}?success"; //IDEA: usar un RequestParam en el endpoint del get para mostrar el mensake de éxito? O tal vez hacer algo con Thymeleaf


    }

    /*----------------------------------------------------ENDPOINTS PARA BORRAR PREGUNTAS----------------------------------------------------*/


    @GetMapping("/admin/delete/{id}")
    public String deleteQuizForm(
        @PathVariable  int id,
        Model model
                ) {

        //son los datos de la pregunta que vamos a borrar
        Question question=this.questionsService.getQuestions().get(id);
        model.addAttribute("question",question);
        logger.info("Se ha pasado el atributo question con el valor: "+model.getAttribute("question")); //DEBUG: BORRAR

        //OPCION CON MODEL ATTRIBUTES SI NO CONSEGUIMOS EL TOSTRING Y EL ID
        //son los datos antiguos simplemente para display
        String questionString=question.toString();
        model.addAttribute("questionString",questionString);
        logger.info("Se ha pasado el atributo questionString con el valor: "+model.getAttribute("questionString")); //DEBUG: BORRAR


        int questionID=question.getId();
        model.addAttribute("questionID",questionID);
        logger.info("Se ha pasado el atributo questionID con el valor: "+model.getAttribute("questionID")); //DEBUG: BORRAR
        return "deleteQuiz"; // Returns the deleteQuiz.html template


    }
    @PostMapping("/admin/delete/{id}")
    public String deleteQuiz(
            @PathVariable  int id,
            Model model) {




        try {
            //son los datos de la pregunta que vamos a borrar
            Question question=this.questionsService.getQuestions().get(id);
            model.addAttribute("question",question);
            logger.info("Se ha solicitado la eliminación de una pregunta con los siguientes  datos: "+ question.toString());
            //actualizamos nuestra BD local
            questionsService.deleteQuiz(question);
            logger.info("Pregunta borrada con éxito");

        } catch (Exception e) {
            logger.info("Se ha producido un error:  "+ e.getMessage());
            return "redirect:/admin/delete/{id}?error";//DEBUG: tal vez mejor sin UTMs
        }

        return "redirect:/home?deletesuccess";
    }

    /*----------------------------------------------------TODO: ENDPOINTS DE RESULTADOS (USUARIOS)----------------------------------------------------*/

//TODO: DETERMINAR SI ESTE ENDPOINT NOS SIRVE. HABRÍA QUE GUARDAR LAS RESPUESTAS DE ALGUNA FORMA (VARIABLE DE SESION MAYBE?)
    @GetMapping("/results")
    public String result() {

        return "results";
    }

    //Remember: to get all params a Map can be used as a method parameter (prolly requires some clean up)
    @PostMapping("/results")
    public String submitAnswers(
            @RequestParam Map<String,String> allRequestParams,
            Model model) {

        //get all params and clean them (?)
        logger.info("Se han detectado una serie de parámetros y almacenado en un Map. Estos son los contenidos: "+allRequestParams);
        //get all questions for evaluation
        List<Question> listaQuizzes=new ArrayList<>(questionsService.loadQuizzes());
        model.addAttribute("listaQuizzes",listaQuizzes);
        logger.info("En el model se ha añadido el atributo listaQuizzes en el endpoint POST de /results con los siguientes valores: "+listaQuizzes);


        return "results";

    }


}