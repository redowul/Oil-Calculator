
package com.jgh.androidssh;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.jgh.androidssh.sshutils.SessionController;
import com.jgh.androidssh.sshutils.SessionUserInfo;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity. Connect to SSH server and launch command shell.
 *
 */
public class MainActivity extends AppCompatActivity implements OnClickListener, DatePickerDialog.OnDateSetListener {

    private TextView oilBurnedDisplay, endDateDisplay, startDateDisplay;
    private Button confirmationButton, startDateSelectionButton, endDateSelectionButton;
    private Handler mHandler;
    private DatePickerDialog startDatePickerDialog, endDatePickerDialog;
    private String startDate, endDate;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        confirmationButton = findViewById(R.id.confirmationButton);
        startDateSelectionButton = findViewById(R.id.startDateSelectionButton);
        endDateSelectionButton = findViewById(R.id.endDateSelectionButton);

        oilBurnedDisplay = findViewById(R.id.oilBurnedDisplay);
        endDateDisplay = findViewById(R.id.endDateDisplay);
        startDateDisplay = findViewById(R.id.startDateDisplay);

        confirmationButton.setOnClickListener(this);
        startDateSelectionButton.setOnClickListener(this);
        endDateSelectionButton.setOnClickListener(this);

        mHandler = new Handler();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        autoConnect();
        initializeDatePickers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        confirmationButton = findViewById(R.id.confirmationButton);
        startDateSelectionButton = findViewById(R.id.startDateSelectionButton);
        endDateSelectionButton = findViewById(R.id.endDateSelectionButton);

        oilBurnedDisplay = findViewById(R.id.oilBurnedDisplay);
        endDateDisplay = findViewById(R.id.endDateDisplay);
        startDateDisplay = findViewById(R.id.startDateDisplay);

        // if the device isn't currently connected to the pi, try to reconnect automatically
        if(!SessionController.isConnected()) {
            autoConnect();
        }
    }

    /**
     * Create Date Picker objects and set current date so the system doesn't crash when first picking a date.
     */
    void initializeDatePickers() {
        Calendar now = Calendar.getInstance();
        startDatePickerDialog = DatePickerDialog.newInstance( this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Initial day selection
        );
        endDatePickerDialog = DatePickerDialog.newInstance( this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Initial day selection
        );

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = df.format(now.getTime());

        startDate = currentDate;
        String startDateString = getResources().getString(R.string.start_date);
        startDateString = startDateString + " " + currentDate;
        startDateDisplay.setText(startDateString);

        endDate = currentDate;
        String endDateString = getResources().getString(R.string.end_date);
        endDateString = endDateString + " " + currentDate;
        endDateDisplay.setText(endDateString);
    }

    /**
     * Handles the SSH connection to the server
     */
    private void autoConnect() {
        String ipAddress = getResources().getString(R.string.host_name);
        String username = getResources().getString(R.string.username);
        String password = getResources().getString(R.string.password);
        int port = Integer.parseInt(getResources().getString(R.string.port));

        SessionUserInfo mSUI = new SessionUserInfo(
                username,
                ipAddress,
                password, port);

        SessionController.getSessionController().setUserInfo(mSUI);
        Activity activity = this;
        SessionController.getSessionController().connect(activity);
    }

    /**
     * Sends our generated date query string to the server via SSH connection, then displays the result
     */
    private void triggerShell() {
        oilBurnedDisplay = findViewById(R.id.oilBurnedDisplay);
        String storageDirectory = getResources().getString(R.string.storage_path);
        String command = "cd " + storageDirectory;
        Activity activity = this;
        SessionController.getSessionController().executePreGeneratedCommand(mHandler, command, oilBurnedDisplay, activity);

        String dateTimeOne = startDate;
        String dateTimeTwo = endDate;

        command = "python OilCalculator.py " + dateTimeOne + " " + dateTimeTwo;
        SessionController.getSessionController().executePreGeneratedCommand(mHandler, command, oilBurnedDisplay, activity);

        command = "cd/";
        SessionController.getSessionController().executePreGeneratedCommand(mHandler, command, oilBurnedDisplay, activity);
    }

    /**
     * Handles all button click events
     */
    public void onClick(View v) {
        if (v == confirmationButton) {
            if(SessionController.isConnected()) {
                triggerShell(); // sends a command to the pi through the shell
            }
            else {
                autoConnect(); // connect to the pi
            }
        }
        else if(v == startDateSelectionButton) {
            startDatePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
        } else if (v == endDateSelectionButton) {
            endDatePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
        }
    }

    /**
     *
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String yearString = Integer.toString(year);
        if(yearString.length() >= 4) { // Years such as "999" (outside a range of four) may break the system, so don't allow them as a precaution.
            monthOfYear = monthOfYear + 1; // months in this method are returned in a range of 0 - 11; we want numbers ranging from 1 - 12.
            String monthString = Integer.toString(monthOfYear);
            if (monthString.length() == 1) {
                monthString = "0" + monthString;
            }
            String dayString = Integer.toString(dayOfMonth);
            if (dayString.length() == 1) {
                dayString = "0" + dayString;
            }
            String output = yearString + "-" + monthString + "-" + dayString;
            if (view == startDatePickerDialog) {
                String startDateString = getResources().getString(R.string.start_date);
                startDateString = startDateString + " " + output;
                startDateDisplay.setText(startDateString);
                startDate = output;
            }
            else if (view == endDatePickerDialog) {
                String endDateString = getResources().getString(R.string.end_date);
                endDateString = endDateString + " " + output;
                endDateDisplay.setText(endDateString);
                endDate = output;
            }
        }
    }
}
