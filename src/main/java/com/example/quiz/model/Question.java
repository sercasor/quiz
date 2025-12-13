package com.example.quiz.model;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Question {

    private int id;
    @NotEmpty(message="Debes insertar un enunciado para la pregunta")
    private String questionText;
    private ArrayList<String> options;
    private String correctAnswer;
    private static int quizesCreated;

    //variables auxiliares
    @Size(min=3, message = "Debes insertar 3 respuestas")
    private String inputOpcionesString[]; //campo auxiliar que guarda el input del usuario y que se convertirá en un arraylist usando como separador "_"
    @NotEmpty(message="Debes insertar una respuesta correcta para la pregunta")
    private String inputCorrectAnswer; //campo auxiliar que guarda el número de la opción correcta, se usará para acceder a la posición del ArrayList

    public Question() {
    }



    //Constructor que  excluye el ID y el arrayList como parámetros y se basa en los atributos auxiliares para asignar los valores al resto
    public Question(String questionText, String inputOpcionesString[], String inputCorrectAnswer) {
        this.id = Question.quizesCreated+1;
        this.questionText = questionText;
        this.inputOpcionesString = inputOpcionesString;
        this.inputCorrectAnswer = inputCorrectAnswer;

        //asignación de atributos a partir de los atributos auxiliares
        this.options= new ArrayList<>(Arrays.asList(inputOpcionesString));
        this.correctAnswer= options.get(Integer.parseInt(inputCorrectAnswer));//el select nos devuelve un String, de ahí el casting

        //contador de ID simulando un AutoIncrement
        Question.quizesCreated++;

    }

    //constructor completo


    /*Gettters y setters*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public static int getQuizesCreated() {
        return quizesCreated;
    }

    public static void setQuizesCreated(int quizesCreated) {
        Question.quizesCreated = quizesCreated;
    }

    public String[] getInputOpcionesString() {
        return inputOpcionesString;
    }

    public void setInputOpcionesString(String inputOpcionesString[]) {
        this.inputOpcionesString = inputOpcionesString;
    }

    public String getInputCorrectAnswer() {
        return inputCorrectAnswer;
    }

    public void setInputCorrectAnswer(String inputCorrectAnswer) {
        this.inputCorrectAnswer = inputCorrectAnswer;
    }

    /*toString*/

    @Override
    public String toString() {
        return
                "id=" + id +
                ", Enunciado='" + questionText + '\'' +
                ", Opcion A= " + options.get(0) +
                ", Opcion B= " + options.get(1) +
                ", Opcion C= " + options.get(2) +
                ", Respuesta correcta='" + correctAnswer + '\'';
    }
}
