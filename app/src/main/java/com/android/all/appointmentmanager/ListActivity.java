package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
 * Created by yuriyallakhverdov on 27.03.2018.
 */

public class ListActivity extends AppCompatActivity {

    private ListView listAppointments;
    private FloatingActionButton fab;

    //Adapter
    List<Appointment> appointmentList = new ArrayList<>();
    ArrayAdapter adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Init
        compositeDisposable = new CompositeDisposable();

        //Init view
        listAppointments = (ListView) findViewById(R.id.listAppointments);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, appointmentList);
        registerForContextMenu(listAppointments);
        listAppointments.setAdapter(adapter);

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        //Load all data from database
        loadData();

        //Event
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add new Appointment
                //Random email
                Disposable disposable = Observable.create(
                        new ObservableOnSubscribe<Object>() {

                            @Override
                            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                                Appointment appointment = new Appointment("Date", "Time",
                                        "Title", "Details");
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
                                           Toast.makeText(ListActivity.this,
                                                   "Appointment is added!", Toast.LENGTH_SHORT)
                                                   .show();
                                       }
                                   }, new Consumer<Throwable>() {
                                       @Override
                                       public void accept(Throwable throwable) throws Exception {
                                           Toast.makeText(ListActivity.this,
                                                   "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                                   .show();
                                       }
                                   }, new Action() {
                                       @Override
                                       public void run() throws Exception {
                                           loadData();//Refresh data
                                       }
                                   }

                        );
            }
        });

    }

    private void loadData() {
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
                        Toast.makeText(ListActivity.this,
                                "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllAppointmentsSuccess(List<Appointment> appointments) {
        appointmentList.clear();
        appointmentList.addAll(appointments);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                deleteAllAppointments();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllAppointments() {
        Disposable disposable = Observable.create(
                new ObservableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        appointmentRepository.deleteAllAppointments();
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
                                   Toast.makeText(ListActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   loadData();//Refresh data
                               }
                           }

                );
        compositeDisposable.add(disposable);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle("Select action:");
        menu.add(Menu.NONE, 0, Menu.NONE, "UPDATE");
        menu.add(Menu.NONE, 1, Menu.NONE, "DELETE");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Appointment appointment = appointmentList.get(info.position);
        switch (item.getItemId()) {
            case 0://update
            {
                final EditText editTitle = new EditText(ListActivity.this);
                editTitle.setText(appointment.getTitle());
                editTitle.setHint("Enter your name");
                new AlertDialog.Builder(ListActivity.this)
                        .setTitle("Edit")
                        .setMessage("Edit appointment name")
                        .setView(editTitle)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (TextUtils.isEmpty(editTitle.getText().toString())) {
                                            return;
                                        } else {
                                            appointment.setTitle(editTitle.getText().toString());
                                            updateAppointment(appointment);
                                        }
                                    }
                                }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
            break;
            case 1://delete:
            {
                new AlertDialog.Builder(ListActivity.this)
                        .setMessage("Do you want to delete " + appointment.toString())
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteAppointment(appointment);
                                    }
                                }).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
            break;
        }
        return true;
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
                                   Toast.makeText(ListActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   loadData();//Refresh data
                               }
                           }

                );

        compositeDisposable.add(disposable);
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
                                   Toast.makeText(ListActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   loadData();//Refresh data
                               }
                           }

                );

        compositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
