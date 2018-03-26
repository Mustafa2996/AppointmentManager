package com.android.all.appointmentmanager;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.kd.dynamic.calendar.generator.ImageGenerator;

import java.util.Calendar;

/**
 * Created by yuriyallakhverdov on 26.03.2018.
 */

public class CalendarActivity extends AppCompatActivity {

    EditText mDateEditText;
    Calendar mCurrentDate;
    Bitmap mGeneratedDateIcon;
    ImageGenerator mImageGenerator;
    ImageView mDisplayGeneratedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

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
                                        mDateEditText.setText(selectedDay + "-" + (selectedMonth +1) + "-" + selectedYear);

                                        mCurrentDate.set(selectedYear, selectedMonth, selectedDay);
                                        mGeneratedDateIcon = mImageGenerator
                                                .generateDateImage(mCurrentDate, R.drawable.empty_calendar);
                                        mDisplayGeneratedImage.setImageBitmap(mGeneratedDateIcon);
                                    }
                                }, year, month, day);
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
                                mDateEditText.setText(selectedDay + "-" + (selectedMonth +1) + "-" + selectedYear);

                                mCurrentDate.set(selectedYear, selectedMonth, selectedDay);
                                mGeneratedDateIcon = mImageGenerator
                                        .generateDateImage(mCurrentDate, R.drawable.empty_calendar);
                                mDisplayGeneratedImage.setImageBitmap(mGeneratedDateIcon);
                            }
                        }, year, month, day);
        mDatePicker.show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


}
