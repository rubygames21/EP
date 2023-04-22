package org.example;

import java.util.ArrayList;
import java.util.List;

public class ReunionDoodle extends Poll{
    private String link;
    private String duree;
    private String localisation = "Pas de localisation précisé";
    private String Organisateur;
    private String Nom;
    private boolean toujourValable = true;
    private List<PropsDoodle> propsDoodleList = new ArrayList<PropsDoodle>();
    public List<PropsDoodle> getPropsDoodleList(){
        return propsDoodleList;
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

