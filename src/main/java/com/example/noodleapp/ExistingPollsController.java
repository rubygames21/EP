package com.example.noodleapp;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ExistingPollsController {
    @FXML
    private Button backButton;

    public void goBack(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("helloView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
