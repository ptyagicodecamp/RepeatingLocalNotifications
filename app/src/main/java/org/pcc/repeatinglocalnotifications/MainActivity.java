package org.pcc.repeatinglocalnotifications;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import org.pcc.repeatinglocalnotifications.notification.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    private EditText hours;
    private EditText minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        hours = (EditText)findViewById(R.id.editTextHH);
        minutes = (EditText) findViewById(R.id.editTextMM);
    }

    public void clickToggleButtonRTC(View view) {
        boolean isEnabled = ((ToggleButton)view).isEnabled();

        if (isEnabled) {
            NotificationHelper.scheduleRepeatingRTCNotification(mContext, hours.getText().toString(), minutes.getText().toString());
            NotificationHelper.enableBootReceiver(mContext);
        } else {
            NotificationHelper.cancelAlarmRTC();
            NotificationHelper.disableBootReceiver(mContext);
        }
    }

    public void clickToggleButtonElapsed(View view) {
        boolean isEnabled = ((ToggleButton)view).isEnabled();

        if (isEnabled) {
            NotificationHelper.scheduleRepeatingElapsedNotification(mContext);
            NotificationHelper.enableBootReceiver(mContext);
        } else {
            NotificationHelper.cancelAlarmElapsed();
            NotificationHelper.disableBootReceiver(mContext);
        }
    }
}
