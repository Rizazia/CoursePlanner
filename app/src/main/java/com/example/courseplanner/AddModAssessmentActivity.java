package com.example.courseplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class AddModAssessmentActivity extends AppCompatActivity {
    private TextView tvAssessmentDate, tvRangeStart, tvRangeEnd;
    private EditText etName, etNotes;
    private Spinner spnnrStatus, spnnrType, spnnrCourse;
    private Button btnDelete;
    private DBHelper courseDB;
    private FragmentManager fragManager;
    private FragmentTransaction fragTransaction;
    private RadioButton rbSetNotificationTrue, rbSetNotificationFalse;
    private LinearLayout llRange;
    private boolean isMod;
    private int assessmentId;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mod_assessment);

        courseDB = MainActivity.getCourseDB();
        tvAssessmentDate = findViewById(R.id.tvAssessmentDate);
        tvRangeStart = findViewById(R.id.tvRangeStart);
        tvRangeEnd = findViewById(R.id.tvRangeEnd);
        etName = findViewById(R.id.etName);
        etNotes = findViewById(R.id.etNotes);
        spnnrStatus = findViewById(R.id.spnnrStatus);
        spnnrType = findViewById(R.id.spnnrType);
        spnnrCourse = findViewById(R.id.spnnrCourse);
        rbSetNotificationTrue = findViewById(R.id.rbTrue);
        rbSetNotificationFalse = findViewById(R.id.rbFalse);
        btnDelete = findViewById(R.id.btnDelete);
        llRange = findViewById(R.id.llRange);
        llRange.setVisibility(View.INVISIBLE);
        fragManager = getSupportFragmentManager();

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setDefaults();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDefaults();
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

    /*
     * set everything to default values
     * to be called when onCreate() and onResume() executes
     */
    private void setDefaults(){
        //Course Spinner
        Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName());
        ArrayList<String> courseList = new ArrayList<>();
        if (result.moveToFirst()) {
            do {
                courseList.add(result.getString(result.getColumnIndex(courseDB.getNameField())));
            } while (result.moveToNext());
        }

        ArrayAdapter courseAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnnrCourse.setAdapter(courseAdapter);

        /*
         * set a listener to spnntCourse that changes the range of dates that appears spnnr course to show the range of valid dates for this course
         * if no course is somehow selected, hide the range of dates
         * since the course ids are cascaded and displayed in order, their position in the spinner is the same as their id - 1
         * this lets the application use spnnrCourse.getSelectedPosition() + 1 to find find the id of the selected course
         */
        spnnrCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(spnnrCourse.getSelectedItemPosition() + 1));
                if(result.moveToFirst()) {
                    llRange.setVisibility(View.VISIBLE);

                    tvRangeStart.setText(result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " ");
                    tvRangeEnd.setText(" " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));
                } else {
                    llRange.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                llRange.setVisibility(View.INVISIBLE);
            }
        });


        //Status spinner
        ArrayList<String> statusValueList = new ArrayList<>();
        statusValueList.addAll(Arrays.asList(getResources().getStringArray(R.array.statusListAssessment)));

        ArrayAdapter statusAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, statusValueList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrStatus.setAdapter(statusAdapter);

        //Type Spinner
        ArrayList<String> typeValueList = new ArrayList<>();
        typeValueList.addAll(Arrays.asList(getResources().getStringArray(R.array.typeAssessmentList)));

        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, typeValueList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrType.setAdapter(typeAdapter);

        //handle arguments, set all fields to the data provided
        Bundle args = getIntent().getBundleExtra("ARGS");
        if (args != null) { //if extra data is passed to this  activity the assessment is being modded
            etName.setText(args.getString("NAME"));
            spnnrStatus.setSelection(statusAdapter.getPosition(args.getString("STATUS")));
            spnnrCourse.setSelection(getTablesIndex(spnnrCourse, courseDB.getCourseTableName(), String.valueOf(args.getInt("COURSE"))));
            spnnrType.setSelection(typeAdapter.getPosition(args.getString("TYPE")));
            tvAssessmentDate.setText(args.getString("DATE"));
            etNotes.setText(args.getString("NOTES"));

            assessmentId = args.getInt("ID");
            isMod = true;
            setTitle("Modify Assessment");
        } else { //extra data was not pass and this assessment is a new assessment
            result = courseDB.getAllOfField(courseDB.getRequirementTableName(), courseDB.getIdPkField());

            //completely disable btnDelete since there is nothing to delete
            btnDelete.setVisibility(View.GONE);
            btnDelete.setClickable(false); //possibly redundant, but clicking this button could be REALLY bad in this context

            etName.setText("Assessment " + String.valueOf(result.getCount() + 1));
            assessmentId = result.getCount() + 1; //used to infer that the assessment is new

            isMod = false;
            setTitle("Add Assessment");
        }
    }

    //open a date picker that will change the text value of tvAssessment
    public void tvAssessmentDateIsClicked(View view) {
        Bundle args = new Bundle();
        args.putString("ID_TO_UPDATE", String.valueOf(tvAssessmentDate.getId()));
        args.putString("CALLING_ACTIVITY", this.getClass().getSimpleName());

        fragTransaction = fragManager.beginTransaction();

        DialogFragment newFragment = new DatePickerDialogFragment();
        newFragment.setArguments(args);
        newFragment.show(fragManager.beginTransaction(), "TAG");
    }

    public void onDateSet(String result) {
        if (TextUtils.isEmpty(result)) { //if there was no result (the dialog box was empty)
            tvAssessmentDate.setText(R.string.tvSelect);
        } else { //put the result into the text view
            tvAssessmentDate.setText(result);
        }
    }

    /*
     * Ensure inputs are valid then either insert a new assessment or update the one being modified
     */
    public void btnSubmitIsClicked(View view) throws ParseException {
        //if input is valid
        if (isValid()) {
            if (isMod) { //if modding an existing assessment
                Cursor result = courseDB.getField(courseDB.getRequirementTableName(), courseDB.getHasNotification(), courseDB.getIdPkField(), String.valueOf(assessmentId));
                if (result.moveToFirst()) {
                    if (result.getString(result.getColumnIndex(courseDB.getHasNotification())).equals("true") && rbSetNotificationFalse.isChecked()) { //if the current assessment has notifications but the user clicked no notifications
                        cancelNotification();
                    }
                }
                if (courseDB.updateAssessment(String.valueOf(assessmentId), String.valueOf(assessmentId), etName.getText().toString(), String.valueOf(spnnrCourse.getSelectedItemPosition()), spnnrType.getSelectedItem().toString(), spnnrStatus.getSelectedItem().toString(), tvAssessmentDate.getText().toString(), etNotes.getText().toString(), String.valueOf(rbSetNotificationTrue.isChecked()))) {
                    if (handleNewNotification()) {
                        Toast.makeText(this, "Assessment updated successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Assessment could not be updated at this time", Toast.LENGTH_LONG).show();
                }
            } else { //insert a new assessment
                if (courseDB.insertAssessment(etName.getText().toString(), String.valueOf(spnnrCourse.getSelectedItemPosition() + 1) , spnnrType.getSelectedItem().toString(), spnnrStatus.getSelectedItem().toString(), tvAssessmentDate.getText().toString(), etNotes.getText().toString(), String.valueOf(rbSetNotificationTrue.isChecked()))) {
                    if (handleNewNotification()) {
                        Toast.makeText(this, "Assessment created successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Assessment could not be made successfully", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void btnCancelIsClicked(View view) {
        finish();
    }

    public void btnDeleteIsClicked(View view) {
        if (isMod) {
            if (courseDB.deleteField(courseDB.getRequirementTableName(), courseDB.getIdPkField(), String.valueOf(assessmentId))) {
                Toast.makeText(this, "This assessment was deleted successfully", Toast.LENGTH_LONG).show();
                cancelNotification();
                finish();
            } else {
                Toast.makeText(this, "The assessment could not be deleted at this time", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
     * allows the user to send the current assessment to others
     * only available when modding an assessment if the assessment is valid
     */
    public void ibtnShareIsClicked(View view) {
        if (isValid()) {
            Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
            intentShare.setType("text/plain");
            String shareBody = etName.getText().toString() + ": " + tvAssessmentDate.getText().toString();
            intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            intentShare.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intentShare, "Share via"));
        }
    }

    public boolean isValid() {
        //fields cannot be null, it is assumed valid otherwise, spnnrStatus defaults to the first item in its array so it can never be null
        if (etName.equals("") || spnnrCourse.getSelectedItem().equals("") || spnnrType.getSelectedItem().equals("") || tvAssessmentDate.getText().equals(R.string.tvSelect) || etNotes.equals("")) {
            return false;
        }
        //date must be between the assigned course's start and end
        try {
            Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(spnnrCourse.getSelectedItemPosition() + 1));
            result.moveToFirst();
            Date dueDate = DATE_FORMAT.parse(tvAssessmentDate.getText().toString());
            Date courseStart = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getStartDateField())));
            Date courseEnd = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getEndDateField())));

            if (dueDate.before(courseStart) || dueDate.after(courseEnd)) {
                Toast.makeText(this, "You have entered invalid dates. Please enter valid dates.", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            Log.e("error", "isValid: ", e);
            Toast.makeText(this, "You have entered invalid dates. Please enter valid dates.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void fabNewCourseIsClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), AddModCourseActivity.class);
        startActivity(intent);
    }

    /*
     * creates a new notification if the user checked the proper radio button
     * if the user does not want a notification, return true so that the application may continue
     * otherwise return whether a pointer to the notification was created or not
     * this function executes right after the rest of the assessment was created and since assessment only has the id if a course is being modified, the id is found through the courseDB
     */
    private boolean handleNewNotification() throws ParseException {
        if (rbSetNotificationFalse.isChecked()) {//if the user doesn't want a notification
            return true; //return true so the application may proceed
        } else {
            NotificationHelper nh = MainActivity.getNotificationHelper();
            int lastID;

            Cursor result = courseDB.getAllOfTable(courseDB.getRequirementTableName());
            Cursor forNextId = courseDB.getAllOfTable(courseDB.getNotificationTableName());
            result.moveToLast();
            if (forNextId.moveToLast()) {
                lastID = forNextId.getInt(forNextId.getColumnIndex(courseDB.getIdPkField()));
            } else {
                lastID = 0;
            }

            //notification for start of of assessment
            Date date = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getDueDate())));
            long timeInMillis = date.getTime();
            nh.addNotification(timeInMillis, lastID + 1);

            return courseDB.insertNotification(courseDB.getRequirementTableName(), result.getString(result.getColumnIndex(courseDB.getIdPkField())), etName.getText().toString());
        }
    }

    /*
     * cancels any notification associated with assessmentId and deletes its associated field in the SQLite database
     */
    private boolean cancelNotification() {
        NotificationHelper nh = MainActivity.getNotificationHelper();
        nh.cancelNotification(assessmentId);

        return courseDB.deleteField(courseDB.getNotificationTableName(), courseDB.getIdPkField(), String.valueOf(assessmentId));
    }

    /*
    * used to find the index of a string value in a spinner
    * stringId is the primary key id of the course that is being searched, passed as an arg into this activity
    * returns the index of string's name
     */
    private int getTablesIndex(Spinner spinner, String table,String stringId){
        Cursor result = courseDB.getAllOfTable(table, courseDB.getIdPkField(), stringId);

        if(result.moveToFirst()) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(result.getString(result.getColumnIndex(courseDB.getNameField())))) {
                    return i + 1;
                }
            }
        }

        return 0;
    }
}
