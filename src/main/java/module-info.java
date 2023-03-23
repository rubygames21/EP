module com.example.noodelapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mnode.ical4j.core;
    requires htmlunit;


    opens com.example.noodleapp to javafx.fxml;
    exports com.example.noodleapp;
}