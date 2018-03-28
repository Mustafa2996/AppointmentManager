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



public class EditAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "EditAppointmentActivity";

    EditText appointmentEditTitle;
    EditText appointmentEditTime;
    EditText appointmentEditDetails;
    Button updateAppointment;
    Intent initIntent;
    String stringId;
    String mEditedAppointmentDate;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appointment);

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        initIntent = getIntent();
        stringId = initIntent.getStringExtra("Id");
        mEditedAppointmentDate = initIntent.getStringExtra("Date");

        appointmentEditTitle = (EditText) findViewById(R.id.appointmentEditTitle);
        appointmentEditTime = (EditText) findViewById(R.id.appointmentEditTime);
        appointmentEditDetails = (EditText) findViewById(R.id.appointmentEditDetails);

        loadDataForEdit(Integer.valueOf(stringId));


        updateAppointment = (Button) findViewById(R.id.btnUpdateAppointment);
        updateAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Appointment editedAppointment = new Appointment(
                        mEditedAppointmentDate,
                        appointmentEditTime.getText().toString(),
                        appointmentEditTitle.getText().toString(),
                        appointmentEditDetails.getText().toString()
                );
                editedAppointment.setId(Integer.valueOf(stringId));
                Log.d(TAG, editedAppointment.toString());
                updateAppointmentAfterEdit(editedAppointment);
            }
        });
    }

    private void updateAppointmentAfterEdit(final Appointment appointment) {
        Disposable disposable = Observable.create(
                new ObservableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        appointmentRepository.updateAppointment(appointment);
                        e.onComplete();
                    }
                }
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                               @Override
                               public void accept(Object o) throws Exception {

                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(EditAppointmentActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   Intent intent = new Intent(EditAppointmentActivity.this,
                                           ListActivity.class);
                                   startActivity(intent);
                               }
                           }

                );

        compositeDisposable.add(disposable);
    }

    private void loadDataForEdit(int id) {
        {
            //Use RxJava
            Disposable disposable = appointmentRepository.getAppointmentById(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Appointment>() {
                        @Override
                        public void accept(Appointment appointment) throws Exception {
                            onGetAppointmentByIdSuccess(appointment);
                        }

                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(EditAppointmentActivity.this,
                                    "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void onGetAppointmentByIdSuccess(Appointment appointment) {
        appointmentEditTitle.setText(appointment.getTitle());
        appointmentEditTime.setText(appointment.getTime());
        appointmentEditDetails.setText(appointment.getDetails());
    }



}
