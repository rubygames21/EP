package com.example.test_javafx_xml;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ControllerView2 {
    private Scene previousScene;

    @FXML
    private Button prevPageButton;

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void changePagePrev(MouseEvent mouseEvent) {
        Stage stage = (Stage) prevPageButton.getScene().getWindow();
        stage.setScene(this.previousScene);
    }
}
