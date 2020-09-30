package com.example.courseplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;
    private static final String DATABASE_NAME = "coursePlan";

    //fields that maintain names for the fields in the database
    //fields that appear in more than one table
    private static final String ID_PK_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String MENTOR_ID_FK_FIELD  = "mentorId";
    private static final String START_DATE_FIELD = "Start";
    private static final String END_DATE_FIELD = "End";
    private static final String NOTES_FIELD = "notes";
    private static final String HAS_NOTIFICATION_FIELD = "hasNotification";
    //Term (includes ID_PK_FIELD, NAME_FIELD, START_DATE_FIELD, and END_DATE_FIELD)
    private static final String TERM_TABLE_NAME = "Term";
    //Course (includes ID_PK_FIELD, NAME_FIELD, MENTOR_ID_FIELD, START_DATE_FIELD, END_DATE_FIELD, NOTES_FIELD, HAS_NOTIFICATION)
    private static final String COURSE_TABLE_NAME = "Course";
    private static final String TERM_ID_FK_FIELD = "termId";
    private static final String STATUS_FIELD = "status";
    //Requirement (table used for assessments & objectives) (includes ID_PK_FIELD, NAME_FIELD, STATUS_FIELD, NOTES_FIELD, HAS_NOTIFICATION)
    private static final String REQUIREMENT_TABLE_NAME = "Requirement";
    private static final String COURSE_ID_FK_FIELD = "courseId";
    private static final String TYPE_FIELD = "type";
    private static final String DUE_DATE = "dueDate";
    //Mentor table only consists of shared name (ID_PK_FIELD & NAME_FIELD)
    private static final String MENTOR_TABLE_NAME = "Mentor";
    //MentorEmail (includes ID_PK_FIELD and MENTOR_IF_FK_FIELD)
    private static final String MENTOR_EMAIL_TABLE_NAME = "MentorEmail";
    private static final String EMAIL_FIELD = "email";
    //MentorPhone (includes ID_PK_FIELD and MENTOR_ID_FK_FIELD)
    private static final String MENTOR_PHONE_TABLE_NAME = "MentorPhone";
    private static final String PHONE_NUMBER_FIELD = "phoneNumber";
    //Notification (includes ID_PK_FIELD, NAME_FIELD)
    private static final String NOTIFICATION_TABLE_NAME = "Notification";
    private static final String TABLE_ID_FK_FIELD = "tableIdFK";
    private static final String TABLE_NAME_FK_FIELD = "tableNameFK";


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        db = this.getWritableDatabase();
    }

    /*
    * Establish the structure of the database
    * mentorEmail and mentorPhone is broken into two tables to allow a mentor to possess multiple of the represented data
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //term table
        db.execSQL("CREATE TABLE " + TERM_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + NAME_FIELD + " string NOT NULL, " + START_DATE_FIELD + " date NOT NULL, "+ END_DATE_FIELD + " date NOT NULL);");
        //courses table
        db.execSQL("CREATE TABLE " + COURSE_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + NAME_FIELD + " string NOT NULL, " + TERM_ID_FK_FIELD + " integer NOT NULL, " + MENTOR_ID_FK_FIELD + " integer NOT NULL, " + START_DATE_FIELD + " date NOT NULL, " + END_DATE_FIELD + " date NOT NULL, " + STATUS_FIELD + " string NOT NULL, " + NOTES_FIELD + " string, " + HAS_NOTIFICATION_FIELD + " boolean NOT NULL DEFAULT 'FALSE');");
        //assessment table
        db.execSQL("CREATE TABLE " + REQUIREMENT_TABLE_NAME +" (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + COURSE_ID_FK_FIELD +" integer NOT NULL, " + NAME_FIELD + " string NOT NULL, " + TYPE_FIELD + " string NOT NULL, " + NOTES_FIELD + " string NOT NULL," + DUE_DATE + " date NOT NULL," + STATUS_FIELD + " string NOT NULL, " + HAS_NOTIFICATION_FIELD + " boolean NOT NULL DEFAULT 'FALSE');");
        //mentor table
        db.execSQL("CREATE TABLE " + MENTOR_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + NAME_FIELD + " string NOT NULL);");
        //mentorPhone table
        db.execSQL("CREATE TABLE " + MENTOR_PHONE_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + MENTOR_ID_FK_FIELD + " integer NOT NULL, " + PHONE_NUMBER_FIELD + " string NOT NULL);");
        //mentorEmail table
        db.execSQL("CREATE TABLE " + MENTOR_EMAIL_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + MENTOR_ID_FK_FIELD + " integer NOT NULL, " + EMAIL_FIELD + " string NOT NULL);");
        //notification table (this table only contains pointers to notifications made by the application on the application's device and not the actual notification. notification interactions handled by NotificationHelper.class)
        //this table is not meant to be interacted with by the user outside of setting and removing notifications
        db.execSQL("CREATE TABLE " + NOTIFICATION_TABLE_NAME + " (" + ID_PK_FIELD + " integer PRIMARY KEY AUTOINCREMENT, " + TABLE_ID_FK_FIELD + " integer NOT NULL, " + TABLE_NAME_FK_FIELD + " string NOT NULL, " + NAME_FIELD + " string NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP DATABASE [IF EXISTS] " + DATABASE_NAME);
            onCreate(db);
        }
    }

    /*
    * Used in unit tests and not meant to be used elsewhere
    * ---------WILL DELETE ALL EXISTING DATA IN THE DATABASE, IRRECOVERABLY---------
    *
    * creates the following:
    * 5 terms
    * 10 courses, 2 per term
    * 10 Assessments, 1 per course
    * 10 Mentors
    * 11 Emails and 11 Phone Numbers, one mentor (mentor 4) has 2 emails, one other mentor (mentor 6) has 2 phone numbers, all others have 1 email and 1 phone number, each assigned to one of the courses in order
    *
    * 57 total
    *
    * All dates are created based on the current date, but dont actually matter in current test phases
    *
    * no notifications are created for this test, adjust as needed if testing notifications
    */
    public void createTestValues(){
        //drop all tables
        Cursor tables = db.rawQuery("SELECT * FROM sqlite_master where type='table' AND name != 'sqlite_sequence'",null);
        if(tables.moveToFirst()){
            do{
                db.execSQL("DROP TABLE IF EXISTS " + tables.getString(tables.getColumnIndex("name")));
            } while(tables.moveToNext());
        }

        //remake database
        onCreate(db);

        ContentValues values = new ContentValues();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar currDate = Calendar.getInstance();
        int fkVal;

        try{
            currDate.setTime(DATE_FORMAT.parse(Calendar.getInstance().get(Calendar.YEAR) + "-" + Calendar.getInstance().get(Calendar.MONTH)+ "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        } catch (ParseException e){
            e.printStackTrace();
        }
        Calendar alterDate = (Calendar) currDate.clone();
        //terms
        for (int i = 1; i <= 5; i++){
            values.clear();
            values.put(NAME_FIELD, "Term " + i);
            values.put(START_DATE_FIELD, alterDate.get(Calendar.YEAR) + "-" + alterDate.get(Calendar.MONTH) + "-" + alterDate.get(Calendar.DAY_OF_MONTH));
            //terms are assumed to last one month for the sake of testing (even though no real term is that short)
            //this also sets the START_DATE_FIELD value for the next cycle as all terms are assumed to begin at the same time as the previous one's end for the sake of testing
            //the above applies to all start -> end date relationships and will not be repeated
            alterDate.add(Calendar.MONTH, 1);
            values.put(END_DATE_FIELD, alterDate.get(Calendar.YEAR) + "-" + alterDate.get(Calendar.MONTH) + "-" + alterDate.get(Calendar.DAY_OF_MONTH));

            db.insert(TERM_TABLE_NAME, null, values);
        }

        //mentors
        for(int i = 1; i <= 10; i++){
            //main mentor table
            values.clear();
            values.put(NAME_FIELD, "Mentor " + i);

            db.insert(MENTOR_TABLE_NAME, null, values);
            //email
            values.clear();
            values.put(MENTOR_ID_FK_FIELD, i);
            values.put(EMAIL_FIELD, "MENTOR EMAIL " + i);

            db.insert(MENTOR_EMAIL_TABLE_NAME, null, values);
            //phone
            values.clear();
            values.put(MENTOR_ID_FK_FIELD, i);
            values.put(PHONE_NUMBER_FIELD, "MENTOR PHONE " + i);

            db.insert(MENTOR_PHONE_TABLE_NAME, null, values);
        }
        //the two mentors that have an extra email/phone number must be hand coded in
        //mentor 4 - extra email
        values.clear();
        values.put(MENTOR_ID_FK_FIELD, 4);
        values.put(EMAIL_FIELD, "MENTOR EMAIL 4 - 2");
        db.insert(MENTOR_EMAIL_TABLE_NAME, null, values);
        //mentor 6 - extra phone number
        values.clear();
        values.put(MENTOR_ID_FK_FIELD, 6);
        values.put(PHONE_NUMBER_FIELD, "MENTOR PHONE NUMBER 6 - 2");
        db.insert(MENTOR_PHONE_TABLE_NAME, null, values);

        //courses
        alterDate = (Calendar) currDate.clone(); //reset the date
        for(int i = 1; i <= 10; i++) {
            values.clear();
            values.put(NAME_FIELD, "Course " + i);
            //there are 5 terms, each term gets 2 courses, the below equation will assign each term 2 courses based on i
            fkVal = i / 2;
            values.put(TERM_ID_FK_FIELD, fkVal);
            values.put(MENTOR_ID_FK_FIELD, i); //just i is used as each mentor gets 1 assigned to 1 course
            values.put(START_DATE_FIELD, alterDate.get(Calendar.YEAR) + "-" + alterDate.get(Calendar.MONTH) + "-" + alterDate.get(Calendar.DAY_OF_MONTH));
            //each course is assumed to be 2 weeks for the sake of testing
            alterDate.add(Calendar.WEEK_OF_MONTH, 2);
            values.put(END_DATE_FIELD, alterDate.get(Calendar.YEAR) + "-" + alterDate.get(Calendar.MONTH) + "-" + alterDate.get(Calendar.DAY_OF_MONTH));
            values.put(STATUS_FIELD, "Plan to Take"); //hardcoded value for simplicity, actual value does not matter
            values.put(NOTES_FIELD, "TEST NOTE");
            values.put(HAS_NOTIFICATION_FIELD, false);

            db.insert(COURSE_TABLE_NAME, null, values);
        }

        //assessments
        alterDate = (Calendar) currDate.clone();
        for (int i = 1; i <= 10; i++){
            values.clear();
            values.put(COURSE_ID_FK_FIELD, i);//i used as the amount of requirements and courses are the same
            values.put(NAME_FIELD, "REQUIREMENT " + i);
            values.put(TYPE_FIELD, "ASSESSMENT");//hardcoded for simplicity, value does not matter
            values.put(NOTES_FIELD, "TEST NOTES");
            //each test is assumed to take place on the last day of the planned course which happens to be 2 weeks
            alterDate.add(Calendar.WEEK_OF_MONTH, 2);
            values.put(DUE_DATE, alterDate.get(Calendar.YEAR) + "-" + alterDate.get(Calendar.MONTH) + "-" + alterDate.get(Calendar.DAY_OF_MONTH));
            values.put(STATUS_FIELD, "Plan to Take");//hardcoded for simplicity, value does not matter
            values.put(HAS_NOTIFICATION_FIELD, false);

            db.insert(REQUIREMENT_TABLE_NAME, null, values);
        }
    }


    /*
    *Inserts data into the term table
    */
    public boolean insertDataTerm(int id, String name, String start, String end) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_PK_FIELD, id);
        contentValues.put(NAME_FIELD, name);
        contentValues.put(START_DATE_FIELD, start);
        contentValues.put(END_DATE_FIELD, end);

        long result = db.insert(TERM_TABLE_NAME, null, contentValues);
        //if insert failed
        if (result == -1) return false;

        //insert successful
        return true;
    }

    public final Cursor getField(String table, String field, String whereColumn, String whereValue){
        Cursor result = db.rawQuery("SELECT " + field + " FROM " + table + " WHERE " + whereColumn + " = ?;", new String[]{whereValue});

        return result;
    }

    public final Cursor getAllOfField(String table, String field){
        Cursor result = db.rawQuery("SELECT " + field + " FROM " + table  + ";", null);

        return result;
    }
    public final Cursor getAllOfTable(String table){
        Cursor result = db.rawQuery("SELECT * FROM " + table + ";", null);

        return result;
    }
    public final Cursor getAllOfTable(String table, String whereColumn, String whereValue){
        Cursor result = db.rawQuery("SELECT * FROM " + table + " WHERE " + whereColumn + " = ?;", new String[]{whereValue});

        return result;
    }

    public final Cursor getAllOfGreaterValues(String table, String whereColumn, String whereValue){
        Cursor result = db.rawQuery("SELECT * FROM " + table + " WHERE " + whereColumn + " > ?;", new String[]{whereValue});

        return result;
    }

    public boolean updateTerm(String whereValue, String id, String name, String start, String end){
        ContentValues values = new ContentValues();
        values.put(ID_PK_FIELD, id);
        values.put(NAME_FIELD, name);
        values.put(START_DATE_FIELD, start);
        values.put(END_DATE_FIELD, end);

        long result = db.update(TERM_TABLE_NAME, values, ID_PK_FIELD + " = ?", new String[]{ whereValue });

        //if update failed
        if (result == -1) return false;

        return true;
    }

    public boolean updateAssessment(String whereValue, String id, String name, String courseFK, String type, String status, String date, String notes, String hasNotification){
        ContentValues values = new ContentValues();
        values.put(ID_PK_FIELD, id);
        values.put(NAME_FIELD, name);
        values.put(COURSE_ID_FK_FIELD, courseFK);
        values.put(TYPE_FIELD, type);
        values.put(STATUS_FIELD, status);
        values.put(DUE_DATE, date);
        values.put(NOTES_FIELD, notes);
        values.put(HAS_NOTIFICATION_FIELD, hasNotification);

        long result = db.update(REQUIREMENT_TABLE_NAME, values, ID_PK_FIELD + " = ?", new String[]{ whereValue });

        if(result == -1) return false;

        return true;
    }
    public boolean updateMentor(String whereValue, String name){
        ContentValues values = new ContentValues();
        values.put(NAME_FIELD, name);

        long result = db.update(MENTOR_TABLE_NAME, values, ID_PK_FIELD + " = ?", new String[]{ whereValue });

        if(result == -1) return false;

        return true;
    }


    public boolean insertMentor(String name){
        ContentValues values = new ContentValues();
        values.put(NAME_FIELD, name);

        long result = db.insert(MENTOR_TABLE_NAME, null, values);

        if(result == -1) return false;

        return true;
    }

    public boolean insertAssessment(String name, String courseFK, String type, String status, String date, String notes, String hasNotification){
        ContentValues values = new ContentValues();
        values.put(NAME_FIELD, name);
        values.put(COURSE_ID_FK_FIELD, courseFK);
        values.put(TYPE_FIELD, type);
        values.put(STATUS_FIELD, status);
        values.put(DUE_DATE, date);
        values.put(NOTES_FIELD, notes);
        values.put(HAS_NOTIFICATION_FIELD, hasNotification);

        long result = db.insert(REQUIREMENT_TABLE_NAME, null, values);

        if(result == -1) return false;

        return true;
    }

    public boolean insertDataCourse(String name, String termId, String mentorId, String start, String end, String status, String notes, String hasNotification){
        ContentValues values = new ContentValues();
        values.put(NAME_FIELD, name);
        values.put(TERM_ID_FK_FIELD, termId);
        values.put(MENTOR_ID_FK_FIELD, mentorId);
        values.put(START_DATE_FIELD, start);
        values.put(END_DATE_FIELD, end);
        values.put(STATUS_FIELD, status);
        values.put(NOTES_FIELD, notes);
        values.put(HAS_NOTIFICATION_FIELD, hasNotification);

        long result = db.insert(COURSE_TABLE_NAME, null, values);

        //if insert failed
        if (result == -1) return false;

        //insert successful
        return true;
    }

    public boolean insertEmail(String mentorId, String email){
        ContentValues values = new ContentValues();
        values.put(MENTOR_ID_FK_FIELD, mentorId);
        values.put(EMAIL_FIELD, email);

        long result = db.insert(MENTOR_EMAIL_TABLE_NAME, null, values);

        //if insert failed
        if (result == -1) return false;

        //insert successful
        return true;
    }

    public boolean insertPhoneNumber(String mentorId, String phoneNum){
        ContentValues values = new ContentValues();
        values.put(MENTOR_ID_FK_FIELD, mentorId);
        values.put(PHONE_NUMBER_FIELD, phoneNum);

        long result = db.insert(MENTOR_PHONE_TABLE_NAME, null, values);

        //if insert failed
        if (result == -1) return false;

        //insert successful
        return true;
    }

    public boolean updateCourse(String id, String name, String termId, String mentorId, String start, String end, String status, String notes){
        ContentValues values = new ContentValues();
        values.put(NAME_FIELD, name);
        values.put(TERM_ID_FK_FIELD, termId);
        values.put(MENTOR_ID_FK_FIELD, mentorId);
        values.put(START_DATE_FIELD, start);
        values.put(END_DATE_FIELD, end);
        values.put(STATUS_FIELD, status);
        values.put(NOTES_FIELD, notes);

        long result = db.update(COURSE_TABLE_NAME, values, ID_PK_FIELD + " = ?;", new String[]{ id });

        //if insert failed
        if (result == -1) return false;

        //insert successful
        return true;
    }

    public boolean insertNotification(String tableName, String tableId, String name){
        ContentValues values = new ContentValues();
        values.put(TABLE_NAME_FK_FIELD, tableName);
        values.put(TABLE_ID_FK_FIELD, tableId);
        values.put(NAME_FIELD, name);

        long result = db.insert(NOTIFICATION_TABLE_NAME, null, values);

        //if delete failed
        if(result == -1) return false;

        //delete successful
        return true;
    }

    public boolean deleteField(String table, String whereField, String whereValue){
        long result = db.delete(table, whereField + " = ?;", new String[]{whereValue});

        //if delete failed
        if(result == -1) return false;

        //delete successful
        return true;
    }

    public final String getTermTableName(){ return TERM_TABLE_NAME; }
    public final String getCourseTableName(){ return COURSE_TABLE_NAME; }
    public final String getIdPkField(){ return ID_PK_FIELD; }
    public final String getNameField(){ return NAME_FIELD; }
    public final String getStartDateField(){ return START_DATE_FIELD; }
    public final String getEndDateField(){ return END_DATE_FIELD; }
    public final String getMentorTableName(){ return MENTOR_TABLE_NAME; }
    public final String getMentorIdFkField(){ return MENTOR_ID_FK_FIELD; }
    public final String getMentorEmailTableName(){ return MENTOR_EMAIL_TABLE_NAME; }
    public final String getMentorPhoneTableName(){ return MENTOR_PHONE_TABLE_NAME; }
    public final String getPhoneNumberField(){ return PHONE_NUMBER_FIELD; }
    public final String getEmailField(){ return  EMAIL_FIELD; }
    public final String getRequirementTableName() { return REQUIREMENT_TABLE_NAME; }
    public final String getTypeField(){ return TYPE_FIELD; }
    public final String getStatusField(){ return STATUS_FIELD; }
    public final String getNotesField(){ return NOTES_FIELD; }
    public final String getDueDate(){ return DUE_DATE; }
    public final String getCourseIdFkField() {return COURSE_ID_FK_FIELD; }
    public final String getTermIdFkField(){ return TERM_ID_FK_FIELD; }
    public final String getNotificationTableName(){ return NOTIFICATION_TABLE_NAME; }
    public final String getTableIdFkField() { return TABLE_ID_FK_FIELD; }
    public final String getTableNameFkField(){ return TABLE_NAME_FK_FIELD; }
    public final String getHasNotification(){ return HAS_NOTIFICATION_FIELD; }
}
