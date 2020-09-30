package com.example.courseplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddModTermActivity extends AppCompatActivity {

    private TextView tvStart, tvEnd;
    EditText etName;
    private DatePickerDialog.OnDateSetListener dpListener;
    private String dpSelection;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private int termId;
    private DBHelper courseDB;
    private FragmentManager fragManager;
    private FragmentTransaction fragTransaction;
    private Button btnDelete;
    private boolean isMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mod_term);
        etName = findViewById(R.id.etName);
        tvStart = findViewById(R.id.tvTermSelectedStart);
        tvEnd = findViewById(R.id.tvTermSelectedEnd);
        courseDB = MainActivity.getCourseDB();
        btnDelete = findViewById(R.id.btnDelete);
        isMod = false;

        //set action bar for this activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragManager = getSupportFragmentManager();

        Bundle args = getIntent().getBundleExtra("ARGS");
        if (args != null){ //if extra data is passed to this activity the term is being modded
            termId = args.getInt("ID");
            etName.setText(args.getString("NAME"));
            tvStart.setText(args.getString("START"));
            tvEnd.setText(args.getString("END"));
            isMod = true;
        } else { //extra data was not pass and this term is a new term
            Cursor result = courseDB.getAllOfField(courseDB.getTermTableName(), courseDB.getIdPkField());

            //completely disable btnDelete since there is nothing to delete
            btnDelete.setVisibility(View.GONE);
            btnDelete.setClickable(false); //most likely redundant, but clicking this button could be REALLY bad in this context

            etName.setText("Term " + (result.getCount() + 1));
            termId = result.getCount() + 1; //used to infer that the term is new, this will be overwritten if a term is being modified
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

    //executes when btnCancel is clicked and simply functions as a back press
    public void btnCancelIsClicked(View view) {
        finish();
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
    * checks to see if data has been inputted properly
    * if so adds/updates the data then closes this activity
     */
    public void btnSubmitIsClicked(View view){
        if (isValid()){ //if data has been inserted properly
            if(isMod){ //if a pre-existing term is being modified
                if(courseDB.updateTerm(String.valueOf(termId), String.valueOf(termId), etName.getText().toString(), tvStart.getText().toString(), tvEnd.getText().toString())){//update the current term
                    Toast.makeText(this,"Term updated successfully", Toast.LENGTH_LONG).show();
                    finish();//close this activity
                } else {//update failed for some reason
                    Toast.makeText(this,"Term could not be updated at this time", Toast.LENGTH_LONG).show();
                }
            } else { //add a new term
                Cursor result = courseDB.getAllOfField(courseDB.getTermTableName(), courseDB.getIdPkField());

                if(courseDB.insertDataTerm(termId, etName.getText().toString(),tvStart.getText().toString(), tvEnd.getText().toString())){//insert the new term
                    Toast.makeText(this,"New term created successfully", Toast.LENGTH_LONG).show();
                    finish();//close this activity
                } else {
                    Toast.makeText(this,"Term could not be created at this time", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*
     * checks to see if the input in this fragment is valid
     *
     * conditions to be valid:
     * tvName must contain any string
     * tvStart and tvEnd must contain a date
     * tvStart must be a date before tvEnd
     */
    private boolean isValid() {
        //name cannot be null, it is assumed valid otherwise
        if (etName.equals("")){
            return false;
        }
        //name must be unique
        Cursor nameSearch = courseDB.getAllOfTable(courseDB.getTermTableName(), courseDB.getNameField(), etName.getText().toString());
        if(nameSearch.moveToFirst()) { //if a duplicate name is found
            if(isMod){//if modding
                do {
                    if (nameSearch.getInt(nameSearch.getColumnIndex(courseDB.getIdPkField())) != termId) { //if the current id does not match the found name
                        Toast.makeText(this, "There is already a term with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } while (nameSearch.moveToNext());//if this every loops, false will be returned
            } else { //else, a duplicate was already found
                Toast.makeText(this, "There is already a term with the chosen name. Please use a different name", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        /*
         * the start and end dates must be set to something, this checks to see if there are still their default value
         * this is checked via the try catch on top on catching any other unexpected behavior that is not anticipated
         */
        try {
            //start must be before the end date
            if (DATE_FORMAT.parse(tvStart.getText().toString()).after(DATE_FORMAT.parse(tvEnd.getText().toString())) || (DATE_FORMAT.parse(tvStart.getText().toString()).equals(DATE_FORMAT.parse(tvEnd.getText().toString())))) {
                Toast.makeText(this,"The dates you entered are invalid. Please enter valid dates", Toast.LENGTH_LONG).show();
                return false;
            }

            Cursor result = courseDB.getAllOfTable(courseDB.getTermTableName());
            Date thisStart = DATE_FORMAT.parse(tvStart.getText().toString());
            Date thisEnd = DATE_FORMAT.parse(tvEnd.getText().toString());
            Date compareStart;
            Date compareEnd;
            //for new terms, start must be after the end of all other end dates
            if (!isMod) {//if the term is new
                if(result.moveToFirst()){
                    do {
                        compareEnd = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getEndDateField())));
                        if (thisStart.before(compareEnd)) { //if the start is before the end of another term
                            Toast.makeText(this, "New terms must occur after all previous terms. Please enter valid dates", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }while (result.moveToNext());
                }
            } else { //the term is being modded
                if(result.moveToFirst()) {
                    do {
                        if(termId != result.getInt(result.getColumnIndex(courseDB.getIdPkField()))) { //if the modded term is not comparing it to itself
                            compareStart = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getStartDateField())));
                            compareEnd = DATE_FORMAT.parse(result.getString(result.getColumnIndex(courseDB.getEndDateField())));
                            //modded terms must not be between the start and end of other terms
                            if (thisStart.after(compareStart) && (thisStart.before(compareEnd))) {
                                Toast.makeText(this, "Terms cannot be placed between other terms. Please enter valid dates", Toast.LENGTH_LONG).show();
                                return false;
                            } else if (thisEnd.after(compareStart) && (thisEnd.before(compareEnd))) {
                                Toast.makeText(this, "New terms must occur after all previous terms. Please enter valid dates", Toast.LENGTH_LONG).show();
                                return false;
                            }
                        }
                    } while (result.moveToNext());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"You have entered invalid dates. Please enter valid dates.", Toast.LENGTH_LONG).show();
            return false;
        }

        //input is valid if this point is reached
        return true;
    }

    /*
    * Handles the event of btnDelete being clicked
    * Deletes the currently viewed term
    * requires the term to have no courses assigned to it, this condition is returned from hasNoCourses() as a boolean
    * Cannot be called when a new term is being added as btnDelete is hidden in that situation
    */
    public void btnDeleteIsClicked(View view){
        if(isMod){ //btnDelete shouldn't even be visible if a term is being added but this serves as error prevention
            if(hasNoCourses()){
                if(courseDB.deleteField(courseDB.getTermTableName(), courseDB.getIdPkField(), String.valueOf(termId))){
                    Toast.makeText(this, "Term was deleted successfully", Toast.LENGTH_LONG).show();
                    cascadeTermIds();
                    finish();
                } else {
                    Toast.makeText(this, "The term could not be deleted at this time", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*
    * checks to see if no courses are assigned to current termId's term
     */
    private boolean hasNoCourses(){
        Cursor result = courseDB.getAllOfTable(courseDB.getCourseTableName(), courseDB.getIdPkField(), String.valueOf(termId));
        if (result.moveToFirst()){
            Toast.makeText(this, "You cannot delete a term that has courses assigned to it.", Toast.LENGTH_LONG).show();
            return false;
        }

        //if this point is reached, there are no courses in this term
        return true;
    }
    /*
    * when a term is deleted, decrease the id of each term after the deleted term by 1
    * this is done as the default name of terms is of a specific pattern (Term 1, Term 2 etc)
    * ex. terms 1,2,3,4,5 exist, term 3 is deleted
    * without this function the term ids would be, 1,2,4,5 which is wrong as the ids are supposed to to count a sequential order of a real idea
    * this function turns the above ids into 1,2,3,4 after 3 is deleted
    * the other values are not changed
     */
    private void cascadeTermIds(){
        Cursor result = courseDB.getAllOfGreaterValues(courseDB.getTermTableName(),courseDB.getIdPkField(), String.valueOf(termId));

        while(result.moveToNext()){
            int newId = result.getInt(result.getColumnIndex(courseDB.getIdPkField())) - 1;
            courseDB.updateTerm(result.getString(result.getColumnIndex(courseDB.getIdPkField())), String.valueOf(newId), result.getString(result.getColumnIndex(courseDB.getNameField())) ,result.getString(result.getColumnIndex(courseDB.getStartDateField())), result.getString(result.getColumnIndex(courseDB.getEndDateField())));
        }
    }
}
