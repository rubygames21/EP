package com.example.noodleapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;

import javafx.scene.image.Image;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;


public class HomeViewController implements ChangeListener<Integer> {

    private User user;
    private ObservableList<String> framPollsList;


    private ObservableList<Integer> eventoPollsList;
    private ObservableList<Integer> doodlePollsList;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private ListView<Integer> listViewEvento;
    @FXML
    private ListView<Integer> listViewDoodle;
    @FXML
    private Label timeSync;
    @FXML
    private VBox centerVBox;
    @FXML
    private ImageView logo;

    @FXML
    private RadioButton mergeButton;

    @FXML
    private MenuItem menuNewUser;

    public void initialize() throws IOException {       //se lance dès le chargement de la scène automatique
        //image
        //Image jaune = new Image(HomeViewController.class.getRessource("serpent_ia2.png").toExternalForm());




        this.user = HelloApplication.getUser();

        //création de la treeView

        TreeItem<String> rootItem = new TreeItem<>();   //création de la racine, un TreeItem "virtuel" qu'on cache
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.getRoot().setExpanded(true);  //on veut que les enfants de la racine soient toujours visibles, pas besoin de cliquer pour les voir


        //remplissage de la TreeView à partir de chaque scrapper


        int nbScrapper = -1;     //utile pour rerouver l'indice des scrapper dans le .getChildren() du treeView

        //traitement du FramadateScrapper

        if(user.framadateScrapper!=null){
            nbScrapper++;

            //ajout du scrapper à la treeView
            treeView.getRoot().getChildren().add(new TreeItem<>("Framadate"));

            //création liste de sondages
            ArrayList<String> listeFram = new ArrayList<>();

            for (FramadatePoll fp : user.getFramadateScrapper().getFpolls()) {
                fp.title = fp.getTitle(user.webClient);

                //récupération des props
                fp.fillPoll(user.webClient); //permet de remplir l'attribut prop --> à changer
                String tmp = "[";
                for (Props p : fp.getProps()) {
                    pollAnswer myVote = fp.getMyVote(p, user.framadateScrapper.names);
                    //on ne garde que les oui et peut etre
                    if (myVote == pollAnswer.Yes || myVote == pollAnswer.Maybe) {
                        tmp += p.date.dateDisplay() + ", " + p.hour.hourDisplay() + " : " + myVote + " - ";
                    }
                }
                //on enlève le dernier espace en trop
                tmp = tmp.substring(0, tmp.length() - 3);
                tmp += "]";
                String display = fp.getTitle() + " " + tmp;
                listeFram.add(display);
            }
            //tri par ordre alphabétique
            Collections.sort(listeFram);

            //ajout à SONDAGES FRAMADATE
            for (String s : listeFram) {
                treeView.getRoot().getChildren().get(nbScrapper).getChildren().add(new TreeItem<>(s));
            }
        }

        //traitement des autres scrapper (hors Framadate)

        for(Scrapper scrapper : user.scrappers){
            nbScrapper++;

            //cas Doodle

            if(scrapper instanceof Doodle){

                //ajout du scrapper à la TreeView
                String nameScrapper = ((Doodle) scrapper).display();        // pour savoir comment l'appeler sur l'affichage
                treeView.getRoot().getChildren().add(new TreeItem<>(nameScrapper));

                //création liste de sondages
                ArrayList<String> listeDoodle = new ArrayList<>();      //liste des sondages

                for (DoodlePoll dp : ((Doodle)user.scrappers.get(nbScrapper-1)).getReunionsGarde()) {  //reunionGarde supprime déjà les "non"

                    //on recup les prop
                    String tmp = "[";
                    for (PropsDoodle p : dp.getPropsDoodleList()) {       //prop ou propdoodle
                        //valeur de notre réponse
                        pollAnswer myVote = p.getEachAnswer().get("MOI");      //bizzar l'implementtation de PropDoodle avec une map à un seul couple
                        tmp += p.getDateConforme().dateDisplay() + ", " + p.getHourDebutConforme().hourDisplay() + " : " + myVote + " - ";
                    }
                    //on enlève le dernier espace en trop
                    tmp = tmp.substring(0, tmp.length() - 3);
                    tmp += "]";
                    String display = dp.gettitle() + " " + tmp;
                    listeDoodle.add(display);
                }
                //tri par ordre alphabétique
                Collections.sort(listeDoodle);

                //on ajoute chaque sondage à la treeView
                for(String s: listeDoodle){
                    treeView.getRoot().getChildren().get(nbScrapper).getChildren().add(new TreeItem<>(s));
                }
            }

            //cas Evento

            if (scrapper instanceof EventoScrapper){

                //ajout du scrapper à la TreeView
                String nameScrapper = ((EventoScrapper) scrapper).display();        // pour savoir comment l'appeler sur l'affichage
                treeView.getRoot().getChildren().add(new TreeItem<>(nameScrapper));

                //création liste de sondages
                ArrayList<String> listeEvento   = new ArrayList<>();      //liste des sondages

                for (EventoPoll ep : ((EventoScrapper)user.scrappers.get(nbScrapper-1)).getEvento()) {  //reunionGarde supprime déjà les "non"

                    //on recup les prop
                    String tmp = "[";
                    for (Props p : ep.props) {
                        //valeur de notre réponse
                        pollAnswer myVote = p.eachAnswer.get(((EventoScrapper) scrapper).name);      //bizzar l'implementtation de prop avec une map à un seul couple
                        tmp += p.date.dateDisplay() + ", " + p.hour.hourDisplay() + " : " + myVote + " - ";
                    }
                    //on enlève le dernier espace en trop
                    tmp = tmp.substring(0, tmp.length() - 3);
                    tmp += "]";
                    String display = ep.title + " " + tmp;
                    listeEvento.add(display);
                }
                //tri par ordre alphabétique
                Collections.sort(listeEvento);

                //on ajoute chaque sondage à la treeView
                for(String s: listeEvento){
                    treeView.getRoot().getChildren().get(nbScrapper).getChildren().add(new TreeItem<>(s));
                }
            }

            //TODO : à faire
        }









        System.out.println("Initialisation ok");


        //gestion du temps de synchronisation

        final int[] timer = {3703};
        Timeline loop4 = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            public void handle(ActionEvent arg) {
                timer[0]--;
                int hour = (int) timer[0] / 3600;  //on prend la partie entière
                int minutes = (int) (timer[0] - hour * 3600) / 60;     //encore la partie entière
                int seconds = (timer[0] - hour * 3600 - minutes * 60);
                String time = "";
                if (hour != 0) {
                    time += hour + " h ";
                }
                if (minutes != 0) {
                    time += minutes + " m ";
                }
                if (seconds != 0) {
                    time += seconds + " s ";
                }
                timeSync.setText("Prochaine sync :  " + time);
            }
        }));
        loop4.setCycleCount(Timeline.INDEFINITE);
        loop4.play();


    }

    @Override
    public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
        System.out.println("Changement effectué");
    }


    public void mergeICS(MouseEvent actionEvent) {
        if (mergeButton.isSelected()) {
            user.setMergeICS(true);
            System.out.println(user.getMergeICS());
        } else {
            user.setMergeICS(false);
            System.out.println(user.getMergeICS());


        }
    }

    public void switchSceneToNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newUserView.fxml"));
            Stage stage = (Stage) timeSync.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchSceneToManageAccounts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageAccountsView.fxml"));
            Stage stage = (Stage) timeSync.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToByTheWay(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("byTheWay.fxml"));
            Stage stage = (Stage) timeSync.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}