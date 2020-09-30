package com.example.courseplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ItemListActivity extends AppCompatActivity {

    private DBHelper courseDB;
    private LinearLayout llContainer;
    private String mode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        courseDB = MainActivity.getCourseDB();
        llContainer = findViewById(R.id.clContainer);

        Bundle args = getIntent().getBundleExtra("ARGS");
        if (args.isEmpty()){ //all ways to access this should pass an arg to this activity, this is for error prevention
            Toast.makeText(this, "ERROR: Illegal access to item list activity.", Toast.LENGTH_LONG).show();
            //since this activity is being accessed  in an improper manner, close it
            finish();
        }
        //at this point going forward, args exists
        mode = args.getString("MODE");

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createItems();
    }

    /*
     * creates buttons for all items that allows them to be accessed
     */
    public void createItems(){
        Cursor result = courseDB.getAllOfTable(mode);
        //effectively a switch statement, but since non-static values can't be used in a switch, this is an if statement
        //splits the creation of items into the parts that are different
        if (mode.equals(courseDB.getTermTableName())){
            setTitle("Terms");
            createTerms(result);
        } else if (mode.equals(courseDB.getCourseTableName())){
            setTitle("Courses");
            createCourses(result);
        } else if (mode.equals(courseDB.getMentorTableName())){
            setTitle("Mentors");
            createMentors(result);
        } else if (mode.equals(courseDB.getRequirementTableName())){
            setTitle("Assessments");
            createAssessments(result);
        } else { //mode contains an invalid value
            Toast.makeText(this, "ERROR: Illegal mode used to access activity:" + mode, Toast.LENGTH_LONG).show();
            finish(); //close this activity to return to expected run-time conditions
        }
    }

    //creates buttons that can access all terms found in the database, or notify user that they're no entries
    private void createTerms(Cursor result){
        llContainer.removeAllViews();

        if (result.moveToFirst()){ //if terms exist
            do{ //while terms exist
                ViewGroup.LayoutParams params= new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Button btnCurr = new Button(this);

                //set text of the new button to the stored name in the database
                btnCurr.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + "\n" + result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " - " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));

                /*style & positioning*/

                //height & width (match_parent x wrap_content)
                btnCurr.setLayoutParams(new ViewGroup.LayoutParams(params));
                btnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));

                //add button to layout
                llContainer.addView(btnCurr);

                //set a listener that opens the corresponding term when the term is clicked
                btnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //query data to pass to the modify activity
                        final Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName(),courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        if(result.moveToFirst()) { //error prevention
                            llContainer.removeAllViews();//clear screen

                            Intent intent = new Intent(getApplicationContext(), AddModTermActivity.class);

                            Bundle args = new Bundle();
                            args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//sets the id of the term
                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField()))); //sets name of the term
                            args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));// sets the start date of the term
                            args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));// sets the end date of the term
                            intent.putExtra("ARGS", args);
                            startActivity(intent);
                        }
                    }
                });
            }while(result.moveToNext());
        } else {//no terms exist, so display a message asking user to make one
            TextView newTV = new TextView(this);
            newTV.setText("You have not entered any terms.\nPush the \"+\" to create a new term.");

            llContainer.addView(newTV);
        }
    }

    //creates buttons that can access all courses found in the database, or notify user that they're no entries
    private void createCourses(Cursor result){
        llContainer.removeAllViews();

        if (result.moveToFirst()){ //if Course exist
            do{ //while course exist
                final int SPACING = 130;
                FrameLayout flGroup = new FrameLayout(this); //used as a wrapper for the two buttons being made
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels - SPACING, ViewGroup.LayoutParams.WRAP_CONTENT));
                Button btnCurr = new Button(this);

                //set text of the new button to the stored name in the database with the start - end dates of the of the course
                btnCurr.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + "\n" + result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " - " + result.getString(result.getColumnIndex(courseDB.getEndDateField())));
                //set id to match the id of the primary key
                btnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));

                /*style & positioning*/

                //height & width (match_parent x wrap_content)
                btnCurr.setLayoutParams(new ViewGroup.LayoutParams(btnParams));

                //add button to layout
                flGroup.addView(btnCurr);

                //set a listener that opens the corresponding term when the term is clicked
                btnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //query data to pass to the modify activity
                        Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        if(result.moveToFirst()) { //error prevention
                            llContainer.removeAllViews();//clear screen

                            Intent intent = new Intent(getApplicationContext(), AddModCourseActivity.class);

                            Bundle args = new Bundle();
                            args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));//sets the id of the course
                            args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField()))); //sets name of the course
                            args.putString("START", result.getString(result.getColumnIndex(courseDB.getStartDateField())));// sets the start date of the course
                            args.putString("END", result.getString(result.getColumnIndex(courseDB.getEndDateField())));// sets the end date of the course
                            args.putInt("TERM", result.getInt(result.getColumnIndex(courseDB.getTermIdFkField())));//sets the term the course take place in
                            args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));//sets the status of the course
                            args.putInt("MENTOR", result.getInt(result.getColumnIndex(courseDB.getMentorIdFkField())));//sets the mentor assigned to the course
                            args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));//sets the notes of the course

                            intent.putExtra("ARGS", args);
                            startActivity(intent);
                        }
                    }
                });

                //place an image button on the side of the above button that allows the user to share the assessment
                ImageButton ibtnCurr = new ImageButton(this);
                ibtnCurr.setImageResource(R.drawable.ic_mail_outline_black_24dp);
                ibtnCurr.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                ibtnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                //params for ibtnCurr
                FrameLayout.LayoutParams ibtnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                ibtnCurr.setLayoutParams(ibtnParams);
                ibtnCurr.setX(displayMetrics.widthPixels - SPACING);
                ibtnCurr.setY(15);
                flGroup.addView(ibtnCurr);//add ibtnCurr to the group


                ibtnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        result.moveToFirst();

                        Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
                        intentShare.setType("text/plain");
                        String shareBody = result.getString(result.getColumnIndex(courseDB.getNameField())) + ": " + result.getString(result.getColumnIndex(courseDB.getStartDateField())) + " - " + result.getString(result.getColumnIndex(courseDB.getEndDateField()));
                        intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        intentShare.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(intentShare, "Share via"));
                    }
                });

                ibtnCurr.bringToFront();
                //add the new buttons to the parent view
                llContainer.addView(flGroup);
            }while(result.moveToNext());
        } else {//no terms exist, so display a message asking user to make one
            TextView newTV = new TextView(this);
            newTV.setText("You have not entered any courses.\nPush the \"+\" to create a new course.");

            llContainer.addView(newTV);
        }
    }

    //creates buttons that can access all mentors found in the database, or notify user that they're no entries
    private void createMentors(Cursor result){
        Cursor phoneNumberCursor;
        Cursor emailCursor;

        llContainer.removeAllViews();

        if(result.moveToFirst()){
            do{
                ViewGroup.LayoutParams params= new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                Button btnCurr = new Button(this);
                phoneNumberCursor = courseDB.getField(courseDB.getMentorPhoneTableName(), courseDB.getPhoneNumberField(), courseDB.getMentorIdFkField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())));
                emailCursor = courseDB.getField(courseDB.getMentorEmailTableName(), courseDB.getEmailField(), courseDB.getMentorIdFkField(), result.getString(result.getColumnIndex(courseDB.getIdPkField())));

                //set text of the new button to the stored name in the database with a list of the mentors email(s) and phone number(s)
                String text;
                //add name to string
                text = result.getString(result.getColumnIndex(courseDB.getNameField()));
                if(phoneNumberCursor.moveToFirst() && emailCursor.moveToFirst()) {
                    do { //add phone numbers to the string
                        text += "\n" + phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(courseDB.getPhoneNumberField()));
                    } while (phoneNumberCursor.moveToNext());
                    do { //add emails to the string
                        text += "\n" + emailCursor.getString(emailCursor.getColumnIndex(courseDB.getEmailField()));
                    } while (emailCursor.moveToNext());
                }
                //set string to the button
                btnCurr.setText(text);
                //set tag to match the id of the primary key
                btnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));

                /*style & positioning*/

                //height & width (match_parent x wrap_content)
                btnCurr.setLayoutParams(new ViewGroup.LayoutParams(params));

                //set a listener that opens the corresponding mentor with its data when the term is clicked
                btnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AddModMentorActivity.class);
                        Cursor result = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        result.moveToFirst();

                        Bundle args = new Bundle();

                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                        args.putInt("ID", Integer.parseInt(v.getTag().toString()));

                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });

                //add button to layout
                llContainer.addView(btnCurr);
            }while(result.moveToNext());
        } else { //no mentor data exists
            TextView newTV = new TextView(this);
            newTV.setText("You have not entered any mentors.\nPush the \"+\" to create a new mentor.");

            llContainer.addView(newTV);
        }
    }

    //creates buttons that can access all assessments found in the database, or notify user that they're no entries
    private void createAssessments(Cursor result){
        llContainer.removeAllViews();
        if(result.moveToFirst()){
            do{
                final int SPACING = 130;
                FrameLayout flGroup = new FrameLayout(this); //used as a wrapper for the two buttons being made
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(displayMetrics.widthPixels - SPACING, ViewGroup.LayoutParams.WRAP_CONTENT));
                Button btnCurr = new Button(this);

                //set text of the new button to the stored name in the database
                btnCurr.setText(result.getString(result.getColumnIndex(courseDB.getNameField())) + "\n" + result.getString(result.getColumnIndex(courseDB.getDueDate())));
                //set tag to match the id of the primary key (used to retrieve data about the assessment later)
                btnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));


                //height & width (match_parent x wrap_content)
                btnCurr.setLayoutParams(new ViewGroup.LayoutParams(btnParams));

                //add button to layout
                flGroup.addView(btnCurr);

                //set a listener that opens the corresponding assessment with its data when the assessment is clicked
                btnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor result = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        result.moveToFirst();
                        Intent intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);

                        Bundle args = new Bundle();
                        args.putInt("ID", result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                        args.putInt("COURSE", result.getInt(result.getColumnIndex(courseDB.getCourseIdFkField())));
                        args.putString("NAME", result.getString(result.getColumnIndex(courseDB.getNameField())));
                        args.putString("TYPE", result.getString(result.getColumnIndex(courseDB.getTypeField())));
                        args.putString("NOTES", result.getString(result.getColumnIndex(courseDB.getNotesField())));
                        args.putString("STATUS", result.getString(result.getColumnIndex(courseDB.getStatusField())));
                        args.putString("DATE", result.getString(result.getColumnIndex(courseDB.getDueDate())));

                        intent.putExtra("ARGS", args);
                        startActivity(intent);
                    }
                });

                //place an image button on top of the above button that allows the user to share the assessment
                ImageButton ibtnCurr = new ImageButton(this);
                ibtnCurr.setImageResource(R.drawable.ic_mail_outline_black_24dp);
                ibtnCurr.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                ibtnCurr.setTag(result.getInt(result.getColumnIndex(courseDB.getIdPkField())));
                //params for ibtnCurr
                FrameLayout.LayoutParams ibtnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

                ibtnCurr.setLayoutParams(ibtnParams);
                ibtnCurr.setX(displayMetrics.widthPixels - SPACING);
                ibtnCurr.setY(15);
                flGroup.addView(ibtnCurr);//add ibtnCurr to the group


                ibtnCurr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor result = courseDB.getAllOfTable(courseDB.getRequirementTableName(), courseDB.getIdPkField(), String.valueOf(v.getTag()));
                        result.moveToFirst();

                        Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
                        intentShare.setType("text/plain");
                        String shareBody = result.getString(result.getColumnIndex(courseDB.getNameField())) + ": " + result.getString(result.getColumnIndex(courseDB.getDueDate()));
                        intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        intentShare.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(intentShare, "Share via"));
                    }
                });

                ibtnCurr.bringToFront();
                //add the new buttons to the parent view
                llContainer.addView(flGroup);
            } while (result.moveToNext());
        } else { //no assessments have been made
            TextView newTV = new TextView(this);
            newTV.setText("You have not entered any assessments.\nPush the \"+\" to create a new assessment.");

            llContainer.addView(newTV);
        }
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

    public void fabNewItemIsClicked(View view){
        Intent intent = new Intent();
        if (mode.equals(courseDB.getTermTableName())){
            intent = new Intent(getApplicationContext(), AddModTermActivity.class);
        } else if (mode.equals(courseDB.getCourseTableName())){
            intent = new Intent(getApplicationContext(), AddModCourseActivity.class);
        } else if (mode.equals(courseDB.getMentorTableName())){
            intent = new Intent(getApplicationContext(), AddModMentorActivity.class);
        } else if (mode.equals(courseDB.getRequirementTableName())){
            intent = new Intent(getApplicationContext(), AddModAssessmentActivity.class);
        } else { //should never execute but exists to prevent a fatal exception
            Toast.makeText(this,"ERROR: this feature is being accessed in an illegal manor. Returning to previous activity.", Toast.LENGTH_LONG).show();
            finish(); //backs out of this activity, preventing the startActivity() command on the next line from causing any errors or unexpected actions
        }
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        createItems();
    }

}
