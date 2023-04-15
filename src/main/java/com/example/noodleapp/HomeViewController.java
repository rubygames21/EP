package com.example.noodleapp;

import javafx.beans.value.ChangeListener;


import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;


public class HomeViewController implements ChangeListener<Integer>{

    private ObservableList<Integer> framPollsList;
    private ObservableList<Integer> eventoPollsList;
    private ObservableList<Integer> doodlePollsList;
    @FXML
    private ListView<Integer> listViewFramadate;
    @FXML
    private ListView<Integer> listViewEvento;
    @FXML
    private ListView<Integer> listViewDoodle;

    @FXML
    private VBox centerVBox;
    @FXML
    public void initialize(){       //se lance dès le chargement de la scène automatique

        framPollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11);
        eventoPollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8);
        doodlePollsList = FXCollections.observableArrayList(1,2,3,4,5,6,7,8);

        //ArrayList<FramadatePoll> listPollsFramadate = new ArrayList<>();
        //FramadateScrapper fScrapper = new FramadateScrapper();                          //mais si je créé un scrapper à chaque fois je perd les données


        listViewFramadate.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                //System.out.println("Vous avez sélectionné : " + newValue);
                System.out.println("Mise à jour");
            }
        });

        listViewEvento.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                //System.out.println("Vous avez sélectionné : " + newValue);
                System.out.println("Mise à jour");
            }
        });

        listViewDoodle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                // System.out.println("Vous avez sélectionné : " + newValue);
                System.out.println("Mise à jour");
            }
        });
        /*fPollsList.add(1);
        fPollsList.add(2);
        fPollsList.add(3);
        fPollsList.add(4);
        fPollsList.add(5);*/


        System.out.println("Initialisation ok");

        listViewFramadate.setItems(framPollsList); //cast obligatoire mais pas très légal
        listViewEvento.setItems(eventoPollsList);
        listViewDoodle.setItems(doodlePollsList);


    }


    @Override
    public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
        System.out.println("Changement effectué");
    }
}
