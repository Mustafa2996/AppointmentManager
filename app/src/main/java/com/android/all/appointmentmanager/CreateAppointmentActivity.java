package com.android.all.appointmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yuriyallakhverdov on 26.03.2018.
 */

public class CreateAppointmentActivity extends AppCompatActivity {

    EditText appointmentTitle;
    EditText appointmentTime;
    EditText appointmentDetails;
    Button saveAppointment;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        final Intent initIntent = getIntent();

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        appointmentTitle = (EditText) findViewById(R.id.appointmentTitle);
        appointmentTime = (EditText) findViewById(R.id.appointmentTime);
        appointmentDetails = (EditText) findViewById(R.id.appointmentDetails);
        saveAppointment = (Button) findViewById(R.id.btnSaveAppointment);
        saveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add new Appointment
                //Random email
                Disposable disposable = Observable.create(
                        new ObservableOnSubscribe<Object>() {

                            @Override
                            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                                Appointment appointment =
                                        new Appointment(initIntent.getStringExtra("Date"),
                                                appointmentTime.getText().toString(),
                                                appointmentTitle.getText().toString(),
                                                appointmentDetails.getText().toString());
                                appointmentRepository.insertAppointment(appointment);
                                e.onComplete();
                            }
                        }
                )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer() {
                                       @Override
                                       public void accept(Object o) throws Exception {
                                           Toast.makeText(CreateAppointmentActivity.this,
                                                   "Appointment is added!", Toast.LENGTH_SHORT)
                                                   .show();
                                       }
                                   }, new Consumer<Throwable>() {
                                       @Override
                                       public void accept(Throwable throwable) throws Exception {
                                           Toast.makeText(CreateAppointmentActivity.this,
                                                   "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                                   .show();
                                       }
                                   }, new Action() {
                                       @Override
                                       public void run() throws Exception {
                                           Intent intent = new Intent(CreateAppointmentActivity.this,
                                                   ListActivity.class);
                                           startActivity(intent);
                                       }
                                   }

                        );
            }
        });
    }


}
