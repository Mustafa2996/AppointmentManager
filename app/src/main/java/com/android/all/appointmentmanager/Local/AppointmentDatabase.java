package com.android.all.appointmentmanager.Local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.android.all.appointmentmanager.Model.Appointment;

import static com.android.all.appointmentmanager.Local.UserDatabase.DATABASE_VERSION;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

@Database(entities = Appointment.class, version = DATABASE_VERSION)
public abstract class AppointmentDatabase extends RoomDatabase {

    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "Uraall-Database-Room";

    public abstract AppointmentDAO appointmentDAO();

    private static AppointmentDatabase mInstance;

    public static AppointmentDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context, AppointmentDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return mInstance;
    }

}
