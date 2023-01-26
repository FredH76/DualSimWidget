package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textWidget;
    AppWidgetManager appWidgetManager;
    int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*textWidget = findViewById(R.id.textView2);

        appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
        appWidgetId = 1;

        textWidget.setText("coucou");*/
    }
}