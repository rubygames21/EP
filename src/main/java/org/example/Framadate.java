package org.example;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

@SuppressWarnings("LanguageDetectionInspection")
public class Framadate extends pollWebSiteClass {

    private HtmlPage page;
    //pourquoi pas faire une Map avec les url associés à sont nom utilisé pour le sondages
    private List<String> myAllName;

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

    //fonction qui récuprère l'ID du sondage (la chaine de caractère après le dernier soit 3eme "/")
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

    //fonction qui à d'un WebCLien, d'un boolean qui dis si la page contient un mot de passe, du mot de passe , et d'un url d'un sondage
    //crée un fichier .csv qui contient l'export du site
    public void saveCSV(WebClient webClient,Boolean asPassword,String pswd,String url) throws IOException {
        setPage(webClient.getPage(url));
        if(asPassword){
            HtmlInput pwdInput = (HtmlInput) page.getElementByName("password");
            pwdInput.type(pswd);
            List<HtmlElement> elements = getAllElement(page,false);
            setPage(elements.get(elements.size()-1).click());
        }
        HtmlAnchor a = page.getAnchorByHref("https://framadate.org/exportcsv.php?poll="+getPollID(url));
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

    //fonction qui à partir d'un export(file .csv) de framadate cree un nouveau fichier qui ne contient que
    // les infos qui le concerne :
    //      - les deux première ligne (jours et heures)
    //      - sa ligne qui commence par son nom utiliser pour le sondage puis ses différentes réponses
    public void modifyFileCSV(String pathFileToModify,String myName){
        //on ajoute son nom à la list de nom
        myAllName = new ArrayList<>();
        myAllName.add(myName);
        File reunionCSV = new File(pathFileToModify);
        File reunionModifyCsv = new File("/home/ubuntu/PERSO.local/INSA/3A/EP/workspaceEP/HtmlUnit/reunionModify.csv");
        if(!reunionModifyCsv.exists()){
            try{
                reunionModifyCsv.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try{
            FileWriter writer = new FileWriter(reunionModifyCsv);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(reunionCSV),"UTF-8"));
            BufferedWriter bf = new BufferedWriter(writer);
            String line = reader.readLine();
            //ecrire les deux premières ligne qui sont :
            // - les dates du sondage
            // - les horaires rescpectives des dates
            line.substring(1); //eneleve la première virgule ce qui facilite le traitement de la ligne avec le String.slipt
            bf.write(line);
            bf.newLine();
            line = reader.readLine();
            line.substring(1);
            bf.write(line);
            bf.newLine();
            line = reader.readLine();
            //Les ligne suivante d'un fichier CSV sont les différentes réponses pour chaque propositions de tout les votants
            //la ligne commence par "unNom","sesRéponses"
            boolean find = false;
            //pour toute les lignes qui reste et si on à toujours pas trouver sa ligne
            while(line!=null&&!find) {
                //pour tout les nom de sa liste de nom on regarde si la ligne.substring(1) (car je crois
                // quelle comence par \") commence par son nom dans ce cas on ecrit la ligne et on sort
                for (String name : myAllName) {
                    System.out.println(name.toLowerCase());
                    if (line.substring(1).startsWith(name.toLowerCase())) {
                        bf.write(line);
                        find = true;
                        break;
                    }
                }
                line=reader.readLine();
            }
            reader.close();
            bf.close();
        }catch (IOException e ){
            e.printStackTrace();
        }
    }

    //ne marche pas encore et n'est pas forcement obligatoire si on passe par un convertisseur en ligne
    public void ocnvertCSVInICS(String pathFile) throws IOException {
        File fileCSV = new File(pathFile);
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileCSV),"UTF-8"));
            String line = reader.readLine();
            line = line.substring(1);
            String[] tabdate = line.split(",");
            //pas sur que ce soit obligatoire
            for(String s : tabdate){
                s.substring(1);
                s.substring(0,s.length()-1);
            }
            //TODO ajouter un verification qu'il y a au moins une date
            int nbDateDiff = 1;
            String buffer = tabdate[0];
            for(int i = 1; i < tabdate.length;i++){
                System.out.println(!buffer.equals(tabdate[i]));
                if(!buffer.equals(tabdate[i])){
                    nbDateDiff++;
                    buffer = tabdate[i];
                }
            }
            String[][] date = new String[2][nbDateDiff];
            buffer = tabdate[0];
            int nbOcc = 1;
            int colonne = 0;
            date[0][0] = buffer;
            date[1][0] = Integer.toString(nbOcc);
            for (int i = 1 ; i < tabdate.length; i++){
                System.out.println(!buffer.equals(tabdate[i]));

            }

            for(int i = 0; i < date.length;i++){
                for(int j= 0; j< date[0].length;j++){
                    System.out.print(date[i][j]+ " / ");
                }
                System.out.println();
            }
            Map<String, Integer> dateNBMap = new HashMap<String,Integer>();
            String buffer2 = tabdate[0];
            dateNBMap.put(buffer2,1);
            System.out.println("______________");
            for(int i = 1; i < tabdate.length;i++){
                System.out.println(dateNBMap.toString());
                if(true){
                    dateNBMap.put(buffer2,1);
                    buffer2 = tabdate[i];
                }
                else {
                    dateNBMap.replace(buffer2,dateNBMap.get(buffer2),dateNBMap.get(buffer2)+1);
                }
            }
            System.out.println("______________");
            for(String s:dateNBMap.keySet()){
                System.out.println(s);
            }

            for(Integer integer:dateNBMap.values()){
                System.out.println(integer);
            }
            line = reader.readLine();
            line = line.substring(1);
            String[] tabHour = line.split(",");

            while(line!=null){
                line = reader.readLine();
                System.out.println(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //fonction qui ne sert pu c'etait pour des tests
    public void createIcsTemp(){
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
