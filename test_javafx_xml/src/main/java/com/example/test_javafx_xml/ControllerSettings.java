package com.example.test_javafx_xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ControllerSettings {

    private Scene previousScene;

    @FXML
    private Button prevPageButton;
    @FXML
    private Label welcomeText;
    @FXML
    private TextField pseudo1;
    @FXML
    private TextField pseudo2;
    @FXML
    private TextField pseudo3;
    @FXML
    private Label listePseudos;
    @FXML
    private Label pollsText;
    @FXML
    private Button nextPageButton;

    private List<HelloApplication.MyPoll> listPolls;        //liste de sondages

    private int indice;             //pour parcourir la liste de sondages
    @FXML
    private Button getPrev;
    @FXML
    private Button getNext;
    @FXML
    private RadioButton yes;
    @FXML
    private RadioButton maybe;
    @FXML
    private RadioButton no;


    @FXML
    protected void onInitButton() {
        welcomeText.setText("Liste réinitialisée");
    }

    public void clearProject1(MouseEvent mouseEvent) {
        pseudo1.clear();
        pseudo2.clear();
        pseudo3.clear();
    }

    public void getPollsAction(MouseEvent mouseEvent) {
        HelloApplication.MyPoll p1 = new HelloApplication.MyPoll("mardi 6","février","2023","14h00","alice","Réu1");
        HelloApplication.MyPoll p2 = new HelloApplication.MyPoll("mercredi 7","mars","2023","15h00","bob","Réu2");
        HelloApplication.MyPoll p3 = new HelloApplication.MyPoll("jeudi 8","avril","2023","16h00","claude","Réu3");
        this.listPolls= new ArrayList<>() ;
        this.listPolls.add(p1);
        this.listPolls.add(p2);
        this.listPolls.add(p3);

        this.indice=0;

        this.pollsText.setText(this.listPolls.get(this.indice).toString());

        //on affiche les possibilités de réponses
        yes.setVisible(true);
        maybe.setVisible(true);
        no.setVisible(true);

        //on affiche le bouton suivant SI il y a plusieurs sondages
        if(this.listPolls.size()>1){
            getNext.setVisible(true);

        }

    }

    public void listePseudosAction(MouseEvent mouseEvent) {
        listePseudos.setText("Liste des pseudos : "+pseudo1.getText()+" "+pseudo2.getText()+" "+pseudo3.getText());
    }

    public void getNextAction(MouseEvent mouseEvent) {
        this.indice++; //on l'implémente avant pour pouvoir faire avec pred aussi
        this.pollsText.setText(this.listPolls.get(this.indice).toString());
        getPrev.setVisible(true);
        if(this.indice==this.listPolls.size()-1){
            getNext.setVisible(false);
        }
    }

    public void getPreviousAction(MouseEvent mouseEvent) {
        this.indice--;
        this.pollsText.setText(this.listPolls.get(this.indice).toString());
        if(this.indice==0){
            getPrev.setVisible(false);
        }
    }

    public void changePageNext(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader2 = new FXMLLoader(HelloApplication.class.getResource("view2.fxml"));
        Parent root = fxmlLoader2.load();  //peut génerer une exception
        //récupération de la scène actuelle
        Scene currentScene = nextPageButton.getScene();

        //récupération de l'autre controleur
        ControllerView2 nextController = fxmlLoader2.getController();

        //on dit à l'autre controlleur que sa scène précédente sera la scène actuelle
        nextController.setPreviousScene(currentScene);

        //création d'une nouvelle scène
        Scene scene = new Scene(root,700,850);

        //récuperation du stage
        Stage stage = (Stage) nextPageButton.getScene().getWindow();

        //on charge la nouvelle scène
        stage.setScene(scene);

    }

    public void changePagePrev(MouseEvent mouseEvent) {
        Stage stage= (Stage) prevPageButton.getScene().getWindow();
        stage.setScene(previousScene);

    }

    public void setPreviousScene(Scene s) {
        this.previousScene=s;
    }
}