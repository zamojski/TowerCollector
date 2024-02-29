/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static long getCurrentDateWithoutTime() {
        long dateTime = System.currentTimeMillis();
        long dateOnly = (dateTime / MILLIS_IN_DAY) * MILLIS_IN_DAY;// like integer division
        return dateOnly;
    }

    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    public static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static long getTimeDiff(long start, long end) {
        long diffInMillis = start - end;
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        return diffInDays;
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    public static Date addHours(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    public static Date getTimeFrom(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }
}
