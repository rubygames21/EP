package org.example;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import net.sourceforge.htmlunit.xpath.operations.Bool;

import java.io.*;
import java.util.List;

public class Main {


    public static void main(String[] args) throws IOException {
        //creation web client
        Client client = new Client();
        WebClient webClient = client.creatWebClient(BrowserVersion.CHROME,false,false,false);
        //creation framadate
        Framadate framadate = new Framadate();
        //on recupere les pages des 2 sondages de framadate
        HtmlPage page2 = webClient.getPage(framadate.getBasedUrlOpen());
        //on telecharge et sauvegarde le fichier de la reunion en csv
        framadate.saveCSV(webClient,true,"noodleEP",framadate.getBasedUrlOpen());
        //framadate.createIcs();
        framadate.modifyFileCSV("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunion.csv","Jean");
        webClient.close();
        //framadate.ocnvertCSVInICS("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunion.csv");
    }
}