package com.example.noodleapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import java.io.IOException;

public class HelloApplication extends Application {

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
}