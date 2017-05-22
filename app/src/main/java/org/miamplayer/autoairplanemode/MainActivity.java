package org.miamplayer.autoairplanemode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * MainActivity class.
 *
 * @author Matthieu BACHELIER
 * @since 2017.02
 * @version 1.2
 */
public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";

    private final AlarmBroadcastReceiver airplaneBroadcastReceiver = new AlarmBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (RootUtil.isDeviceRooted()) {
            try {
                Process p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("pm grant " + getApplicationContext().getPackageName() + " android.permission.WRITE_SECURE_SETTINGS \n");
                os.writeBytes("exit\n");
                os.flush();
            } catch (RuntimeException | IOException e) {
                Log.e(TAG, "Exception :( " + e.getMessage());
            }
        } else {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.root_dialog_title)
                .setMessage(getString(R.string.root_dialog_desc_main) + "\n\n" + getString(R.string.root_dialog_desc_exit))
                .setCancelable(false)
                .setPositiveButton(R.string.root_dialog_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                }).show();
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Switch switchEnableAirplane = (Switch) findViewById(R.id.switchEnableAirplane);
        switchEnableAirplane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                airplaneBroadcastReceiver.cancelAlarm(MainActivity.this, Constants.ID_ENABLE);
                TextView editEnableAirplane = (TextView) findViewById(R.id.editEnableAirplane);
                editEnableAirplane.setEnabled(isChecked);

                if (isChecked) {
                    airplaneBroadcastReceiver.setAlarmEnableAirplaneMode(MainActivity.this);
                } else {
                    displayToast("This device won't turn on Airplane Mode any more");
                }

                SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = s.edit();
                editor.putBoolean(Constants.AUTOMATIC_ENABLE, isChecked);
                editor.apply();
            }
        });

        Switch switchDisableAirplane = (Switch) findViewById(R.id.switchDisableAirplane);
        switchDisableAirplane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 airplaneBroadcastReceiver.cancelAlarm(MainActivity.this, Constants.ID_DISABLE);
                 TextView editDisableAirplane = (TextView) findViewById(R.id.editDisableAirplane);
                 editDisableAirplane.setEnabled(isChecked);

                 if (isChecked) {
                     airplaneBroadcastReceiver.setAlarmDisableAirplaneMode(MainActivity.this);
                 } else {
                     displayToast("This device won't turn off Airplane Mode any more");
                 }

                 SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                 SharedPreferences.Editor editor = s.edit();
                 editor.putBoolean(Constants.AUTOMATIC_DISABLE, isChecked);
                 editor.apply();
             }
        });

        final TextView editEnableAirplane = (TextView) findViewById(R.id.editEnableAirplane);
        editEnableAirplane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        editEnableAirplane.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        //updateNextDay();
                        saveClocks();
                        airplaneBroadcastReceiver.cancelAlarm(MainActivity.this, Constants.ID_ENABLE);
                        airplaneBroadcastReceiver.setAlarmEnableAirplaneMode(MainActivity.this);
                    }
                }, 23, 0, true).show();
            }
        });

        final TextView editDisableAirplane = (TextView) findViewById(R.id.editDisableAirplane);
        editDisableAirplane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        editDisableAirplane.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        //updateNextDay();
                        saveClocks();
                        airplaneBroadcastReceiver.cancelAlarm(MainActivity.this, Constants.ID_DISABLE);
                        airplaneBroadcastReceiver.setAlarmDisableAirplaneMode(MainActivity.this);
                    }
                }, 8, 0, true).show();
            }
        });

        editEnableAirplane.setText(settings.getString(Constants.ENABLE_AIRPLANE_TIME, "23:00"));
        editDisableAirplane.setText(settings.getString(Constants.DISABLE_AIRPLANE_TIME, "08:00"));
        //updateNextDay();

        // Restore settings
        switchEnableAirplane.setChecked(settings.getBoolean(Constants.AUTOMATIC_ENABLE, false));
        switchDisableAirplane.setChecked(settings.getBoolean(Constants.AUTOMATIC_DISABLE, false));
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.about_description_main) + "\n\n" + getString(R.string.about_description_sleep_well))
                        .setCancelable(true)
                        .setPositiveButton(R.string.about_ok_button, null).show();
                break;
            case R.id.menu_settings:
                Intent aboutIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(aboutIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveClocks();
    }

    /**
     *
     */
    private void updateNextDay() {
        final TextView editEnableAirplane = (TextView) findViewById(R.id.editEnableAirplane);
        final TextView editDisableAirplane = (TextView) findViewById(R.id.editDisableAirplane);
        final TextView nextDay = (TextView) findViewById(R.id.nextDay);
        String enable = editEnableAirplane.getText().toString();
        String disable = editDisableAirplane.getText().toString();
        String[] e = enable.split(":");
        String[] d = disable.split(":");
        int eHour = Integer.valueOf(e[0]);
        int eMinute = Integer.valueOf(e[1]);
        int dHour = Integer.valueOf(d[0]);
        int dMinute = Integer.valueOf(d[1]);
        if ((dHour < eHour) || (dHour == eHour && dMinute < eMinute)) {
            nextDay.setVisibility(View.VISIBLE);
        } else {
            nextDay.setVisibility(View.INVISIBLE);
        }
    }

    /**
     *
     */
    private void saveClocks() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        final TextView editEnableAirplane = (TextView) findViewById(R.id.editEnableAirplane);
        final TextView editDisableAirplane = (TextView) findViewById(R.id.editDisableAirplane);
        editor.putString(Constants.ENABLE_AIRPLANE_TIME, editEnableAirplane.getText().toString());
        editor.putString(Constants.DISABLE_AIRPLANE_TIME, editDisableAirplane.getText().toString());
        Log.d(TAG, "enable airplane mode at: " + editEnableAirplane.getText().toString());
        Log.d(TAG, "disable airplane mode at: " + editDisableAirplane.getText().toString());
        editor.apply();
    }
}
