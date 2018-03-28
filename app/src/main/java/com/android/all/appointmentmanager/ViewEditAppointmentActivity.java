package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import java.util.ArrayList;
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

public class ViewEditAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "ViewEditAppointmentActivity";
    private String mDateString;
    private TextView mEditDateAppointmentsList;
    private EditText mEditAppointmentNumberEditText;
    private Button edit;
    private List<Appointment> mDateAppointments;
    private String mEditId;

    //Adapter
    List<Appointment> appointmentList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_appointment);

        mEditDateAppointmentsList = (TextView)findViewById(R.id.editDateAppointmentsList);
        mEditAppointmentNumberEditText = (EditText) findViewById(R.id.txtEditAppointmentNumber);
        edit = (Button) findViewById(R.id.btnEdit);
        mDateString = getIntent().getStringExtra("Date");
        mDateAppointments = new ArrayList<>();

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        getDateAppointments(mDateString);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditId = mEditAppointmentNumberEditText.getText().toString();
                new AlertDialog.Builder(ViewEditAppointmentActivity.this)
                        .setMessage("Would you like to edit event: " +
                                mEditId + "?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editAppointmentFromDateList(mDateAppointments, mEditId);
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

    private void editAppointmentFromDateList(final List<Appointment> list, final String id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Appointment appointment : list) {
                    if (appointment.getId() == Integer.valueOf(id)) {
                        updateAppointment(appointment);
                    }
                }
                return null;
            }

        }.execute();
    }

    private void getDateAppointments(final String date) {
        new AsyncTask<Void, Void, List<Appointment>>() {
            @Override
            protected List<Appointment> doInBackground(Void... params) {
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                ViewEditAppointmentActivity.this.mDateAppointments.clear();
                ViewEditAppointmentActivity.this.mDateAppointments = appointmentList;
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
                mEditDateAppointmentsList.setText(appointmentsListBuilder.toString());
            }

        }.execute();
    }

    private void updateAppointment(final Appointment appointment) {
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
                                   Toast.makeText(ViewEditAppointmentActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   Intent intent = new Intent(ViewEditAppointmentActivity.this,
                                           ListActivity.class);
                                   startActivity(intent);

                                   // loadData();//Refresh data
                               }
                           }

                );

        compositeDisposable.add(disposable);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewEditAppointmentActivity.this,
                CalendarActivity.class);
        startActivity(intent);
    }


}
