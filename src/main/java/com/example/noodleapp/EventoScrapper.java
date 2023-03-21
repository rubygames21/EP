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
    Set<String> listRep;
    //List<Poll> evento;

    public EventoScrapper() {
        // nom et email pas encore trouvés
        name = null;
        email = null;
        listRep  = new HashSet<>(); //initialiser la liste d'ID
        //evento = new ArrayList<>();
    }

    public void connect(WebClient client,String user, String pass, String organization) throws Exception {
        client.getOptions().setJavaScriptEnabled(true); // nécessaire pour la liste automatique de sélection d'établissement

        HtmlPage page = client.getPage("https://evento.renater.fr/Shibboleth.sso/Login?target=https%3A%2F%2Fevento.renater.fr%2Fhome");
        HtmlForm form = page.getForms().get(0);

        // Ajoute focus
        page = form.getSelectByName("user_idp").click(); // ou getElementById("userIdPSelection")

        // Clique le champ de saisie "Veuillez saisir l'établissement auquel vous appartenez."
        HtmlElement el = page.getFirstByXPath("//*[@id='select2-userIdPSelection-container']/span/div/div[2]");
        page = el.click();
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

    public void getPools(WebClient client) throws Exception{
        // ALLER A LA PAGE DES SONDAGES
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        String[] tempTab = StringUtils.substringsBetween(page.asNormalizedText(), " - ", "Last update of the organiser");
        for(String s : tempTab) {
            String id = StringUtils.substringAfterLast(s," ");
            listRep.add(id); // Liste de tout les id des réunions
        }

        for(String reuID : listRep) { //pour tout les sondages repondus
            page = client.getPage("https://evento.renater.fr/survey/results/" + reuID); //recup la page en fonction de l'ID
            String temp = StringUtils.substringBetween(page.asNormalizedText(), "Export results to CSV", "×");
            if (temp.startsWith("\nThis Evento is closed")) {
                //POOL CLOSED

                //System.out.println(page.asXml());

                //get closed date
                long timeClosed = Long.parseLong(StringUtils.substringBetween(page.asXml(), "<span data-timestamp=\"", "\""));
                //get start and finish date
                temp = StringUtils.substringBetween(page.asXml(),"Selected final answer :","</section>");
                long timeStart = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));
                temp = StringUtils.substringBetween(temp,"</span>","data-localize=\"time\">");
                long timeFinish = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));


                //get name of the pool
                temp = StringUtils.substringBetween(page.asXml(),"class=\"survey_title\">","<span class=\"help-text\">");
                String poolTitle = temp.trim();

                //get organizer of the pool
                temp = StringUtils.substringBetween(page.asXml(),"(Organized by : ",")");
                String poolOrganizer = temp.trim();

                //get description of the pool
                temp = StringUtils.substringBetween(page.asXml(),"class=\"survey-description\"","</article>");
                String poolDesc = StringUtils.substringBetween(temp,"<p>","</p>").trim();

                //HtmlElement button = (HtmlElement) page.getByXPath("/html/body/main/section/section[4]/section/section[1]/span").get(0);
                //page = button.click();

                //System.out.println(page.asXml());
                //temp = StringUtils.substringBetween(page.asXml(),String.valueOf(timeStart),"</html>");

                //LIEN !!!!

            } else {
                //OPEN
                //TODO
            }
        }
        //for(Poll sondage : evento) System.out.println(sondage);


    }


    // RECUPERER LE NOMBRE DE SONDAGES REPONDUS
    public void getNumberPools(WebClient client) throws Exception{
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        int nbSondagesRep = Integer.parseInt(StringUtils.substringBetween(page.asNormalizedText(), "Answered (", ")"));
        System.out.println("Sondages repondus : "+nbSondagesRep);
    }

    public void getNameEmail(WebClient client) throws Exception {
        HtmlPage page = client.getPage("https://evento.renater.fr/user");
        for ( HtmlElement el : page.getHtmlElementDescendants() ) {
            if (el instanceof HtmlSection)
                if (el.toString().contains("row user-info")) { //petite triche mais fonctionne
                    Iterator<HtmlElement> it = el.getHtmlElementDescendants().iterator();
                    while( it.hasNext()) {
                        HtmlElement child = it.next();
                        if (child instanceof HtmlLabel) {
                            String label = child.asNormalizedText();
                            if (label.startsWith("Name"))
                                name = it.next().asNormalizedText(); //stocker le nom
                            else if (label.startsWith("Email"))
                                email = it.next().asNormalizedText(); //stocker le mail
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

    @Override
    public void createICS(WebClient wb) throws IOException {
        //TODO
    }

    public static void main(String[] args) throws Exception{
        // ENLEVER LES WARNINGS HTMLUNIT
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        // INITIALISER LE CLIENT
        WebClient client= new WebClient(BrowserVersion.CHROME);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true); // activer javascript
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false); //enlever les erreurs javascript
        EventoScrapper e = new EventoScrapper();
        e.connect(client,"atharrea","fdqj8t,\\g(","INSA Rennes");
        e.getNameEmail(client);
        e.getPools(client);
        client.close();
    }

}
