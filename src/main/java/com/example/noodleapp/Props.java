package com.example.noodleapp;

import net.fortuna.ical4j.model.TimeZone;

import java.util.Map;

public class Props {
    TimeZone timeZone;
    Date date;
    Hour hour;
    Hour hourEnd;
    Map<String ,pollAnswer > eachAnswer;


    public Props() {
    }


    public Props(TimeZone timeZone, Props.Date date, Props.Hour hour, Props.Hour hourEnd, Map<String, pollAnswer> eachAnswer) {
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


        public Date() {
        }

        public Date(int month, int day, int year) {
            this.month = month;
            this.day = day;
            this.year = year;
        }

        @Override
        public String toString() {
            return "Date{" +
                    "month=" + month +
                    ", day=" + day +
                    ", year=" + year +

                    '}';
        }

        public String dateDisplay(){
            return day+"/"+month+"/"+year;
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
        public String hourDisplay(){
            if(minute==0){
                return hour+"h";
            }
            return hour+"h"+minute;
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


    //affichage de la date de la prop sur l'écran d'accueil

}
