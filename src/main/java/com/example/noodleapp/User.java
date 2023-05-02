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
        // remove warnings of Htmlunit
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        this.webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setThrowExceptionOnScriptError(false); // remove javascript errors
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);

        this.scrappers = new ArrayList<>();
    }

    public void createAllICS(String path) throws IOException {
        if(framadateScrapper == null){
            System.out.println("add a FScrapper before creating ics");
        }
        else {
            if(mergeICS) {
                framadateScrapper.createAndMergeAllICS(webClient, path);
            }else{
                framadateScrapper.createICS(webClient, path);

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

    public void addUser(Scrapper user) {
        this.scrappers.add(user);
    }

    public void deleteUser(Scrapper user) {
        this.scrappers.remove(user);
    }

    public void start() throws Exception {
        for(Scrapper s : this.scrappers) {
            if(s instanceof EventoScrapper e) {
                webClient.getOptions().setJavaScriptEnabled(true);
                webClient.getOptions().setUseInsecureSSL(true);
                e.connect(webClient);
                e.getNameEmail(webClient);
                e.getPolls(webClient);
                e.createICS(webClient,this.pathFiles);
            }
            if (s instanceof FramadateScrapper) {
                //DO SOMETHING
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        User user = new User();
        //user.path = "C:\Users\HDrag\Documents\GitHub\EP\";
        user.mergeICS = true;
        *//*user.addFPoll("https://framadate.org/hQXzCKULUtih3S3m");*//*
        user.addFPoll("https://framadate.org/WKrK67UXBcJNRRHf");
        user.addFPoll("https://framadate.org/eU7TWnTBo9TmHUcL");
        user.addFName("jeanM");
        user.addFName("jean");
        user.createAllICS();
    }*/
    public static void main(String[] args) throws Exception {
        User user = new User();
        user.pathFiles = "C:\\Users\\HDrag\\Documents\\GitHub\\EP\\";
        EventoScrapper e = new EventoScrapper("atharrea","fdqj8t,\\g(","INSA Rennes");
        user.addUser(e);
        user.start();
    }
    /*
    // remove warnings of Htmlunit
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        // Initialize the client
        WebClient client= new WebClient(BrowserVersion.FIREFOX);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false); // remove javascript errors
        EventoScrapper e = new EventoScrapper();
        e.connect(client,"atharrea","fdqj8t,\\g(","INSA Rennes");
        e.getNameEmail(client);
        e.getPolls(client);
        String path = "C:\\Users\\HDrag\\Documents\\GitHub\\EP\\";
        e.createICS(client,path);
        client.close();
     */
}
