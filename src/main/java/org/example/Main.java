package org.example;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class Main {

    public static String URL = "https://google.com";

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        WebClient webClient = client.creatWebClient(BrowserVersion.CHROME,false,true,true);
        HtmlPage page = webClient.getPage(URL);
        
    }
}