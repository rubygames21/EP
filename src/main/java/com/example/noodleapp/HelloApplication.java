package com.example.noodleapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

<<<<<<< HEAD:src/main/java/com/example/noodleapp/HelloApplication.java
import java.io.IOException;

public class HelloApplication extends Application {

    static User user;
    @Override
    public void start(Stage stage) throws IOException {
        createUser();

        //mes sondages
        user.addFPoll("https://framadate.org/cYOX1OO8EZlDVuTh");
        user.addFPoll("https://framadate.org/LDJ5k3TN3j3AgDPK");
        user.addFPoll("https://framadate.org/TL4Dc1Lqee1rxUFQ");
        //mes noms
        user.addFName("Clem");
        user.addFName("Clement");
        user.addFName("adol");


        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("homeView.fxml"));

public class NoodleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NoodleApplication.class.getResource("homeView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Noodle");
        stage.setScene(scene);
        stage.show();
              //provisoire car l'idéal serait de récupérer le user existant
        //this.user.setMergeICS(true);
    }

    private void createUser() {
        user=new User();
    }

    public static void main(String[] args) {    //inutile

        launch();

    }

    public static User getUser() {
        return user;
    }

    /*

    //Simon

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("framadateView.fxml"));
        Parent root = fxmlLoader.load();
        ControllerFramadate controllerFramadate = fxmlLoader.getController();
        User user = new User();
        user.mergeICS = false;
        controllerFramadate.setUser(user);
        Scene scene = new Scene(root, 700, 850);
        stage.setTitle("Noodle");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
*/
}