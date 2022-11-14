package org.example;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class Client {

    //class pas forcement obligatoire
    public WebClient creatWebClient(BrowserVersion browserVersion, Boolean js, Boolean css, Boolean ssl){
        WebClient client = new WebClient(browserVersion);
        client.getOptions().setJavaScriptEnabled(js);
        client.getOptions().setCssEnabled(css);
        client.getOptions().setUseInsecureSSL(ssl);
        return client;
    }


}
