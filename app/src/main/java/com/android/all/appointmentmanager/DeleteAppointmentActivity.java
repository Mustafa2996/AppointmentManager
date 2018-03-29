package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.arch.persistence.room.Database;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;
import com.android.all.appointmentmanager.Model.AppointmentTimeComparator;
import com.kd.dynamic.calendar.generator.ImageGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
 * Created by yuriyallakhverdov on 28.03.2018.
 */

public class DeleteAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "DeleteAppointmentActivity";
    private String mDateString;
    private TextView mDateAppointmentsList;
    private EditText mAppointmentNumberEditText;
    private Button delete;
    private List<Appointment> mDateAppointments;
    private String mDeleteId;

    //Adapter
    List<Appointment> appointmentList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_appointment);

        mDateAppointmentsList = (TextView)findViewById(R.id.dateAppointmentsList);
        mAppointmentNumberEditText = (EditText) findViewById(R.id.txtAppointmentNumber);
        delete = (Button) findViewById(R.id.btnDelete);
        mDateString = getIntent().getStringExtra("Date");
        mDateAppointments = new ArrayList<>();
        mDeleteId = "";

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        createPromptDialog();



        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteId = mAppointmentNumberEditText.getText().toString();
                if (mDeleteId == null || mDeleteId.matches("")) {
                    new AlertDialog.Builder(DeleteAppointmentActivity.this)
                            .setMessage("You should input appointment ID")
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                }
                new AlertDialog.Builder(DeleteAppointmentActivity.this)
                        .setMessage("Would you like to delete event: " +
                                mDeleteId + "?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteAppointmentFromDateList(mDateAppointments, mDeleteId);
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
            }
        });



    }

    private void deleteAppointmentFromDateList(final List<Appointment> list, final String id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Appointment appointment : list) {
                    if (appointment.getId() == Integer.valueOf(id)) {
                        deleteAppointment(appointment);
                    }
                }
                return null;
            }

        }.execute();
    }

    private void createPromptDialog() {
        new AlertDialog.Builder(DeleteAppointmentActivity.this)
                .setMessage("Choose the action")
                .setPositiveButton("Select appointment to delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getDateAppointments(mDateString);
                            }
                        })
                .setNegativeButton("Delete all appointments for that date",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDateAppointments(mDateString);
                            }
                        })
                .create().show();
    }

    private void getDateAppointments(final String date) {
        new AsyncTask<Void, Void, List<Appointment>>() {
            @Override
            protected List<Appointment> doInBackground(Void... params) {
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                DeleteAppointmentActivity.this.mDateAppointments.clear();
                DeleteAppointmentActivity.this.mDateAppointments = appointmentList;
                return appointmentList;
            }

            @Override
            protected void onPostExecute(List<Appointment> appointmentList) {
                StringBuilder appointmentsListBuilder = new StringBuilder();
                for (Appointment appointment : appointmentList) {
                    String appointmentDescription = "" +
                            appointment.getId() + ". " + appointment.getTime() +
                            " " + appointment.getTitle();
                    appointmentsListBuilder.append(appointmentDescription + "\n");
                }
                mDateAppointmentsList.setText(appointmentsListBuilder.toString());
            }

        }.execute();
    }

    private void deleteDateAppointments(final String date) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                for (Appointment appointment : appointmentList) {
                    deleteAppointment(appointment);
                }
                return null;
            }
        }.execute();
    }

    private void deleteAppointment(final Appointment appointment) {
        Disposable disposable = Observable.create(
                new ObservableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        appointmentRepository.deleteAppointment(appointment);
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
                                   Toast.makeText(DeleteAppointmentActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {

                                   //loadData();//Refresh data
                                   getDateAppointments(mDateString);
                               }
                           }

                );

        compositeDisposable.add(disposable);
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DeleteAppointmentActivity.this,
                CalendarActivity.class);
        startActivity(intent);
    }


}
