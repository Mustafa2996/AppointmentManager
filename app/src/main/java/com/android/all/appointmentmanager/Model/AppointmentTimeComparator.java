package com.android.all.appointmentmanager.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by yuriyallakhverdov on 28.03.2018.
 */

public class AppointmentTimeComparator implements Comparator<Appointment> {
    public int compare(Appointment appointment1, Appointment appointment2) {


        String appointment1DateTimeStr =
                appointment1.getDate() + " " + appointment1.getTime();
        String appointment2DateTimeStr =
                appointment2.getDate() + " " + appointment2.getTime();

        Date appointment1Date =
                null;
        try {
            appointment1Date = new SimpleDateFormat("DD-MM-YYYY hh:mm").parse(appointment1DateTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date appointment2Date =
                null;
        try {
            appointment2Date = new SimpleDateFormat("DDMMYYYY hh:mm").parse(appointment2DateTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return appointment1Date.compareTo(appointment2Date);
    }
}
