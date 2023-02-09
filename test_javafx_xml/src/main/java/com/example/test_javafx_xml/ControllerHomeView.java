package com.example.test_javafx_xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerHomeView {

    @FXML
    private Button linkFramadateView;

    @FXML
    private Button linkSettings;

    public void addFramadateUrlAction(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader2 = new FXMLLoader(HelloApplication.class.getResource("framadateView.fxml"));
        Parent root = fxmlLoader2.load();  //peut génerer une exception
        //récupération de la scène actuelle
        Scene currentScene = linkFramadateView.getScene();

        //récupération de l'autre controleur
        ControllerFramadate framadateController = fxmlLoader2.getController();

        //on dit à l'autre controlleur que sa scène précédente sera la scène actuelle
        framadateController.setPreviousScene(currentScene);

        //création d'une nouvelle scène
        Scene scene = new Scene(root,700,850);

        //récuperation du stage
        Stage stage = (Stage) linkFramadateView.getScene().getWindow();

        //on charge la nouvelle scène
        stage.setScene(scene);

    }

    public void goSettings(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader2 = new FXMLLoader(HelloApplication.class.getResource("settingsView.fxml"));
        Parent root = fxmlLoader2.load();  //peut génerer une exception
        //récupération de la scène actuelle
        Scene currentScene = linkSettings.getScene();

        //récupération de l'autre controleur
        ControllerSettings settingsController = fxmlLoader2.getController();

        //on dit à l'autre controlleur que sa scène précédente sera la scène actuelle
        settingsController.setPreviousScene(currentScene);

        //création d'une nouvelle scène
        Scene scene = new Scene(root,700,850);

        //récuperation du stage
        Stage stage = (Stage) linkSettings.getScene().getWindow();

        //on charge la nouvelle scène
        stage.setScene(scene);
    }
}
