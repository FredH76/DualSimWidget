package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.euicc.EuiccManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textWidget;
    Button sendSmsBtn, switchBtn;
    AppWidgetManager appWidgetManager;
    int appWidgetId;
    Integer simSlot = null;
    private static String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("openSimConfig")) {
            int arg = getIntent().getIntExtra("openSimConfig", 0);
            openSimConfig();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchBtn = findViewById(R.id.switchBtn);
        sendSmsBtn = findViewById(R.id.sendSmsBtn);
        switchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openSimConfig(); // Open SIM selection page
            }
        });

        sendSmsBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendDirectSMS(); // send SMS on first SIM
            }
        });
    }

    private void openSimConfig() {
        Intent lIntent;
        if(Build.VERSION.SDK_INT >= 31){
            // Page gestionnaire de carte SIM
            lIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            // Page connexion (puis choisir "Gestion des cartes SIM")
            lIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        }
        lIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lIntent);
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
                        Toast.makeText(getBaseContext(), "SMS envoyé par SIM"+ (simSlot+1), Toast.LENGTH_SHORT).show();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 0) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();


                /*Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("test", 1);
                PendingIntent pendingUpdate = PendingIntent.getActivity(this, 0, intent, 0);

                TelephonyManager telephonyManager = this.getSystemService(TelephonyManager.class);
                Boolean test = telephonyManager.hasCarrierPrivileges();

                EuiccManager euiccManager = this.getSystemService(EuiccManager.class);
                euiccManager.switchToSubscription(0, pendingUpdate);*/ // API 33
                //SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From first sim available
                TelephonyManager telephonyManager = this.getSystemService(TelephonyManager.class);
                for (int i = 0; i < localList.toArray().length; i++) {
                    if(telephonyManager.getSimState(i) == TelephonyManager.SIM_STATE_READY){
                        simSlot = i;
                        break;
                    }
                }
                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(simSlot);
                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage("0664545968", null, smsText, sentPI, deliveredPI);

                //SendSMS From SIM Two
                //SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage("0664545968", null, smsText, sentPI, deliveredPI);
            }
        } else {
            //SmsManager.getDefault().sendTextMessage(customer.getMobile(), null, smsText, sentPI, deliveredPI);
            Toast.makeText(getBaseContext(), R.string.sms_sending, Toast.LENGTH_SHORT).show();
        }
    }
}