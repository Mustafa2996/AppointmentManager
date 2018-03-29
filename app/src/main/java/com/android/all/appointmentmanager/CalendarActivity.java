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
 * Created by yuriyallakhverdov on 26.03.2018.
 */

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    String mDateString;
    EditText mDateEditText;
    Calendar mCurrentDate;
    Bitmap mGeneratedDateIcon;
    ImageGenerator mImageGenerator;
    ImageView mDisplayGeneratedImage;

    Button createAppointment;
    Button deleteAppointment;
    Button viewEditAppointment;
    Button moveAppointment;
    Button searchAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        createAppointment = (Button) findViewById(R.id.btnCreateAppointment);
        createAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateString == null || mDateString.equals("")) {
                    showDatePicker();
                } else {
                    Intent intent = new Intent(CalendarActivity.this,
                            CreateAppointmentActivity.class);
                    intent.putExtra("Date", mDateString);
                    startActivity(intent);
                }

            }
        });

        deleteAppointment = (Button) findViewById(R.id.btnDeleteAppointment);
        deleteAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateString == null || mDateString.equals("")) {
                    showDatePicker();
                } else {
                    Intent intent = new Intent(CalendarActivity.this,
                            DeleteAppointmentActivity.class);
                    intent.putExtra("Date", mDateString);
                    startActivity(intent);
                }
            }
        });

        viewEditAppointment = (Button) findViewById(R.id.btnViewEditAppointment);
        viewEditAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateString == null || mDateString.equals("")) {
                    showDatePicker();
                } else {
                    Intent intent = new Intent(CalendarActivity.this,
                            ViewEditAppointmentActivity.class);
                    intent.putExtra("Date", mDateString);
                    startActivity(intent);
                }
            }
        });

        moveAppointment = (Button) findViewById(R.id.btnMoveAppointment);
        moveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateString == null || mDateString.equals("")) {
                    showDatePicker();
                } else {
                    Intent intent = new Intent(CalendarActivity.this,
                            MoveAppointmentActivity.class);
                    intent.putExtra("Date", mDateString);
                    startActivity(intent);
                }
            }
        });

        searchAppointment = (Button) findViewById(R.id.btnSearchAppointment);
        searchAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateString == null || mDateString.equals("")) {
                    showDatePicker();
                } else {
                    Intent intent = new Intent(CalendarActivity.this,
                            SearchAppointmentActivity.class);
                    intent.putExtra("Date", mDateString);
                    startActivity(intent);
                }
            }
        });

        mImageGenerator = new ImageGenerator(this);
        mDateEditText = (EditText) findViewById(R.id.txtDateEntered);
        mDisplayGeneratedImage = (ImageView) findViewById(R.id.imageGen);

        mImageGenerator.setIconSize(50, 50);
        mImageGenerator.setDateSize(30);
        mImageGenerator.setMonthSize(10);

        mImageGenerator.setDatePosition(42);
        mImageGenerator.setMonthPosition(14);

        mImageGenerator.setDateColor(Color.parseColor("#3c6eaf"));
        mImageGenerator.setMonthColor(Color.WHITE);

        mImageGenerator.setStorageToSDCard(true);

        showDatePicker();

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentDate = Calendar.getInstance();
                int year = mCurrentDate.get(Calendar.YEAR);
                int month = mCurrentDate.get(Calendar.MONTH);
                int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
                Log.d("Calendar", year + " " + month + " " + day);

                DatePickerDialog mDatePicker =
                        new DatePickerDialog(CalendarActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                        mDateString = (selectedDay + "-" + (selectedMonth +1) + "-" + selectedYear);
                                        mDateEditText.setText(mDateString);
                                        mCurrentDate.set(selectedYear, selectedMonth, selectedDay);
                                        mGeneratedDateIcon = mImageGenerator
                                                .generateDateImage(mCurrentDate, R.drawable.empty_calendar);
                                        mDisplayGeneratedImage.setImageBitmap(mGeneratedDateIcon);
                                    }
                                }, year, month, day);
                mDatePicker.setCancelable(false);
                mDatePicker.show();
            }
        });
    }

    private void showDatePicker() {
        mCurrentDate = Calendar.getInstance();
        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        Log.d("Calendar", year + " " + month + " " + day);

        DatePickerDialog mDatePicker =
                new DatePickerDialog(CalendarActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                mDateString = (selectedDay + "-" + (selectedMonth +1) + "-" + selectedYear);
                                mDateEditText.setText(mDateString);

                                mCurrentDate.set(selectedYear, selectedMonth, selectedDay);
                                mGeneratedDateIcon = mImageGenerator
                                        .generateDateImage(mCurrentDate, R.drawable.empty_calendar);
                                mDisplayGeneratedImage.setImageBitmap(mGeneratedDateIcon);
                            }
                        }, year, month, day);
        mDatePicker.setCancelable(false);
        mDatePicker.show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


}
