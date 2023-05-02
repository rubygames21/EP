module com.example.noodelapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mnode.ical4j.core;
    requires htmlunit;
    requires java.logging;
    requires org.apache.commons.lang3;


    opens com.example.noodleapp to javafx.fxml;
    exports com.example.noodleapp;
}