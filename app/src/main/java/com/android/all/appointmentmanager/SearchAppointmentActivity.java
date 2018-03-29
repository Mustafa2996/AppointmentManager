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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

public class SearchAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "SearchAppointmentActivity";
    private EditText mSearchAppointmentEditText;
    private Button search;
    private ListView listSearchedAppointments;
    private String searchingText;

    //Adapter
    private List<Appointment> mSearchedAppointments;
    ArrayAdapter adapter;


    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_appointment);

        mSearchAppointmentEditText = (EditText) findViewById(R.id.txtSearchAppointmentText);
        search = (Button) findViewById(R.id.btnSearch);
        listSearchedAppointments = (ListView) findViewById(R.id.listSearchedAppointments);
        mSearchedAppointments = new ArrayList<>();

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, mSearchedAppointments);
        registerForContextMenu(listSearchedAppointments);
        listSearchedAppointments.setAdapter(adapter);

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchingText = mSearchAppointmentEditText.getText().toString();
                findAppointmentsByText(searchingText);
            }
        });
    }

    private void findAppointmentsByText(String s) {
        getAllAppointments();
    }

    private void getAllAppointments() {
        //Use RxJava
        Disposable disposable = appointmentRepository.getAllAppointments()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Appointment>>() {
                    @Override
                    public void accept(List<Appointment> appointments) throws Exception {
                        onGetAllAppointmentsSuccess(appointments);
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(SearchAppointmentActivity.this,
                                "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllAppointmentsSuccess(List<Appointment> appointments) {
        List<Appointment> searchedAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            String title = appointment.getTitle();
            String details = appointment.getDetails();
            if (Pattern.compile(Pattern.quote(searchingText), Pattern.CASE_INSENSITIVE).matcher(title).find() ||
                    Pattern.compile(Pattern.quote(searchingText), Pattern.CASE_INSENSITIVE).matcher(details).find()) {
                searchedAppointments.add(appointment);
            }
        }

        mSearchedAppointments.clear();
        mSearchedAppointments.addAll(searchedAppointments);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchAppointmentActivity.this,
                CalendarActivity.class);
        startActivity(intent);
    }


}
