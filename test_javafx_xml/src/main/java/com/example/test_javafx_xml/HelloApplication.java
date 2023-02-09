package com.example.test_javafx_xml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HelloApplication extends Application {



    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("homeView.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 700, 850);
        stage.setTitle("Noodle");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class MyPoll{
        private String title;
        private String organizer;
        private String day;
        private String month; //savoir si on met en int [1;12] ou alors en string genre janvier
        private String year;  //on met en String pour que tout soit en String
        private String hour;

        public MyPoll(String d,String m,String y, String h,String o,String t){
            this.title=t;
            this.day=d;
            this.month=m;
            this.year=y;
            this.hour=h;
            this.organizer=o;
        }

        public String getDay() {
            return day;
        }

        public String getMonth() {
            return month;
        }

        public String getYear() {
            return year;
        }

        public String getHour() {
            return hour;
        }

        public String getOrganizer() {
            return organizer;
        }

        public String getTitle() {
            return title;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public void setOrganizer(String organizer) {
            this.organizer = organizer;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "Réunion : " +title +" par " + organizer + " le " + day +" " + month +" " + year +" à " + hour ;
        }
    }
}