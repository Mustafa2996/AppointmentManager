package com.android.all.appointmentmanager.Database;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.android.all.appointmentmanager.Model.User;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by yuriyallakhverdov on 25.03.2018.
 */

public interface IUserDataSource {

    Flowable<User> getUserById(int userId);

    Flowable<List<User>> getAllUsers();

    void insertUser(User... users);

    void updateUser(User... users);

    void deleteUser(User user);

    void deleteAllUsers();
}
