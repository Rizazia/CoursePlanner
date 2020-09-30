package com.example.courseplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button btnCurrent;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static DBHelper courseDB;
    private static NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCurrent = findViewById(R.id.btnCurrent);

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        courseDB = new DBHelper(this);
        notificationHelper = new NotificationHelper(this);

        if(BuildConfig.DEBUG) {
            courseDB.createTestValues();
        }
        //set text for btnCurrent based on the currently enrolled term and course
        try {
            Date currDate = DATE_FORMAT.parse(DATE_FORMAT.format(Calendar.getInstance().getTime())); //set currTime from system time
            Cursor allTerms = courseDB.getAllOfTable(courseDB.getTermTableName());

            if(allTerms.moveToFirst()){//if a term exists
                do{
                    //if currDate is between the term's dates (user is assumed enrolled)
                    if(currDate.after(DATE_FORMAT.parse(allTerms.getString(allTerms.getColumnIndex(courseDB.getStartDateField())))) && currDate.before(DATE_FORMAT.parse(allTerms.getString(allTerms.getColumnIndex(courseDB.getEndDateField()))))){
                        Cursor allCourses = courseDB.getAllOfTable(courseDB.getCourseTableName());
                        if (allCourses.moveToFirst()){
                            do{
                                //if currDate is between the course's dates (user is assumed enrolled)
                                if(currDate.after(DATE_FORMAT.parse(allCourses.getString(allCourses.getColumnIndex(courseDB.getStartDateField())))) && currDate.before(DATE_FORMAT.parse(allCourses.getString(allCourses.getColumnIndex(courseDB.getEndDateField()))))){
                                    //set text to be the term's name, start, and end dates \n course's name, start, and end date
                                    btnCurrent.setText("Current Term: " + allTerms.getString(allTerms.getColumnIndex(courseDB.getNameField())) + " " + allTerms.getString(allTerms.getColumnIndex(courseDB.getStartDateField())) + " to " + allTerms.getString(allTerms.getColumnIndex(courseDB.getEndDateField()))
                                            + "\nCurrent Course: " + allCourses.getString(allCourses.getColumnIndex(courseDB.getNameField())) + " " + allCourses.getString(allCourses.getColumnIndex(courseDB.getStartDateField())) + " to " + allCourses.getString(allCourses.getColumnIndex(courseDB.getEndDateField())));
                                    break;
                                } else if(allCourses.isLast()){ //if this is the last course (not enrolled in a course)
                                    //set text to be the term's name, start, and end dates \n course's name, start, and end date
                                    btnCurrent.setText("Current Term: " + allTerms.getString(allTerms.getColumnIndex(courseDB.getNameField())) + " " + allTerms.getString(allTerms.getColumnIndex(courseDB.getStartDateField())) + " to " + allTerms.getString(allTerms.getColumnIndex(courseDB.getEndDateField()))
                                            + "\nYou are currently not enrolled in any courses.");
                                }
                            } while (allCourses.moveToNext());
                        } else { //no enrolled course found
                            //set text to be the term's name, start, and end dates \n course's name, start, and end date
                            btnCurrent.setText("Current Term: " + allTerms.getString(allTerms.getColumnIndex(courseDB.getNameField())) + " " + allTerms.getString(allTerms.getColumnIndex(courseDB.getStartDateField())) + " to " + allTerms.getString(allTerms.getColumnIndex(courseDB.getEndDateField()))
                                    + "\nYou are currently not enrolled in any courses.");
                        }
                        break;
                    } else if (allTerms.isLast()){ //no term's found, user is not enrolled in either term or course (term enrollment is required for course enrollment)
                        btnCurrent.setText("You are not in any terms or courses.");
                    }
                } while(allTerms.moveToNext());
            } else { //user is not enrolled in a term or course
                btnCurrent.setText("You are not in a course in any terms or courses.");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        setTitle("School Planner Home");
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

    public void btnTermsIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("MODE", courseDB.getTermTableName());
        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
        intent.putExtra("ARGS", args);
        startActivity(intent);
    }

    public void btnCoursesIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("MODE", courseDB.getCourseTableName());
        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
        intent.putExtra("ARGS", args);
        startActivity(intent);
    }

    public void btnAssessmentsIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("MODE", courseDB.getRequirementTableName());
        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
        intent.putExtra("ARGS", args);
        startActivity(intent);
    }

    public void btnMentorsIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("MODE", courseDB.getMentorTableName());
        Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
        intent.putExtra("ARGS", args);
        startActivity(intent);
    }

    public void btnCurrentIsClicked(View view){
        Intent intent = new Intent(getApplicationContext(), CurrentCourseActivity.class);
        startActivity(intent);
    }

    public static DBHelper getCourseDB(){ return courseDB; }
    public static NotificationHelper getNotificationHelper() { return notificationHelper; }
}