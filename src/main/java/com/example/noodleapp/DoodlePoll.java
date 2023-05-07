package com.example.noodleapp;

import com.example.noodleapp.Poll;
import com.example.noodleapp.Props;
import com.example.noodleapp.PropsDoodle;
import net.sourceforge.htmlunit.xpath.operations.Or;

import java.util.ArrayList;
import java.util.List;

public class DoodlePoll extends Poll {
    private String link;
    private String duree;
    private String localisation = "Pas de localisation précisé";
    private String Organisateur;
    private String title;
    private boolean toujourValable = true;
    private List<PropsDoodle> propsDoodleList = new ArrayList<PropsDoodle>();
    private List<Props> props = new ArrayList<>();

    public List<Props> getProps() {
        return props;
    }

    public List<PropsDoodle> getPropsDoodleList(){
        return propsDoodleList;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String gettitle() {
        return this.title;
    }

    public void settitle(String title) {
        this.title = title;
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

    public String getOrganisateur(){
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

