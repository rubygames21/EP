package com.example.noodleapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NewUserController implements Initializable {
    @FXML
    private RadioButton doodleButton;
    @FXML
    private RadioButton eventoButton;
    @FXML
    private RadioButton framadateButton;
    @FXML
    private Button backButton;
    @FXML
    private Button submitButton;
    @FXML
    private Label loginLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private TextField loginText;
    @FXML
    private TextField passwordText;
    @FXML
    private Label eventoCASLabel;
    @FXML
    private ChoiceBox<String> eventoCASChoiceBox;
    @FXML
    private Label framadateURLLabel;
    @FXML
    private TextField framadateURLText;

    private String login;
    private String password;
    private String cas;
    private String url;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventoCASChoiceBox.getItems().add("INSA Rennes");
        eventoCASChoiceBox.getItems().add("IRISA");
        eventoCASChoiceBox.getItems().add("INRIA");
    }

    public void getInfos(ActionEvent event) {
        submitButton.setVisible(true);
        if (doodleButton.isSelected()) {
            loginLabel.setVisible(true);
            passwordLabel.setVisible(true);
            loginText.setVisible(true);
            passwordText.setVisible(true);
            eventoCASLabel.setVisible(false);
            eventoCASChoiceBox.setVisible(false);
            framadateURLLabel.setVisible(false);
            framadateURLText.setVisible(false);
        }
        else if (eventoButton.isSelected()) {
            loginLabel.setVisible(true);
            passwordLabel.setVisible(true);
            loginText.setVisible(true);
            passwordText.setVisible(true);
            eventoCASLabel.setVisible(true);
            eventoCASChoiceBox.setVisible(true);
            framadateURLLabel.setVisible(false);
            framadateURLText.setVisible(false);
        }
        else if (framadateButton.isSelected()) {
            loginLabel.setVisible(false);
            passwordLabel.setVisible(false);
            loginText.setVisible(false);
            passwordText.setVisible(false);
            eventoCASLabel.setVisible(false);
            eventoCASChoiceBox.setVisible(false);
            framadateURLLabel.setVisible(true);
            framadateURLText.setVisible(true);
        }
    }

    public void submit(ActionEvent event) {
        if (doodleButton.isSelected()) {
            login = loginText.getText();
            password = passwordText.getText();
            System.out.println(login);
            System.out.println(password);
        }
        else if (eventoButton.isSelected()) {
            login = loginText.getText();
            password = passwordText.getText();
            cas = eventoCASChoiceBox.getValue();
            System.out.println(login);
            System.out.println(password);
            System.out.println(cas);
        }
        else if (framadateButton.isSelected()) {
            url = framadateURLText.getText();
            System.out.println(url);
        }
    }

    public void goBack(ActionEvent event) {
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

