package com.android.all.appointmentmanager.Local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.android.all.appointmentmanager.Model.Appointment;
import com.android.all.appointmentmanager.Model.User;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

@Dao
public interface AppointmentDAO {

    @Query("SELECT * FROM appointments WHERE id=:appointmentId")
    Flowable<Appointment> getAppointmentById(int appointmentId);

    @Query("SELECT * FROM appointments")
    Flowable<List<Appointment>> getAllAppointments();

    @Insert
    void insertAppointment(Appointment... appointments);

    @Update
    void updateAppointment(Appointment... appointments);

    @Delete
    void deleteAppointment(Appointment appointment);

    @Query("DELETE FROM appointments")
    void deleteAllAppointments();
}
