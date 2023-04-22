package org.example;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.validate.ValidationException;

import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreateCalendar {
    public void mainCreateCalendar(List<ReunionDoodle> reunionDoodles){
        try {
            // Define timezone
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone timezone = registry.getTimeZone("Europe/Zurich");

// Create calendar
            net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
            calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);

            for(ReunionDoodle reunionDoodle : reunionDoodles) {
                //avoir le format d'une adresse mail
                for (PropsDoodle p : reunionDoodle.getPropsDoodleList()) {
                    try {
                        if (reunionDoodle.getPropsDoodleList().get(0).getReponse().equals(Reponse.ORGANISATEUR)) {
                            VEvent event = createEvent("moi@noodle.com", reunionDoodle.getNom() + " Vous êtes l'organisateur ", reunionDoodle.getLocalisation(), p.getDate(), p.getHeureDebut(), p.getHeureFin(), timezone);
                            event.getProperties().add(new Uid(UUID.randomUUID().toString())); // add UID property
                            calendar.getComponents().add(event);
                        } else {
                            String[] adressMailspliter = reunionDoodle.getOrganisateur().split(" ");
                            String mailAdresse = adressMailspliter[0] + adressMailspliter[1] + "@noodle.com";
                            VEvent event = createEvent(mailAdresse, reunionDoodle.getNom() + " Vous avez dit : " + p.getReponse(), reunionDoodle.getLocalisation(), p.getDate(), p.getHeureDebut(), p.getHeureFin(), timezone);
                            event.getProperties().add(new Uid(UUID.randomUUID().toString())); // add UID property
                            calendar.getComponents().add(event);
                        }
                    } catch (Exception e) {
                        System.out.println("Je n'arrive pas à créer l'évenement");
                    }
                }
            }

// Output the calendar
            CalendarOutputter outputter = new CalendarOutputter();
            FileOutputStream fout = new FileOutputStream("eventsTest0.ics");
            outputter.output(calendar, fout);

        }catch (ValidationException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static VEvent createEvent(String organizer, String summary, String location, String date, String startTime,
                                      String endTime, TimeZone timezone) throws ValidationException, URISyntaxException {
        // Define the event
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.setTimeZone(timezone);
        end.setTimeZone(timezone);
        String[] startDateArray = date.split("-");
        String[] startTimeArray = startTime.split(":");
        String[] endTimeArray = endTime.split(":");
        start.set(Calendar.YEAR, Integer.parseInt(startDateArray[0]));
        start.set(Calendar.MONTH, Integer.parseInt(startDateArray[1])-1);
        start.set(Calendar.DATE, Integer.parseInt(startDateArray[2]));
        start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeArray[0]));
        start.set(Calendar.MINUTE, Integer.parseInt(startTimeArray[1].split(" ")[0]));
        end.set(Calendar.YEAR, Integer.parseInt(startDateArray[0]));
        end.set(Calendar.MONTH, Integer.parseInt(startDateArray[1])-1);
        end.set(Calendar.DATE, Integer.parseInt(startDateArray[2]));
        end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTimeArray[0]));
        end.set(Calendar.MINUTE, Integer.parseInt(endTimeArray[1].split(" ")[0]));
        DateTime startDateTime = new DateTime(start.getTime());
        DateTime endDateTime = new DateTime(end.getTime());
        VEvent event = new VEvent(startDateTime, endDateTime, summary);

        // Set organizer, location, and unique identifier for the event
        UidGenerator ug = new RandomUidGenerator();
        event.getProperties().add(ug.generateUid());
        event.getProperties().add(new Organizer("MAILTO:" + organizer));
        event.getProperties().add(new Location(location));
        event.getProperties().add(new Uid("event12345"));
        return event;
    }



}

