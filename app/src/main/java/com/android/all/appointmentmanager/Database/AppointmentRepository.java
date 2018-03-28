package com.android.all.appointmentmanager.Database;

import com.android.all.appointmentmanager.Model.Appointment;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

public class AppointmentRepository implements IAppointmentDataSource {

    private IAppointmentDataSource mLocalDataSource;

    private static AppointmentRepository mInstance;

    public AppointmentRepository(IAppointmentDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
    }

    public static AppointmentRepository getInstance(IAppointmentDataSource mLocalDataSource) {
        if (mInstance == null) {
            mInstance = new AppointmentRepository(mLocalDataSource);
        }
        return mInstance;
    }

    @Override
    public Flowable<Appointment> getAppointmentById(int appointmentId) {
        return mLocalDataSource.getAppointmentById(appointmentId);
    }

    @Override
    public List<Appointment> getAppointmentsByDate(String appointmentDate) {
        return mLocalDataSource.getAppointmentsByDate(appointmentDate);
    }

    @Override
    public Flowable<List<Appointment>> getAllAppointments() {
        return mLocalDataSource.getAllAppointments();
    }

    @Override
    public void insertAppointment(Appointment... appointments) {
        mLocalDataSource.insertAppointment(appointments);
    }

    @Override
    public void updateAppointment(Appointment... appointments) {
        mLocalDataSource.updateAppointment(appointments);
    }

    @Override
    public void deleteAppointment(Appointment appointment) {
        mLocalDataSource.deleteAppointment(appointment);
    }

    @Override
    public void deleteAllAppointments() {
        mLocalDataSource.deleteAllAppointments();
    }
}
