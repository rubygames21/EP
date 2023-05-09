package com.example.noodleapp;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

/** Improved list collection to get HTML elements. */
public class MyList<T extends HtmlElement> extends ArrayList<T> {

    public MyList() {
        super();
    }

    /** Gets first HTML element of the list. */
    public T getFirst() {
        return get(0);
    }

    /** Gets last HTML element of the list. */
    public T getLast() {
        return get(size()-1);
    }

    /** Gets a HTML-element by the name of its class attribute. */
    public T get(String classNameOfElement) {
        for ( T el : this ) {
            try {
                if (el.getAttributesMap().get("class").getValue().equals(classNameOfElement))
                    return el;
            }
            catch(Exception e) {
                // Absorbe plantage si tel ou tel appel intermédiaire est null ci-dessus
            }
        }
        return null;
    }

}

