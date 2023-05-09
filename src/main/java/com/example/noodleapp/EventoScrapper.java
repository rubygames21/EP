package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EventoScrapper extends Scrapper{

    String username;
    String password;
    String cas;

    String name; //format "Prenom Nom"
    String email;
    Set<String> listRep; //list of all the ID of the polls
    List<EventoPoll> evento;

    public EventoScrapper(String username, String password, String cas) {
        name = null;
        email = null;
        listRep  = new HashSet<>(); //initialize ID list
        evento = new ArrayList<>();
        this.username = username;
        this.password = password;
        this.cas = cas;
    }

    @Override
    public String display(){
        return "Evento : " +this.email;
    }

    public List<EventoPoll> getEvento(){
        return this.evento;
    }
    public void connect(WebClient client) throws Exception {
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
        el.type(this.cas); // we reduce the possible answers by specifying the establishment sought

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
        connectCas(client,this.username, this.password, el.getAttribute("value"));
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
        noAnswer.put(name, pollAnswer.No);


        // TIMEZONE = GMT+0 (London)
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        net.fortuna.ical4j.model.TimeZone timezone = registry.getTimeZone("Europe/London");

        client.getOptions().setJavaScriptEnabled(true);

        // go to the right poll page
        HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        System.out.println(page.asNormalizedText());
        String tempFirst = StringUtils.substringBetween(page.asNormalizedText(), "Closed", "Last update of the organiser").strip();
        String[] tempTab = StringUtils.substringsBetween(page.asNormalizedText(), "Number of questions", "Last update of the organiser");

        for(int i = 0 ; i< tempTab.length ; i++) {
            tempTab[i] = tempTab[i].strip();
        }

        String idtemp = "";
        String id = "";

        idtemp = StringUtils.substringAfterLast(tempFirst, "\n");
        if(idtemp.contains(" ")) id = StringUtils.substringAfterLast(idtemp, " ").strip();
        else id = idtemp.strip();
        listRep.add(id);

        for(String s : tempTab) {
            idtemp = StringUtils.substringAfterLast(s, "\n");
            if(idtemp.contains(" ")) id =StringUtils.substringAfterLast(idtemp, " ").strip();
            else id = idtemp.strip();
            listRep.add(id); // List of all meeting IDs
        }
        /*HtmlPage page = client.getPage("https://evento.renater.fr/survey/manage#invitedto");
        String[] tempTab = StringUtils.substringsBetween(page.asNormalizedText(), " - ", "Last update of the organiser");

        for(String s : tempTab) {
            String id = StringUtils.substringAfterLast(s," ");
            listRep.add(id); // List of all meeting IDs
        }

         */

        //variables for stocking the right Strings
        String temp;
        String temp2;
        String temp3;
        for(String reuID : listRep) { //for all the survey answered
            System.out.println(reuID);
            System.out.println(reuID);



            page = client.getPage("https://evento.renater.fr/survey/results/" + reuID ); //recup la page en fonction de l'ID

            EventoPoll poll = new EventoPoll();
            poll.props = new ArrayList<>();

            //ID, URL & NAME
            poll.ID=reuID;
            poll.url=("https://evento.renater.fr/survey/results/" + reuID);
            System.out.println(page.getBaseURL());
            System.out.println(poll.url);
            poll.name = name; // don't think it's important

            System.out.println(page.getBaseURL());
            System.out.println(page.asXml());

            //get name of the pool
            poll.title = StringUtils.substringBetween(page.asXml(),"class=\"survey_title\">","<span class=\"help-text\">").trim();

            //get organizer of the pool
            poll.organizer = StringUtils.substringBetween(page.asXml(),"(Organized by : ",")").trim();

            //get description of the pool
            temp = StringUtils.substringBetween(page.asXml(),"class=\"survey-description\"","</article>");
            //poll.description = StringUtils.substringBetween(temp,"<p>","</p>").trim();
            try{
                poll.description = StringUtils.substringBetween(temp,"<p>","</p>").trim();
            } catch (NullPointerException e) {
                poll.description = "";
            }

            poll.numberParticipant = Integer.parseInt(StringUtils.substringBetween(page.asXml(),"<td class=\"first\">"," Participant(s)").trim());
            System.out.println("TEST : " + poll.numberParticipant);

            try {
                temp = StringUtils.substringBetween(page.asNormalizedText(), "Export results to CSV", "×");
            } catch (NullPointerException e) {
                temp = "";
                System.out.println("page problem");
            }

            if (temp.startsWith("\nThis Evento is closed")) {
                //CASE : POOL CLOSED
                temp = StringUtils.substringBetween(page.asXml(),"Selected final answer :","</section>");
                if(temp != null) {

                    //get closed date (not so useful)
                    //long timeClosed = Long.parseLong(StringUtils.substringBetween(page.asXml(), "<span data-timestamp=\"", "\""));

                    //get start and finish date
                    long timeStart = Long.parseLong(StringUtils.substringBetween(temp, "span data-timestamp=\"", "\""));
                    temp = StringUtils.substringBetween(temp, "</span>", "data-localize=\"time\">");
                    long timeFinish = Long.parseLong(StringUtils.substringBetween(temp, "span data-timestamp=\"", "\""));

                    // ADD TO A PROP : always YES
                    Props prop = new Props(timezone, tsToDate(timeStart), tsToHour(timeStart), tsToHour(timeFinish), yesAnswer);
                    poll.props.add(prop);
                    // date finish ? : case if the meeting during more than 1 day

                /*
                System.out.println(prop.date + " " + prop.hour + " " + prop.hourEnd);
                System.out.println(poll.description + " " + poll.title + " " + poll.organizer);
                 */

                    // organizer
                    if (!poll.organizer.equals(this.name)) {
                        // I assume that the organizer participates in the meeting that he creates himself so if organizer = name of the account we keep the date of the answer directly
                        //...something
                    }
                    poll.closed = true;
                    //problem : poll with no description => don't work
                } else {
                    //CLOSE + NOT FINAL ANSWER SELECTED

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
                        temp3 = StringUtils.substringBetween(temp2,"<br/>"," data-localize=\"time\">");
                        long timeFinish = Long.parseLong(StringUtils.substringBetween(temp3,"\"","\""));

                        //System.out.println("TESTING : " + tsToDate(timeStart) + " " + tsToHour(timeStart) + " " + tsToHour(timeFinish));

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
                        poll.closed = true; // poll closed
                    }
                }
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

                    temp3 = StringUtils.substringBetween(temp2,"<br/>"," data-localize=\"time\">");
                    long timeFinish=0;
                    if(temp3!=null){
                        timeFinish = Long.parseLong(StringUtils.substringBetween(temp3,"\"","\""));
                    }else{
                        timeFinish=timeStart+3600;
                    }

                    //System.out.println("TESTING : " + tsToDate(timeStart) + " " + tsToHour(timeStart) + " " + tsToHour(timeFinish));

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
    public void createICS(WebClient wb, String path) throws IOException {
        File calendarICS = new File (path + "evento_calendar.ics");
        if (!calendarICS.exists()) {
            try {
                calendarICS.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
            for(EventoPoll ev : evento) {
                for (Props props : ev.props) {
                    System.out.println("TESTING : " + getMyVote(props.eachAnswer));
                    if (getMyVote(props.eachAnswer) != pollAnswer.No && getMyVote(props.eachAnswer) != null) {
                        Calendar startDate = createStartDate(props);
                        Calendar endDate = createEndDate(props);

                        //TODO : get other participants

                        String eventName = "";
                        Status status;
                        if (!ev.closed) {
                            eventName += "[TENTATIVE] "; // ?
                            status = new Status("TENTATIVE");
                        } else {
                            status = new Status("CONFIRMED");
                        }
                        eventName += getMyVote(props.eachAnswer) + " | " + ev.title;
                        Description desc = new Description(ev.title + " " + ev.description + " Organizer : " +  ev.organizer + " URL : " + ev.url);
                        Url url = new Url();
                        url.setValue(ev.url.trim());
                        DateTime start = new DateTime(startDate.getTime());
                        DateTime end = new DateTime(endDate.getTime());
                        VEvent meeting = new VEvent(start, end, eventName);
                        meeting.getProperties().add(tz.getTimeZoneId());
                        meeting.getProperties().add(desc);
                        meeting.getProperties().add(status);
                        meeting.getProperties().add(url);
                        UidGenerator ug = new RandomUidGenerator();
                        Uid uid = ug.generateUid();
                        meeting.getProperties().add(uid);
                        calendar.getComponents().add(meeting);
                    }
                }
            }
            bw.write(calendar.toString());
            bw.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public pollAnswer getMyVote(Map<String ,pollAnswer> eachAnswer) {
        for(String a : eachAnswer.keySet()) {
            if(a.equals(name)) {
                return eachAnswer.get(a);
            }
        }
        return pollAnswer.No;
    }

    //TODO : regrouper ces 2 fonctions en une
    public Calendar createStartDate(Props props){
        Calendar date = new GregorianCalendar();
        date.setTimeZone(props.timeZone);
        //add Date
        date.set(Calendar.MONTH, props.date.month);
        date.set(Calendar.DAY_OF_MONTH, props.date.day);
        date.set(Calendar.YEAR, props.date.year);
        //add Hour
        date.set(Calendar.HOUR_OF_DAY, props.hour.hour-1);
        date.set(Calendar.MINUTE, props.hour.minute);
        date.set(Calendar.SECOND, props.hour.second);
        return date;
    }

    public Calendar createEndDate(Props props){
        Calendar date = new GregorianCalendar();
        date.setTimeZone(props.timeZone);
        //add Date
        date.set(Calendar.MONTH, props.date.month);
        date.set(Calendar.DAY_OF_MONTH, props.date.day);
        date.set(Calendar.YEAR, props.date.year);
        //add Hour
        date.set(Calendar.HOUR_OF_DAY, props.hourEnd.hour-1);
        date.set(Calendar.MINUTE, props.hour.minute);
        date.set(Calendar.SECOND, props.hour.second);
        return date;
    }


    public void start(WebClient client) throws Exception {
        // remove warnings of Htmlunit
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false); // remove javascript errors

        this.connect(client);
        this.getNameEmail(client);
        this.getPolls(client);
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
        EventoScrapper e = new EventoScrapper("atharrea","fdqj8t,\\g(","INSA Rennes");
        e.connect(client);
        e.getNameEmail(client);
        e.getPolls(client);
        String path = "C:\\Users\\HDrag\\Documents\\GitHub\\EP\\";
        e.createICS(client,path);
        client.close();
    }

}
