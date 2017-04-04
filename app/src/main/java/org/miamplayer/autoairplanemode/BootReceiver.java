package org.miamplayer.autoairplanemode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BootReceiver class reschedule alarms after rebooting.
 *
 * @author Matthieu BACHELIER
 * @since 2017.02
 * @version 1.2
 */
public class BootReceiver extends BroadcastReceiver
{
    private final AlarmBroadcastReceiver alarmBroadcastReceiver = new AlarmBroadcastReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            alarmBroadcastReceiver.setAlarms(context);
        }
    }
}
