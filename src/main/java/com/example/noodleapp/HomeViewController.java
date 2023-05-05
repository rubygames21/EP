package com.example.noodleapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;


import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class HomeViewController implements ChangeListener<Integer> {

    private User user;
    private ObservableList<String> framPollsList;

    private ObservableList<Integer> eventoPollsList;
    private ObservableList<Integer> doodlePollsList;
    @FXML
    private TreeView<String> treeViewFramadate;

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
    public void initialize() throws IOException {       //se lance dès le chargement de la scène automatique

        //chargement de l'image /!\ exception
       /* FileInputStream inputStream=new FileInputStream("@../../../Image/logo.png");
        Image image = new Image(inputStream);
        logo.setImage(image);*/

        //HelloApplication.getUser().setMergeICS(true);

        //recupération de la liste de sondages dans FramadateSrapper

        //on regarde si la classe User a déjà un Framadate scrapper, sinon on en créé un

        //comment récupérer le User existant?
        this.user = HelloApplication.getUser();

        ArrayList<String> listeFram = new ArrayList<>();

        //TODO : mettre ca dans le modèle, pas dans le controleur

        for (FramadatePoll fp : user.getFramadateScrapper().getFpolls()) {
            fp.title = fp.getTitle(user.webClient);

            //on recup les prop
            fp.fillPoll(user.webClient); //permet de remplir l'attribut prop --> à changer
            String tmp = "[";
            for (Props p : fp.getProps()) {
                pollAnswer myVote = fp.getMyVote(p, user.framadateScrapper.names);
                //on ne garde que les oui et peut etre
                if (myVote == pollAnswer.Yes || myVote == pollAnswer.Maybe) {
                    tmp +=  p.date.dateDisplay() + ", "+ p.hour.hourDisplay()+" : " + myVote+" - ";
                }
            }
            //on enlève le dernier espace en trop
            tmp=tmp.substring(0,tmp.length()-3);
            tmp+="]";
            String display = fp.getTitle() + " " + tmp;
            listeFram.add(display);
        }

        //tri par ordre alphabétique
        Collections.sort(listeFram);


    //création de la treeView

        //Créer les TreeItems à partir de la liste
        //création de la racine, un TreeItem "virtuel" qu'on cache
        TreeItem<String> rootItem = new TreeItem<>();
        treeViewFramadate.setRoot(rootItem);
        treeViewFramadate.setShowRoot(false);

        //on veut que les enfants de la racine soient toujours visibles, pas besoin de cliquer pour les voir
        treeViewFramadate.getRoot().setExpanded(true);

        //TODO : mettre une boucle for sur une liste de comptes pour boucler dynamiquement
        //item du "compte" framadate
        ArrayList<String> accountsArrayList = new ArrayList<>();
        accountsArrayList.add("Sondages Framadate");
        accountsArrayList.add("Sondages Evento INSA");
        accountsArrayList.add("Sondages Evento IRISA");
        accountsArrayList.add("Sondages Doodle");


        for(String s : accountsArrayList){
            treeViewFramadate.getRoot().getChildren().add(new TreeItem<>(s));
        }


        //ajout à SONDAGES FRAMADATE
        for (String s : listeFram) {
            treeViewFramadate.getRoot().getChildren().get(1).getChildren().add(new TreeItem<>(s));
        }


        //treeViewFramadate.refresh();


        /*
        framPollsList.forEach(element -> rootItem.getChildren().add(new TreeItem<>((String)element)));
        ;
        //Créer la TreeView et ajouter le TreeItem racine
        treeViewFramadate = new TreeView<>(rootItem);
         */



/*

        listViewEvento.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                //System.out.println("Vous avez sélectionné : " + newValue);
                System.out.println("Vous avez sélectionné : " + t1);
            }
        });

        listViewDoodle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                System.out.println("Vous avez sélectionné : " + t1);

            }
        });
        /*fPollsList.add(1);
        fPollsList.add(2);
        fPollsList.add(3);
        fPollsList.add(4);
        fPollsList.add(5);*/


        System.out.println("Initialisation ok");

        //treeViewFramadate.setItems(framPollsList);
        //listViewEvento.setItems(eventoPollsList);
        //listViewDoodle.setItems(doodlePollsList);
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
}
