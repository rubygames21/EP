package org.example;

public class ReunionDoodle {

    private Reponse reponse;
    private String link;
    private String date;
    private String heureDepart;
    private String heureFin;
    private String duree;
    private String localisation = "Pas de localisation précisé";
    private String Organisateur;
    private String Nom;
    private boolean toujourValable;


    public Reponse getReponse() {
        return reponse;
    }

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNom() {
        return Nom;
    }

    public void setNom(String nom) {
        Nom = nom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getOrganisateur() {
        return Organisateur;
    }

    public void setOrganisateur(String organisateur) {
        Organisateur = organisateur;
    }

    public boolean isToujourValable() {
        return toujourValable;
    }

    public void setToujourValable(boolean toujourValable) {
        this.toujourValable = toujourValable;
    }
}
