module com.example.test_javafx_xml {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.test_javafx_xml to javafx.fxml;
    exports com.example.test_javafx_xml;
}