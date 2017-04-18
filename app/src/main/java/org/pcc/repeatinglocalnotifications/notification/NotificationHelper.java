package org.pcc.repeatinglocalnotifications.notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by ptyagi on 4/17/17.
 */

public class NotificationHelper {
    public static int ALARM_TYPE_RTC = 100;
    private static AlarmManager alarmManagerRTC;
    private static PendingIntent alarmIntentRTC;

    public static int ALARM_TYPE_ELAPSED = 101;
    private static AlarmManager alarmManagerElapsed;
    private static PendingIntent alarmIntentElapsed;

    /**
     * This is the real time /wall clock time
     * @param context
     */
    public static void scheduleRepeatingRTCNotification(Context context, String hour, String min) {
        //get calendar instance to be able to select what time notification should be scheduled
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //Setting time of the day (8am here) when notification will be sent every day (default)
        calendar.set(Calendar.HOUR_OF_DAY,
                Integer.getInteger(hour, 8),
                Integer.getInteger(min, 0));

        //Setting intent to class where Alarm broadcast message will be handled
        Intent intent = new Intent(context, AlarmReceiver.class);
        //Setting alarm pending intent
        alarmIntentRTC = PendingIntent.getBroadcast(context, ALARM_TYPE_RTC, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting instance of AlarmManager service
        alarmManagerRTC = (AlarmManager)context.getSystemService(ALARM_SERVICE);

        //Setting alarm to wake up device every day for clock time.
        //AlarmManager.RTC_WAKEUP is responsible to wake up device for sure, which may not be good practice all the time.
        // Use this when you know what you're doing.
        //Use RTC when you don't need to wake up device, but want to deliver the notification whenever device is woke-up
        //We'll be using RTC.WAKEUP for demo purpose only
        alarmManagerRTC.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntentRTC);
    }

    /***
     * This is another way to schedule notifications using the elapsed time.
     * Its based on the relative time since device was booted up.
     * @param context
     */
    public static void scheduleRepeatingElapsedNotification(Context context) {
        //Setting intent to class where notification will be handled
        Intent intent = new Intent(context, AlarmReceiver.class);

        //Setting pending intent to respond to broadcast sent by AlarmManager everyday at 8am
        alarmIntentElapsed = PendingIntent.getBroadcast(context, ALARM_TYPE_ELAPSED, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting instance of AlarmManager service
        alarmManagerElapsed = (AlarmManager)context.getSystemService(ALARM_SERVICE);

        //Inexact alarm everyday since device is booted up. This is a better choice and
        //scales well when device time settings/locale is changed
        //We're setting alarm to fire notification after 15 minutes, and every 15 minutes there on
        alarmManagerElapsed.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntentElapsed);
    }

    public static void cancelAlarmRTC() {
        if (alarmManagerRTC!= null) {
            alarmManagerRTC.cancel(alarmIntentRTC);
        }
    }

    public static void cancelAlarmElapsed() {
        if (alarmManagerElapsed!= null) {
            alarmManagerElapsed.cancel(alarmIntentElapsed);
        }
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Enable boot receiver to persist alarms set for notifications across device reboots
     */
    public static void enableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Disable boot receiver when user cancels/opt-out from notifications
     */
    public static void disableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
