package com.android.all.appointmentmanager.Local;

import com.android.all.appointmentmanager.Database.IAppointmentDataSource;
import com.android.all.appointmentmanager.Model.Appointment;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

public class AppointmentDataSource implements IAppointmentDataSource {

    private AppointmentDAO appointmentDAO;
    private static AppointmentDataSource mInstance;

    public AppointmentDataSource(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
    }

    public static AppointmentDataSource getInstance(AppointmentDAO appointmentDAO) {
        if (mInstance == null) {
            mInstance = new AppointmentDataSource(appointmentDAO);
        }
        return mInstance;
    }

    @Override
    public Flowable<Appointment> getAppointmentById(int appointmentId) {
        return appointmentDAO.getAppointmentById(appointmentId);
    }

    @Override
    public List<Appointment> getAppointmentsByDate(String appointmentDate) {
        return appointmentDAO.getAppointmentsByDate(appointmentDate);
    }

    @Override
    public Flowable<List<Appointment>> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    @Override
    public void insertAppointment(Appointment... appointments) {
        appointmentDAO.insertAppointment(appointments);
    }

    @Override
    public void updateAppointment(Appointment... appointments) {
        appointmentDAO.updateAppointment(appointments);
    }

    @Override
    public void deleteAppointment(Appointment appointment) {
        appointmentDAO.deleteAppointment(appointment);
    }

    @Override
    public void deleteAllAppointments() {
        appointmentDAO.deleteAllAppointments();
    }
}
