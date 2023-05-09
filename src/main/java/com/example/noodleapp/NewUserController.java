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
import java.text.ParseException;
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
    private PasswordField passwordText;
    @FXML
    private Label eventoCASLabel;
    @FXML
    private TextField eventoCAS;
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
    private static User user;

    public void setUser(User user) {
        user = user;
    }

    public static User getUser(){
        return user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       // eventoCASChoiceBox.getItems().add("INSA Rennes");
       // eventoCASChoiceBox.getItems().add("IRISA");
        //eventoCASChoiceBox.getItems().add("INRIA");
        setUser(HelloApplication.getUser());
        System.out.println("rentré dans initialize");
    }

    public void getInfos(ActionEvent event) {
        if (doodleButton.isSelected()) {
            loginLabel.setVisible(true);
            passwordLabel.setVisible(true);
            loginText.setVisible(true);
            passwordText.setVisible(true);
            eventoCASLabel.setVisible(false);
            eventoCAS.setVisible(false);
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
            eventoCAS.setVisible(true);
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
            eventoCAS.setVisible(false);
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

    public void submit(ActionEvent event) throws Exception {
        if (doodleButton.isSelected()) {
            //récupération des données
            login = loginText.getText();
            password = passwordText.getText();

            //création du scrapper
            Doodle scrapperDoodle = new Doodle();
            scrapperDoodle.mainDoodle(login,password);
            HelloApplication.user.scrappers.add(scrapperDoodle);

            confirmation.setText("Compte Doodle ajouté");
            confirmation.setVisible(true);

            loginText.clear();
            passwordText.clear();
            System.out.println(login);
            System.out.println(password);
        }
        else if (eventoButton.isSelected()) {
            //récupération des données
            login = loginText.getText();
            password = passwordText.getText();
            cas = eventoCAS.getText();

            //création du scrapper
            EventoScrapper scrapperEvento = new EventoScrapper("cadol","myPassword","INSA Rennes");
            scrapperEvento.start(HelloApplication.user.webClient);
            HelloApplication.user.scrappers.add(scrapperEvento);

            confirmation.setText("Compte Evento ajouté");
            confirmation.setVisible(true);

            loginText.clear();
            passwordText.clear();
            eventoCAS.clear();

            System.out.println(login);
            System.out.println(password);
            System.out.println(cas);
        }
        System.out.println(HelloApplication.user.scrappers);
    }

    public void framadateSubmitName(ActionEvent event) {
        name = framadateNameText.getText();
        framadateNameText.clear();
        HelloApplication.user.addFName(name);
        confirmation.setText("Nom ajouté");
        confirmation.setVisible(true);
        System.out.println(HelloApplication.user.framadateScrapper.names.toString());
    }

    public void framadateSubmitURL(ActionEvent event) {
        url = framadateURLText.getText();
        framadateURLText.clear();
        HelloApplication.user.addFPoll(url);
        confirmation.setText("URL ajouté");
        confirmation.setVisible(true);
        System.out.println(HelloApplication.user.framadateScrapper.toString());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homeView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchSceneToNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newUserView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchSceneToManageAccounts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageAccountsView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

