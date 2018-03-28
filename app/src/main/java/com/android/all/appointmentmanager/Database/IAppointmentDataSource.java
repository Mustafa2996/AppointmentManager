package com.android.all.appointmentmanager.Database;

import com.android.all.appointmentmanager.Model.Appointment;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

public interface IAppointmentDataSource {

    Flowable<Appointment> getAppointmentById(int appointmentId);

    List<Appointment> getAppointmentsByDate(String appointmentDate);

    Flowable<List<Appointment>> getAllAppointments();

    void insertAppointment(Appointment... appointments);

    void updateAppointment(Appointment... appointments);

    void deleteAppointment(Appointment appointment);

    void deleteAllAppointments();

}
