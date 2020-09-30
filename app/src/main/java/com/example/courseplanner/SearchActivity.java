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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    EditText etTarget;
    LinearLayout llResultContainer;
    DBHelper courseDB;
    ArrayList<String> tableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etTarget = findViewById(R.id.etTarget);
        llResultContainer = findViewById(R.id.llResultContainer);

        courseDB = MainActivity.getCourseDB();

        tableList = new ArrayList<>();
        tableList.add(courseDB.getTermTableName());
        tableList.add(courseDB.getCourseTableName());
        tableList.add(courseDB.getMentorTableName());
        tableList.add(courseDB.getRequirementTableName());
        tableList.add(courseDB.getMentorEmailTableName());
        tableList.add(courseDB.getMentorPhoneTableName());

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Search");

        //debug test
        if (BuildConfig.DEBUG) {
            testSearch();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    private void conductSearch(int tableListIndex){
        Cursor result;
        //tables email and phone number do not use a name as a searchable element ao they must conduct a query on their own if their index is being checked
        if (tableList.get(tableListIndex).equals(courseDB.getMentorEmailTableName())){ //if currently searching the email table
            result = courseDB.getAllOfTable(tableList.get(tableListIndex), courseDB.getEmailField(), etTarget.getText().toString());
        } else if (tableList.get(tableListIndex).equals(courseDB.getMentorPhoneTableName())){ //if searching the phone number table
            result = courseDB.getAllOfTable(tableList.get(tableListIndex), courseDB.getPhoneNumberField(), etTarget.getText().toString());
        } else { //all other tables use "name" as their searchable element, so the same line of courseDB.getAllOfTable() can be used
            result = courseDB.getAllOfTable(tableList.get(tableListIndex), courseDB.getNameField(), etTarget.getText().toString());
        }

        if(result.moveToFirst()){
            do{
                TextView newResult =  new TextView(this);
                if (tableList.get(tableListIndex).equals(courseDB.getMentorEmailTableName())){ //if currently searching the email table
                    newResult.setText(result.getString(result.getColumnIndex(courseDB.getEmailField())));
                    newResult.setTag(R.string.ID, result.getString(result.getColumnIndex(courseDB.getMentorIdFkField())));
                } else if (tableList.get(tableListIndex).equals(courseDB.getMentorPhoneTableName())){ //if searching the email table
                    newResult.setText(result.getString(result.getColumnIndex(courseDB.getPhoneNumberField())));
                    newResult.setTag(R.string.ID, result.getString(result.getColumnIndex(courseDB.getMentorIdFkField())));
                } else { //all other tables use "name" as their searchable element
                    newResult.setText(result.getString(result.getColumnIndex(courseDB.getNameField())));
                    newResult.setTag(R.string.ID, result.getString(result.getColumnIndex(courseDB.getIdPkField())));
                }
                newResult.setText(newResult.getText() + " " + tableList.get(tableListIndex));

                newResult.setTag(R.string.TABLE, tableList.get(tableListIndex));

                newResult.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        Bundle args = new Bundle();
                        Cursor result;
                        String table = v.getTag(R.string.TABLE).toString();//exists to shorthand the v.getTag(0) statement that would otherwise be repeated

                        if(table.equals(courseDB.getTermTableName())){ //if searched data was a term
                            intent = new Intent(getApplicationContext(), AddModTermActivity.class);
                            result = courseDB.getAllOfTable(courseDB.getTermTableName(), courseDB.getIdPkField(), v.getTag(R.string.ID).toString());
                            result.moveToFirst();

                            Log.d("test", courseDB.getIdPkField() + " " + v.getTag(R.string.ID).toString());

                            args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//sets the id of the term
                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField()))); //sets name of the term
                            args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));// sets the start date of the term
                            args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));// sets the end date of the term

                        } else if (table.equals(courseDB.getCourseTableName())){ //else if searched data was a course
                            intent = new Intent(getApplicationContext(), AddModCourseActivity.class);
                            result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), v.getTag(R.string.ID).toString());
                            result.moveToFirst();

                            args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//sets the id of the course
                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField()))); //sets name of the course
                            args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));// sets the start date of the course
                            args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));// sets the end date of the course
                            args.putInt("TERM", result.getInt(result.getColumnIndex(courseDB.getTermIdFkField())));//sets the term the course take place in
                            args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));//sets the status of the course
                            args.putInt("MENTOR", result.getInt(result.getColumnIndex(courseDB.getMentorIdFkField())));//sets the mentor assigned to the course
                            args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));//sets the notes of the course

                        } else if (table.equals(courseDB.getRequirementTableName())){ //else if searched data was an assessment
                            intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);
                            result = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getIdPkField(), v.getTag(R.string.ID).toString());
                            result.moveToFirst();

                            args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                            args.putInt("COURSE", result.getInt(result.getColumnIndex(courseDB.getCourseIdFkField())));
                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                            args.putString("TYPE", result.getString(result.getColumnIndex(courseDB.getTypeField())));
                            args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));
                            args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));
                            args.putString("DATE", result.getString(result.getColumnIndex(courseDB.getDueDate())));

                        } else { //else the data search was a mentor or related to mentors
                            intent = new Intent(getApplicationContext(), AddModMentorActivity.class);
                            result = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getIdPkField(), v.getTag(R.string.ID).toString());
                            result.moveToFirst();

                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                            args.putInt("ID", Integer.parseInt(result.getString(result.getColumnIndex(courseDB.getIdPkField()))));
                        }

                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });
            llResultContainer.addView(newResult);
            } while (result.moveToNext());
        }

        if (tableListIndex + 1 < tableList.size()) conductSearch(++tableListIndex);
    }

    public void ivSearchIsClicked(View v){
        if (!etTarget.getText().toString().equals("")){ //if etTarget is not empty, assume it is valid and search for it in the database
            llResultContainer.removeAllViews();
            conductSearch(0);
        } else {
            Toast.makeText(this,"Please enter a valid value into the search bar.", Toast.LENGTH_LONG).show();
        }
    }


    //bellow only matters when in debug mode

    private int successCount = 0;
    void testSearch() {
        DBHelper courseDB = MainActivity.getCourseDB();
        int total;

        //get all data from database and put it in corresponding containers
        Cursor terms = courseDB.getAllOfTable(courseDB.getTermTableName());
        Cursor assessments = courseDB.getAllOfTable(courseDB.getRequirementTableName());
        Cursor mentors = courseDB.getAllOfTable(courseDB.getMentorTableName());
        Cursor courses = courseDB.getAllOfTable(courseDB.getCourseTableName());
        Cursor emails = courseDB.getAllOfTable(courseDB.getMentorEmailTableName());
        Cursor phones = courseDB.getAllOfTable(courseDB.getMentorPhoneTableName());

        //view courseDB.createTestValues for expected value
        total = terms.getCount() + assessments.getCount() + mentors.getCount() + courses.getCount() + emails.getCount() + phones.getCount();

        //cycle through the cursors and count successful searches, loop() is recursive and will fully search the Cursor it represents by simulating a click on ivSearch, which proceeds with the search function
        if (terms.moveToFirst()) loop(terms, courseDB.getNameField());
        if (assessments.moveToFirst()) loop(assessments, courseDB.getNameField());
        if (mentors.moveToFirst()) loop(mentors, courseDB.getNameField());
        if (courses.moveToFirst()) loop(courses, courseDB.getNameField());
        if (emails.moveToFirst()) loop(emails, courseDB.getEmailField());
        if (phones.moveToFirst()) loop(phones, courseDB.getPhoneNumberField());

        //print results
        System.out.println(successCount + " of " + total + " searches succeeded");

        //clear container and text box to resume normal functionality
        etTarget.setText("");
        llResultContainer.removeAllViews();
    }

    private void loop(Cursor currSearchTarget,  String field){
        etTarget.setText(currSearchTarget.getString(currSearchTarget.getColumnIndex(field)));
        ivSearchIsClicked(null);
        if (llResultContainer.getChildCount() > 0) successCount++; //llResultContainer will only contain something if the search succeeds

        if (currSearchTarget.moveToNext()) loop(currSearchTarget, field);
    }
}