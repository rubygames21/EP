package com.example.noodleapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class User {

    WebClient webClient;
    //plutot qu'une liste de scrapper pk pas une DoodleScrapper/EventoScrapper ...
    List<Scrapper> scrappers;
    FramadateScrapper framadateScrapper;
    String pathFiles;
    Boolean mergeICS;

    public User(){
        this.webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        this.scrappers=new ArrayList<>();
    }

    public void createAllICS() throws IOException {
        if(framadateScrapper == null){
            System.out.println("add a FScrapper before creating ics");
        }
        else {
            if(mergeICS) {
                framadateScrapper.createAndMergeAllICS(webClient,this.pathFiles);
            }else{
                framadateScrapper.createICS(webClient,this.pathFiles);
            }
        }
    }

    public void addFPoll(String url){
        if(framadateScrapper==null){
            framadateScrapper = new FramadateScrapper();
            framadateScrapper.addFPoll(url);
        }
        else{
            framadateScrapper.addFPoll(url);
        }
    }

    public void addFName(String name){
        if(framadateScrapper==null){
            System.out.println("pas de Fscrapper donc impossible d'ajouter un nom au scrapper");
        }
        else{
            framadateScrapper.addName(name);
        }
    }

    public void addFName(Set<String> names){
        if(framadateScrapper==null){
            System.out.println("pas de Fscrapper donc impossible d'ajouter un nom au scrapper");
        }
        else{
            for(String name:names) {
                framadateScrapper.addName(name);
            }
        }
    }

    public FramadateScrapper getFramadateScrapper() {
        return framadateScrapper;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public List<Scrapper> getScrappers() {
        return scrappers;
    }

    public String getPathFiles() {
        return pathFiles;
    }

    public Boolean getMergeICS() {
        return mergeICS;
    }
    public void setMergeICS(Boolean b){
        this.mergeICS=b;
        System.out.println("Merge ICS modifié");
    }

    /*public static void main(String[] args) throws IOException {
        User user = new User();
        user.mergeICS = true;
        *//*user.addFPoll("https://framadate.org/hQXzCKULUtih3S3m");*//*
        user.addFPoll("https://framadate.org/WKrK67UXBcJNRRHf");
        user.addFPoll("https://framadate.org/eU7TWnTBo9TmHUcL");
        user.addFName("jeanM");
        user.addFName("jean");
        user.createAllICS();
    }*/
}
