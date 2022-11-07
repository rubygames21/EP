package org.example;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;

public class Main {

    public static String URL = "https://doodle.com/fr/";

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        WebClient webClient = client.creatWebClient(BrowserVersion.CHROME,false,false,false);
        HtmlPage page = webClient.getPage(URL);
        System.out.println(page.asXml());
        HtmlElement element = (HtmlElement) page.getElementById("header-log-in");
        List<HtmlElement> elements = page.getTabbableElements();
        System.out.println("-------------------------------------------------------");
        for(HtmlElement elementI : elements){
            System.out.println(elementI.asXml());
        }
        System.out.println("-------------------------------------------------------");
        HtmlButton buttonLogin = (HtmlButton) elements.get(2);
        System.out.println(buttonLogin.asXml());
        System.out.println("-------------------------------------------------------");
        HtmlPage page2 = buttonLogin.click();
        System.out.println(buttonLogin.click().isHtmlPage());
        System.out.println(page.asXml()==page2.asXml());
        System.out.println(page2.getUrl());
        System.out.println("-------------------------------------------------------");
        System.out.println(buttonLogin.getNameAttribute());
        System.out.println("-------------------------------------------------------");
        /*System.out.println(page2.asXml());
        List<HtmlElement> elements2 = page2.getTabbableElements();
        System.out.println("-------------------------------------------------------");
        for(HtmlElement elementI : elements2){
            System.out.println(elementI.asXml());
            System.out.println("***************************");
        }*/
    }
}