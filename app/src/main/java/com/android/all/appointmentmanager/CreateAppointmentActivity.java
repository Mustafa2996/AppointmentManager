package com.android.all.appointmentmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by yuriyallakhverdov on 26.03.2018.
 */

public class CreateAppointmentActivity extends AppCompatActivity {

    Button saveAppointment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);

        saveAppointment = (Button) findViewById(R.id.btnSaveAppointment);
        saveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAppointmentActivity.this,
                        ListActivity.class);
                startActivity(intent);
            }
        });
    }
}
