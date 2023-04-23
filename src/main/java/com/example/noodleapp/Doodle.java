
package org.example;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Doodle{

    private WebDriver driver;
    private Actions actions;
    private ArrayList<ReunionDoodle> reunions;
    private ArrayList<ReunionDoodle> reunionsGarde = new ArrayList<>();
    private ArrayList<String> links = new ArrayList<>();
    private String hreff;
    private String pwd;
    private String login;


    public Doodle() {
        //WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        String driverPath = "src/chromedriverSTABLE";
        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver(options);
        actions = new Actions(driver);
        reunions = new ArrayList<>();
    }
    public Doodle(List<String> href) throws ParseException, InterruptedException {        //lien href de la reunion a utilisé que quand on est connecte
        //WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        String driverPath = "src/chromedriverSTABLE";
        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver(options);
        actions = new Actions(driver);
        reunions = new ArrayList<>();
    }

    public void mainDoodle(String login, String password) throws ParseException, InterruptedException {
        this.login = login;
        this.pwd = password;
        System.out.println("Est ce que vous preferez nous donner vos identifiant L ou vous identifiez vous même V ?");
        Scanner saisieUtilisateur = new Scanner(System.in);
        String c = saisieUtilisateur.nextLine();
        if(c.equals("L")){
            multiDriver(login, password);
        }
        else if(c.equals("V")) {
            utilisateurSeConnecteSolo();
        }
        setTimeZoneForAllPoll(reunions);
        seConformiser(reunions);
        garderLesCreneauDeReunionSouhaite(reunions);

        for (ReunionDoodle r : reunionsGarde) {
            System.out.println("----------\nOrganisateur : " + r.getOrganisateur() + "\nIntitulé : " + r.getNom() + "\nle :" + r.getPropsDoodleList().get(0).getDate());
            for(PropsDoodle p : r.getPropsDoodleList()) {
                System.out.println("le :" + p.getDate() + " de :" + p.getHourDebutConforme() + " à :" + p.getHeureFin() + " ai-je déjà validé ce pool ? :" + p.getEachAnswer().get("MOI") + " TimeZone : " + p.getTimeZone());
            }
            System.out.println("à :" + r.getLocalisation());
        }
    }

    public void utilisateurSeConnecteSolo() throws InterruptedException {
        driver.get("https://doodle.com/login");
        while (!driver.getCurrentUrl().equals("https://doodle.com/dashboard")) {      //tant que le user ne s'est pas connecter
        }
        try { //accept the cookies
            Thread.sleep(5000);
            WebElement acceptButton = driver.findElement(By.id("onetrust-accept-btn-handler"));
            actions.moveToElement(acceptButton).click().perform();
        } catch (Exception e) {
            //je n'arrive pas a accepeter les cookies
        }
        // Minimiser la fenêtre du navigateur
        driver.manage().window().setPosition(new Point(-2000, 0));
        while (true) {
            getAllTheLinks();
            for (String h : links) {
                getAllTheInfomationPerLink(h);
            }
            for (ReunionDoodle r : reunions) {
                System.out.println("----------\nOrganisateur : " + r.getOrganisateur() + "\nIntitulé : " + r.getNom() + "\nle :" + r.getPropsDoodleList().get(0).getDate());
                for (PropsDoodle p : r.getPropsDoodleList()) {
                    System.out.println("le :" + p.getDate() + " de :" + p.getHeureDebut() + " à :" + p.getHeureFin() + " ai-je déjà validé ce pool ? :" + p.getReponse() + " TimeZone : " + p.getTimeZone());
                }
                System.out.println("à :" + r.getLocalisation());
                Thread.sleep(300000);
                driver.get("https://doodle.com/dashboard");
            }
        }
    }


    public void multiDriver(String login, String password) throws ParseException, InterruptedException {
        connectDoodle(login, password);
        getAllTheLinks();
        if(links.size() <= 5){
            for(String s : links){
                getAllTheInfomationPerLink(s);
            }
            driver.quit();
        }
        //code pour diviser une liste en 2
        if(links.size() > 5 && links.size() <10){
            driver.quit();
            ArrayList<String> list1 = new ArrayList<String>();
            ArrayList<String> list2 = new ArrayList<String>();
            int midpoint = links.size() / 2;
            for (int i = 0; i < midpoint; i++) {
                list1.add(links.get(i));
            }
            for (int i = midpoint; i < links.size(); i++) {
                list2.add(links.get(i));
            }

            Doodle d1 = new Doodle(list1);
            Doodle d2 = new Doodle(list2);

            ExecutorService executors = Executors.newFixedThreadPool(2);

            List<Callable<Void>> taskss = Arrays.asList(
                    () -> {
                        d1.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d2.connectDoodle(login, password);
                        return null;
                    }
            );

            executors.invokeAll(taskss);
            executors.shutdown();


            ExecutorService executor = Executors.newFixedThreadPool(2);

            List<Callable<Void>> tasks = Arrays.asList(
                    () -> {
                        for (String s : list1) {
                            d1.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list2) {
                            d2.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    }
            );
            executor.invokeAll(tasks);
            executor.shutdown();

            d1.driver.quit();
            d2.driver.quit();

            reunions.addAll(d1.reunions);
            reunions.addAll(d2.reunions);

        }
        else if(links.size() >= 10 && links.size() <= 20){
            driver.quit();
            ArrayList<String> list1 = new ArrayList<String>();
            ArrayList<String> list2 = new ArrayList<String>();
            ArrayList<String> list3 = new ArrayList<String>();
            int size = links.size();
            int split = size / 3;
            for (int i = 0; i < size; i++) {
                if (i < split) {
                    list1.add(links.get(i));
                } else if (i < split * 2) {
                    list2.add(links.get(i));
                } else {
                    list3.add(links.get(i));
                }
            }

            Doodle d1 = new Doodle(list1);
            Doodle d2 = new Doodle(list2);
            Doodle d3 = new Doodle(list3);

            ExecutorService executors = Executors.newFixedThreadPool(3);

            List<Callable<Void>> taskss = Arrays.asList(
                    () -> {
                        d1.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d2.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d3.connectDoodle(login, password);
                        return null;
                    }
            );

            executors.invokeAll(taskss);
            executors.shutdown();


            ExecutorService executor = Executors.newFixedThreadPool(3);

            List<Callable<Void>> tasks = Arrays.asList(
                    () -> {
                        for (String s : list1) {
                            d1.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list2) {
                            d2.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list3) {
                            d3.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    }

            );
            executor.invokeAll(tasks);
            executor.shutdown();

            d1.driver.quit();
            d2.driver.quit();
            d3.driver.quit();

            reunions.addAll(d1.reunions);
            reunions.addAll(d2.reunions);
            reunions.addAll(d3.reunions);

        }
        else if(links.size() > 20) {
            driver.quit();
            ArrayList<String> list1 = new ArrayList<String>();
            ArrayList<String> list2 = new ArrayList<String>();
            ArrayList<String> list3 = new ArrayList<String>();
            ArrayList<String> list4 = new ArrayList<String>();
            for (int i = 0; i < links.size(); i++) {
                if (i % 4 == 0) {
                    list1.add(links.get(i));
                } else if (i % 4 == 1) {
                    list2.add(links.get(i));
                } else if (i % 4 == 2) {
                    list3.add(links.get(i));
                } else {
                    list4.add(links.get(i));
                }
            }

            Doodle d1 = new Doodle(list1);
            Doodle d2 = new Doodle(list2);
            Doodle d3 = new Doodle(list3);
            Doodle d4 = new Doodle(list4);

            ExecutorService executors = Executors.newFixedThreadPool(4);

            List<Callable<Void>> taskss = Arrays.asList(
                    () -> {
                        d1.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d2.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d3.connectDoodle(login, password);
                        return null;
                    },
                    () -> {
                        d4.connectDoodle(login, password);
                        return null;
                    }
            );

            executors.invokeAll(taskss);
            executors.shutdown();


            ExecutorService executor = Executors.newFixedThreadPool(4);

            List<Callable<Void>> tasks = Arrays.asList(
                    () -> {
                        for (String s : list1) {
                            d1.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list2) {
                            d2.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list3) {
                            d3.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    },
                    () -> {
                        for (String s : list4) {
                            d4.getAllTheInfomationPerLink(s);
                        }
                        return null;
                    }

            );
            executor.invokeAll(tasks);
            executor.shutdown();

            d1.driver.quit();
            d2.driver.quit();
            d3.driver.quit();
            d4.driver.quit();


            reunions.addAll(d1.reunions);
            reunions.addAll(d2.reunions);
            reunions.addAll(d3.reunions);
            reunions.addAll(d4.reunions);
        }
    }

    public void connectDoodle(String login, String password) throws InterruptedException, ParseException {
        driver.get("https://doodle.com/login");
        this.pwd = password;
        this.login = login;

        try {
            WebElement emailField = driver.findElement(By.cssSelector("input[name='email']"));
            actions.moveToElement(emailField).click().perform();
            emailField.sendKeys(login);
            WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
            actions.moveToElement(passwordField).click().perform();
            passwordField.sendKeys(password);
            WebElement loginButton = driver.findElement(By.cssSelector(".Button.Button--green[type='submit']"));
            actions.moveToElement(loginButton).click().perform();
            driver.get("https://doodle.com/dashboard");
            //si je me fais rediriger pour me reconnecter
            Thread.sleep(1500);
            if (!driver.getCurrentUrl().equals("https://doodle.com/dashboard")) {
                driver.get(driver.getCurrentUrl());
                try { //accept the cookies
                    Thread.sleep(1000);
                    WebElement acceptButton = driver.findElement(By.id("onetrust-accept-btn-handler"));
                    actions.moveToElement(acceptButton).click().perform();
                } catch (Exception e) {
                    //je n'arrive pas a accepeter les cookies
                }
                while (!driver.getCurrentUrl().equals("https://doodle.com/dashboard")) {      //tant que je ne suis pas connecter retenter
                    //re entrer l'adresse mail et le mdp
                    WebElement emailFieldd = driver.findElement(By.cssSelector("input[name='username']"));
                    actions.moveToElement(emailFieldd).click().perform();
                    emailFieldd.sendKeys(login);
                    WebElement passwordFieldd = driver.findElement(By.cssSelector("input[name='password']"));
                    actions.moveToElement(passwordFieldd).click().perform();
                    passwordFieldd.sendKeys(password);
                    WebElement loginButtonn = driver.findElement(By.cssSelector("Button.Button--blue.Button--login-signup"));
                    actions.moveToElement(loginButtonn).click().perform();
                    driver.get("https://doodle.com/dashboard");
                    Thread.sleep(2000);
                }

            } else {
                //accepter les cookies
                Thread.sleep(2000);
                WebElement acceptButton = driver.findElement(By.id("onetrust-accept-btn-handler"));
                actions.moveToElement(acceptButton).click().perform();
            }

        } catch (Exception e) {
            System.out.println("Je n'arrive pas à me connecter veillez à relancer le programme :)");
        }


    }




    public ArrayList<String> getAllTheLinks() throws InterruptedException {
        Thread.sleep(900);  //temps d'attente obligatoire
        List<WebElement> linksWeb = driver.findElements(By.cssSelector("li:has(a)"));
        linksWeb.remove(0);
        links = new ArrayList<>();
        for (WebElement link : linksWeb) {
            WebElement linkContent = link.findElement(By.cssSelector("article > div > div"));
            boolean isLinkOutdated = linkContent.getAttribute("class").contains("ActivityWrapper--confirmed-past__cf0PxLyioeQK8jfllK64");
            if (!isLinkOutdated) {
                String href = link.findElement(By.cssSelector("a")).getAttribute("href");
                links.add(href);
            }
        }
        return links;
    }


    public void getAllTheInfomationPerLink(String href) throws InterruptedException {
        driver.get(href);
        Thread.sleep(3000);
        //peut etre qu'on nous redemande d'accepter les cookies
        try {
            WebElement acceptButton = driver.findElement(By.id("onetrust-accept-btn-handler"));
            actions.moveToElement(acceptButton).click().perform();
            Thread.sleep(500);
        } catch (Exception e) {
            //  System.out.println("on nous redemande pas d'accepter les cookies");
        }

        //le cas où mon user est l'organisateur

        try {
            Thread.sleep(500);
            //get l'organisateur
            WebElement orga = driver.findElement(By.cssSelector(".OrganizerInfo__invitation-phrase")); // si je n'ai pas cet element ca me fait une erreur et ca veut dire que je ne suis pas l'organisateur
            try {
                WebElement tryCatchElement = driver.findElement(By.cssSelector(".OptionHeader.OptionHeader__option-default"));
                int nombreDeReu = driver.findElements(By.cssSelector(".OptionHeader.OptionHeader__option-default")).size();
                if (nombreDeReu == 1) {
                    ReunionDoodle reunion = new ReunionDoodle();
                    isThePollStillPresent(reunion);
                    if (reunion.isToujourValable()) {
                        reunion.setOrganisateur("MOI");
                        getDetailsOrgaDriver(reunion);
                        reunions.add(reunion);
                    }
                } else if(nombreDeReu != 0 && nombreDeReu != 1) {
                    try {
                        WebElement tryCatchBoolean = driver.findElement(By.cssSelector(".OptionHeader.OptionHeader__option-default"));
                        List<WebElement> elements = driver.findElements(By.cssSelector(".OptionHeader.OptionHeader__option-default"));
                        ReunionDoodle reunionDoodle = new ReunionDoodle();
                        List<WebElement> filteredElements = new ArrayList<>();
                        for (WebElement element : elements) {
                            if (!element.getAttribute("class").contains("OptionHeader__option-past")) {
                                filteredElements.add(element);
                            }
                        }
                        for (WebElement e : filteredElements) {
                            getDetailsOrga(reunionDoodle, e);
                        }
                        /*for (PropsDoodle p : reunionDoodle.getPropsDoodleList()) {
                            System.out.println("Nom : " + reunionDoodle.getNom() + "\nDate : " + p.getDate() + "\nHeure debut :" + p.getHeureDebut() + "\nHeure Fin : " + p.getHeureFin() + "\nNombre de votes :" + p.getNbrDeVotant() + "\nMa réponse :" + p.getReponse());
                        }
                         */
                        reunionDoodle.setOrganisateur("MOI");
                        reunions.add(reunionDoodle);
                    }catch (Exception ee){

                    }
                }
            }catch (Exception ee){
                try{
                    WebElement tryCatchBoolean = driver.findElement(By.cssSelector(".OptionHeader.OptionHeader__option-selection"));
                    List<WebElement> elements = driver.findElements(By.cssSelector(".OptionHeader.OptionHeader__option-selection"));
                    List<WebElement> filteredElements = new ArrayList<>();
                    for (WebElement element : elements) {
                        if (!element.getAttribute("class").contains("OptionHeader__option-past")) {
                            filteredElements.add(element);
                        }
                    }
                    ReunionDoodle reunionDoodle = new ReunionDoodle();
                    for (WebElement e : filteredElements) {
                        getDetailsOrga(reunionDoodle, e);
                    }
                    /*
                    for (PropsDoodle p : reunionDoodle.getPropsDoodleList()) {
                        System.out.println("Nom : " + reunionDoodle.getNom() + "\nDate : " + p.getDate() + "\nHeure debut :" + p.getHeureDebut() + "\nHeure Fin : " + p.getHeureFin() + "\nNombre de votes :" + p.getNbrDeVotant() + "\nMa réponse :" + p.getReponse());
                    }
                     */
                    reunionDoodle.setOrganisateur("MOI");
                    reunions.add(reunionDoodle);
                }catch (Exception eee){

                }
            }

            try {
                //check si cet element est present avant de faire un appel de fonction
                WebElement dateElement = driver.findElement(By.cssSelector("div.MetadataItem.MeetingMetadataInfo__meeting-time > div.MetadataItem__content > div > div"));
                if(isDateAfterCurrentDate()){
                    ReunionDoodle reunionDoodle = new ReunionDoodle();
                    getInformationPollBookedOrga(reunionDoodle);
                    reunionDoodle.setOrganisateur("MOI");
                    reunionDoodle.getPropsDoodleList().get(0).setReponse(Reponse.ORGANISATEUR);
                    reunions.add(reunionDoodle);
                }

            }catch (Exception e){

            }
        } catch (Exception e) {
            //  System.out.println("je ne suis pas l'organisateur");
        }


        //le cas quand je ne suis pas l'organisateur
        try {
            Thread.sleep(1000);
            WebElement etat_reunion = driver.findElement(By.cssSelector("h1[data-testid='instructions-main-title']"));  //soit response submitted soit select your prefered time
            String text = etat_reunion.getText();
            if(text.equals("You’re booked!")) {
                ReunionDoodle reunion = new ReunionDoodle();
                getInformationPollBooked(reunion);
                reunion.getPropsDoodleList().get(0).setReponse(Reponse.OUI);
                reunions.add(reunion);
            }
            else {
                int nombreDeReu = driver.findElements(By.cssSelector(".votes-option-module_day__GfSdw")).size(); //le cas où je n'ai qu'une seul reunion
                if (nombreDeReu == 1) {
                    ReunionDoodle reunion = new ReunionDoodle();
                    //voir si la reunion est deja passé avant de s'aventurer
                    isThePollStillPresent(reunion);
                    if (reunion.isToujourValable()) {
                        if (text.equals("Response submitted!")) {
                            Thread.sleep(3000);
                            try {
                                getDetailsInviteDriver(reunion);
                                setTheUserResponse(reunion.getPropsDoodleList().get(0));
                                reunions.add(reunion);

                            }catch (Exception e) {
                                System.out.println("Je n'arrive pas avoir les details d'une reunion");
                            }
                        } else if (text.equals("Select your preferred times")) {
                            reunion.setLink(href);
                            getDetailsInviteDriver(reunion);
                            //validerReunion(reunion);
                            reunions.add(reunion);
                        }
                    }
                } else if(nombreDeReu != 0 && nombreDeReu != 1) {

                    //Je prends toute les réunion et je scroll pour charger toute les reu
                    long m = 0;
                    while(true){ //je scroll jusqu'à la fin
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        long scrollHeight = (long) js.executeScript("return Math.max( document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight );");
                        long scrollPosition = m;
                        long scrollBy = 20;
                        while (scrollPosition < scrollHeight) {
                            scrollPosition += scrollBy;
                            if (scrollPosition > scrollHeight) {
                                scrollBy -= scrollPosition - scrollHeight;
                                scrollPosition = scrollHeight;
                            }
                            js.executeScript("window.scrollTo(0, " + scrollPosition + ");");
                            Thread.sleep(50);
                        }
                        if(m != scrollPosition){
                            m = scrollPosition;
                        }else {
                            break;
                        }
                        Thread.sleep(500);
                    }

                    WebElement tryCatchWebElement = driver.findElement(By.cssSelector(".votes-option-module_day__tooltip-wrap__qD4ol"));
                    List<WebElement> touteLesReunion = driver.findElements(By.cssSelector(".votes-option-module_votes-option__LxLEl"));
                    //System.out.println("Nombre de prop: "+touteLesReunion.size());
                    ReunionDoodle r = new ReunionDoodle();

                    List<WebElement> filteredElements = new ArrayList<>();
                    for (WebElement element : touteLesReunion) {
                        if (!element.getAttribute("class").contains("votes-option-module_votes-option--past__TQ0XP")) {
                            filteredElements.add(element);
                        }
                    }
                    //System.out.println("Nombre de prop still present: "+filteredElements.size());
                    for (WebElement webElement : filteredElements) {
                        getDetailsInvite(r, webElement);
                    }
                    /*for (PropsDoodle p : r.getPropsDoodleList()) {
                        System.out.println("Nom : " + r.getNom() + "\nDate : " + p.getDate() + "\nHeure debut :" + p.getHeureDebut() + "\nHeure Fin : " + p.getHeureFin() + "\nNombre de votes :" + p.getNbrDeVotant()+"\nMa réponse :" + p.getReponse());
                    }
                     */
                    reunions.add(r);
                    //passer à la page d'apres vu que ca affiche que 5 webelement par 5 webElement



                }
            }
        } catch (Exception e) {

        }
    }


    public void getDetailsInvite(ReunionDoodle reunion, WebElement webElement){
        PropsDoodle propsDoodle = new PropsDoodle();
        //getTitre
        try {
            WebElement titleElement = driver.findElement(By.cssSelector(".metadata-title-module_metadata-title__AWda0"));
            reunion.setNom(titleElement.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir le titre");
        }

        //get Heure Depart et fin
        try {
            WebElement timeDiv = webElement.findElement(By.cssSelector(".votes-option-module_day__time__NQeTT"));
            List<WebElement> timeElements = timeDiv.findElements(By.cssSelector("p.chakra-text.css-lc98d3"));
            propsDoodle.setHeureDebut(timeElements.get(0).getText());
            propsDoodle.setHeureFin(timeElements.get(1).getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir l'horaire");
        }

        //getLaDate
        try {
            WebElement dateDiv = webElement.findElement(By.cssSelector(".votes-option-module_day__info__RsRB1"));
            String day = dateDiv.findElement(By.cssSelector(".votes-option-module_day__weekday__-3EBm")).getText();
            String date = dateDiv.findElement(By.cssSelector(".votes-option-module_day__date__AYz6T")).getText();
            String month = dateDiv.findElement(By.cssSelector(".votes-option-module_day__month__r-IA4")).getText();
            String dateString = day + " " + date + " " + month;
            propsDoodle.setDate(dateString);
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la date");
        }

        //get Duree
        try {
            WebElement durationElement = driver.findElement(By.cssSelector(".metadata-duration-module_metadata-duration__e-26U"));
            reunion.setDuree(durationElement.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la durée");
        }

        //set the year of the poll + get la date exploitable

        try {
            propsDoodle.setDate(setTheYearOfThePoll(propsDoodle));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        // get la localisation
        try {
            List<WebElement> toggleButton = driver.findElements(By.id("metadata-module_metadata__toggle-button__sdBqo"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", toggleButton.get(0));
            Thread.sleep(500);
            // obtenir le label du svg
            String svgLabel = toggleButton.get(0).findElement(By.tagName("svg")).getAttribute("aria-label");
            if(svgLabel.equals("ChevronDown")) {
                actions.moveToElement(toggleButton.get(0)).click().perform();
                Thread.sleep(500);
            }
            WebElement loc = driver.findElement(By.cssSelector(".metadata-location-module_metadata-location__a-TWl"));
            WebElement l = loc.findElement(By.cssSelector("p"));
            if (l.getText().equals("Switzerland - Zürich, Genève, Basel, Lausanne")) {
                reunion.setLocalisation("Localisation non précisé");
            } else {
                reunion.setLocalisation(l.getText());
            }

        } catch (Exception e) {
            reunion.setLocalisation("Localisation non précisé");
            System.out.println("Localisation non précisé");
        }


        // get l'organisateur
        try {
            WebElement organizerDiv = driver.findElement(By.cssSelector(".metadata-organizer-module_metadata-organizer__q4-XS"));
            WebElement organizerName = organizerDiv.findElement(By.cssSelector(".metadata-organizer-module_metadata-organizer__name__wk00p"));
            reunion.setOrganisateur(organizerName.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir l'organisateur");
        }

        // get nombre de votant
        try{
            WebElement button = webElement.findElement(By.cssSelector("button.votes-option-module_total-votes__cd8Rq"));
            String voteCount = button.findElement(By.cssSelector("p")).getText();
            propsDoodle.setNbrDeVotant(voteCount);
        }catch (Exception e){
            try{
                WebElement button = webElement.findElement(By.className("votes-option-module_total-votes__cd8Rq"));
                String chiffre = button.findElement(By.tagName("p")).getText();
                propsDoodle.setNbrDeVotant(chiffre);
            }catch (Exception ee) {
                System.out.println("je n'arrive pas à avoir le nombre de votant");
            }
        }

        //get Timezone
        try {
            // Trouver l'élément span contenant le fuseau horaire
            WebElement timeZoneElement = driver.findElement(By.className("timezone-label-module_timezone-label__rXIc6"));

            // Extraire le fuseau horaire en le récupérant dans le texte de l'élément
            String timeZone = timeZoneElement.getText();
            propsDoodle.setTimeZone(timeZone);
        } catch (Exception ee) {
            System.out.println("Je n'arrive pas à avoir le timeZone");
        }


        setTheUserResponseWebElement(propsDoodle, webElement);

        //getTheUserResponse
        reunion.getPropsDoodleList().add(propsDoodle);

    }



    public void getDetailsInviteDriver(ReunionDoodle reunion){
        PropsDoodle propsDoodle = new PropsDoodle();
        //getLaDate
        try {
            WebElement dateDiv = driver.findElement(By.cssSelector(".votes-option-module_day__info__RsRB1"));
            String day = dateDiv.findElement(By.cssSelector(".votes-option-module_day__weekday__-3EBm")).getText();
            String date = dateDiv.findElement(By.cssSelector(".votes-option-module_day__date__AYz6T")).getText();
            String month = dateDiv.findElement(By.cssSelector(".votes-option-module_day__month__r-IA4")).getText();
            String dateString = day + " " + date + " " + month;
            //reunion.setDate(dateString);
            propsDoodle.setDate(dateString);
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la date");
        }

        //getTitre
        try {
            WebElement titleElement = driver.findElement(By.cssSelector(".metadata-title-module_metadata-title__AWda0"));
            reunion.setNom(titleElement.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir le titre");
        }

        //get Heure Depart et fin
        try {
            WebElement timeDiv = driver.findElement(By.cssSelector(".votes-option-module_day__time__NQeTT"));
            List<WebElement> timeElements = timeDiv.findElements(By.cssSelector("p.chakra-text.css-lc98d3"));
            //reunion.setHeureDepart(timeElements.get(0).getText());
            //reunion.setHeureFin(timeElements.get(1).getText());
            propsDoodle.setHeureDebut(isTheHourConforme(timeElements.get(0).getText()));
            propsDoodle.setHeureFin(isTheHourConforme(timeElements.get(1).getText()));
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir l'horaire");
        }

        //get Timezone
        try {
            // Trouver l'élément span contenant le fuseau horaire
            WebElement timeZoneElement = driver.findElement(By.className("current-timezone-module_label-wrapper__Vasq8"));

            // Extraire le fuseau horaire en le récupérant dans le texte de l'élément
            String timeZone = timeZoneElement.getText();
            propsDoodle.setTimeZone(timeZone);
        } catch (Exception ee) {
            System.out.println("Je n'arrive pas à avoir le timeZone");
        }

        //get Duree
        try {
            WebElement durationElement = driver.findElement(By.cssSelector(".metadata-duration-module_metadata-duration__e-26U"));
            reunion.setDuree(durationElement.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la durée");
        }

        //set the year of the poll + get la date exploitable

        try {
            //reunion.setDate(setTheYearOfThePoll(reunion));
            propsDoodle.setDate(setTheYearOfThePoll(propsDoodle));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


// get la localisation
        try {
            List<WebElement> toggleButton = driver.findElements(By.id("metadata-module_metadata__toggle-button__sdBqo"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", toggleButton.get(0));
            Thread.sleep(500);
            // obtenir le label du svg
            String svgLabel = toggleButton.get(0).findElement(By.tagName("svg")).getAttribute("aria-label");
            if(svgLabel.equals("ChevronDown")) {
                actions.moveToElement(toggleButton.get(0)).click().perform();
            }
            WebElement loc = driver.findElement(By.cssSelector(".metadata-location-module_metadata-location__a-TWl"));
            WebElement l = loc.findElement(By.cssSelector("p"));
            if (l.getText().equals("Switzerland - Zürich, Genève, Basel, Lausanne")) {
                reunion.setLocalisation("Localisation non précisé");
            } else {
                reunion.setLocalisation(l.getText());
            }

        } catch (Exception e) {
            reunion.setLocalisation("Localisation non précisé");
            //System.out.println("Localisation non précisé");
        }


        // get l'organisateur
        try {
            WebElement organizerDiv = driver.findElement(By.cssSelector(".metadata-organizer-module_metadata-organizer__q4-XS"));
            WebElement organizerName = organizerDiv.findElement(By.cssSelector(".metadata-organizer-module_metadata-organizer__name__wk00p"));
            reunion.setOrganisateur(organizerName.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir l'organisateur");
        }
        reunion.getPropsDoodleList().add(propsDoodle);
    }


    public void getDetailsOrga(ReunionDoodle reunionDoodle, WebElement webElement){
        PropsDoodle propsDoodle = new PropsDoodle();
        try {
            //getTheDuration
            Thread.sleep(500);
            List<WebElement> conteneur = driver.findElements(By.cssSelector(".MetadataItem__content"));
            reunionDoodle.setDuree(conteneur.get(0).getText());
            //reunionDoodle.setLocalisation(conteneur.get(1).getText());
        } catch (Exception e) {
            System.out.println("Je n'arrive pas à avoir la duree");
        }

        //get The location
        try{
            WebElement button = driver.findElement(By.cssSelector(".Button--linkDark.Metadata__toggle-info-button > span"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            if (button.getText().equals("Show meeting details")) {
                actions.moveToElement(button).click().perform();
                Thread.sleep(500);
                WebElement location = driver.findElement(By.cssSelector(".MeetingMetadataInfo__location-text"));
                reunionDoodle.setLocalisation(location.getText());
            }
        }catch (Exception e){
            System.out.println("je n'arrive pas avoir la location");
        }


        //getTitle
        try {
            WebElement title = driver.findElement(By.cssSelector(".OpenStatePageContent__meeting-title"));
            reunionDoodle.setNom(title.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la titre");
        }

        //get nombre de vote
        try {
            // Find the div containing the "1"
            WebElement element = webElement.findElement(By.cssSelector("div.OptionHeader__participants"));
            // Get the text content of the div
            WebElement e = element.findElement(By.cssSelector("div"));
            String content = e.getText();
            propsDoodle.setNbrDeVotant(content);
        }catch (Exception e){
            System.out.println("Je n'arrive pas à avoir le nombre de votant");
        }

        //getFuseauHoraire
        try{
            WebElement element = driver.findElement(By.cssSelector("div.MetadataItem__content span[data-testid='meeting-metadata-info-timezone']"));
            //reunionDoodle.setFuseauHoraire(element.getText());
            propsDoodle.setTimeZone(element.getText());
        }catch (Exception e) {
            System.out.println("je n'arrive pas a avoir le fuseau horaire");
        }


        //get Date et Heure
        try {
            WebElement infoReu = webElement.findElement(By.cssSelector(".OptionHeader__date"));
            List<WebElement> elements = infoReu.findElements(By.cssSelector("div"));
            String date = elements.get(2).getText() + " " + elements.get(1).getText() + " " + elements.get(0).getText();
            String debut = elements.get(3).getText();
            String fin = elements.get(4).getText();
            propsDoodle.setDate(date);
            propsDoodle.setHeureDebut(debut);
            propsDoodle.setHeureFin(fin);
            propsDoodle.setDate(setTheYearOfThePoll(propsDoodle));
        } catch (Exception e) {
            System.out.println("je n'arrive a avoir la date et l'heure de la reunion");
        }
        propsDoodle.setReponse(Reponse.ORGANISATEUR);
        reunionDoodle.getPropsDoodleList().add(propsDoodle);
    }



    public void getDetailsOrgaDriver(ReunionDoodle reunionDoodle){
        try {
            //showMore
            WebElement buttonShowMore = driver.findElement(By.cssSelector(".Button.Button--linkDark.Metadata__toggle-info-button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buttonShowMore);
            actions.moveToElement(buttonShowMore).click().perform();
            //getTheDuration and localisation
            Thread.sleep(500);
            List<WebElement> conteneur = driver.findElements(By.cssSelector(".MetadataItem__content"));
            reunionDoodle.setDuree(conteneur.get(0).getText());
            reunionDoodle.setLocalisation(conteneur.get(1).getText());
        } catch (Exception e) { //si la localisation est non precisé, alors le bouton show more n'existe pas
            try {
                List<WebElement> conteneur = driver.findElements(By.cssSelector(".MetadataItem__content"));
                reunionDoodle.setDuree(conteneur.get(0).getText());
            } catch (Exception ee) {
                System.out.println("je n'arrive pas a avoir la localisation");
            }

        }

        //getTitle
        try {
            WebElement title = driver.findElement(By.cssSelector(".OpenStatePageContent__meeting-title"));
            reunionDoodle.setNom(title.getText());
        } catch (Exception e) {
            System.out.println("je n'arrive pas a avoir la titre");
        }

        PropsDoodle propsDoodle = new PropsDoodle();
        //get Date et Heure
        try {
            WebElement infoReu = driver.findElement(By.cssSelector(".OptionHeader__date"));
            List<WebElement> elements = infoReu.findElements(By.cssSelector("div"));
            String date = elements.get(2).getText() + " " + elements.get(1).getText() + " " + elements.get(0).getText();
            String debut = elements.get(3).getText();
            String fin = elements.get(4).getText();
            propsDoodle.setDate(date);
            propsDoodle.setHeureDebut(isTheHourConforme(debut));
            propsDoodle.setHeureFin(isTheHourConforme(fin));
            propsDoodle.setDate(setTheYearOfThePoll(propsDoodle));
        } catch (Exception e) {
            System.out.println("je n'arrive a avoir la date et l'heure de la reunion");
        }
        propsDoodle.setReponse(Reponse.ORGANISATEUR);

        //get The location
        try{
            WebElement button = driver.findElement(By.cssSelector(".Button--linkDark.Metadata__toggle-info-button > span"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            if (button.getText().equals("Show meeting details")) {
                actions.moveToElement(button).click().perform();
                WebElement location = driver.findElement(By.cssSelector(".MeetingMetadataInfo__location-text"));
                reunionDoodle.setLocalisation(location.getText());
            }
        }catch (Exception e){
            System.out.println("je n'arrive pas avoir la location");
        }

        //getFuseauHoraire
        try{
            WebElement element = driver.findElement(By.cssSelector("div.MetadataItem__content span[data-testid='meeting-metadata-info-timezone']"));
            //reunionDoodle.setFuseauHoraire(element.getText());
            propsDoodle.setTimeZone(element.getText());
        }catch (Exception e) {
            System.out.println("je n'arrive pas a avoir le fuseau horaire");
        }
        reunionDoodle.getPropsDoodleList().add(propsDoodle);

    }

    public void getInformationPollBooked(ReunionDoodle reunionDoodle){
        PropsDoodle propsDoodle = new PropsDoodle();
        try {
            // Recherche de l'élément div avec le CSS selector
            WebElement selectedOption = driver.findElement(By.cssSelector("div[data-testid='selectedOption']"));
            // Récupération de la date
            String date = selectedOption.findElement(By.className("selected-option-module_day__weekday__jBEt4"))
                    .getText() + " " + selectedOption.findElement(By.className("selected-option-module_day__date__Law46"))
                    .getText() + " " + selectedOption.findElement(By.className("selected-option-module_day__month__JA9dp"))
                    .getText();

            // Récupération des heures de départ et de fin
            String heureDepart = selectedOption.findElement(By.className("selected-option-module_day__time__m82-f"))
                    .findElements(By.className("chakra-text")).get(0).getText();
            String heureFin = selectedOption.findElement(By.className("selected-option-module_day__time__m82-f"))
                    .findElements(By.className("chakra-text")).get(1).getText();
            propsDoodle.setHeureDebut(isTheHourConforme(heureDepart));
            propsDoodle.setHeureFin(isTheHourConforme(heureFin));
            propsDoodle.setDate(date);
            propsDoodle.setDate(setTheYearOfThePoll(propsDoodle));
        }catch (Exception e){
            System.out.println("Je n'arrive pas à avoir la date et l'heure de depart et l'heure de fin");
        }
        // get the organizer name
        try {
            WebElement organizerElement = driver.findElement(By.cssSelector(".metadata-organizer-module_metadata-organizer__name__wk00p"));
            String organizerName = organizerElement.getText();
            reunionDoodle.setOrganisateur(organizerName);
        }catch (Exception e){
            System.out.println("je n'aarive pas à avoir l'organisateur");
        }

        // get the meeting title
        try {
            WebElement meetingTitleElement = driver.findElement(By.cssSelector(".metadata-title-module_metadata-title__AWda0"));
            String meetingTitle = meetingTitleElement.getText();
            reunionDoodle.setNom(meetingTitle);
        }catch (Exception e){
            System.out.println("je n'aarive pas à avoir le titre");

        }
        // get la localisation
        try {
            List<WebElement> toggleButton = driver.findElements(By.id("metadata-module_metadata__toggle-button__sdBqo"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", toggleButton.get(0));
            Thread.sleep(500);
            // obtenir le label du svg
            String svgLabel = toggleButton.get(0).findElement(By.tagName("svg")).getAttribute("aria-label");
            if(svgLabel.equals("ChevronDown")) {
                actions.moveToElement(toggleButton.get(0)).click().perform();
            }
            WebElement loc = driver.findElement(By.cssSelector(".metadata-location-module_metadata-location__a-TWl"));
            WebElement l = loc.findElement(By.cssSelector("p"));
            if (l.getText().equals("Switzerland - Zürich, Genève, Basel, Lausanne")) {
                reunionDoodle.setLocalisation("Localisation non précisé");
            } else {
                reunionDoodle.setLocalisation(l.getText());
            }

        } catch (Exception e) {
            reunionDoodle.setLocalisation("Localisation non précisé");
            //System.out.println("Localisation non précisé");
        }

        // get the timezone
        try {
            WebElement element = driver.findElement(By.className("timezone-label-module_timezone-label__rXIc6"));
            String timezone = element.getText();
            propsDoodle.setTimeZone(timezone);
        }catch (Exception e){
            System.out.println("je n'arive pas à avoir le timezone");

        }



        reunionDoodle.getPropsDoodleList().add(propsDoodle);
    }

    public void getInformationPollBookedOrga(ReunionDoodle reunionDoodle) {
        PropsDoodle propsDoodle = new PropsDoodle();
        reunionDoodle.getPropsDoodleList().add(propsDoodle);
        //get Titre
        try {
            // Recherche des éléments contenant les informations
            WebElement titleElement = driver.findElement(By.cssSelector(".ClosedStatePageContent__meeting-title"));
            WebElement metadataElement = driver.findElement(By.cssSelector(".ClosedStatePageContent__metadata"));
            // Extraction du titre
            String title = titleElement.getText();
            reunionDoodle.setNom(title);
        } catch (Exception e) {
            System.out.println("je n'arrive pas avoir le titre");
        }

        //get date
        try {
            // Récupération de l'élément contenant l'heure de début et l'heure de fin
            WebElement element = driver.findElement(By.className("MetadataItem__content"));

            // Extraire l'heure de début et l'heure de fin
            String text = element.getText();
            String[] parts = text.split("•");

            String date = parts[0].trim();
            reunionDoodle.getPropsDoodleList().get(0).setDate(formatDate(date));
        }catch (Exception e){
            System.out.println("Je n'arrive pas à avoir la date");
        }
        try {// Rechercher l'élément contenant les informations de temps
            WebElement meetingTimeElement = driver.findElement(By.className("MetadataItem__content"));
            // Extraire le texte de l'élément
            String meetingTimeText = meetingTimeElement.getText();
            // Diviser le texte en deux parties séparées par le symbole "•"
            String[] meetingTimeParts = meetingTimeText.split("•");
            // Extraire les heures de début et de fin des parties de texte
            String startTime = meetingTimeParts[1].trim().split("-")[0].trim();
            String midEndTime = meetingTimeParts[1].trim().split("-")[1].trim();
            String[] midd = midEndTime.split(" ");
            String start = startTime + " " + midd[1];
            String end = midEndTime;
            // Afficher les heures de début et de fin
            //System.out.println("Heure de début : " + start);
            //System.out.println("Heure de fin : " + end);
            reunionDoodle.getPropsDoodleList().get(0).setHeureDebut(isTheHourConforme(start));
            reunionDoodle.getPropsDoodleList().get(0).setHeureFin(isTheHourConforme(end));
        }catch (Exception e){
            System.out.println("Je n'arrive pas à avoir l'heure de début et l'heure de fin");
        }

        //get The location
        try{
            WebElement button = driver.findElement(By.cssSelector(".Button--linkDark.Metadata__toggle-info-button > span"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            if (button.getText().equals("Show meeting details")) {
                actions.moveToElement(button).click().perform();
                Thread.sleep(500);
                WebElement location = driver.findElement(By.cssSelector(".MeetingMetadataInfo__location-text"));
                reunionDoodle.setLocalisation(location.getText());
            }
        }catch (Exception e){
            System.out.println("je n'arrive pas avoir la location");
        }

        //get Time Zone
        try{
            WebElement timeZoneText = driver.findElement(By.cssSelector(".MeetingMetadataInfo__timezone-info span"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", timeZoneText);
            reunionDoodle.getPropsDoodleList().get(0).setTimeZone(timeZoneText.getText());
        }catch (Exception e){
            System.out.println("Je n'arrive pas à avoir le timezone");
        }

    }
    public String formatDate(String dateStr) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, inputFormatter);
        return outputFormatter.format(date);
    }


    public void setTheUserResponse(PropsDoodle propsDoodle) {        //On peut savoir que si j'ai repondu oui ou maybe
        try {
            WebElement e = driver.findElement(By.cssSelector(".votes-option-module_day__vote__lXE0z"));
            WebElement element = e.findElement(By.cssSelector("[aria-label]"));
            String ariaLabel = element.getAttribute("aria-label");
            if(ariaLabel.toLowerCase().equals("checkmark")){
                propsDoodle.setReponse(Reponse.OUI);
            }
            else if(ariaLabel.toLowerCase().equals("bracketscheck")){
                propsDoodle.setReponse(Reponse.PEUTETRE);
            } else if (ariaLabel.toLowerCase().equals("cross")) {
                propsDoodle.setReponse(Reponse.NON);
            }

        } catch (Exception e) {
            //System.out.println("j'arrive pas a avoir la reponse du user");
            propsDoodle.setReponse(Reponse.ATTENTE);
        }
    }
    public void setTheUserResponseWebElement(PropsDoodle propsDoodle, WebElement webElement) {        //On peut savoir que si j'ai repondu oui ou maybe
        try {

            WebElement e = webElement.findElement(By.cssSelector(".votes-option-module_day__vote__lXE0z"));
            WebElement element = e.findElement(By.cssSelector("[aria-label]"));
            String ariaLabel = element.getAttribute("aria-label");
            if(ariaLabel.toLowerCase().equals("checkmark")){
                propsDoodle.setReponse(Reponse.OUI);
            }
            else if(ariaLabel.toLowerCase().equals("bracketscheck")){
                propsDoodle.setReponse(Reponse.PEUTETRE);
            } else if (ariaLabel.toLowerCase().equals("cross")) {
                propsDoodle.setReponse(Reponse.NON);
            }

        } catch (Exception e) {
            propsDoodle.setReponse(Reponse.ATTENTE);
            //System.out.println("j'arrive pas a avoir la reponse du user");
        }

    }


    public List<ReunionDoodle> getReunions(){
        return reunions;
    }
    public List<ReunionDoodle> getReunionsGarde(){
        return reunionsGarde;
    }

    public void isThePollStillPresent(ReunionDoodle reunionDoodle) throws InterruptedException {
        try {
            WebElement changeResponseButton = driver.findElement(By.cssSelector(".OptionHeader__option-past"));
            reunionDoodle.setToujourValable(false);
        }catch (Exception e) {
            try{
                WebElement elemntPast = driver.findElement(By.cssSelector(".votes-option-module_votes-option--past__TQ0XP"));
                reunionDoodle.setToujourValable(false);
            }catch (Exception ee){
                reunionDoodle.setToujourValable(true);
            }

        }
    }


    public void traitementDate(PropsDoodle propsDoodle) throws ParseException {
        String inputDate = propsDoodle.getDate(); // Exemple de date en entrée
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE dd MMM", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = inputFormat.parse(inputDate);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if (date.before(calendar.getTime())) {
            year++;
        }
        calendar.set(Calendar.YEAR, year);
        calendar.setTime(date);
    }


    public int moisNum(String s){
        if(s.equals("JAN")){
            return 1;
        } else if (s.equals("FEB")) {
            return 2;
        } else if (s.equals("MAR")) {
            return 3;
        }else if (s.equals("APR")) {
            return 4;
        }else if (s.equals("MAY")) {
            return 5;
        }else if (s.equals("JUN")) {
            return 6;
        }else if (s.equals("JUL")) {
            return 7;
        }else if (s.equals("AUG")) {
            return 8;
        }else if (s.equals("SEP")) {
            return 9;
        }else if (s.equals("OCT")) {
            return 10;
        } else if (s.equals("NOV")) {
            return 11;
        }else if (s.equals("DEC")) {
            return 12;
        }
        return 0;
    }

    public String setTheYearOfThePoll(PropsDoodle propsDoodle) throws ParseException {
        LocalDate now = LocalDate.now();
        String inputDate = propsDoodle.getDate();
        String inputHourB = propsDoodle.getHeureDebut();
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE dd MMM");
        Date date = inputFormat.parse(inputDate);
        SimpleDateFormat outputFormat = new SimpleDateFormat("YYYY-MM-dd");
        String outputDate = outputFormat.format(date);
        String[] act = now.toString().split("-");
        String[] re = outputDate.toString().split("-");
        int moisAct = Integer.parseInt(act[1]);
        int moisRe = Integer.parseInt(re[1]);
        int jourAct = Integer.parseInt(act[2]);
        int jourRe = Integer.parseInt(re[2]);
        if (moisAct < moisRe) {
            SimpleDateFormat outputFormatr = new SimpleDateFormat(act[0] + "-MM-dd");
            String outputDatee = outputFormatr.format(date);
            return outputDatee;
        } else if (moisAct > moisRe) {
            int anneePro = Integer.parseInt(act[0]) + 1;
            SimpleDateFormat outputFormatr = new SimpleDateFormat(anneePro + "-MM-dd");
            String outputDatee = outputFormatr.format(date);
            return outputDatee;
        } else if (moisAct == moisRe) {
            if (jourAct < jourRe) {
                SimpleDateFormat outputFormatr = new SimpleDateFormat(act[0] + "-MM-dd");
                String outputDatee = outputFormatr.format(date);
                return outputDatee;
            } else if (jourAct > jourRe) {
                int anneePro = Integer.parseInt(act[0]) + 1;
                SimpleDateFormat outputFormatr = new SimpleDateFormat(anneePro + "-MM-dd");
                String outputDatee = outputFormatr.format(date);
                return outputDatee;
            } else if (jourAct == jourRe) {
                LocalTime nowe = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                String[] heureAct = formatter.format(nowe).toString().split(" ");
                String[] heureRe = inputHourB.split(" ");
                if (heureAct[1].equals("PM") && heureRe[1].equals("AM")) {
                    int anneePro = Integer.parseInt(act[0]) + 1;
                    SimpleDateFormat outputFormatr = new SimpleDateFormat(anneePro + "-MM-dd");
                    String outputDatee = outputFormatr.format(date);
                    return outputDatee;
                } else if (heureAct[1].equals("AM") && heureRe[1].equals("PM")) {
                    SimpleDateFormat outputFormatr = new SimpleDateFormat(act[0] + "-MM-dd");
                    String outputDatee = outputFormatr.format(date);
                    return outputDatee;
                } else if (heureAct[1].equals(heureRe[1])) {
                    String[] hhmmActuel = heureAct[0].split(":");
                    String[] hhmmRe = heureRe[0].split(":");
                    if (Integer.parseInt(hhmmActuel[0]) > Integer.parseInt(hhmmRe[0])) {
                        int anneePro = Integer.parseInt(act[0]) + 1;
                        SimpleDateFormat outputFormatr = new SimpleDateFormat(anneePro + "-MM-dd");
                        String outputDatee = outputFormatr.format(date);
                        return outputDatee;
                    } else if (Integer.parseInt(hhmmActuel[0]) < Integer.parseInt(hhmmRe[0])) {
                        SimpleDateFormat outputFormatr = new SimpleDateFormat(act[0] + "-MM-dd");
                        String outputDatee = outputFormatr.format(date);
                        return outputDatee;
                    } else if (Integer.parseInt(hhmmActuel[0]) == Integer.parseInt(hhmmRe[0])) {
                        if (Integer.parseInt(hhmmActuel[1]) < Integer.parseInt(hhmmRe[1])) {
                            SimpleDateFormat outputFormatr = new SimpleDateFormat(act[0] + "-MM-dd");
                            String outputDatee = outputFormatr.format(date);
                            return outputDatee;
                        } else if (Integer.parseInt(hhmmActuel[1]) > Integer.parseInt(hhmmRe[1])) {
                            int anneePro = Integer.parseInt(act[0]) + 1;
                            SimpleDateFormat outputFormatr = new SimpleDateFormat(anneePro + "-MM-dd");
                            String outputDatee = outputFormatr.format(date);
                            return outputDatee;
                        }
                    }
                }
            }
        }

        return null;
    }

    public void validerReunion(ReunionDoodle reunion) throws InterruptedException {
        //pas la peine de faire un driver.get(link) car j'utilise cette methode dans la bonne page
        Scanner saisieUtilisateur = new Scanner(System.in);
        //System.out.println("est ce que vous validez l'invistation de " + reunion.getOrganisateur() + " pour la reunion du " + reunion.getDate() + " à " + reunion.getHeureDepart() + "d'une durée de " + reunion.getDuree() + ", Saisir soit Y pour oui ou N pour non ou M pour maybe ou R pour rien :");
        String c = saisieUtilisateur.nextLine();

        if (c.equals("R")) {
            reunion.getPropsDoodleList().get(0).setReponse(Reponse.ATTENTE);
        } else if (c.equals("N")) {
            WebElement footer = driver.findElement(By.cssSelector(".votes-footer-module_votes-footer__X0v9h"));
            List<WebElement> buttons = footer.findElements(By.cssSelector(".chakra-button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buttons.get(0));  //get(1) car c'est le bonton decline
            actions.moveToElement(buttons.get(0)).click().perform();
            Thread.sleep(3000);
            try {
                WebElement submit_response = driver.findElement(By.cssSelector(".css-1j4nv8d"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit_response);
                Thread.sleep(1000);
                actions.moveToElement(submit_response).click().perform();
            } catch (Exception e) {
                System.out.println("je n'arrive pas valider la réunion");
            }
            reunion.getPropsDoodleList().get(0).setReponse(Reponse.NON);
        } else if (c.equals("Y")) {
            //CONFIRMER LA PRESENCE
            try {
                Thread.sleep(2000);
                WebElement checkbox = driver.findElement(By.cssSelector(".votes-option-module_day__checkbox__dk7Vq:has(input)"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkbox);
                actions.moveToElement(checkbox).click().perform();
                Thread.sleep(2000);
                WebElement footer = driver.findElement(By.cssSelector(".votes-footer-module_votes-footer__X0v9h"));
                List<WebElement> buttons = footer.findElements(By.cssSelector(".chakra-button"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buttons.get(1));  //get(1) car c'est le bonton continue
                Thread.sleep(2000);
                actions.moveToElement(buttons.get(1)).click().perform();
                WebElement submit_response = driver.findElement(By.cssSelector(".css-1j4nv8d"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit_response);
                Thread.sleep(1000);
                actions.moveToElement(submit_response).click().perform();
            } catch (Exception e) {
                System.out.println("je n'arrive pas valider la réunion");
            }
            reunion.getPropsDoodleList().get(0).setReponse(Reponse.OUI);
        } else if (c.equals("M")) {
            //CONFIRMER LA PRESENCE
            Thread.sleep(2000);

            WebElement checkbox = driver.findElement(By.cssSelector(".votes-option-module_day__checkbox__dk7Vq:has(input)"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkbox);
            actions.moveToElement(checkbox).click().perform();
            Thread.sleep(500);
            actions.moveToElement(checkbox).click().perform();
            Thread.sleep(2000);
            WebElement footer = driver.findElement(By.cssSelector(".votes-footer-module_votes-footer__X0v9h"));
            List<WebElement> buttons = footer.findElements(By.cssSelector(".chakra-button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", buttons.get(1));  //get(1) car c'est ce qu'on veut
            actions.moveToElement(buttons.get(1)).click().perform();
            Thread.sleep(2000);
            try {
                WebElement submit_response = driver.findElement(By.cssSelector(".css-1j4nv8d"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit_response);
                Thread.sleep(1000);
                actions.moveToElement(submit_response).click().perform();
                reunion.getPropsDoodleList().get(0).setReponse(Reponse.PEUTETRE);
            } catch (Exception e) {
                System.out.println("je n'arrive pas valider la réunion");
            }
        }

    }

    public boolean isDateAfterCurrentDate(){
        WebElement dateElement = driver.findElement(By.cssSelector("div.MetadataItem__content > div > div"));
        // Extraire la date et l'heure de l'élément HTML
        String dateString = dateElement.getText().split(" • ")[0];

        // Formater la date en objet Date Java
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (Exception e) {
            System.out.println("probleeeeeeme");
            return false;
        }
        // Comparer la date extraite avec la date actuelle
        boolean isAfterCurrentDate = date.after(new Date());
        // Renvoyer le booléen indiquant si la date extraite est postérieure à la date actuelle
        return isAfterCurrentDate;
    }

    public String isTheHourConforme(String heure){ //si j'ai une heure = 2 PM je dois la rendre 2:00 PM
        String[] s = heure.split(":");
        if(s.length == 1){
            String[] f = heure.split(" ");
            return f[0]+":00 "+f[1];
        }
        return heure;
    }

    public void setTimeZoneForAllPoll(ArrayList<ReunionDoodle> reunionDoodles){
        String gmt = "";
        for(ReunionDoodle reunionDoodle : reunionDoodles){
            for(PropsDoodle p : reunionDoodle.getPropsDoodleList()){
                String regex = "GMT[+-]\\d+";
                Pattern pattern = Pattern.compile(regex);

                Matcher matcher1 = pattern.matcher(p.getTimeZone());
                if (matcher1.find()) {
                    gmt = matcher1.group();
                    //System.out.println(gmt); // output: GMT+2
                    break;
                }

            }
        }
        for(ReunionDoodle reunionDoodle : reunionDoodles) {
            for (PropsDoodle p : reunionDoodle.getPropsDoodleList()) {
                if (!gmt.equals("")) {
                    p.setTimeZone(gmt);
                }
            }
        }
    }

    public void seConformiser(ArrayList<ReunionDoodle> reunionDoodles) throws ParseException {
        for(ReunionDoodle reunionDoodle : reunionDoodles){
            for(PropsDoodle p : reunionDoodle.getPropsDoodleList()) {
                //conformiser l'heure de debut
                String timeString = p.getHeureDebut();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                Date date = dateFormat.parse(timeString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);

                Props.Hour hourObject = new Props.Hour();
                hourObject.hour = hour;
                hourObject.minute = minute;
                hourObject.second = second;
                p.setHourDebutConforme(hourObject);

                //conformiser le timeZone
                String gmt = p.getTimeZone();
                TimeZone timeZone = TimeZone.getTimeZone(gmt);
                p.setTimeZoneConforme(timeZone);

                //conformiser la date
                String dateString = p.getDate();
                String[] parts = dateString.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                Props.Date date1 = new Props.Date();
                date1.year = year;
                date1.month = month;
                date1.day = day;

                p.setDateConforme(date1);

                //confomiser la map eachAnswer
                if (p.getReponse().equals(Reponse.OUI) || p.getReponse().equals(Reponse.ATTENTE) || p.getReponse().equals(Reponse.ORGANISATEUR)) {
                    p.addAnswer("MOI", pollAnswer.Yes);
                } else if(p.getReponse().equals(Reponse.NON)){
                    p.addAnswer("MOI", pollAnswer.No);
                } else if (p.getReponse().equals(Reponse.PEUTETRE)) {
                    p.addAnswer("MOI", pollAnswer.Maybe);
                }

            }
        }
    }

    public void garderLesCreneauDeReunionSouhaite(ArrayList<ReunionDoodle> reunionDoodles){
        for (ReunionDoodle reunion : reunionDoodles) {
            if (reunion.getPropsDoodleList().size() > 1) {
                ReunionDoodle r = new ReunionDoodle();
                List<PropsDoodle> propsList = reunion.getPropsDoodleList();
                List<PropsDoodle> filteredPropsList = new ArrayList<>();

                // Filtre les propositions avec les réponses "OUI", "ATTENTE", "MAYBE" ou "ORGANISATEUR".
                for (PropsDoodle props : propsList) {
                    if (props.getReponse() == Reponse.OUI || props.getReponse() == Reponse.ATTENTE || props.getReponse() == Reponse.PEUTETRE || props.getReponse() == Reponse.ORGANISATEUR) {
                        filteredPropsList.add(props);
                    }
                }

                // Détermine le nombre maximum de votants.
                int maxVotes = 0;
                for (PropsDoodle props : propsList) {
                    int votes = Integer.parseInt(props.getNbrDeVotant());
                    if (votes > maxVotes) {
                        maxVotes = votes;
                    }
                }

                // Ajoute toutes les propositions qui ont le nombre maximum de votants.
                List<PropsDoodle> finalPropsList = new ArrayList<>();
                for (PropsDoodle props : propsList) {
                    int votes = Integer.parseInt(props.getNbrDeVotant());
                    if (votes == maxVotes) {
                        finalPropsList.add(props);
                    }
                }
                r = reunion;
                r.getPropsDoodleList().clear();
                r.getPropsDoodleList().addAll(finalPropsList);
                reunionsGarde.add(r);

            }
            else if (reunion.getPropsDoodleList().size() == 1){
                    if (reunion.getPropsDoodleList().get(0).getReponse().equals(Reponse.OUI) || reunion.getPropsDoodleList().get(0).getReponse().equals(Reponse.ATTENTE) || reunion.getPropsDoodleList().get(0).getReponse().equals(Reponse.PEUTETRE) || reunion.getPropsDoodleList().get(0).getReponse().equals(Reponse.ORGANISATEUR)) {
                        reunionsGarde.add(reunion);
                    }

            }
        }
    }

}




//ce qui ne marche pas :
//je ne prend pas en compte si j'ai un lien vision
//j'ai un soucis avec l'export CSV



