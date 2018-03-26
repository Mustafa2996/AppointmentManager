package com.android.all.appointmentmanager.Local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.android.all.appointmentmanager.Model.User;

import static com.android.all.appointmentmanager.Local.UserDatabase.DATABASE_VERSION;

/**
 * Created by yuriyallakhverdov on 24.03.2018.
 */

@Database(entities = User.class, version = DATABASE_VERSION)
public abstract class UserDatabase extends RoomDatabase {

    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "Uraall-Database-Room";

    public abstract UserDAO userDAO();

    private static UserDatabase mInstance;

    public static UserDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context, UserDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return mInstance;
    }

}
