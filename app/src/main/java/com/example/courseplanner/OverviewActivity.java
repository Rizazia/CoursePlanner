package com.example.courseplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewActivity extends AppCompatActivity {
    private TextView tvTerms, tvCourses, tvMentors, tvAssesments;
    private LinearLayout llTermListContainer, llCourseListContainer, llAssessmentListContainer, llMentorListContainer;
    private DBHelper courseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        //initialize data
        tvTerms = findViewById(R.id.tvTerms);
        tvCourses = findViewById(R.id.tvCourses);
        tvMentors = findViewById(R.id.tvMentors);
        tvAssesments = findViewById(R.id.tvAssessments);

        llTermListContainer = findViewById(R.id.llTermListContainer);
        llCourseListContainer = findViewById(R.id.llCourseListContainer);
        llAssessmentListContainer = findViewById(R.id.llAssessmentListContainer);
        llMentorListContainer = findViewById(R.id.llMentorListContainer);

        courseDB = MainActivity.getCourseDB();

        setTitle("Overview");
        setValues();

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setValues(){
        //clear all containers for the case that this activity involes this method by onResume() after changing how the report is made
        llAssessmentListContainer.removeAllViews();
        llMentorListContainer.removeAllViews();
        llCourseListContainer.removeAllViews();
        llTermListContainer.removeAllViews();
        //set data to display the amount of its respective type in the database
        //then create a dropdown menu of each item that links to the modify activity of the clicked data

        //terms
        Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName());
        if (result.moveToFirst()){ //at least one term exists
            tvTerms.setText("Terms: " + result.getCount());
            do{ //while a term exists in the database, make a button that can go to its modify activity
                TextView newTermTV = new TextView(this);
                newTermTV.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField()))); //used to retrieve this term's ID later
                newTermTV.setWidth(ViewGroup.LayoutParams.MATCH_PARENT); //ensures the text view will take up all of its allocated width (to make it easier to click/touch)
                //set text of the current term to be its name followed by its start and end dates (ex. Term N: 1/1/9990 - 1/4/9990)
                newTermTV.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + ": " + result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " : " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));

                //listener that will open the term in the modify activity when clicked
                newTermTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName(), courseDB.getIdPkField(), v.getTag().toString());
                        result.moveToFirst();
                        //values to be passed
                        Bundle args = new Bundle();
                        args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                        args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));
                        args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));

                        Intent intent = new Intent(getApplicationContext(), AddModTermActivity.class);
                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });

                //add this view to the list of terms
                llTermListContainer.addView(newTermTV);
            } while(result.moveToNext());
        } else { //no terms exist
            tvTerms.setText("No terms exist");
        }
        //courses
        result = courseDB.getAllOfTable(courseDB.getCourseTableName());
        if(result.moveToFirst()){ //if at least one course exists
            tvCourses.setText("Courses: " + result.getCount());
            do{ //while a course exists in the database, make a button that can go to its modify activity
                TextView newCourseTV = new TextView(this);
                newCourseTV.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField()))); //used to retrieve this course's ID later
                newCourseTV.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                //set text of the current course to be its name followed by its start and end dates (ex. Course XYZ: 1/1/9990 - 1/4/9990)
                newCourseTV.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + ": " + result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " : " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));

                //listener that will open the term in the modify activity when clicked
                newCourseTV.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), v.getTag().toString());
                        result.moveToFirst();
                        //values to be passed
                        Bundle args = new Bundle();
                        args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//sets the id of the course
                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField()))); //sets name of the course
                        args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));// sets the start date of the course
                        args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));// sets the end date of the course
                        args.putInt("TERM", result.getInt(result.getColumnIndex(courseDB.getTermIdFkField())));//sets the term the course take place in
                        args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));//sets the status of the course
                        args.putInt("MENTOR", result.getInt(result.getColumnIndex(courseDB.getMentorIdFkField())));//sets the mentor assigned to the course
                        args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));//sets the notes of the course

                        Intent intent = new Intent(getApplicationContext(), AddModCourseActivity.class);
                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });

                //add this to the list of courses
                llCourseListContainer.addView(newCourseTV);
            } while(result.moveToNext());
        } else { //no courses exist
            tvCourses.setText("No courses exist");
        }
        //assessments
        result = courseDB.getAllOfTable(courseDB.getRequirementTableName());
        if(result.moveToFirst()){ //if at least one assessment exists
            tvAssesments.setText("Assessments: " + result.getCount());
            do{
                TextView newAssessmentTV = new TextView(this);
                newAssessmentTV.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField()))); //used to retrieve this assessment's ID later
                newAssessmentTV.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                //set text of the current course to be its name followed by its start date (ex. Course XYZ Final: 1/4/9990)
                newAssessmentTV.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + ": " + result.getString(result.getColumnIndex(courseDB.getDueDate())));

                //listener that will open the assessment in the modify activity when clicked
                newAssessmentTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor result = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getIdPkField(), v.getTag().toString());
                        result.moveToFirst();
                        //value to be passed
                        Bundle args = new Bundle();
                        args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                        args.putInt("COURSE", result.getInt(result.getColumnIndex(courseDB.getCourseIdFkField())));
                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                        args.putString("TYPE", result.getString(result.getColumnIndex(courseDB.getTypeField())));
                        args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));
                        args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));
                        args.putString("DATE", result.getString(result.getColumnIndex(courseDB.getDueDate())));

                        Intent intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);
                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });
                llAssessmentListContainer.addView(newAssessmentTV);
            } while (result.moveToNext());
        } else { //no assessments exist
            tvAssesments.setText("No assessments exist");
        }
        //mentors
        result = courseDB.getAllOfTable(courseDB.getMentorTableName());
        if(result.moveToFirst()){ //if at least one mentor exists
            Cursor emailResult = courseDB.getAllOfTable(courseDB.getMentorEmailTableName());
            Cursor phoneResult = courseDB.getAllOfTable(courseDB.getMentorPhoneTableName());
            tvMentors.setText("Mentors: " + result.getCount() + " (Emails: " + emailResult.getCount() + " Phone Numbers: " + phoneResult.getCount() + ")");

            do {
                TextView newMentorTV = new TextView(this);
                newMentorTV.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//used to retrieve this mentor's ID later
                newMentorTV.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                //set text of the current mentor to its name followed by the amount of email and phone numbers assigned to it (ex. John Smith - Email(s): 1 Phone Number(s): 2)
                emailResult = courseDB.getAllOfTable(courseDB.getMentorEmailTableName(), courseDB.getMentorIdFkField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())));
                phoneResult = courseDB.getAllOfTable(courseDB.getMentorPhoneTableName(), courseDB.getMentorIdFkField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())));
                newMentorTV.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + " - Emails(s): " + emailResult.getCount() + " Phone Number(s): " + phoneResult.getCount());

                //listener that will open the mentor in the modify activity when clicked
                newMentorTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("test", "onClick: test");
                        Cursor result = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getIdPkField(), v.getTag().toString());
                        result.moveToFirst();

                        //values to be passed
                        Bundle args = new Bundle();

                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                        args.putInt("ID", Integer.parseInt(v.getTag().toString()));

                        Intent intent =  new Intent(getApplicationContext(), AddModMentorActivity.class);
                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });
                //add this to the list of mentors
                llMentorListContainer.addView(newMentorTV);
            } while(result.moveToNext());
        } else { //no mentors exist
            tvMentors.setText("No mentors exist");
        }
    }

    //when tvTerm is clicked show/hide the list of terms
    public void tvTermsIsClicked(View v){
        if (llTermListContainer.getVisibility() == View.GONE){ //term list is hidden
            llTermListContainer.setVisibility(View.VISIBLE);
        } else { //term list is visible
            llTermListContainer.setVisibility(View.GONE);
        }
    }

    //when tvCourse is clicked show/hide the list of courses
    public void tvCoursesIsClicked(View v){
        if (llCourseListContainer.getVisibility() == View.GONE){ //course list is hidden
            llCourseListContainer.setVisibility(View.VISIBLE);
        } else { //course list is visible
            llCourseListContainer.setVisibility(View.GONE);
        }
    }

    //when tvMentor is clicked show/hide the list of mentors
    public void tvMentorsIsClicked(View v){
        if (llMentorListContainer.getVisibility() == View.GONE){ //mentor list is hidden
            llMentorListContainer.setVisibility(View.VISIBLE);
        } else { //mentor list is visible
            llMentorListContainer.setVisibility(View.GONE);
        }
    }

    //when tvAssessments is clicked show/hide the list of assessments
    public void tvAssessmentsIsClicked(View v){
        if (llAssessmentListContainer.getVisibility() == View.GONE){ //assessment list is hidden
            llAssessmentListContainer.setVisibility(View.VISIBLE);
        } else { //assessment list is visible
            llAssessmentListContainer.setVisibility(View.GONE);
        }
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

    //Create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public void onResume(){
        setValues();
        super.onResume();
    }
}