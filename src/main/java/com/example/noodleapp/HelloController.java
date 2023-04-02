package com.example.noodleapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Button newUserButton;
    @FXML
    private Button existingPollsButton;

    public void switchSceneToNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newUserView.fxml"));
            Stage stage = (Stage) newUserButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchSceneToExistingPolls(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("existingPollsView.fxml"));
            Stage stage = (Stage) existingPollsButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
