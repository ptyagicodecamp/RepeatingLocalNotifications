Title: FileProvider Demo
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

```
//TODO: Code to create notification and assiging to alarm manager

```
***Note:*** Don't forget to add Alarm permission in `<uses-permissions>` tag in `AndroidManifest.xml`
```
 //TODO: code to include uses-permissions tag
```

#### AlarmReceiver (BroadcastReceiver)
`AlarmReceiver` class extends `BroadcastReceiver`. This handles the AlarmManager's broadcast
 about sending local notifications at a given time.
 ```
 //TODO Code to override onReceived() method
 ```
 
***Note:*** Don't forget to add `<receiver>` tag in `AndroidManifest.xml`
 ```
 //TODO: code to include receiver tag
 ```
 
#### NotificationHelper
Convenience helper class to generate notification

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

References:
- Source Code is available [here]()
- More details you can find [here](https://developer.android.com/training/scheduling/alarms.html)
