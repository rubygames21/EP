module com.example.test_javafx_xml {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mnode.ical4j.core;
    requires htmlunit;
    requires htmlunit.xpath;
    requires selenium.api;
    requires selenium.chrome.driver;
    requires org.apache.commons.lang3;
    requires java.logging;


    opens com.example.noodleapp to javafx.fxml;
    exports com.example.noodleapp;
}