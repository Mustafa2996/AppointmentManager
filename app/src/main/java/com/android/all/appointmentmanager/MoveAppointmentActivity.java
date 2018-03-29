package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.kd.dynamic.calendar.generator.ImageGenerator;

import java.util.ArrayList;
import java.util.Calendar;
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
 * Created by yuriyallakhverdov on 29.03.2018.
 */

public class MoveAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "MoveAppointmentActivity";
    private String mDateString;
    private TextView mMoveDateAppointmentsList;
    private EditText mMoveAppointmentNumberEditText;
    private Button move;
    private Button pickDate;
    private List<Appointment> mMoveDateAppointments;
    private String mMoveId;

    Calendar mCurrentDate;
    String pickedDateString;

    //Adapter
    List<Appointment> appointmentList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_appointment);

        mMoveDateAppointmentsList = (TextView)findViewById(R.id.moveDateAppointmentsList);
        mMoveAppointmentNumberEditText = (EditText) findViewById(R.id.txtMoveAppointmentNumber);
        move = (Button) findViewById(R.id.btnMove);
        pickDate = (Button) findViewById(R.id.btnPickDate);
        mDateString = getIntent().getStringExtra("Date");
        mMoveDateAppointments = new ArrayList<>();
        mMoveId = "";

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        getDateAppointments(mDateString);

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoveId = mMoveAppointmentNumberEditText.getText().toString();
                if (mMoveId == null || mMoveId.matches("")) {
                    new AlertDialog.Builder(MoveAppointmentActivity.this)
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
                } else {
                    new AlertDialog.Builder(MoveAppointmentActivity.this)
                            .setMessage("Would you like to move event: " +
                                    mMoveId + "?")
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showMoveDatePicker();
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

            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMoveId = mMoveAppointmentNumberEditText.getText().toString();
                if (pickedDateString == null || pickedDateString.equals("")) {
                    new AlertDialog.Builder(MoveAppointmentActivity.this)
                            .setMessage("You should pick the date")
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showMoveDatePicker();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                } else {
                    getAppointmentById(Integer.valueOf(Integer.valueOf(mMoveId)));
                }
            }
        });

    }

    private void showMoveDatePicker() {
        pickedDateString = "";
        mCurrentDate = Calendar.getInstance();
        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        Log.d("Calendar", year + " " + month + " " + day);

        DatePickerDialog mDatePicker =
                new DatePickerDialog(MoveAppointmentActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                pickedDateString = (selectedDay + "-" + (selectedMonth +1) + "-" + selectedYear);
                            }
                        }, year, month, day);
        mDatePicker.setCancelable(false);
        mDatePicker.show();
    }

    private void getAppointmentById(int id) {
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
                        Toast.makeText(MoveAppointmentActivity.this,
                                "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAppointmentByIdSuccess(Appointment appointment) {
        appointment.setDate(pickedDateString);
        //createAppointment(appointment);
        moveAppointment(appointment);
    }

    private void moveAppointment(final Appointment appointment) {
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
                                   Toast.makeText(MoveAppointmentActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   /*Intent intent = new Intent(MoveAppointmentActivity.this,
                                           ListActivity.class);
                                   startActivity(intent);*/
                                   getDateAppointments(pickedDateString);
                               }
                           }

                );

        compositeDisposable.add(disposable);
    }

    private void getDateAppointments(final String date) {
        new AsyncTask<Void, Void, List<Appointment>>() {
            @Override
            protected List<Appointment> doInBackground(Void... params) {
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                MoveAppointmentActivity.this.mMoveDateAppointments.clear();
                MoveAppointmentActivity.this.mMoveDateAppointments = appointmentList;
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
                mMoveDateAppointmentsList.setText(appointmentsListBuilder.toString());
            }

        }.execute();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MoveAppointmentActivity.this,
                CalendarActivity.class);
        startActivity(intent);
    }


}
