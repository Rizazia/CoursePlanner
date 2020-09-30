package com.example.courseplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class AddModMentorActivity extends AppCompatActivity {
    private EditText etName;
    private Button btnDelete;
    private LinearLayout llPhoneContainer, llEmailContainer;
    private boolean isMod;
    private int mentorId;
    private List<EditText> allPhoneNums, allEmails;
    private DBHelper courseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mod_mentor);

        etName =  findViewById(R.id.etName);
        btnDelete = findViewById(R.id.btnDelete);
        llPhoneContainer = findViewById(R.id.llPhoneContainer);
        llEmailContainer = findViewById(R.id.llEmailContainer);
        courseDB = MainActivity.getCourseDB();

        allPhoneNums = new ArrayList<>();
        allEmails = new ArrayList<>();

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle args = getIntent().getBundleExtra("ARGS");
        if (args != null){ //if extra data is passed to this  activity the mentor is being modded
            etName.setText(args.getString("NAME"));
            mentorId = args.getInt("ID");
            //create all associated phone numbers
            Cursor result;
            result = courseDB.getAllOfTable(courseDB.getMentorPhoneTableName(), courseDB.getMentorIdFkField(), String.valueOf(mentorId));
            if (result.moveToFirst()){
                do {
                    final EditText etNewPhone = new EditText(AddModMentorActivity.this);
                    etNewPhone.setText(result.getString(result.getColumnIndex(courseDB.getPhoneNumberField())));
                    allPhoneNums.add(etNewPhone);
                    llPhoneContainer.addView(etNewPhone);

                    //if focus is lost, if this edit text is empty and not the only edit text, delete itself
                    etNewPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                if((allPhoneNums.size() > 1) && (allPhoneNums.get(Integer.parseInt(v.getTag().toString())).getText().toString().equals(""))){ //if this edit text is empty and this isn't the last phone number
                                    allPhoneNums.remove(Integer.parseInt(v.getTag().toString())); //remove itself from the list of phone numbers
                                    v.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                } while (result.moveToNext());
            }
            //create all associated emails
            result = courseDB.getAllOfTable(courseDB.getMentorEmailTableName(), courseDB.getMentorIdFkField(), String.valueOf(mentorId));
            if (result.moveToFirst()){
                do {
                    final EditText etNewEmail = new EditText(AddModMentorActivity.this);
                    etNewEmail.setText(result.getString(result.getColumnIndex(courseDB.getEmailField())));
                    allEmails.add(etNewEmail);
                    llEmailContainer.addView(etNewEmail);

                    //if focus is lost, if this edit text is empty and not the only edit text, delete itself
                    etNewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                if((allEmails.size() > 1) && (allEmails.get(Integer.parseInt(v.getTag().toString())).getText().toString().equals(""))){ //if this edit text is empty and this isn't the last phone number
                                    allEmails.remove(Integer.parseInt(v.getTag().toString())); //remove itself from the list of phone numbers
                                    v.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                } while (result.moveToNext());
            }
            isMod = true;

        } else { //extra data was not pass and this mentor is a new mentor
            //create lines so the mandatory 1 phone and email can be entered
            fabNewPhoneIsClicked(llPhoneContainer);
            fabNewEmailIsClicked(llEmailContainer);
            btnDelete.setVisibility(View.GONE);
            isMod = false;
        }
    }

    public void fabNewPhoneIsClicked(final View view){
        EditText etNewPhone = new EditText(AddModMentorActivity.this);
        etNewPhone.setTag(String.valueOf(allPhoneNums.size()));
        allPhoneNums.add(etNewPhone);
        llPhoneContainer.addView(etNewPhone);

        //if focus is lost, if this edit text is empty and not the only edit text, delete itself
        etNewPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if((allPhoneNums.size() > 1) && (allPhoneNums.get(Integer.parseInt(v.getTag().toString())).getText().toString().equals(""))){ //if this edit text is empty and this isn't the last phone number
                        allPhoneNums.remove(Integer.parseInt(v.getTag().toString())); //remove itself from the list of phone numbers
                        v.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void fabNewEmailIsClicked(View view){
        EditText etNewEmail = new EditText(AddModMentorActivity.this);
        allEmails.add(etNewEmail);
        llEmailContainer.addView(etNewEmail);

        //if focus is lost, if this edit text is empty and not the only edit text, delete itself
        etNewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if((allEmails.size() > 1) && (allEmails.get(Integer.parseInt(v.getTag().toString())).getText().toString().equals(""))){ //if this edit text is empty and this isn't the last phone number
                        allEmails.remove(Integer.parseInt(v.getTag().toString())); //remove itself from the list of phone numbers
                        v.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void btnSubmitIsClicked(View view){
        if(isValid()) {
            if (isMod) {
                //remake all of the phone numbers for this mentor
                if (courseDB.deleteField(courseDB.getMentorPhoneTableName(), courseDB.getIdPkField(), String.valueOf(mentorId))) {
                    for (int i = 0; i < allPhoneNums.size(); i++) {
                        if(!courseDB.insertPhoneNumber(String.valueOf(mentorId), allPhoneNums.get(i).getText().toString())){
                            Toast.makeText(this, "Phone update failed.", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    //remake all of the emails for this mentor
                    if (courseDB.deleteField(courseDB.getMentorEmailTableName(), courseDB.getIdPkField(), String.valueOf(mentorId))) {
                        for (int i = 0; i < allEmails.size(); i++) {
                            if(!courseDB.insertEmail(String.valueOf(mentorId), allEmails.get(i).getText().toString())){
                                Toast.makeText(this, "Email update failed.", Toast.LENGTH_LONG).show();
                                break;
                            }
                        }
                        if(courseDB.updateMentor(String.valueOf(mentorId), etName.getText().toString())){ //update mentor
                            Toast.makeText(this, "Mentor update successful.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Mentor update failed.", Toast.LENGTH_LONG).show();
                        }
                    } else { //email update failed
                        Toast.makeText(this, "Email update failed.", Toast.LENGTH_LONG).show();
                    }
                } else { //phone number update failed
                    Toast.makeText(this, "Phone number update failed.", Toast.LENGTH_LONG).show();
                }
            } else { //insert new data
                if (courseDB.insertMentor(etName.getText().toString())){
                    Cursor newestId = courseDB.getAllOfField(courseDB.getMentorTableName(), courseDB.getIdPkField());
                    newestId.moveToLast();
                    //insert phone numbers
                    for (int i = 0; i < allPhoneNums.size(); i++) {
                        if (!courseDB.insertPhoneNumber(newestId.getString(newestId.getColumnIndex(courseDB.getIdPkField())), allPhoneNums.get(i).getText().toString())) {
                            Toast.makeText(this, "Phone update failed.", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    //insert emails
                    for(int i = 0; i < allEmails.size(); i++){
                        if (!courseDB.insertEmail(newestId.getString(newestId.getColumnIndex(courseDB.getIdPkField())), allEmails.get(i).getText().toString())){
                            Toast.makeText(this, "Phone update failed.", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    Toast.makeText(this, "Mentor created.", Toast.LENGTH_LONG).show();
                } else { //insert failed
                    Toast.makeText(this, "Mentor could not be created at this time.", Toast.LENGTH_LONG).show();
                }
            }
        }
        finish();
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

    public void btnCancelIsClicked(View view){
        finish();
    }

    public void btnDeleteIsClicked(View v){
        if(isMod){
            //if the mentor's phone, mentor's email, and mentor's own field are deleted
            if((courseDB.deleteField(courseDB.getMentorPhoneTableName(), courseDB.getIdPkField(), String.valueOf(mentorId)))
                    && (courseDB.deleteField(courseDB.getMentorEmailTableName(), courseDB.getIdPkField(), String.valueOf(mentorId)))
                    && (courseDB.deleteField(courseDB.getMentorTableName(), courseDB.getIdPkField(), String.valueOf(mentorId)))){
                Toast.makeText(this, "This mentor was deleted successfully", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "The mentor could not be deleted at this time", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
    * ensures inputs are valid
     */
    public boolean isValid(){
        //etName must contain some non-default value
        if(etName.getText().toString().equals("") || etName.getText().toString().equals(R.id.etName)){
           Toast.makeText(this,"You have entered an invalid name.", Toast.LENGTH_LONG).show();
           return false;
        }
        //name must be unique
        Cursor nameSearch = courseDB.getAllOfTable(courseDB.getMentorTableName(), courseDB.getNameField(), etName.getText().toString());
        if(nameSearch.moveToFirst()) { //if a duplicate name is found
            if(isMod){//if modding
                do {
                    if (nameSearch.getInt(nameSearch.getColumnIndex(courseDB.getIdPkField())) != mentorId) { //if the current id does not match the found name
                        Toast.makeText(this, "There is already a mentor with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } while (nameSearch.moveToNext());//if this every loops, false will be returned
            } else { //else, a duplicate was already found
                Toast.makeText(this, "There is already a mentor with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        //there must at least phone number and email
        if(allPhoneNums.size() < 1 || allEmails.size() < 1){
            Toast.makeText(this,"You must include at least one valid phone number and email", Toast.LENGTH_LONG).show();
            return false;
        } else {
            for (int i = 0; i < allPhoneNums.size(); i++){ //all phone numbers must contain something
                if(allPhoneNums.get(i).getText().toString().equals("")){
                    Toast.makeText(this,"All included phone numbers must be valid", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            for(int i = 0; i < allEmails.size(); i++){ //all emails must contain something
                if(allEmails.get(i).getText().toString().equals("")){
                    Toast.makeText(this,"All included emails must be valid.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        //data is valid if this is reached
        return true;
    }
}
