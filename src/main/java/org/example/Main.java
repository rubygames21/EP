package org.example;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import net.sourceforge.htmlunit.xpath.operations.Bool;

import java.io.*;
import java.util.List;

public class Main {

    public static String URL = "https://doodle.com/auth/realms/doodle/protocol/openid-connect/auth?client_id=web-static-site&redirect_uri=https%3A%2F%2Fdoodle.com%2Fdashboard&state=171b57d6-f061-4ad0-8a15-bb14a24972b4&response_mode=fragment&response_type=none&scope=openid&nonce=4ede6f9f-2fbc-455b-80e4-7120f951c5df&code_challenge=s87RYcsrh880q3v6B-2P1sczVh7re2rD9bcJGkISSFE&code_challenge_method=S256";

    public static void main(String[] args) throws IOException {
        //creation web client
        Client client = new Client();
        WebClient webClient = client.creatWebClient(BrowserVersion.CHROME,false,false,false);
        //creation framadate
        Framadate framadate = new Framadate();
        //on recupere les pages des 2 sondages de framadate
        HtmlPage page2 = webClient.getPage(framadate.getBasedUrlOpen());
        //on telecharge et sauvegarde le fichier de la reunion en csv
        framadate.saveCSV(webClient,true,"noodleEP");
        framadate.createIcs();
        webClient.close();
    }
}