package com.example.test_javafx_xml;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
public class ControllerFramadate {

    private Scene previousScene;

    @FXML
    private Button backButton;
    @FXML
    private TextField urlFramadateTextField;

    @FXML
    private Label confirmationAdd;


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
        confirmationAdd.setVisible(true);
        confirmationAdd.setText("     L'url    \n"+url+"\na bien été ajouté");   //il faudra se servir de l'url pour l'inserer
    }
}
