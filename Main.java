package org.example;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException, ParseException, URISyntaxException {
        Doodle doodle = new Doodle();
        doodle.mainDoodle("hamza.azeroual@insa-rennes.fr", "Heyhey12*");
        CreateCalendar createCalendar = new CreateCalendar();
        createCalendar.mainCreateCalendar(doodle.getReunions());

    }

}