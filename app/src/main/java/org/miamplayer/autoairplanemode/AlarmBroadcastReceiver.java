package org.miamplayer.autoairplanemode;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * AlarmBroadcastReceiver class.
 *
 * @author Matthieu BACHELIER
 * @since 2017.02
 * @version 1.2
 */
public class AlarmBroadcastReceiver extends WakefulBroadcastReceiver
{
    private static final String TAG = "AlarmBroadcastReceiver";

    /** The most important object in this class. */
    private AlarmManager alarmManager;

    /** Attach an intent to the manager with specific ID. */
    private PendingIntent enableAirplaneModePendingIntent;

    /** Attach another intent to the manager with specific ID. */
    private PendingIntent disableAirplaneModePendingIntent;

    /** Display days/month. */
    private static final SimpleDateFormat SDF_1 = new SimpleDateFormat("dd/MM", Locale.getDefault());

    /** Display hours/minutes. */
    private static final SimpleDateFormat SDF_2 = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive");
        if (intent == null) {
            Log.d(TAG, "intent shouldn't be null!");
            return;
        }

        Intent service = new Intent(context, AutoAirplaneModeService.class);
        int id = intent.getIntExtra(Constants.ID, 0);
        if (id == Constants.ID_ENABLE) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            service.putExtra(Constants.END, settings.getString(Constants.DISABLE_AIRPLANE_TIME, "08:00"));
        }
        service.putExtra(Constants.ID, id);
        startWakefulService(context, service);
    }

    /**
     * Set both start / stop alarms.
     *
     * @param context the context
     * @return true if alarm was set
     */
    /*public String setAlarm(Context context, int alarmType)
    {
        Log.d(TAG, "setAlarms");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String enableAutoAirplaneMode = settings.getString(Constants.ENABLE_AIRPLANE_TIME, "23:00");
        String disableAutoAirplaneMode = settings.getString(Constants.DISABLE_AIRPLANE_TIME, "08:00");

        String[] enable = enableAutoAirplaneMode.split(":");
        String[] disable = disableAutoAirplaneMode.split(":");

        Calendar now = Calendar.getInstance();
        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        calendarStart.setTimeInMillis(now.getTimeInMillis());
        calendarEnd.setTimeInMillis(now.getTimeInMillis());

        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.valueOf(enable[0]));
        calendarStart.set(Calendar.MINUTE, Integer.valueOf(enable[1]));
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.valueOf(disable[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.valueOf(disable[1]));
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);

        // If settings are like this [23:00 -> 08:00], convert to [23:00 -> (day +1) 08:00]
        if (calendarStart.after(calendarEnd)) {
            calendarEnd.add(Calendar.DATE, 1);
        }

        if (!getNextScheduledDay(context, now, calendarStart, calendarEnd)) {
            Toast.makeText(context, "No day was checked in Settings, so Automatic mode cannot be scheduled", Toast.LENGTH_LONG).show();
            return "";
        }

        Intent intentEnable = new Intent(context, AlarmBroadcastReceiver.class);
        Intent intentDisable = new Intent(context, AlarmBroadcastReceiver.class);

        intentEnable.putExtra(Constants.ID, Constants.ID_ENABLE);
        intentDisable.putExtra(Constants.ID, Constants.ID_DISABLE);

        enableAirplaneModePendingIntent = PendingIntent.getBroadcast(context, Constants.ID_ENABLE, intentEnable, 0);
        disableAirplaneModePendingIntent = PendingIntent.getBroadcast(context, Constants.ID_DISABLE, intentDisable, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), enableAirplaneModePendingIntent);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarEnd.getTimeInMillis(), disableAirplaneModePendingIntent);
        setAlarmAfterReboot(context, true);

        String message;
        if (now.get(Calendar.DATE) == calendarStart.get(Calendar.DATE)) {
            message = context.getString(R.string.toast_next_airplane_mode_today);
        } else if (calendarStart.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            message = context.getString(R.string.toast_next_airplane_mode_tomorrow);
        } else {
            message = String.format(context.getString(R.string.toast_next_airplane_mode_later),
                    SDF_1.format(calendarStart.getTime()),
                    SDF_2.format(calendarStart.getTime()));
        }
        return message;
    }*/

    public void setAlarmDisableAirplaneMode(Context context)
    {
        Log.d(TAG, "setAlarmDisableAirplaneMode");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String disableAutoAirplaneMode = settings.getString(Constants.DISABLE_AIRPLANE_TIME, "08:00");

        String[] disable = disableAutoAirplaneMode.split(":");

        Calendar now = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        calendarEnd.setTimeInMillis(now.getTimeInMillis());

        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.valueOf(disable[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.valueOf(disable[1]));
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);

        Intent intentDisable = new Intent(context, AlarmBroadcastReceiver.class);
        intentDisable.putExtra(Constants.ID, Constants.ID_DISABLE);

        disableAirplaneModePendingIntent = PendingIntent.getBroadcast(context, Constants.ID_DISABLE, intentDisable, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarEnd.getTimeInMillis(), disableAirplaneModePendingIntent);
        setAlarmAfterReboot(context, true);
    }

    public void setAlarmEnableAirplaneMode(Context context)
    {
        Log.d(TAG, "setAlarmEnableAirplaneMode");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String enableAutoAirplaneMode = settings.getString(Constants.ENABLE_AIRPLANE_TIME, "23:00");

        String[] enable = enableAutoAirplaneMode.split(":");

        Calendar now = Calendar.getInstance();
        Calendar calendarStart = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        calendarStart.setTimeInMillis(now.getTimeInMillis());

        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.valueOf(enable[0]));
        calendarStart.set(Calendar.MINUTE, Integer.valueOf(enable[1]));
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        Intent intentEnable = new Intent(context, AlarmBroadcastReceiver.class);
        intentEnable.putExtra(Constants.ID, Constants.ID_ENABLE);

        enableAirplaneModePendingIntent = PendingIntent.getBroadcast(context, Constants.ID_ENABLE, intentEnable, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), enableAirplaneModePendingIntent);
        setAlarmAfterReboot(context, true);
    }

    /**
     * Find next matching day. Updates parameters start and end.
     *
     * @param context the context
     * @param now current time
     * @param start when automatic airplane mode starts
     * @param end when automatic airplane mode ends
     * @return true if one day has been found in settings
     */
    private boolean getNextScheduledDay(Context context, Calendar now, Calendar start, Calendar end) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean checked = false;
        for (int i = 0; i < 7; i++) {
            checked = checked || settings.getBoolean("switch_" + String.valueOf(i), true);
        }
        if (!checked) {
            return false;
        }

        // today is monday, monday = 2
        // next week to scan is [ 2, 3, 4, 5, 6, 6, 1] if now < start or start <= now < end
        // next week to scan is [ 3, 4, 5, 6, 6, 1, 2] else
        int WEEK = 7;
        int[] days = new int[WEEK];
        for (int i = 0; i < WEEK; i++) {
            int dow = now.get(Calendar.DAY_OF_WEEK);
            if (now.before(start) || (now.after(start) && now.before(end))) {
                days[i] = (dow + i) % 7;
            } else {
                days[i] = (dow + 1 + i) % 7;
            }
            if (days[i] == 0) {
                days[i] = WEEK;
            }
        }

        // Find the first matching week day
        int dow = Calendar.MONDAY;
        for (int i = 0; i < WEEK; i++) {
            dow = days[i];
            if (settings.getBoolean("switch_" + String.valueOf(dow), true)) {
                break;
            }
        }

        // Let's shift start date
        int s = start.get(Calendar.DAY_OF_WEEK);
        int diff = Math.abs(dow - s);
        start.add(Calendar.DATE, diff);
        end.add(Calendar.DATE, diff);

        SimpleDateFormat sdf = new SimpleDateFormat("E, dd/MM HH:mm", Locale.getDefault());
        Log.d(TAG, sdf.format(start.getTime()));
        Log.d(TAG, sdf.format(end.getTime()));
        return true;
    }

    /**
     * Removes an alarm identified by its ID.
     *
     * @param context the context
     * @param alarmType the alarmType
     */
    public void cancelAlarm(Context context, int alarmType)
    {
        Log.d(TAG, "cancelAlarms");
        if (alarmManager != null) {
            if (alarmType == Constants.ID_ENABLE && enableAirplaneModePendingIntent != null) {
                alarmManager.cancel(enableAirplaneModePendingIntent);
            } else if (alarmType == Constants.ID_DISABLE && disableAirplaneModePendingIntent != null) {
                alarmManager.cancel(disableAirplaneModePendingIntent);
            }
        }
        setAlarmAfterReboot(context, false);
    }

    /**
     * Reschedules the alarm when rebooting.
     *
     * @param context the context
     * @param keep true if alarm needs to be rescheduled after reboot
     */
    private void setAlarmAfterReboot(Context context, boolean keep) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        if (keep) {
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}

