package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.WebClient;

import java.io.IOException;

public abstract class Scrapper {


    abstract public void createICS(WebClient wb,String path) throws IOException;

    abstract public String display();
}
