package org.example;

import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;

public interface pollWebSite {

    public abstract HtmlPasswordInput getHtmlPasswordInput(HtmlPage p);
    public HtmlPage login(HtmlInput email, String sEmail, HtmlPasswordInput password, String sPwd, HtmlButton buttonLogin) throws IOException;
    public void newDisplay();
    public List<HtmlElement> getAllElement(HtmlPage p, Boolean diplayElement);
    public void displayHtmlPage(HtmlPage p);


}
