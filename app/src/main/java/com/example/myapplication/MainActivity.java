package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textWidget;
    Button button;
    AppWidgetManager appWidgetManager;
    int appWidgetId;
    private static String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*textWidget = findViewById(R.id.textView2);

        appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
        appWidgetId = 1;

        textWidget.setText("coucou");*/

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Log.v("EditText", button.getText().toString());
                sendDirectSMS();
            }
        });
    }

    private void sendDirectSMS() {
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        // SEND BroadcastReceiver
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivré", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "echec envoi.", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "pas de service SMS", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "pas de service SMS-PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "pas de service SMS-RADIO-OFF", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        // DELIVERY BroadcastReceiver
        BroadcastReceiver deliverSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), R.string.sms_delivered, Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), R.string.sms_not_delivered, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(sendSMS, new IntentFilter(SENT));
        registerReceiver(deliverSMS, new IntentFilter(DELIVERED));
        String smsText = "coucou"; //getSmsText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From SIM One
                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage("0664545968", null, smsText, sentPI, deliveredPI);

                //SendSMS From SIM Two
                SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage("0664545968", null, smsText, sentPI, deliveredPI);
            }
        } else {
            //SmsManager.getDefault().sendTextMessage(customer.getMobile(), null, smsText, sentPI, deliveredPI);
            Toast.makeText(getBaseContext(), R.string.sms_sending, Toast.LENGTH_SHORT).show();
        }
    }
}