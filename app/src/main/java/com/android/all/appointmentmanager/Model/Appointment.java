package com.android.all.appointmentmanager.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import io.reactivex.annotations.NonNull;

/**
 * Created by yuriyallakhverdov on 27.03.2018.
 */

@Entity(tableName = "appointments")
public class Appointment {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "details")
    private String details;

    public Appointment() {
    }

    public Appointment(String date, String time, String title, String details) {
        this.date = date;
        this.time = time;
        this.title = title;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return new StringBuilder(date).append("\n").append(time)
                .append("\n").append(title).append("\n")
                .append(details).toString();
    }
}
