package org.example;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.sourceforge.htmlunit.xpath.operations.Bool;

import java.io.IOException;
import java.util.List;

public class Main {

    public static String URL = "https://doodle.com/auth/realms/doodle/protocol/openid-connect/auth?client_id=web-static-site&redirect_uri=https%3A%2F%2Fdoodle.com%2Fdashboard&state=171b57d6-f061-4ad0-8a15-bb14a24972b4&response_mode=fragment&response_type=none&scope=openid&nonce=4ede6f9f-2fbc-455b-80e4-7120f951c5df&code_challenge=s87RYcsrh880q3v6B-2P1sczVh7re2rD9bcJGkISSFE&code_challenge_method=S256";

    public void displayHtmlPage(HtmlPage p){
        System.out.println(p.asXml());
    }

    public List<HtmlElement> getAllElement(HtmlPage p, Boolean diplayElement){
        List<HtmlElement> elements = p.getTabbableElements();
        if(diplayElement){
            for(HtmlElement elementI : elements){
                System.out.println(elementI.asXml());
                System.out.println("*************************");
            }
        }
        return elements;
    }
    public void newDisplay(){
        System.out.println("--------------------------------------");
    }
    public void main(String[] args) throws IOException {
        Client client = new Client();
        WebClient webClient = client.creatWebClient(BrowserVersion.CHROME,false,false,false);
        HtmlPage page = webClient.getPage(URL);
        displayHtmlPage(page);
        newDisplay();
        List<HtmlElement> elements = getAllElement(page,true);
        HtmlButton buttonLogin = (HtmlButton) elements.get(2);
        System.out.println(buttonLogin.asXml());
        newDisplay();
        HtmlPage page2 = buttonLogin.click();
        System.out.println(buttonLogin.click().isHtmlPage());
        System.out.println(page.asXml()==page2.asXml());
        System.out.println(page2.getUrl());
        newDisplay();
        System.out.println(buttonLogin.getNameAttribute());
        newDisplay();
    }
}