package org.miamplayer.autoairplanemode;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This service can activate or desactivate Airplane Mode.
 *
 * @author Matthieu BACHELIER
 * @since 2017.02
 * @version 1.2
 */
public class AutoAirplaneModeService extends IntentService
{
    private static final String TAG = "AutoAirplaneModeService";
    private static final String COMMAND_FLIGHT_MODE_1 = "settings put global airplane_mode_on ";
    private static final String COMMAND_FLIGHT_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state ";

    public AutoAirplaneModeService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        int id = intent.getIntExtra(Constants.ID, 0);
        if (toggleAirplaneMode(id)) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (settings.getBoolean("notification_airplane_mode_started", true)) {
                sendNotificationWhenAirplaneIsEnabled(intent.getStringExtra(Constants.END));
            }
        } else {
            // Auto close notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(0);
            }
            Log.d(TAG, "airplane mode is going off -> scheduling new alarm!");
            AlarmBroadcastReceiver r = new AlarmBroadcastReceiver();
            if (id == Constants.ID_DISABLE) {
                r.setAlarmDisableAirplaneMode(getApplicationContext());
            } else if (id == Constants.ID_ENABLE) {
                r.setAlarmEnableAirplaneMode(getApplicationContext());
            }
        }
        AlarmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Send a notification when airplane mode has been triggered. It also displays when this mode will disabled itself.
     *
     * @param endOfAirplaneMode date to display in message
     */
    private void sendNotificationWhenAirplaneIsEnabled(String endOfAirplaneMode) {
        Log.d(TAG, "sendNotificationWhenAirplaneIsEnabled");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.notification_title));
        builder.setContentText(String.format(getString(R.string.notification_content),
                endOfAirplaneMode));
        builder.setSmallIcon(R.drawable.ic_moon);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }

    /**
     * Change the system settings of Airplane Mode.
     *
     * @param id the id
     * @return true is airplane mode should be enabled
     */
    private boolean toggleAirplaneMode(int id) {
        Log.d(TAG, "toggleAirplaneMode");
        boolean enable = id == Constants.ID_ENABLE;
        String v = enable ? "1" : "0";
        String command = COMMAND_FLIGHT_MODE_1 + v;
        executeCommandWithoutWait(command);
        String command2 = COMMAND_FLIGHT_MODE_2 + enable;
        executeCommandWithoutWait(command2);
        Settings.Global.putInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enable ? 1 : 0);
        return enable;
    }

    /**
     * Execute a command with root user.
     *
     * @param command the command to execute
     */
    private void executeCommandWithoutWait(String command) {
        Log.d(TAG, "executeCommandWithoutWait");
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Log.d(TAG, command);
        } catch (IOException e) {
            Log.e(TAG, "su command has failed due to: " + e.fillInStackTrace());
        }
    }
}