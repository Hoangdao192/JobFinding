package com.vnsoftware.jobfinder.util;

import java.util.Calendar;

public class TimestampToString {
    public static final String convert(Long timestamp) {
        Long diff = Calendar.getInstance().getTimeInMillis() / 1000 - timestamp;
        if (diff < 60) {
            return diff + " giây";
        } else if (diff < 3600) {
            return diff / 60 + " phút";
        } else if (diff < 86400) {
            long hours = diff / 3600;
            long minutes = diff % 3600 / 60;
            return hours + " giờ " + minutes + " phút";
        }

        long day = diff / 86400;
        if (day < 31) {
            return day + " ngày";
        } else if (day < 365) {
            return day / 31 + " tháng";
        }

        return "";
    }
}
