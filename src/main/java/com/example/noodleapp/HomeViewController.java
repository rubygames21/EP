package com.example.noodleapp;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;


import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class HomeViewController implements ChangeListener<Integer>{

    private ObservableList framPollsList;
    private ObservableList<Integer> eventoPollsList;
    private ObservableList<Integer> doodlePollsList;
    @FXML
    private ListView<Integer> listViewFramadate;
    @FXML
    private ListView<Integer> listViewEvento;
    @FXML
    private ListView<Integer> listViewDoodle;

    @FXML
    private Label timeSync;

    @FXML
    private VBox centerVBox;
    @FXML
    public void initialize(){       //se lance dès le chargement de la scène automatique

        //recupération de la liste de sondages dans FramadateSrapper
        FramadateScrapper fscrapper=new FramadateScrapper();
        ArrayList liste = (ArrayList) fscrapper.getFpolls();

        //ajout de quelques sondages
        framPollsList = FXCollections.observableArrayList(liste);
        //framPollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11);

        eventoPollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8);
        doodlePollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8);

        //ArrayList<FramadatePoll> listPollsFramadate = new ArrayList<>();
        //FramadateScrapper fScrapper = new FramadateScrapper();                          //mais si je créé un scrapper à chaque fois je perd les données


        listViewFramadate.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                //System.out.println("Vous avez sélectionné : " + newValue);
                System.out.println("Vous avez sélectionné : " + t1);
            }
        });

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

        listViewFramadate.setItems(framPollsList);
        listViewEvento.setItems(eventoPollsList);
        listViewDoodle.setItems(doodlePollsList);
    //gestion du temps de synchronisation

        final int[] timer = {3703};
        Timeline loop4 = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>(){
            public void handle(ActionEvent arg) {
                timer[0]--;
                int hour = (int)timer[0]/3600;  //on prend la partie entière
                int minutes = (int)(timer[0]-hour*3600)/60;
                int seconds = (timer[0]-hour*3600-minutes*60);
                String time = "";
                if(hour!=0){
                    time+=hour+" h ";
                }
                if(minutes!=0){
                    time+=minutes+" m ";
                }
                if(seconds!=0){
                    time+=seconds+" s ";
                }
                timeSync.setText("Temps restant avant synchronisation :  " + time);
            }
        }));
        loop4.setCycleCount(Timeline.INDEFINITE);
        loop4.play();



    }


    @Override
    public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
        System.out.println("Changement effectué");
    }
}
