package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EventoScrapper extends Scrapper{

    String name; //format "Prenom Nom"
    String email;
    Set<String> listRep; //list of all the ID of the polls
    List<EventoPoll> evento;
    private static final int MAXPROPS = 100;

    public EventoScrapper() {
        name = null;
        email = null;
        listRep  = new HashSet<>(); //initialize ID list
        evento = new ArrayList<>();
    }

    public void connect(WebClient client,String user, String pass, String organization) throws Exception {
        client.getOptions().setJavaScriptEnabled(true); // necessary for the automatic establishment selection list

        HtmlPage page = client.getPage("https://evento.renater.fr/Shibboleth.sso/Login?target=https%3A%2F%2Fevento.renater.fr%2Fhome");
        HtmlForm form = page.getForms().get(0);

        // Add focus
        page = form.getSelectByName("user_idp").click(); // or getElementById("userIdPSelection")

        // Click the input field "Veuillez saisir l'établissement auquel vous appartenez."
        HtmlElement el = page.getFirstByXPath("//*[@id='select2-userIdPSelection-container']/span/div/div[2]");
        try {
            page = el.click();
        } catch (NullPointerException e) {
            System.out.println("bug"); //bug who crash the program (1 times over 3)
            return;
        }
        // Now the drop-down list is open, and the interactive input field is blank

        // We type the information of the name of the establishment in the input field
        el = page.getFirstByXPath("//input[@class='select2-search__field']"); // field SearchInput
        el.type(organization); // we reduce the possible answers by specifying the establishment sought

        // Waits (at the max the indicated limit) for the search in javascript to finish listing the answers
        if (0 != client.waitForBackgroundJavaScript(5000)) // 5 seconds max
            throw new InterruptedException("Javascript job not finished");

        // Get and click in the list the 1st form answer <div class="select2-result-repository__title">
        el = (HtmlElement) page.getByXPath("//div[@class='select2-result-repository__title']").get(1); // get(1) because there is before another element with the same class name
        el.click(); // click on the answer => close the search and transfer it to the input field

        // Now the HTML of the select list has expanded from the clicked response, in the form :
        // <option value="https://idp.insa-rennes.fr/idp/shibboleth" data-select2-id="18" selected="selected">
        //   INSA Rennes
        // </option>
        el = page.getFirstByXPath("//*[@id='userIdPSelection']/option[1]"); // the 1st and only option in the list
        System.out.println(el.getAttribute("value"));
        connectCas(client,user, pass, el.getAttribute("value"));
    }

    /** Connects to Evento via a given CAS server (Central Authentication Service). */
    protected void connectCas(WebClient client, String user, String pass, String cas) throws Exception {
        client.getOptions().setJavaScriptEnabled(false); // must be false for Rennes 1 and Rennes 2 for example
        // it is the possible page (wrongly often) that Javascript is not present and which surprisingly has some < tags turned into &lt; if JavaScript enabled.
        HtmlPage page = client.getPage(
                "https://evento.renater.fr/Shibboleth.sso/Login?SAMLDS=1&target=https%3A%2F%2Fevento.renater.fr%2Fhome&entityID="
                        + URLEncoder.encode(cas, StandardCharsets.UTF_8) );

        // If session already connected before, we are on the service without needing the CAS
        if (!page.toString().startsWith("https://evento.renater.fr",9)) { // 9 to avoid "HtmlPage"

            // Fills in the fields of the CAS form
            HtmlForm form = page.getForms().get(0);
            form.getInputByName("username").setValueAttribute(user);
            form.getInputByName("password").setValueAttribute(pass);

            // Submit the CAS form by clicking
            page = getInputOrButtonByName(form, "submit").click();

            // If we have not arrived at the requested service, it can be:
            // - an intermediate information page
            // - a page indicating the absence of Javascript in the browser
            // - an identification failure page
            // => to skip the first 2 cases we allow ourselves up to 2 additional clicks
            for(int i=0; i<2 && !page.toString().startsWith("https://evento.renater.fr",9); i++) {
                page = getInputsAndButtons(page.getForms().get(0)).getLast().click();
            }
            if (!page.toString().startsWith("https://evento.renater.fr",9)) {
                throw new Exception("CAS authentication failed");
            }
        }
    }

    public void getPolls(WebClient client) throws Exception {

        // each answer always YES
        Map<String, pollAnswer> yesAnswer= new HashMap<>();
        yesAnswer.put(name, pollAnswer.Yes);

        // each answer always NO
        Map<String, pollAnswer> noAnswer= new HashMap<>();
        yesAnswer.put(name, pollAnswer.No);


        // TIMEZONE = GMT+0 (London)
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        net.fortuna.ical4j.model.TimeZone timezone = registry.getTimeZone("Europe/London");

        client.getOptions().setJavaScriptEnabled(true);

        // go to the right poll page
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        String[] tempTab = StringUtils.substringsBetween(page.asNormalizedText(), " - ", "Last update of the organiser");

        for(String s : tempTab) {
            String id = StringUtils.substringAfterLast(s," ");
            listRep.add(id); // List of all meeting IDs
        }

        //variables for stocking the right Strings
        String temp;
        String temp2;
        String temp3;
        for(String reuID : listRep) { //for all the survey answered

            page = client.getPage("https://evento.renater.fr/survey/results/" + reuID); //recup la page en fonction de l'ID

            EventoPoll poll = new EventoPoll();
            poll.props = new ArrayList<>();

            //ID, URL & NAME
            poll.ID=reuID;
            poll.url=("https://evento.renater.fr/survey/results/" + reuID);
            poll.name = name; // don't think it's important

            //get name of the pool
            poll.title = StringUtils.substringBetween(page.asXml(),"class=\"survey_title\">","<span class=\"help-text\">").trim();

            //get organizer of the pool
            poll.organizer = StringUtils.substringBetween(page.asXml(),"(Organized by : ",")").trim();

            //get description of the pool
            temp = StringUtils.substringBetween(page.asXml(),"class=\"survey-description\"","</article>");
            poll.description = StringUtils.substringBetween(temp,"<p>","</p>").trim();

            poll.numberParticipant = Integer.parseInt(StringUtils.substringBetween(page.asXml(),"<td class=\"first\">"," Participant(s)").trim());

            try {
                temp = StringUtils.substringBetween(page.asNormalizedText(), "Export results to CSV", "×");
            } catch (NullPointerException e) {
                temp = "";
                System.out.println("page problem");
            }

            if (temp.startsWith("\nThis Evento is closed")) {
                //CASE : POOL CLOSED

                //get closed date (not so useful)
                //long timeClosed = Long.parseLong(StringUtils.substringBetween(page.asXml(), "<span data-timestamp=\"", "\""));

                //get start and finish date
                temp = StringUtils.substringBetween(page.asXml(),"Selected final answer :","</section>");
                long timeStart = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));
                temp = StringUtils.substringBetween(temp,"</span>","data-localize=\"time\">");
                long timeFinish = Long.parseLong(StringUtils.substringBetween(temp,"span data-timestamp=\"","\""));

                // ADD TO A PROP : always YES
                Props prop = new Props(timezone,tsToDate(timeStart),tsToHour(timeStart),tsToHour(timeFinish),yesAnswer);
                poll.props.add(prop);
                // date finish ? : case if the meeting during more than 1 day

                /*
                System.out.println(prop.date + " " + prop.hour + " " + prop.hourEnd);
                System.out.println(poll.description + " " + poll.title + " " + poll.organizer);
                 */

                // organizer
                if(!poll.organizer.equals(this.name)) {
                    // I assume that the organizer participates in the meeting that he creates himself so if organizer = name of the account we keep the date of the answer directly
                    //...something
                }
                poll.closed = true;
                //problem : poll with no description => don't work
            } else {
                //OPEN

                tempTab = StringUtils.substringsBetween(page.asXml(), "<td class=\"sum", "</td>");

                int[] tempAnswers = new int[tempTab.length];
                for (int order = 0; order<tempTab.length; order++) {
                    tempAnswers[order] = Integer.parseInt(StringUtils.substringAfter(tempTab[order], ">").trim());
                }

                temp = StringUtils.substringBetween(page.asXml(),"<th class=\"first\"/>","</tr>");
                for(int datapos = 0; datapos<tempTab.length; datapos++) {
                    temp2 = StringUtils.substringBetween(temp,"data-position=\""+datapos+"\">","</th>");
                    if(temp2 == null) break; // = no more proposition

                    //get start and finish date
                    temp3 = StringUtils.substringBetween(temp2,"<span data-timestamp=","-");
                    long timeStart = Long.parseLong(StringUtils.substringBetween(temp3,"\"","\""));
                    temp3 = StringUtils.substringBetween(temp2,"-"," data-localize=\"time\">");
                    long timeFinish = Long.parseLong(StringUtils.substringBetween(temp3,"\"","\""));

                    System.out.println("TESTING : " + tsToDate(timeStart) + " " + tsToHour(timeStart) + " " + tsToHour(timeFinish));

                    //if organizer = name of the user -> get all the props : YES
                    if(poll.organizer.equals(this.name)) {
                        // ADD TO A PROP
                        Props prop = new Props(timezone, tsToDate(timeStart), tsToHour(timeStart), tsToHour(timeFinish), yesAnswer);
                        poll.props.add(prop);
                    } else {
                        //else get Yes and Maybe
                        if(tempAnswers[datapos]==0){ // 0 = NO
                            // ADD TO A PROP
                            Props prop = new Props(timezone, tsToDate(timeStart), tsToHour(timeStart), tsToHour(timeFinish), noAnswer);
                            poll.props.add(prop);
                        } else { // 1 or more = YES
                            // ADD TO A PROP
                            Props prop = new Props(timezone, tsToDate(timeStart), tsToHour(timeStart), tsToHour(timeFinish), yesAnswer);
                            poll.props.add(prop);
                        } // MAYBE ????
                    }
                    poll.closed = false; // poll not closed here
                }
            }
            evento.add(poll);
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
        File calendarICS = new File ("C:\\Users\\HDrag\\Documents\\GitHub\\EP"+ "evento_calendar.ics");
        if (!calendarICS.exists()) {
            try {
                calendarICS.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
        try {
            FileWriter writer = new FileWriter(calendarICS);
            BufferedWriter bw = new BufferedWriter(writer);
            net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
            calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            // Create a TimeZone
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            net.fortuna.ical4j.model.TimeZone timezone = registry.getTimeZone("Europe/Paris");
            VTimeZone tz = timezone.getVTimeZone();
            for (Props props : props) {
                if(getMyVote(props,names)!=pollAnswer.No&&getMyVote(props,names)!=null){
                    Calendar startDate = createStartDate(props);
                    Calendar endDate = createEndDate(props);
                    String allPresentParticipant = getAllPresentParticipant(props,names);
                    if(allPresentParticipant.isEmpty()){
                        allPresentParticipant = "nobody";
                    }
                    String allMaybePresentParticipant = getAllMaybePresentParticipant(props,names);
                    if(allMaybePresentParticipant.isEmpty()){
                        allMaybePresentParticipant = "nobody";
                    }
                    //pourquoi je ne peut pas mettre de , ?
                    String eventName = ("Poll : " + this.title + " and you vote : " + getMyVote(props,names) + " and people who will be present : " + allPresentParticipant + " and people who will maybe be present : " + allMaybePresentParticipant);
                    DateTime start = new DateTime(startDate.getTime());
                    DateTime end = new DateTime(endDate.getTime());
                    VEvent meeting = new VEvent(start, end, eventName);
                    meeting.getProperties().add(tz.getTimeZoneId());
                    UidGenerator ug = new RandomUidGenerator();
                    Uid uid = ug.generateUid();
                    meeting.getProperties().add(uid);
                    calendar.getComponents().add(meeting);
                }
            }
            bw.write(calendar.toString());
            bw.close();
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

         */
    }

    public static void main(String[] args) throws Exception{
        // remove warnings of Htmlunit
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        // Initialize the client
        WebClient client= new WebClient(BrowserVersion.FIREFOX);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false); // remove javascript errors
        EventoScrapper e = new EventoScrapper();
        e.connect(client,"atharrea","fdqj8t,\\g(","INSA Rennes");
        e.getNameEmail(client);
        e.getPolls(client);
        client.close();
    }

}
