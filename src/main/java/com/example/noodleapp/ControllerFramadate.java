package com.example.noodleapp;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerFramadate {


    private User user;
    private Scene previousScene;

    @FXML
    private Button backButton;
    @FXML
    private TextField urlFramadateTextField;

    @FXML
    private TextField nameFramadateTextField;

    @FXML
    private Label confirmationAdd;
    @FXML
    private RadioButton mergeYes;

    public void setUser(User user) {
        this.user = user;
    }

    public void backAction(MouseEvent mouseEvent) {
        Stage stage= (Stage) backButton.getScene().getWindow();
        stage.setScene(previousScene);
    }

    public void setPreviousScene(Scene s) {
        this.previousScene=s;
    }

    public void addUrl(MouseEvent mouseEvent) {
        String url=urlFramadateTextField.getText();
        urlFramadateTextField.clear();
        user.addFPoll(url);
        confirmationAdd.setVisible(true);
        confirmationAdd.setText("     L'url    \n"+url+"\na bien été ajouté");   //il faudra se servir de l'url pour l'inserer
    }

    public void addName(MouseEvent mouseEvent){
        String name = nameFramadateTextField.getText();
        nameFramadateTextField.clear();
        user.addFName(name.toLowerCase());
        confirmationAdd.setVisible(true);
        confirmationAdd.setText("     Le nom    \n"+name+"\na bien été ajouté");   //il faudra se servir de l'url pour l'inserer
    }

    public void createICS(MouseEvent mouseEvent) throws IOException {
        user.createAllICS();
    }

    public void setBoolMergeYes(MouseEvent mouseEvent){
        user.mergeICS=mergeYes.isSelected();
    }
}
