package com.example.courseplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CurrentCourseActivity extends AppCompatActivity {
    private TextView tvMentor, tvCourse, tvTerm, tvPhoneNumber, tvEmail;
    private int currCourseId, currMentorId;
    private LinearLayout llAssessmentContainer;
    private DBHelper courseDB;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_course);

        tvMentor = findViewById(R.id.tvMentorName);
        tvCourse = findViewById(R.id.tvCourse);
        tvTerm = findViewById(R.id.tvTerm);
        tvPhoneNumber = findViewById(R.id.tvPrimaryPhone);
        tvEmail = findViewById(R.id.tvPrimaryEmail);
        llAssessmentContainer = findViewById(R.id.llAssessmentContainer);

        courseDB = MainActivity.getCourseDB();

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Current Enrollment");
    }

    private void noCourse() {
        currCourseId = -1;
        currMentorId = -1;

        tvCourse.setText("Course: Not currently enrolled in any courses");
        tvMentor.setText("");
        tvEmail.setText("");
        tvPhoneNumber.setText("");

        llAssessmentContainer.setVisibility(View.INVISIBLE);
    }

    private void noTerm() {
        tvTerm.setText("Term: Not currently enrolled in any terms");
        noCourse();
    }

    private void setData() {
        //set text for btnCurrent based on the currently enrolled term and course
        try {
            Date currDate = DATE_FORMAT.parse(DATE_FORMAT.format(Calendar.getInstance().getTime())); //set currTime from system time
            Cursor allTerms = courseDB.getAllOfTable(courseDB.getTermTableName());

            if (allTerms.moveToFirst()) {//if a term exists
                do {
                    //if currDate is between the term's dates (user is assumed enrolled)
                    if (currDate.after(DATE_FORMAT.parse(allTerms.getString(allTerms.getColumnIndex(courseDB.getStartDateField())))) && currDate.before(DATE_FORMAT.parse(allTerms.getString(allTerms.getColumnIndex(courseDB.getEndDateField()))))) {
                        Cursor allCourses = courseDB.getAllOfTable(courseDB.getCourseTableName());
                        if (allCourses.moveToFirst()) {
                            do {
                                //if currDate is between the course's dates (user is assumed enrolled)
                                if (currDate.after(DATE_FORMAT.parse(allCourses.getString(allCourses.getColumnIndex(courseDB.getStartDateField())))) && currDate.before(DATE_FORMAT.parse(allCourses.getString(allCourses.getColumnIndex(courseDB.getEndDateField()))))) {
                                    currCourseId = allCourses.getInt(allCourses.getColumnIndex(courseDB.getIdPkField()));
                                    //find current mentor
                                    Cursor coursesMentor = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getIdPkField(), allCourses.getString(allCourses.getColumnIndex(courseDB.getMentorIdFkField())));
                                    coursesMentor.moveToFirst();
                                    currMentorId = coursesMentor.getInt(coursesMentor.getColumnIndex(courseDB.getIdPkField()));
                                    Cursor firstPhone = courseDB.getAllOfTable(courseDB.getMentorPhoneTableName(), courseDB.getMentorIdFkField(), coursesMentor.getString(coursesMentor.getColumnIndex(courseDB.getIdPkField())));
                                    Cursor firstEmail = courseDB.getAllOfTable(courseDB.getMentorEmailTableName(), courseDB.getMentorIdFkField(), coursesMentor.getString(coursesMentor.getColumnIndex(courseDB.getIdPkField())));
                                    firstPhone.moveToFirst();
                                    firstEmail.moveToFirst();

                                    tvTerm.setText(allTerms.getString(allTerms.getColumnIndex(courseDB.getNameField())));
                                    tvCourse.setText("Course: " + allCourses.getString(allCourses.getColumnIndex(courseDB.getNameField())) + " " + allCourses.getString(allCourses.getColumnIndex(courseDB.getStartDateField())) + " to " + allCourses.getString(allCourses.getColumnIndex(courseDB.getEndDateField())));
                                    tvMentor.setText(coursesMentor.getString(coursesMentor.getColumnIndex(courseDB.getNameField())));
                                    tvPhoneNumber.setText("Primary Phone Number: " + firstPhone.getString(firstPhone.getColumnIndex(courseDB.getPhoneNumberField())));
                                    tvEmail.setText("Primary Email: " + firstEmail.getString(firstEmail.getColumnIndex(courseDB.getEmailField())));


                                    //get all assessments
                                    Cursor assessments = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getCourseIdFkField(), allCourses.getString(allCourses.getColumnIndex(courseDB.getIdPkField())));

                                    if (assessments.moveToFirst()) {
                                        llAssessmentContainer.setVisibility(View.VISIBLE); //set the container to be visible in case another part of application made it invisible
                                        llAssessmentContainer.removeAllViews(); //empty the view so it may be updated
                                        do {
                                            final int SPACING = 450;
                                            FrameLayout flAssessment = new FrameLayout(this);
                                            DisplayMetrics displayMetrics = new DisplayMetrics();
                                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                                            //create a text view that describes the current assessment
                                            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels - SPACING, ViewGroup.LayoutParams.WRAP_CONTENT));
                                            TextView tvAssessment = new TextView(this);
                                            tvAssessment.setText(assessments.getString(assessments.getColumnIndex(courseDB.getNameField())) + ": " + assessments.getString(assessments.getColumnIndex(courseDB.getStatusField()))
                                                    + "\n" + assessments.getString(assessments.getColumnIndex(courseDB.getDueDate())));
                                            tvAssessment.setLayoutParams(tvParams);

                                            flAssessment.addView(tvAssessment);

                                            //place an image button on the side of the above button that allows the user to share the assessment
                                            ImageButton ibtnCurr = new ImageButton(this);
                                            ibtnCurr.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
                                            ibtnCurr.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                            ibtnCurr.setTag(assessments.getInt(assessments.getColumnIndex(courseDB.getIdPkField())));
                                            //params for ibtnCurr
                                            FrameLayout.LayoutParams ibtnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                            ibtnCurr.setLayoutParams(ibtnParams);
                                            ibtnCurr.setX(displayMetrics.widthPixels - SPACING);
                                            ibtnCurr.setY(5);
                                            flAssessment.addView(ibtnCurr);//add ibtnCurr to the group

                                            ibtnCurr.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Cursor result = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                                                    result.moveToFirst();
                                                    Intent intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);

                                                    Bundle args = new Bundle();
                                                    args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                                                    args.putInt("COURSE", result.getInt(result.getColumnIndex(courseDB.getCourseIdFkField())) - 1);
                                                    args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                                                    args.putString("TYPE", result.getString(result.getColumnIndex(courseDB.getTypeField())));
                                                    args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));
                                                    args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));
                                                    args.putString("DATE", result.getString(result.getColumnIndex(courseDB.getDueDate())));

                                                    intent.putExtra("ARGS", args);
                                                    startActivity(intent);
                                                }
                                            });

                                            llAssessmentContainer.addView(flAssessment);
                                        } while (assessments.moveToNext());
                                    }
                                } else if (allCourses.isLast()) { //if this is the last course (not enrolled in a course)
                                    noCourse();
                                }
                            } while (allCourses.moveToNext());
                        } else { //no enrolled course found
                            noCourse();
                        }
                        break;
                    } else if (allTerms.isLast()) { //no term's found, user is not enrolled in either term or course (term enrollment is required for course enrollment)
                        noTerm();
                    }
                } while (allTerms.moveToNext());
            } else { //user is not enrolled in a term or course
                noTerm();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    //Create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    public void fabCurrAddMentorIsClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), AddModMentorActivity.class);

        //if the user is enrolled in a course, they will have a valid mentor id
        //if user has a mentor, set arguments to view that mentor
        Log.d("test", "fabCurrAddMentorIsClicked: " + String.valueOf(currMentorId));
        if (currMentorId > 0) {
            Log.d("success", "fabCurrAddMentorIsClicked: " + String.valueOf(currMentorId));
            Cursor result = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getIdPkField(), String.valueOf(currMentorId));
            result.moveToFirst();

            Bundle args = new Bundle();

            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
            args.putInt("ID", currMentorId);

            intent.putExtra("ARGS", args);
        } //else the application will assume a new mentor is being made

        startActivity(intent);
    }

    public void fabAddAssessmentIsClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);
        startActivity(intent);
    }
}
