package org.msf.records.net.model;

import java.util.Map;

/**
 * A single medical concept, usually a question on answer in an observation on a patient. A simple
 * Java bean for GSON converting to and from a JSON encoding. Stores localization and type
 * information.
 */
public class Concept {

    public String uuid;
    /**
     * The server side id. Prefer the UUID for sending to the server, but this is needed for some
     * xforms tasks.
     */
    public Integer xform_id;
    public ConceptType type;

    /**
     * A map from locales to the name in that locale. Eg en->heart, fr->couer, ...
     */
    public Map<String, String> names;
}
