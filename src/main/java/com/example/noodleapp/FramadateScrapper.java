package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FramadateScrapper extends Scrapper {

    List<FramadatePoll> fpolls;
    //un set comme ca pas de doublon
    Set<String> names;


    public FramadateScrapper() {
        this.fpolls = new ArrayList<>();
        this.names = new HashSet<>();
    }

    public FramadateScrapper(Set<String> names) {
        this.fpolls = new ArrayList<>();
        this.names = new HashSet<>();
        this.names = names;
    }

    public void addFPoll(String url) {
        fpolls.add(new FramadatePoll(url));
    }

    @Override
    public void createICS(WebClient webClient) throws IOException {
        if (names.isEmpty()) {
            System.out.println("pas de noms donc impossible de crée un ICS");
        } else {
            for (FramadatePoll fpoll : fpolls) {
                fpoll.createICS(webClient, names);
            }
        }
    }

    public void addName(String name) {
        names.add(name);
    }

    public void createAndMergeAllICS(WebClient webClient)throws IOException{
        if (names.isEmpty()) {
            System.out.println("pas de noms donc impossible de crée un ICS");
        }else {
            if (fpolls.size() == 1) {
                createICS(webClient);
            } else {
                FramadatePoll framadatePoll = new FramadatePoll();
                System.out.println("dommage2");
                framadatePoll.name = "mergeICS";
                framadatePoll.title = "mergeICS";
                framadatePoll.props = new ArrayList<>();
                int nbpoll = fpolls.size();
                System.out.println(nbpoll);
                for (FramadatePoll fPoll : fpolls) {
                    fPoll.fillPoll(webClient);
                    framadatePoll.addProps(fPoll.props);
                    for(Props p:framadatePoll.props){
                        System.out.println(p.toString());
                    }
                }
                framadatePoll.justCreateICS(webClient, names);
            }
        }
    }
}

    /*public static void main(String[] args) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        //sinon ca ne marche pas
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        FramadateScrapper framadateScrapper = new FramadateScrapper();

        framadateScrapper.addName("jean");
        framadateScrapper.fpolls.add(new FramadatePoll("https://framadate.org/hQXzCKULUtih3S3m"));
        framadateScrapper.createICS(webClient);
        }
    }*/

