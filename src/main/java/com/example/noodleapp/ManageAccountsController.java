package com.example.noodleapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ManageAccountsController {

    @FXML
    private ListView<String> accountListView;
    @FXML
    private Button modifyButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button backButton;

    private ObservableList<String> accountList;

    public void initialize() {
        // Initialize the account list
        //accountList = FXCollections.observableArrayList("Account 1", "Account 2", "Account 3");
        ArrayList<String> stringArrayList = new ArrayList<>();
        for(Scrapper scrapper : HelloApplication.user.scrappers){
            stringArrayList.add(scrapper.display());

        }

        accountList = FXCollections.observableArrayList(stringArrayList);

        // Bind the account list to the ListView
        accountListView.setItems(accountList);

        // Add a listener to enable/disable the buttons based on selection
        accountListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isItemSelected = newValue != null;
            modifyButton.setDisable(!isItemSelected);
            deleteButton.setDisable(!isItemSelected);
        });
    }

    public void modifyAccount(ActionEvent event) {
        String selectedAccount = accountListView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            // Handle modify logic here
            System.out.println("Modifying account: " + selectedAccount);
        }
    }

    public void deleteAccount(ActionEvent event) {
        String selectedAccount = accountListView.getSelectionModel().getSelectedItem();
        System.out.println(selectedAccount);
        if (selectedAccount != null) {
            // Handle delete logic here
            System.out.println("Deleting account: " + selectedAccount);
            HelloApplication.user.scrappers.remove(selectedAccount);
        }
    }

    public void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("homeView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
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
