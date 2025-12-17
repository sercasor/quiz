package com.example.quiz.service;


import com.example.quiz.model.Question;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class QuestionsService {

    // Simula una base de datos en memoria usando un Map
    // En una aplicación real, esto sería reemplazado por un Repository que conecte con una BD real
    private final Map<Integer, Question> questions = new HashMap<>();


    public List<Question> loadQuizzes(){

        //devuelve un Collection con los valores del mapa. Por eso teemos  que castearlo con el método values (devuelve una Collection) a List si queremos que devuelva ese tipo
        List<Question> quizz= new ArrayList<>(this.questions.values());

        return quizz;
    }

    public void addQuiz(Question question) throws Exception {

        if(this.questions.containsKey(question.getId())) {
            throw new Exception("Se está añadiendo una pregunta que ya existe");
        } else {
            int nextId = getNextId();
            question.setId(nextId);

            this.questions.put(nextId, question);
        }



    }

    /**
     * Método auxiliar que nos sirve para sacar el número de preguntas almacenadas en nuestra BD.
     * Nos puede servir para determinar el ID de una nueva pregunta y asignarlo tras instanciar el objeto previamente a añadirlo a la BD.
     * @return int que representa la posición del último elemento del mapa de preguntas
     */
    public int getNextId(){

        int id=this.questions.size();
        return id;
    }

    public void editQuiz(Question question) throws Exception{


        if(this.questions.containsKey(question.getId())) {
            this.questions.replace(question.getId(), question);
        } else {
            throw new Exception("Se está intentando editar una pregunta que no existe");
        }

    }

    public void deleteQuiz(String questionID) throws Exception{

        if(this.questions.containsKey(questionID)) {
            this.questions.remove(questionID);
        } else {
            throw new Exception("Se está intentando borrar una pregunta que no existe");
        }

    }



}
