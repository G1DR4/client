package org.msf.records.net.model;

import java.util.Map;

/**
 * A single medical concept, usually a question on answer in an observation on a patient. A simple
 * Java bean for GSON converting to and from a JSON encoding. Stores localization and type
 * information.
 */
public class Concept {

    /**
     * UUID for the (question) concept for the temperature.
     */
    public static final String TEMPERATURE_UUID = "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (question) concept for Diarrhea.
     */
    public static final String DIARRHEA_UUID = "142412AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (question) concept for Vomiting.
     */
    public static final String VOMITING_UUID = "122983AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (question) concept for the notes field.
     */
    public static final String NOTES_UUID = "162169AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (answer) concept None, used for Diarrhea and vomiting.
     */
    public static final String NONE_UUID = "1107AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (answer) concept Mild, used for Diarrhea and vomiting.
     */
    public static final String MILD_UUID = "1498AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (answer) concept Moderate, used for Diarrhea and vomiting.
     */
    public static final String MODERATE_UUID = "1499AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    /**
     * UUID for the (answer) concept Moderate, used for Diarrhea and vomiting.
     */
    public static final String SEVERE_UUID = "1500AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    public String uuid;
    public ConceptType type;
    /**
     * A map from locales to the name in that locale. Eg en->heart, fr->couer, ...
     */
    public Map<String, String> names;
}
