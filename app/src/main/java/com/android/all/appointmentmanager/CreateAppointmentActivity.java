package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import java.util.List;

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

    private static final String TAG = "CreateAppointment";

    EditText appointmentTitle;
    EditText appointmentTime;
    EditText appointmentDetails;
    Button saveAppointment;
    Intent initIntent;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        initIntent = getIntent();

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        appointmentTitle = (EditText) findViewById(R.id.appointmentTitle);
        appointmentTime = (EditText) findViewById(R.id.appointmentTime);
        appointmentDetails = (EditText) findViewById(R.id.appointmentDetails);
        saveAppointment = (Button) findViewById(R.id.btnSaveAppointment);
        saveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "in onClick appointmentDate = " +
                        initIntent.getStringExtra("Date") + ", appointmentTitle = " +
                        appointmentTitle.getText().toString());
                validateTitle(initIntent.getStringExtra("Date"),
                        appointmentTitle.getText().toString());
            }
        });
    }

    private void createPromptDialog() {
        new AlertDialog.Builder(CreateAppointmentActivity.this)
                .setMessage("Appointment " +
                        appointmentTitle.getText().toString() +
                        " already exists, please choose a different date or event title")
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CreateAppointmentActivity.this,
                                        CalendarActivity.class);
                                startActivity(intent);

                            }
                        }).create().show();
    }

    private void validateTitle(final String date, final String title) {

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean isTitleValid = true;
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                for (Appointment appointment : appointmentList) {

                    if (appointment.getTitle().equals(title)) {
                        isTitleValid = false;
                        Log.d(TAG, "in onPostExecute appointmentDate = " +
                                appointment.getDate() + ", appointmentTitle = " +
                                appointment.getTitle() + " isTitleValid[0] = " +
                                isTitleValid);
                        break;
                    }
                }
                return isTitleValid;
            }

            @Override
            protected void onPostExecute(Boolean isTitleValid) {
                if (isTitleValid) {
                    addAppointment();
                } else {
                    createPromptDialog();
                }
            }
        }.execute();
    }

    private void addAppointment() {
        //Add new Appointment
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
        compositeDisposable.add(disposable);
    }


}

