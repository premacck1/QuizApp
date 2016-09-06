package com.prembros.programming.quizapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Created by Prem $ on 7/4/2016.
 */
@SuppressWarnings("unused")
public class DatabaseHolder {
    public static final String field = "field";
    public static final String difficulty = "difficulty";
    public static final String question = "question";
    public static final String option1 = "option1";
    public static final String option2 = "option2";
    public static final String option3 = "option3";
    public static final String option4 = "option4";
    public static final String answer = "answer";
    public static final String tableName = "QuizTable";
    public static final String versionTableName = "QuizVersion";
    public static final String databaseName = "quizDatabase";
    public static final String version = "version";
    public static final String version_id = "version_id";
    public static final int database_version = 1;
    public static final String Table_Create = "create table if not exists QuizTable (field text not null, difficulty text not null,question text not null, option1 text not null, option2 text not null, option3 text not null, option4 text not null, answer text not null);";
    public static final String quiz_version = "create table if not exists QuizVersion (version text not null, version_id text not null);";
    public static final String version_insert = "INSERT INTO QuizVersion(version, version_id) VALUES('1', '1');";
    public static final String correctAttempts = "create table if not exists correctAttempts (question text not null, answer text not null);";
    public static final String incorrectAttempts = "create table if not exists incorrectAttempts (question text not null, givenAnswer text not null, correctAnswer text not null);";
    public static final String skippedAttempts = "create table if not exists skippedAttempts (question text not null, answer text not null);";

