package nl.b3p.kar.struts;

import org.json.JSONObject;

public interface EditorTreeObject {
    /**
     * Indien in het JSON object geen children property moet opgenomen kan dat
     * met de parameter includeChildren worden aangegeven (tbv updaten alleen
     * dit item in de tree)
     */
    public JSONObject serializeToJson(boolean includeChildren) throws Exception;
    
    public JSONObject serializeToJson() throws Exception;
}