package com.example.noodleapp;

import net.fortuna.ical4j.model.TimeZone;

import java.util.Map;

public class PropsEvento {
    TimeZone timeZone;
    Date date;
    Hour hour;
    Map<String ,pollAnswer > eachAnswer;
    //propre à evento ? durée ou date début date fin
    Hour hourEnd;

    public PropsEvento() {
    }

    public PropsEvento(TimeZone timeZone, Date date, Hour hour, Hour hourEnd, Map<String, pollAnswer> eachAnswer) {
        this.timeZone = timeZone;
        this.date = date;
        this.hour = hour;
        this.hourEnd = hourEnd;
        this.eachAnswer = eachAnswer;

    }

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

        public Date() {
        }

        public Date(int month, int day, int year) {
            this.month = month;
            this.day = day;
            this.year = year;
        }
    }

    public static class Hour {
        int hour;
        int minute;
        int second;

        public Hour() {
        }

        public Hour(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

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
                hourEnd.toString()+
                eachAnswer.toString()+
                '}';
    }
}
