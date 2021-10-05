package com.example.pull_and_do;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class CalendarActivity extends AppCompatActivity {
    private TextView  text1;
    private TextView  text2;
    private TextView  text3;
    private SlidrInterface slidr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);
        slidr = Slidr.attach(this);
    }

}
