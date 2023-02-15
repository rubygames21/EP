package com.example.noodleapp;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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

import java.io.*;
import java.net.StandardSocketOptions;
import java.util.*;

enum pollAnswer{
    Yes,
    No,
    Maybe
}

public class FramadatePoll {
    //a revoir cette modélisation
    /* Map<String, Map<Props,pollAnswer>> data;*/
    //List des propostitions et des reponses associées
    List<Props> props;
    /*Map<String,pollAnswer> eachAnswer;*/
    String name;
    String title;
    String url;
    String ID;

    public FramadatePoll(String url){
        this.url = url;
    }
    public FramadatePoll(){

    }


    public void addProps(List<Props> p){
        for(Props prop:p){
            props.add(prop);
        }
    }
    //fonction qui récuprère l'ID du sondage (la chaine de caractère après le dernier (3eme) "/")
    public String getPollID(String url){
        String s="";
        int cpt =0;
        //TODO : on peut ammeliorer avec une regex
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

    //function that return the number of digit in a String
    public int nbDigitInString(String s){
        int res = 0;
        int i = 1;
        while (i<s.length()){
            if(Character.isDigit(s.charAt(i))){
                res++;
            }
            i++;
        }
        return res;
    }

    // function that return an array with all the digit of a String
    public int[] getDigit(String s,int nbDigit){
        int[] res = new int[4];
        int j = 0;
        int i = 1;
        while (i<s.length()){
            if(Character.isDigit(s.charAt(i))){
                res[j] = Character.getNumericValue(s.charAt(i));
                j++;
            }
            i++;
        }
        return res;
    }

    //function that return convert a String into an Hour
    public Props.Hour stringToHour(String s){
        Props.Hour hour = new Props.Hour();
        if(nbDigitInString(s)==4){
            int[] tmp = getDigit(s,4);
            hour.hour = tmp[0]*10+tmp[1];
            hour.minute = (tmp[2]*10)+tmp[3];
        } else if (nbDigitInString(s)==3) {
            int[] tmp = getDigit(s,3);
            hour.hour = tmp[0];
            hour.minute = tmp[1]*10+tmp[2];
        } else if (nbDigitInString(s)==2) {
            int[] tmp = getDigit(s,2);
            hour.hour = tmp[0]*10+tmp[1];
            hour.minute = 00;
        } else if (nbDigitInString(s)==1) {
            int[] tmp = getDigit(s,1);
            hour.hour = tmp[0];
            hour.minute = 00;
        } else {
            System.out.println("Heures à traité ne contient pas un nombre de chiffre cohérents");
        }
        hour.second=0;
        return hour;
    }

    public void fillProps(String[] data,String[] answers){
        this.props = new ArrayList<>();
        //on enlee la premiere virgule puis on split selon les ","
        String[] dateSplit = data[0].substring(1).split(",");
        String[] hourSplit = data[1].substring(1).split(",");
        int nbProp = dateSplit.length;
        for(int i = 0; i < nbProp ; i++){
            Props prop = new Props();
            prop.date = new Props.Date();
            prop.hour = new Props.Hour();
            String[] date = dateSplit[i].replaceAll("\"","").split("-");
            prop.date.year = Integer.parseInt(date[0]);
            prop.date.month = Integer.parseInt(date[1]);
            prop.date.day = Integer.parseInt(date[2]);
            prop.hour = stringToHour(hourSplit[i]);
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            net.fortuna.ical4j.model.TimeZone timezone = registry.getTimeZone("Europe/Paris");
            prop.timeZone = timezone;
            prop.eachAnswer = new HashMap<>();
            for(int j = 0;j <answers.length;j++){
                String[] t = answers[j].replaceAll("\"","").split(",");
                if(t[i+1].equals("Yes")){
                    prop.eachAnswer.put(t[0].toLowerCase(),pollAnswer.Yes);
                } else if (t[i+1].equals("No")) {
                    prop.eachAnswer.put(t[0].toLowerCase(), pollAnswer.No);
                }
                else{
                    prop.eachAnswer.put(t[0].toLowerCase(), pollAnswer.Maybe);
                }
            }
            props.add(prop);
        }
    }

    public String getTitle(WebClient webClient) throws IOException {
        HtmlPage actualPage = webClient.getPage(this.url);
        String s = actualPage.getTitleText();
        String[] tabs= s.split("-");
        String t = tabs[1].replaceAll("\\s","");
        return t;
    }

    public void fillPoll(WebClient wb) throws IOException {
        HtmlPage actualPage = wb.getPage(this.url);
        HtmlAnchor a = actualPage.getAnchorByHref("https://framadate.org/exportcsv.php?poll="+getPollID(url));
        Page p = a.click();
        InputStream is = p.getWebResponse().getContentAsStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        String data = content.toString();
        String[] lines = data.split("\n");
        int nbParticipant = lines.length -2;
        String[] props = new String[2];
        props[0] = lines[0];
        props[1] = lines[1];
        String[] answers = new String[nbParticipant];
        for(int i = 0; i < nbParticipant; i++){
            answers[i] = lines[i+2];
        }
        title = getTitle(wb);
        fillProps(props,answers);
    }

    //TODO : regrouper ces 2 fonctions en une
    public Calendar createStartDate(Props props){
        Calendar date = new GregorianCalendar();
        date.setTimeZone(props.timeZone);
        //add Date
        date.set(Calendar.MONTH, props.date.month-1 );
        date.set(Calendar.DAY_OF_MONTH, props.date.day);
        date.set(Calendar.YEAR, props.date.year);
        //add Hour
        date.set(Calendar.HOUR_OF_DAY, props.hour.hour);
        date.set(Calendar.MINUTE, props.hour.minute);
        date.set(Calendar.SECOND, props.hour.second);
        return date;
    }

    public Calendar createEndDate(Props props){
        Calendar date = new GregorianCalendar();
        date.setTimeZone(props.timeZone);
        //add Date
        date.set(Calendar.MONTH, props.date.month-1 );
        date.set(Calendar.DAY_OF_MONTH, props.date.day);
        date.set(Calendar.YEAR, props.date.year);
        //add Hour
        date.set(Calendar.HOUR_OF_DAY, props.hour.hour+1);
        date.set(Calendar.MINUTE, props.hour.minute);
        date.set(Calendar.SECOND, props.hour.second);
        return date;
    }

    public String[] getAnswerByProp(Props p,String myName){
        String[] res = new String[2];


        return res;
    }

    public pollAnswer getMyVote(Props p,Set<String> names){
        for(String name : names){
            name = name.toLowerCase();
            if(p.eachAnswer.containsKey(name.toLowerCase())){
                return p.eachAnswer.get(name);
            }
        }
        //faire un try catch dans propsToICS pour verifier que l'on a bien voter
        return null;
    }

    public String getAllPresentParticipant(Props p,Set<String> names){
        String allPresentParticipant ="";
        for(String participant: p.eachAnswer.keySet()){
            boolean find = false;
            for(String name : names){
                if(participant.equals(name.toLowerCase())){
                    find = true;
                    break;
                }
            }
            if(!find){
                if(p.eachAnswer.get(participant)==pollAnswer.Yes){
                    allPresentParticipant+=participant+ " ";
                }
            }
        }
        //faire un try catch dans propsToICS pour verifier que l'on a bien voter
        return allPresentParticipant;
    }
    public String getAllMaybePresentParticipant(Props p,Set<String> names) {
        String allMaybePresentParticipant = "";
        for (String participant : p.eachAnswer.keySet()) {
            boolean find = false;
            for (String name : names) {
                if (participant.equals(name.toLowerCase())) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                if (p.eachAnswer.get(participant) == pollAnswer.Maybe) {
                    allMaybePresentParticipant += participant + " ";
                }
            }
        }
        //faire un try catch dans propsToICS pour verifier que l'on a bien voter
        return allMaybePresentParticipant;
    }

    public void propsToICS(Set<String> names) {
        File calendarICS = new File ("E:\\INSA\\3A\\EtudePratique\\Noodle\\NoodleApp\\"+ getPollID(url) + "_calendar.ics");
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
    }



    public void createICS(WebClient wb,Set<String> names) throws IOException {
        fillPoll(wb);
        propsToICS(names);
    }

    public void justCreateICS(WebClient webClient,Set<String> names)throws IOException {
        this.url = "https://framadate.org/testMerge";
        propsToICS(names);
    }


    /*public static void main(String[] args) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        //sinon ca ne marche pas
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);

        FramadatePoll framadatePoll = new FramadatePoll("https://framadate.org/hQXzCKULUtih3S3m");
        framadatePoll.url = "https://framadate.org/hQXzCKULUtih3S3m";
        Set names = new HashSet<>();
        names.add("jean");
        framadatePoll.fillPoll(webClient);
        framadatePoll.createICS(webClient,names);
*//*
        System.out.println(framadatePoll.getMyVote(framadatePoll.props.get(0),names));
*//*
    }*/
}
