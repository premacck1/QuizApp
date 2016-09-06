package com.prembros.programming.quizapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Created by Prem $ on 7/4/2016.
 */
public class QuestionJSONParser extends ArrayList<QuestionBean> {

//    QuestionJSONParser (JSONObject jObject, String field, String difficulty){
//        parse(jObject, field, difficulty);
//    }

    /** Receives a JSONObject and returns a list */
    public ArrayList<QuestionBean> parse(JSONObject jObject, String field, String difficulty){

        JSONArray jFieldArray;
        JSONArray jFinalArray = null;
        
        try {
            /** Retrieves all the elements in the 'field' array */
            jFieldArray = jObject.getJSONArray(field);
            int difficultyIndex = 0;
            switch(difficulty){
                case "Rookie":
                    difficultyIndex = 0;
                    break;
                case "Apprentice":
                    difficultyIndex = 1;
                    break;
                case "Pro":
                    difficultyIndex = 2;
                    break;
                case "Hitman":
                    difficultyIndex = 3;
                    break;
                default:
                    break;
            }
            JSONObject difficultyObj = jFieldArray.getJSONObject(difficultyIndex);
            jFinalArray = difficultyObj.getJSONArray(difficulty);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getQuestions with the array of json object
         * where each json object represent a field
         */
        return getQuestions(jFinalArray,field,difficulty);
    }

    private ArrayList<QuestionBean> getQuestions(JSONArray jQuestions,String field,String difficulty){
        
        int questionCount = jQuestions.length();
        ArrayList<QuestionBean> fieldList = new ArrayList<>();
        QuestionBean question;

        /** Taking each question, parses and adds to list object */
        for(int i=0; i<questionCount; i++){
            try {
                /** Call getQuestion with question JSON object to parse the question */
                question = getQuestion((JSONObject)jQuestions.get(i),field,difficulty);
                fieldList.add(question);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return fieldList;
    }

    /** Parsing the Question JSON object */
    private QuestionBean getQuestion(JSONObject jQuestion, String d, String difficulty){

        QuestionBean field = new QuestionBean();
        String diff, question, opt1, opt2, opt3, opt4, ans;

        try {
            diff = difficulty;
            question = jQuestion.getString("question");
            opt1 = jQuestion.getString("opt1");
            opt2 = jQuestion.getString("opt2");
            opt3 = jQuestion.getString("opt3");
            opt4 = jQuestion.getString("opt4");
            ans =  jQuestion.getString("ans");

            field.setDifficulty(diff);
            field.setQuestion(question);
            field.setOption1(opt1);
            field.setOption2(opt2);
            field.setOption3(opt3);
            field.setOption4(opt4);
            field.setAnswer(ans);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return field;
    }
}