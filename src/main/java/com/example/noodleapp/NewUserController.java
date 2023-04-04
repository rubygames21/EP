package com.example.noodleapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
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
    private TextField loginText;
    @FXML
    private Label passwordLabel;
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
    @FXML
    private Label framadateNameLabel;
    @FXML
    private TextField framadateNameText;
    @FXML
    private Button framadateSubmitURL;
    @FXML
    private Button framadateSubmitName;
    @FXML
    private Label confirmation;
    @FXML
    private RadioButton mergeButton;
    @FXML
    private Button icsButton;

    private String login;
    private String password;
    private String cas;
    private String url;
    private String name;
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventoCASChoiceBox.getItems().add("INSA Rennes");
        eventoCASChoiceBox.getItems().add("IRISA");
        eventoCASChoiceBox.getItems().add("INRIA");
    }

    public void getInfos(ActionEvent event) {
        if (doodleButton.isSelected()) {
            loginLabel.setVisible(true);
            passwordLabel.setVisible(true);
            loginText.setVisible(true);
            passwordText.setVisible(true);
            eventoCASLabel.setVisible(false);
            eventoCASChoiceBox.setVisible(false);
            framadateURLLabel.setVisible(false);
            framadateURLText.setVisible(false);
            framadateNameText.setVisible(false);
            framadateNameLabel.setVisible(false);
            submitButton.setVisible(true);
            framadateSubmitName.setVisible(false);
            framadateSubmitURL.setVisible(false);
            mergeButton.setVisible(false);
            confirmation.setVisible(false);
            icsButton.setVisible(false);
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
            framadateNameText.setVisible(false);
            framadateNameLabel.setVisible(false);
            submitButton.setVisible(true);
            framadateSubmitName.setVisible(false);
            framadateSubmitURL.setVisible(false);
            mergeButton.setVisible(false);
            confirmation.setVisible(false);
            icsButton.setVisible(false);
        }
        else if (framadateButton.isSelected()) {
            User user = new User();
            setUser(user);

            loginLabel.setVisible(false);
            passwordLabel.setVisible(false);
            loginText.setVisible(false);
            passwordText.setVisible(false);
            eventoCASLabel.setVisible(false);
            eventoCASChoiceBox.setVisible(false);
            framadateURLLabel.setVisible(true);
            framadateURLText.setVisible(true);
            framadateNameText.setVisible(true);
            framadateNameLabel.setVisible(true);
            submitButton.setVisible(false);
            framadateSubmitName.setVisible(true);
            framadateSubmitURL.setVisible(true);
            mergeButton.setVisible(true);
            confirmation.setVisible(false);
            icsButton.setVisible(true);
        }
    }

    public void submit(ActionEvent event) {
        if (doodleButton.isSelected()) {
            login = loginText.getText();
            loginText.clear();
            password = passwordText.getText();
            passwordText.clear();
            System.out.println(login);
            System.out.println(password);
        }
        else if (eventoButton.isSelected()) {
            login = loginText.getText();
            loginText.clear();
            password = passwordText.getText();
            passwordText.clear();
            cas = eventoCASChoiceBox.getValue();
            System.out.println(login);
            System.out.println(password);
            System.out.println(cas);
        }
    }

    public void framadateSubmitName(ActionEvent event) {
        name = framadateNameText.getText();
        framadateNameText.clear();
        user.addFName(name.toLowerCase());
        confirmation.setText("Name added");
        confirmation.setVisible(true);
        System.out.println(user.framadateScrapper.names.toString());
    }

    public void framadateSubmitURL(ActionEvent event) {
        url = framadateURLText.getText();
        framadateURLText.clear();
        user.addFPoll(url);
        confirmation.setText("URL added");
        confirmation.setVisible(true);
        System.out.println(user.framadateScrapper.toString());
    }

    public void setBoolMergeYes(ActionEvent event){
        user.mergeICS = mergeButton.isSelected();
        System.out.println(user.mergeICS);
    }

    public void createICS(ActionEvent event) throws IOException {
        user.createAllICS();
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

