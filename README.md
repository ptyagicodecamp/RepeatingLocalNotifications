Title: Scheduling Repeating Local Notifications using Alarm Manager
Date: 2017-4-18 3:33PM
Authors: ptyagi
Category: Development
Tags: Android, Notifications, AlarmManager
Summary: Introduction to scheduling repeating local notification in Android using AlarmManager.

## Introduction
This tutorial is a quick start guide to get you started with
Local Notifications in Android.

### Scheduling Repeating Local Notifications using Alarm Manager

I'll be using Alarm Manager to schedule repeating local notifications in this sample app.
This sample app will send a local notification every morning at 8am.
I'm covering the use case that app is either in background or killed at the time when notification is received.
When user clicks on the notification, it takes user to app's main activity.

#### Give user option to enable/disable notifications
It's very important to give user option to opt-out or opt-in for local notifications.
You may want to do this in Settings Activity. When user has opted-in for local notifications,
then AlarmManager starts sending local notifications to user every morning. Time to send notifications
can be configured or just be default. I'll be using a default of 8am in this demo.

### Components

#### Scheduling notifications using Alarm Manager
There're two types of Alarms can be scheduled:
- Wall clock time
- Elapsed time (since device is booted).
Read more about them [here](https://developer.android.com/training/scheduling/alarms.html#set)
I'll be scheduling notifications using both kind of alarms.

***Scheduling RTC***
```
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
```

***Scheduling Elapsed***

```
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

```

#### AlarmReceiver (BroadcastReceiver)
`AlarmReceiver` class extends `BroadcastReceiver`. This handles the AlarmManager's broadcast
 about sending local notifications at a given time.
 
```
 public class AlarmReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
         //Intent to invoke app when click on notification.
         //In this sample, we want to start/launch this sample app when user clicks on notification
         Intent intentToRepeat = new Intent(context, MainActivity.class);
         //set flag to restart/relaunch the app
         intentToRepeat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

         //Pending intent to handle launch of Activity in intent above
         PendingIntent pendingIntent =
                 PendingIntent.getActivity(context, NotificationHelper.ALARM_TYPE_RTC, intentToRepeat, PendingIntent.FLAG_UPDATE_CURRENT);

         //Build notification
         Notification repeatedNotification = buildLocalNotification(context, pendingIntent).build();

         //Send local notification
         NotificationHelper.getNotificationManager(context).notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
     }

     public NotificationCompat.Builder buildLocalNotification(Context context, PendingIntent pendingIntent) {
         NotificationCompat.Builder builder =
                 (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                 .setContentIntent(pendingIntent)
                 .setSmallIcon(android.R.drawable.arrow_up_float)
                 .setContentTitle("Morning Notification")
                 .setAutoCancel(true);

         return builder;
     }
 }

```

***Note:*** Don't forget to add `<receiver>` tag in `AndroidManifest.xml`
```
<receiver android:name=".notification.AlarmReceiver"/>
```

#### NotificationHelper
[Convenience helper class](https://github.com/ptyagicodecamp/RepeatingLocalNotifications/blob/master/app/src/main/java/org/pcc/repeatinglocalnotifications/notification/NotificationHelper.java) to generate notification

#### Don't loose your notifications settings across device boots
Alarms will be cancelled when a device re-boots, so your notification settings.
In order to persist your notifications setting or alarms across device reboots,
you would need to reset alarm when a device boots up.

##### AndroidManifest.xml configuration
Add this permission in `AndroidManifest.xml`
```
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<application
...>

    <receiver android:name=".notification.AlarmBootReceiver"
            android:enabled="false">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED"></action>
        </intent-filter>
    </receiver>

</application>
```

##### Implementing Boot Receiver

`AlarmBootReceiver` will look like this:

```
public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //only enabling one type of notifications for demo purposes
            NotificationHelper.scheduleRepeatingElapsedNotification(context);
        }
    }
}
```

Enable boot receiver when Alarm is set. It means if user opts-into notifications, then
enable boot receiver as part of enabling notification code.
```
ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
PackageManager pm = context.getPackageManager();

pm.setComponentEnabledSetting(receiver,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
```

When user opts-out from notifications, you can cancel alarm and also disable boot receiver like this:
```
ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
PackageManager pm = context.getPackageManager();

pm.setComponentEnabledSetting(receiver,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);
```

That's it !

***References:***

- Source Code is available [here](https://github.com/ptyagicodecamp/RepeatingLocalNotifications)
- More details you can find [here](https://developer.android.com/training/scheduling/alarms.html)
