package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EventoScrapper extends Scrapper{

    String name; //format "Prenom Nom"
    String email;
    Set<String> listRep; //list of all the ID of the polls
    List<Poll> evento;

    public EventoScrapper() {
        name = null;
        email = null;
        listRep  = new HashSet<>(); //initialize ID list
        //evento = new ArrayList<>();
    }

    public void connect(WebClient client,String user, String pass, String organization) throws Exception,NullPointerException {
        client.getOptions().setJavaScriptEnabled(true); // nécessaire pour la liste automatique de sélection d'établissement

        HtmlPage page = client.getPage("https://evento.renater.fr/Shibboleth.sso/Login?target=https%3A%2F%2Fevento.renater.fr%2Fhome");
        HtmlForm form = page.getForms().get(0);

        // Ajoute focus
        page = form.getSelectByName("user_idp").click(); // ou getElementById("userIdPSelection")

        // Clique le champ de saisie "Veuillez saisir l'établissement auquel vous appartenez."
        HtmlElement el = page.getFirstByXPath("//*[@id='select2-userIdPSelection-container']/span/div/div[2]");
        try {
            page = el.click();
        } catch (NullPointerException e) {
            System.out.println("bug"); //bug who crash the program (1 times over 3)
            return;
        }
        // Maintenant la liste déroulante est ouverte, et le champ de saisie interactive est vierge

        // On tape les infos du nom d'établissement dans le champ de saisie
        el = page.getFirstByXPath("//input[@class='select2-search__field']"); // le champ SearchInput
        el.type(organization); // on réduit les réponses possibles en spécifiant l'établissement recherché

        // Attend (au max la limite indiquée) que la recherche en javascript finisse de lister les réponses
        if (0 != client.waitForBackgroundJavaScript(5000)) // 5 secondes max
            throw new InterruptedException("Javascript job not finished");

        // Récupère et clique dans la liste la 1re des réponses de forme <div class="select2-result-repository__title">
        el = (HtmlElement) page.getByXPath("//div[@class='select2-result-repository__title']").get(1); // get(1) car il y a avant un autre élément de même nom de classe
        el.click(); // clique sur la réponse => ferme la recherche et la reporte dans le champ de saisie

        // Maintenant le HTML de la liste de sélection s'est étoffé de la réponse cliquée, sous la forme :
        // <option value="https://idp.insa-rennes.fr/idp/shibboleth" data-select2-id="18" selected="selected">
        //   INSA Rennes
        // </option>
        el = page.getFirstByXPath("//*[@id='userIdPSelection']/option[1]"); // la 1re et seule option dans la liste
        System.out.println(el.getAttribute("value"));
        connectCas(client,user, pass, el.getAttribute("value"));
    }

    /** Connects to Evento via a given CAS server (Central Authentication Service). */
    protected void connectCas(WebClient client, String user, String pass, String cas) throws Exception {
        client.getOptions().setJavaScriptEnabled(false); // doit être coupé pour Rennes 1 et Rennes 2 par exemple
        // en fait c'est l'éventuelle page (à tort souvent) que Javascript n'est pas présent
        // et qui, étonnamment, a certaines balises < transformées en &lt; si Javascript activé.
        HtmlPage page = client.getPage(
                "https://evento.renater.fr/Shibboleth.sso/Login?SAMLDS=1&target=https%3A%2F%2Fevento.renater.fr%2Fhome&entityID="
                        + URLEncoder.encode(cas, StandardCharsets.UTF_8.toString()) );

        // Si session déjà connectée avant, on est sur le service sans avoir besoin du CAS
        if (!page.toString().startsWith("https://evento.renater.fr",9)) { // 9 pour éviter "HtmlPage("

            // Remplit les champs du formulaire CAS
            HtmlForm form = page.getForms().get(0);
            form.getInputByName("username").setValueAttribute(user);
            form.getInputByName("password").setValueAttribute(pass);

            // Soumet le formulaire CAS en cliquant
            page = getInputOrButtonByName(form, "submit").click();

            // Si on n'est pas arrivé au service demandé, ça peut être :
            // - une page d'information intermédiaire
            // - une page signalant l'absence de Javascript dans le navigateur
            // - une page d'échec d'identification
            // => pour passer les 2 premiers cas on s'autorise jusqu'à 2 clics supplémentaires
            for(int i=0; i<2 && !page.toString().startsWith("https://evento.renater.fr",9); i++) {
                page = getInputsAndButtons(page.getForms().get(0)).getLast().click();
            }
            if (!page.toString().startsWith("https://evento.renater.fr",9)) {
                throw new Exception("CAS authentication failed");
            }
        }
    }

    public void getPools(WebClient client) throws Exception,NullPointerException{
        // ALLER A LA PAGE DES SONDAGES
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        String[] tempTab = StringUtils.substringsBetween(page.asNormalizedText(), " - ", "Last update of the organiser");
        for(String s : tempTab) {
            String id = StringUtils.substringAfterLast(s," ");
            listRep.add(id); // Liste de tout les id des réunions
        }
        String temp = "";
        for(String reuID : listRep) { //for all the survey answered
            page = client.getPage("https://evento.renater.fr/survey/results/" + reuID); //recup la page en fonction de l'ID
            try {
                temp = StringUtils.substringBetween(page.asNormalizedText(), "Export results to CSV", "×");
            } catch (NullPointerException e) {
                temp = "";
            }
            EventoPoll poll = new EventoPoll();
            if (temp.startsWith("\nThis Evento is closed")) {
                //CASE : POOL CLOSED

                //ID, URL & NAME
                poll.ID=reuID;
                poll.url=("https://evento.renater.fr/survey/results/" + reuID);
                poll.name = name; // don't think it's important

                //get closed date
                //long timeClosed = Long.parseLong(StringUtils.substringBetween(page.asXml(), "<span data-timestamp=\"", "\""));

                //get start and finish date
                temp = StringUtils.substringBetween(page.asXml(),"Selected final answer :","</section>");
                long timeStart = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));
                temp = StringUtils.substringBetween(temp,"</span>","data-localize=\"time\">");
                long timeFinish = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));

                // ADD TO A PROP
                Props prop = new Props(null,tsToDate(timeStart),tsToHour(timeStart),tsToHour(timeFinish),null);
                // date finish ? : case if the meeting during more than 1 day

                //get name of the pool
                temp = StringUtils.substringBetween(page.asXml(),"class=\"survey_title\">","<span class=\"help-text\">");
                poll.title = temp.trim();

                //get organizer of the pool
                temp = StringUtils.substringBetween(page.asXml(),"(Organized by : ",")");
                poll.organizer = temp.trim();

                //get description of the pool
                temp = StringUtils.substringBetween(page.asXml(),"class=\"survey-description\"","</article>");
                poll.description = StringUtils.substringBetween(temp,"<p>","</p>").trim();

                System.out.println(prop.date + " " + prop.hour + " " + prop.hourEnd);
                System.out.println(poll.description + " " + poll.title + " " + poll.organizer);

                // organizer
                if(!poll.organizer.equals(this.name)) {
                    // I assume that the organizer participates in the meeting that he creates himself so if organizer = name of the account we keep the date of the answer directly
                    client.getOptions().setCssEnabled(true);
                    client.getOptions().setJavaScriptEnabled(true);
                    page = client.getPage("https://evento.renater.fr/survey/results/" + reuID); // refresh
                    client.waitForBackgroundJavaScript(60_000);
                    HtmlElement button = (HtmlElement) page.getFirstByXPath("/html/body/main/section/section[4]/section/section[1]/span");
                    page = button.click();


                    System.out.println(page.asXml());
                    break;
                    //temp = StringUtils.substringBetween(page.asXml(),String.valueOf(timeStart),"</html>");

                }


            } else {
                //OPEN
                //TODO
            }
        }


    }


    // Get the number of polls answered
    public int getNumberPolls(WebClient client) throws Exception{
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        return Integer.parseInt(StringUtils.substringBetween(page.asNormalizedText(), "Answered (", ")"));
    }

    public void getNameEmail(WebClient client) throws Exception {
        HtmlPage page = client.getPage("https://evento.renater.fr/user");
        for ( HtmlElement el : page.getHtmlElementDescendants() ) {
            if (el instanceof HtmlSection)
                if (el.toString().contains("row user-info")) { //little cheat but works
                    Iterator<HtmlElement> it = el.getHtmlElementDescendants().iterator();
                    while( it.hasNext()) {
                        HtmlElement child = it.next();
                        if (child instanceof HtmlLabel) {
                            String label = child.asNormalizedText();
                            if (label.startsWith("Name"))
                                name = it.next().asNormalizedText(); //get the name
                            else if (label.startsWith("Email"))
                                email = it.next().asNormalizedText(); //get the email
                        }
                    }
                }
        }
        System.out.println("Nom : " + name +"\nEmail : "+ email);
    }

    public MyList<HtmlElement> getInputsAndButtons(HtmlForm form) {
        MyList<HtmlElement> list = new MyList<>();
        for ( HtmlElement el : form.getHtmlElementDescendants() )
            if (el instanceof HtmlInput || el instanceof HtmlButton)
                list.add(el);
        return list;
    }

    /** Method merging getInputByName & getButtonByName in a form. */
    public HtmlElement getInputOrButtonByName(HtmlForm form, String name) {
        try {
            return form.getInputByName(name);
        } catch(Exception e) {
            return form.getButtonByName(name);
        }
    }

    public Props.Date tsToDate(long ts){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(ts*1000));
        return new Props.Date(calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.YEAR));
    }

    public Props.Hour tsToHour(long ts){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(ts*1000));
        return new Props.Hour(calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND));
    }

    @Override
    public void createICS(WebClient wb) throws IOException {
        //TODO
    }

    public static void main(String[] args) throws Exception{
        // remove warnings of Htmlunit
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        // Initialize the client
        WebClient client= new WebClient(BrowserVersion.CHROME);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false); // remove javascript errors
        EventoScrapper e = new EventoScrapper();
        e.connect(client,"atharrea","fdqj8t,\\g(","INSA Rennes");
        e.getNameEmail(client);
        e.getPools(client);
        client.close();
    }

}
