
package com.example.noodleapp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class PropsDoodle{
    private TimeZone timeZoneConforme;
    private Props.Date dateConforme;
    private Props.Hour hourDebutConforme;
    private Map<String, pollAnswer> eachAnswer = new HashMap<>(); //cette map aura comme information que mon choix vu que je prend pas en compte l'avis de tout le monde
    private String timeZone;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String nbrDeVotant = "0";
    private Reponse reponse = Reponse.ATTENTE;

    public TimeZone getTimeZoneConforme() {
        return timeZoneConforme;
    }



    public void setTimeZoneConforme(TimeZone timeZoneConforme) {
        this.timeZoneConforme = timeZoneConforme;
    }

    public Props.Date getDateConforme() {
        return dateConforme;
    }

    public void setDateConforme(Props.Date dateConforme) {
        this.dateConforme = dateConforme;
    }

    public Props.Hour getHourDebutConforme() {
        return hourDebutConforme;
    }

    public void setHourDebutConforme(Props.Hour hourDebutConforme) {
        this.hourDebutConforme = hourDebutConforme;
    }

    public Map<String, pollAnswer> getEachAnswer() {
        return eachAnswer;
    }

    public void addAnswer(String s , pollAnswer pollAnswer) {
        eachAnswer.put(s,pollAnswer);
    }

    public Reponse getReponse() {
        return reponse;
    }

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
    }

    public PropsDoodle(String timeZone, String date, String heureDebut, String heureFin) {
        this.timeZone = timeZone;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public PropsDoodle() {
    }

    public String getNbrDeVotant() {
        return nbrDeVotant;
    }

    public void setNbrDeVotant(String nbrDeVotant) {
        this.nbrDeVotant = nbrDeVotant;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }



}