    DatabaseHelper dbHelper;
    Context context;
    SQLiteDatabase db;
    public DatabaseHolder(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseHolder open() {
        db  = dbHelper.getWritableDatabase();
        return this;
    }
    public void close() {
        dbHelper.close();
    }

    public long insertData(String field_l, String difficulty_l,
                           String question_l,
                           String option1_l, String option2_l, String option3_l, String option4_l,
                           String answer_l) {
        ContentValues content = new ContentValues();
        content.put(field, field_l);
        content.put(difficulty, difficulty_l);
        content.put(question, question_l);
        content.put(option1, option1_l);
        content.put(option2, option2_l);
        content.put(option3, option3_l);
        content.put(option4, option4_l);
        content.put(answer, answer_l);
        return db.insertOrThrow(tableName, null, content);
    }

    public long deleteData(String question_l) {
        return db.delete(tableName, "question='"+question_l+"'", null);
    }
    public long deleteAllQuestions(){
            return db.delete(tableName, "1", null);
    }
    public String getFieldName(int index){
        switch(index){
            case 0:
                return "iOS";
            case 1:
                return "Java";
            case 2:
                return "HTML";
            case 3:
                return "JavaScript";
            default:
                return null;
        }
    }

    public Cursor returnData() {
        return db.query(tableName, new String[] {field,difficulty,question,option1,option2,option3,option4,answer}, null, null, null, null, null);
    }
    public Cursor returnQuestion(){
        return db.query(tableName, new String[]{question}, null, null, null, null, null);
    }
    public Cursor returnBookmarkedQuestion(String field){
        return db.query(true, tableName, new String[]{DatabaseHolder.field, question, answer}, DatabaseHolder.field + " = '"+ field +"'", null, null, null, null, null);
    }

    public void resetTables(){
//        db.execSQL("DROP TABLE IF EXISTS QuizTable");
//        db.execSQL("DROP TABLE IF EXISTS QuizVersion");
        db.execSQL("DROP TABLE IF EXISTS correctAttempts");
        db.execSQL("DROP TABLE IF EXISTS incorrectAttempts");
        db.execSQL("DROP TABLE IF EXISTS skippedAttempts");
        try{
//            db.execSQL(Table_Create);
//            db.execSQL(quiz_version);
//            db.execSQL(version_insert);
            db.execSQL(correctAttempts);
            db.execSQL(incorrectAttempts);
            db.execSQL(skippedAttempts);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public long insertVersion(String version_l, String version_id_l) {
        ContentValues content = new ContentValues();
        content.put(version, version_l);
        content.put(version_id, version_id_l);
        return db.insertOrThrow(versionTableName, null, content);
    }

    public long updateVersion(String version_l,  String version_id_l) {
        ContentValues content = new ContentValues();
        content.put(version, version_l);
        return db.update(versionTableName, content,"version_id="+version_id_l,null);
    }

    public Cursor getQuestionVersion() {
        return db.query(versionTableName, new String[]{version}, null, null, null, null, null);
    }

    public long insertCorrectAnswer(String question_l, String answer_l) {
        ContentValues content = new ContentValues();
        content.put(question, question_l);
        content.put(answer, answer_l);
        Questions.CORRECT_ANSWERS++;
        Questions.QUESTION_COUNT++;
        return db.insertOrThrow("correctAttempts", null, content);
    }

    public Cursor returnCorrectAnswers() {
        return db.query("correctAttempts", new String[] {question,answer}, null, null, null, null, null);
    }

    public long insertIncorrectAnswer(String question_l, String givenAnswer_l, String correctAnswer_l) {
        ContentValues content = new ContentValues();
        content.put(question, question_l);
        content.put("givenAnswer", givenAnswer_l);
        content.put("correctAnswer", correctAnswer_l);
        Questions.INCORRECT_ANSWERS++;
        Questions.QUESTION_COUNT++;
        return db.insertOrThrow("incorrectAttempts", null, content);
    }

    public Cursor returnIncorrectAnswers() {
        return db.query("incorrectAttempts", new String[] {question,"givenAnswer", "correctAnswer"}, null, null, null, null, null);
    }

    public long insertSkippedAnswer(String question_l, String answer_l) {
        ContentValues content = new ContentValues();
        content.put(question, question_l);
        content.put(answer, answer_l);
        Questions.QUESTION_COUNT++;
        return db.insertOrThrow("skippedAttempts", null, content);
    }

    public Cursor returnSkippedAnswers() {
        return db.query("skippedAttempts", new String[] {question,answer}, null, null, null, null, null);
    }

    public long deleteQuestion(int i, String question_l) {
        if (i < 0) {
            Questions.INCORRECT_ANSWERS--;
            Questions.QUESTION_COUNT--;
            return db.delete("incorrectAttempts", "question='" + question_l + "'", null);
        }
        else if (i > 0){
            Questions.CORRECT_ANSWERS--;
            Questions.QUESTION_COUNT--;
            return db.delete("correctAttempts", "question='" + question_l + "'", null);
        }
        else {
            Questions.QUESTION_COUNT--;
            return db.delete("skippedAttempts", "question='" + question_l + "'", null);
        }
    }

    public int isQuestionPresentInAnswersTable(String question){
        Cursor c = db.query(true, "correctAttempts", new String[]{DatabaseHolder.question}, DatabaseHolder.question + " = '"+ question +"'", null, null, null, null, null);
        Cursor c1 = db.query(true, "incorrectAttempts", new String[]{DatabaseHolder.question}, DatabaseHolder.question + " = '"+ question +"'", null, null, null, null, null);
        Cursor c2 = db.query(true, "skippedAttempts", new String[]{DatabaseHolder.question}, DatabaseHolder.question + " = '"+ question +"'", null, null, null, null, null);

//        IF THE QUESTION IS PRESENT IN c (correctAttempts TABLE)
        if (!c.isAfterLast()){
            c.close();
            c1.close();
            c2.close();
            return 1;
        }
//        IF THE QUESTION IS PRESENT IN c1 (incorrectAttempts TABLE)
        else if (!c1.isAfterLast()){
            c.close();
            c1.close();
            c2.close();
            return -1;
        }
//        IF BOTH ABOVE CURSORS ARE NULL, i.e., THE QUESTION IS AVAILABLE IN skippedAttempts table
        else{
            c.close();
            c1.close();
            c2.close();
            return 0;
        }
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, field, null, database_version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try{
                db.execSQL(Table_Create);
                db.execSQL(quiz_version);
                db.execSQL(version_insert);
                db.execSQL(correctAttempts);
                db.execSQL(incorrectAttempts);
                db.execSQL(skippedAttempts);
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS QuizTable");
            db.execSQL("DROP TABLE IF EXISTS QuizVersion");
            onCreate(db);
        }

    }
}