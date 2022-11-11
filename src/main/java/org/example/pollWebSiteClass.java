package org.example;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;

public abstract class pollWebSiteClass {
    public HtmlPage login(HtmlInput email, String sEmail, HtmlPasswordInput password,
                          String sPwd, HtmlButton buttonLogin) throws IOException{
        email.type(sEmail);
        password.type(sPwd);
        return buttonLogin.click();
    }
    public void newDisplay(){
        System.out.println("--------------------------------------");
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
    public void displayHtmlPage(HtmlPage p){
        System.out.println(p.asXml());
    }
}
