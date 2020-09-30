package com.example.courseplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class AddModCourseActivity extends AppCompatActivity {

    private EditText etName, etNotes;
    private TextView tvStart, tvEnd, tvRangeStart, tvRangeEnd;
    private Spinner spnnrTerm, spnnrMentor, spnnrStatus;
    private DBHelper courseDB;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private DatePickerDialog.OnDateSetListener dpListener;
    private String dpSelection;
    private Button btnStart, btnCancel, btnDelete;
    private LinearLayout llTermRange;
    private int courseId;
    private boolean isMod;
    private FragmentManager fragManager;
    private FragmentTransaction fragTransaction;
    private RadioButton rbSetNotificationTrue, rbSetNotificationFalse;
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mod_course);

        courseDB = MainActivity.getCourseDB();
        etName = findViewById(R.id.etName);
        etNotes = findViewById(R.id.etNotes);
        spnnrTerm = findViewById(R.id.spnnrTerm);
        spnnrMentor = findViewById(R.id.spnnrMentor);
        tvStart = findViewById(R.id.tvTermSelectedStart);
        tvEnd = findViewById(R.id.tvTermSelectedEnd);
        tvRangeStart = findViewById(R.id.tvRangeStart);
        tvRangeEnd = findViewById(R.id.tvRangeEnd);
        spnnrStatus = findViewById(R.id.spnnrStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnStart = findViewById(R.id.btnSubmit);
        btnDelete = findViewById(R.id.btnDelete);
        llTermRange = findViewById(R.id.llRange);
        rbSetNotificationTrue = findViewById(R.id.rbTrue);
        rbSetNotificationFalse = findViewById(R.id.rbFalse);

        args = getIntent().getBundleExtra("ARGS");

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setDefaults();
    }

    private void setDefaults(){
        //set spinners
        //term spinner
        Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName());
        ArrayList<String> termValueList = new ArrayList<>();
        if (result.moveToFirst()){
            do{
                termValueList.add(result.getString(result.getColumnIndex(courseDB.getNameField())));
            }while(result.moveToNext());
        }

        ArrayAdapter termAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, termValueList);
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnnrTerm.setAdapter(termAdapter);

        spnnrTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName(), courseDB.getIdPkField(), String.valueOf(spnnrTerm.getSelectedItemPosition() + 1));

                if(result.moveToFirst()) {
                    llTermRange.setVisibility(View.VISIBLE);

                    tvRangeStart.setText(result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " ");
                    tvRangeEnd.setText(" " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));
                } else {
                    llTermRange.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                llTermRange.setVisibility(View.INVISIBLE);
            }
        });

        //Mentor spinner
        Cursor mentorResult = courseDB.getAllOfTable(courseDB.getMentorTableName());
        ArrayList<String> mentorValueList = new ArrayList<>();
        if (mentorResult.moveToFirst()){
            do{
                mentorValueList.add(mentorResult.getString(mentorResult.getColumnIndex(courseDB.getNameField())));
            }while(mentorResult.moveToNext());
        }

        ArrayAdapter mentorAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mentorValueList);
        mentorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrMentor.setAdapter(mentorAdapter);


        //Status Spinner (these values come from R.array instead of the database as they are predefined states a course can be in)
        ArrayList<String>statusValueList = new ArrayList<>();
        statusValueList.addAll(Arrays.asList(getResources().getStringArray(R.array.statusList)));

        ArrayAdapter statusAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, statusValueList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrStatus.setAdapter(statusAdapter);
        //End spinner setup section

        fragManager = getSupportFragmentManager();

        if (args != null){ //if extra data is passed to this activity the term is being modded
            courseId = args.getInt("ID");
            etName.setText(args.getString("NAME"));
            tvStart.setText(args.getString("START"));
            tvEnd.setText(args.getString("END"));
            spnnrTerm.setSelection(getTablesIndex(spnnrTerm, courseDB.getTermTableName(), String.valueOf(args.getInt("TERM"))));
            spnnrStatus.setSelection(getIndex(spnnrStatus, args.getString("STATUS")));
            spnnrMentor.setSelection(getTablesIndex(spnnrMentor, courseDB.getMentorTableName(), String.valueOf(args.getInt("MENTOR"))));
            etNotes.setText(args.getString("NOTES"));

            isMod = true;
            setTitle("Modify Course");
        } else { //extra data was not pass and this term is a new term
            courseId = -1;
            isMod = false;

            //completely disable btnDelete since there is nothing to delete
            btnDelete.setVisibility(View.GONE);
            btnDelete.setClickable(false); //redundant, but clicking this button could be REALLY bad in this context

            setTitle("Add Course");
        }
    }

    /*
     * used to find the index of a string value in a spinner
     * stringId is the primary key id of the course that is being searched, passed as an arg into this activity
     * returns the index of string's name
     */
    private int getTablesIndex(Spinner spinner, String table, String stringId){
        Cursor result = courseDB.getAllOfTable(table, courseDB.getIdPkField(), stringId);
        Log.d("sql", "SELECT * FROM " + table + " WHERE " + courseDB.getIdPkField() + " = " + stringId);
        if(result.moveToFirst()) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(result.getString(result.getColumnIndex(courseDB.getNameField())))) {
                    return i;
                }
            }
        }

        return 0;
    }

    //Create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
    //set functionality of options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent();
        Bundle args = new Bundle();

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        switch(item.getItemId()){
            case R.id.itmCurrent:
                intent = new Intent(getApplicationContext(), CurrentCourseActivity.class);
                startActivity(intent);
                break;
            case R.id.itmHome:
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itmCourses:
                args.putString("MODE", courseDB.getCourseTableName());
                intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("ARGS", args);
                startActivity(intent);
                break;
            case R.id.itmAssessments:
                args.putString("MODE", courseDB.getRequirementTableName());
                intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("ARGS", args);
                startActivity(intent);
                break;
            case R.id.itmMentors:
                args.putString("MODE", courseDB.getMentorTableName());
                intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("ARGS", args);
                startActivity(intent);
                break;
            case R.id.itmTerms:
                args.putString("MODE", courseDB.getTermTableName());
                intent = new Intent(getApplicationContext(), ItemListActivity.class);
                intent.putExtra("ARGS", args);
                startActivity(intent);
                break;
            case R.id.itmOverview:
                intent = new Intent(getApplicationContext(), OverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.itmSearch:
                intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "ERROR: unexpected item in action menu.", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnSubmitIsClicked(View view) throws ParseException {
        //if input is valid
        if(isValid()){
            Cursor result = courseDB.getField(courseDB.getCourseTableName(), courseDB.getHasNotification(), courseDB.getIdPkField(), String.valueOf(courseId));
            if (result.moveToFirst()){
                if (result.getString(result.getColumnIndex(courseDB.getHasNotification())).equals("true") && rbSetNotificationFalse.isChecked()){ //if the current assessment has notifications but the user clicked no notifications
                    cancelNotification();
                }
            }
            //notes are optional, this handles the case if they are empty
            String stringNotes;
            if (etNotes.equals("")){
                stringNotes = "";
            } else {
                stringNotes = etNotes.getText().toString();
            }

            Cursor termName = courseDB.getField(courseDB.getTermTableName(), courseDB.getIdPkField(), courseDB.getNameField(), spnnrTerm.getSelectedItem().toString());
            Cursor mentorName = courseDB.getField(courseDB.getMentorTableName(), courseDB.getIdPkField(), courseDB.getNameField(), spnnrMentor.getSelectedItem().toString());
            termName.moveToFirst();
            mentorName.moveToFirst();

            if(isMod == false) { //a new term is being made
                if (!courseDB.insertDataCourse(etName.getText().toString(), termName.getString(termName.getColumnIndex(courseDB.getIdPkField())), mentorName.getString(mentorName.getColumnIndex(courseDB.getIdPkField())), tvStart.getText().toString(), tvEnd.getText().toString(), spnnrTerm.getSelectedItem().toString(), stringNotes, String.valueOf(rbSetNotificationTrue.isChecked())) || !handleNewNotification()) {
                    Toast.makeText(this, "Course can not be created at this time.", Toast.LENGTH_LONG).show();
                } else { //data entry was successful
                    Toast.makeText(this, "Course created.", Toast.LENGTH_LONG).show();
                }
            } else { //a term is being modified
                if(!courseDB.updateCourse(String.valueOf(courseId), etName.getText().toString(), termName.getString(termName.getColumnIndex(courseDB.getIdPkField())), mentorName.getString(mentorName.getColumnIndex(courseDB.getIdPkField())), tvStart.getText().toString(), tvEnd.getText().toString(), spnnrTerm.getSelectedItem().toString(), stringNotes) || !handleNewNotification()){
                    Toast.makeText(this, "Course can not be created at this time.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Course created.", Toast.LENGTH_LONG).show();
                }
            }
            //regardless close this activity
            finish();
        }
    }

    public void btnCancelIsClicked(View view){
        finish();
    }

    public void btnDeleteIsClicked(View view){
        if(isMod){
            if(courseDB.deleteField(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(courseId)) && cancelNotification()){
                Toast.makeText(this, etName.getText().toString() + " was deleted successfully", Toast.LENGTH_LONG).show();
                cascadeCourseIds();
                finish();
            } else {
                Toast.makeText(this, "The term could not be deleted at this time", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isValid(){

        //the edit text fields must be set to something
        if (etName.equals("") || etNotes.equals("") || spnnrTerm.getSelectedItem() == null || spnnrMentor.getSelectedItem() == null || spnnrStatus.getSelectedItem() == null){
            Toast.makeText(this, "You must input data into each of the fields.", Toast.LENGTH_LONG).show();
            return false;
        }
        //name must be unique
        Cursor nameSearch = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getNameField(), etName.getText().toString());
        if(nameSearch.moveToFirst()) { //if a duplicate name is found
            if(isMod){//if modding
                do {
                    if (nameSearch.getInt(nameSearch.getColumnIndex(courseDB.getIdPkField())) != courseId) { //if the current id does not match the found name
                        Toast.makeText(this, "There is already a term with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } while (nameSearch.moveToNext());//if this every loops, false will be returned
            } else { //else, a duplicate was already found
                Toast.makeText(this, "There is already a term with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        //one of the notification radio buttons must be checked, one is set by default so this should never return false, but exists for error prevention incase something weird happens
        if ((!rbSetNotificationTrue.isChecked() && !rbSetNotificationFalse.isChecked()) || (rbSetNotificationTrue.isChecked() && rbSetNotificationFalse.isChecked())){
            Toast.makeText(this, "One selection must be checked on if you want to make a notification.", Toast.LENGTH_LONG).show();
            return false;
        }

        //start date must be before the end, the logic also catches any possible formatting and unexpected errors
        try {
            //start must be before the end date
            if (DATE_FORMAT.parse(tvStart.getText().toString()).after(DATE_FORMAT.parse(tvEnd.getText().toString())) || (DATE_FORMAT.parse(tvStart.getText().toString()).equals(DATE_FORMAT.parse(tvEnd.getText().toString())))) {
                Toast.makeText(this, "The course's start must be before its own end date.", Toast.LENGTH_LONG).show();
                return false;
            }

            //this course's start and end must lie within etTerms start and end
            Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName(), courseDB.getIdPkField(), String.valueOf(spnnrTerm.getSelectedItemPosition() + 1));
            result.moveToFirst(); //if this somehow fails to acquire any data, it will be caught by the try/catch and I would want false to be returned no matter what
            Date thisStart, thisEnd, courseStart, courseEnd;
            thisStart = DATE_FORMAT.parse(tvStart.getText().toString());
            thisEnd = DATE_FORMAT.parse(tvEnd.getText().toString());
            courseStart = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getStartDateField())));
            courseEnd = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getEndDateField())));

            //if statement that tests the data above
            if (thisStart.before(courseStart) || thisStart.after(courseEnd) || thisEnd.before(courseStart) || thisEnd.after(courseEnd)){
                Toast.makeText(this, "The course's start and end dates must be in between the selected term's start and end dates.", Toast.LENGTH_LONG).show();
                return false;
            }
        }catch(Exception e){
            Toast.makeText(this, "You must set a start and end date for this course", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    //open a date picker that will change the text value of tvStart
    public void tvStartDateIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("ID_TO_UPDATE", String.valueOf(tvStart.getId()));
        args.putString("CALLING_ACTIVITY", this.getClass().getSimpleName());

        fragTransaction = fragManager.beginTransaction();

        DialogFragment newFragment = new DatePickerDialogFragment();
        newFragment.setArguments(args);
        newFragment.show(fragManager.beginTransaction(), "TAG");
    }

    //open a date picker that will change the value of tvEnd
    public void tvEndDateIsClicked(View view){
        Bundle args = new Bundle();
        args.putString("ID_TO_UPDATE", String.valueOf(tvEnd.getId()));
        args.putString("CALLING_ACTIVITY", this.getClass().getSimpleName());

        fragTransaction = fragManager.beginTransaction();

        DialogFragment newFragment = new DatePickerDialogFragment();
        newFragment.setArguments(args);
        newFragment.show(fragManager.beginTransaction(), "TAG");
    }

    public void onDateSet(String result, String idToUpdate){
        TextView toUpdate;
        if (idToUpdate.equals(String.valueOf(tvStart.getId()))){ // if tvStart is being updated
            toUpdate = tvStart;
        } else { //the text view being updated is tvEnd since there are only two text views
            toUpdate = tvEnd;
        }

        if (TextUtils.isEmpty(result)){ //if there was no result (the dialog box was empty)
            toUpdate.setText(R.string.tvSelect);
        } else { //put the result into the text view
            toUpdate.setText(result);
        }
    }

    /*
    * creates a new notification if the user checked the proper radio button
    * if the user does not want a notification, return true so that the application may continue
    * otherwise return whether a pointer to the notification was created or not
    * this function executes right after the rest of course was created and since courseId only has the id if a course is being modified, the id is found through the courseDB
     */
    private boolean handleNewNotification() throws ParseException {
        if (rbSetNotificationFalse.isChecked()){
            return true;
        } else {
            NotificationHelper nh = MainActivity.getNotificationHelper();
            int lastID;

            Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName());
            //forNextId is used to find what the new ids will be when pointers are added to courseDB. lastId + 1 will for the start and lastId + 2 will be for the end
            Cursor forNextId = courseDB.getAllOfTable(courseDB.getNotificationTableName());
            result.moveToLast();
            if(forNextId.moveToLast()){
                lastID = forNextId.getInt(forNextId.getColumnIndex(courseDB.getIdPkField()));
            } else {
                lastID = 0;
            }
            //notification for start of of course
            Date date = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getStartDateField())));
            long timeInMillis = date.getTime();
            nh.addNotification(timeInMillis, lastID + 1);

            //notification for end of course
            date = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getEndDateField())));
            timeInMillis = date.getTime();
            nh.addNotification(timeInMillis, lastID + 2);

            //insert is done twice to make two pointers to the ids of the two new notifications (the course start notification and course end notification)
            return courseDB.insertNotification(courseDB.getStartDateField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())), etName.getText().toString()) && courseDB.insertNotification(courseDB.getEndDateField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())), etName.getText().toString());
        }
    }

    /*
    * cancels any notification associated with courseId and deletes its associated field in the SQLite database
    * should only be accessed by btnDeleteIsClicked() which requires courseId to be set properly and would make isMod = true  preventing false from ever returning
     */
    private boolean cancelNotification(){
        if (isMod) {
            Cursor result = courseDB.getField(courseDB.getNotificationTableName(), courseDB.getIdPkField(), courseDB.getTableIdFkField(), String.valueOf(courseId));
            result.moveToFirst();
            NotificationHelper nh = MainActivity.getNotificationHelper();
            nh.cancelNotification(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));

            return courseDB.deleteField(courseDB.getNotificationTableName(), courseDB.getIdPkField(), String.valueOf(courseId));
        }
        else return false;
    }

    /*
    * Click handler that allows the user to create a new term without having to leave the course editor activity
    * simply opens the new term activity
     */
    public void fabNewTermIsClicked(View view){
        Intent intent = new Intent(getApplicationContext(), AddModTermActivity.class);
        startActivity(intent);
    }
    /*
    * Click handler that allows the user to create a new mentor without having to travel there
    * simply open the new mentor activity
     */
    public void fabNewMentorIsClicked(View view){
        Intent intent = new Intent(getApplicationContext(), AddModMentorActivity.class);
        startActivity(intent);
    }
    public void ibtnShareIsClicked(View view){
        if(isValid()) {
            Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
            intentShare.setType("text/plain");
            String shareBody = etName.getText().toString() + ": " + tvStart.getText().toString() + " - " + tvEnd.getText().toString();
            intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            intentShare.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intentShare, "Share via"));
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        setDefaults();
    }

    /*
    * used to set the position in spinner when trying to set them to a target mString
    *
     */
    private int getIndex(Spinner spinner, String mString){
        for (int i = 0; i < spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).equals(mString)){
                return i;
            }
        }

        //if for some reason mString could not be found, return 0 so that the spinner could set its position to the first position without causing errors
        return 0;
    }

    /*
     * when a course is deleted, decrease the course of each term after the deleted term by 1
     * this is done as the default name of course is of a specific pattern (Term 1, Term 2 etc)
     * ex. terms 1,2,3,4,5 exist, term 3 is deleted
     * without this function the term ids would be, 1,2,4,5 which is wrong as the ids are supposed to to count a sequential order of a real idea
     * this function turns the above ids into 1,2,3,4 after 3 is deleted
     * the other values are not changed
     */
    private void cascadeCourseIds(){
        Cursor result = courseDB.getAllOfGreaterValues(courseDB.getTermTableName(),courseDB.getIdPkField(), String.valueOf(courseId));

        while(result.moveToNext()){
            int newId = result.getInt(result.getColumnIndex(courseDB.getIdPkField())) - 1;
            //update the course with its own data but use newId for its id field
            courseDB.updateCourse(result.getString(result.getColumnIndex(courseDB.getIdPkField())), result.getString(result.getColumnIndex(courseDB.getNameField())), String.valueOf(newId), result.getString(result.getColumnIndex(courseDB.getMentorIdFkField())), result.getString(result.getColumnIndex(courseDB.getStartDateField())), result.getString(result.getColumnIndex(courseDB.getEndDateField())), result.getString(result.getColumnIndex(courseDB.getStatusField())), result.getString(result.getColumnIndex(courseDB.getNotesField())));
        }
    }
}
