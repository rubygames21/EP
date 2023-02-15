package com.example.noodleapp;

import net.fortuna.ical4j.model.TimeZone;

import java.util.Map;

public class Props {
    TimeZone timeZone;
    Date date;
    Hour hour;
    Map<String ,pollAnswer > eachAnswer;

    public static class Date {
        int month;
        int day;
        int year;

        @Override
        public String toString() {
            return "Date{" +
                    "month=" + month +
                    ", day=" + day +
                    ", year=" + year +

                    '}';
        }
    }

    public static class Hour {
        int hour;
        int minute;
        int second;

        @Override
        public String toString() {
            return "Hour{" +
                    "hour=" + hour +
                    ", minute=" + minute +
                    ", second=" + second +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Props{" +
                date.toString()+
                hour.toString()+
                eachAnswer.toString()+
                '}';
    }
}
