package com.example.courseplanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private String result, idToUpdate,callingActivity;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Bundle mArgs = getArguments();
        idToUpdate = mArgs.getString("ID_TO_UPDATE");
        callingActivity = mArgs.getString("CALLING_ACTIVITY");

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /*
    * updates the date field of whatever called this fragment
    * the switch statement finds what called this so the proper function can be executed
    */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        result = year + "-" + (month + 1)+ "-" + day;

        switch(callingActivity){
            case "AddModTermActivity":
                ((AddModTermActivity)getActivity()).onDateSet(result, idToUpdate);
                break;
            case "AddModCourseActivity":
                ((AddModCourseActivity)getActivity()).onDateSet(result, idToUpdate);
                break;
            case "AddModAssessmentActivity":
                ((AddModAssessmentActivity)getActivity()).onDateSet(result);//there is only one date picker in this class so idToUpdate is not needed
                break;
            default:
                Log.e("Call to Activity Failed", "onDateSet() was called within unexpected activity: " + this.getClass().getSimpleName());
                break;
        }
    }

}

