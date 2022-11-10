package org.example;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.*;
import java.util.List;

public class Framadate extends pollWebSiteClass {

    private HtmlPage page;

    //has password to vote and see results
    private final String basedUrlOpen = "https://framadate.org/vKHnLdYOCjWaOkf2";

    //hasn't password to vote and see results
    private final String basedUrlClose = "https://framadate.org/QgaAJfOmZzBae9Tb";
    public HtmlPage getPage() {
        return page;
    }
    public void setPage(HtmlPage page) {
        this.page = page;
    }

    public HtmlPasswordInput getHtmlPasswordInput(HtmlPage p){
        return null;
    }
    public String getBasedUrlOpen() {
        return basedUrlOpen;
    }

    public String getBasedUrlClose() {
        return basedUrlClose;
    }


    //my methods

    public String getPollID(String url){
        String s="";
        int cpt =0;
        for (int i = 0; i < url.length()-1; i++){
            if(url.charAt(i) == '/'){
               cpt++;
            }
            if(cpt ==3){
                s+= url.charAt(i+1);
            }
        }
        return s;
    }
    @Override
    public HtmlButton getHtmlButton(HtmlPage p) {
        return null;
    }

    @Override
    public HtmlInput getHtmlInput(HtmlPage p) {
        return null;
    }

    public void saveCSV(WebClient webClient,Boolean asPassword,String pswd) throws IOException {
        setPage(webClient.getPage(getBasedUrlOpen()));
        if(asPassword){
            HtmlInput pwdInput = (HtmlInput) page.getElementByName("password");
            pwdInput.type(pswd);
            List<HtmlElement> elements = getAllElement(page,false);
            setPage(elements.get(elements.size()-1).click());
        }
        HtmlAnchor a = page.getAnchorByHref("https://framadate.org/exportcsv.php?poll="+getPollID(getBasedUrlOpen()));
        Page p = a.click();
        InputStream is = p.getWebResponse().getContentAsStream();
        File reunionCSV = new File("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunion.csv");
        FileWriter writer = new FileWriter(reunionCSV);
        BufferedWriter bf = new BufferedWriter(writer);
        int b = 0;
        while ((b = is.read()) != -1) {
            bf.write((char)b);
        }
        bf.close();
    }

    public void createIcs(){
        File reunionICS = new File("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunion.ics");
        File reunionCSV = new File("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunion.csv");
        if(!reunionICS.exists()){
            try{
                reunionICS.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try{
            FileWriter writer = new FileWriter(reunionICS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(reunionCSV),"UTF-8"));
            BufferedWriter bf = new BufferedWriter(writer);
            String line = reader.readLine();
            while(line!=null){
               bf.write(line);
               bf.newLine();
               line = reader.readLine();
            }
            reader.close();
            bf.close();
        }catch (IOException e ){
            e.printStackTrace();
        }
    }
}
