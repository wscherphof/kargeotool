package nl.b3p.kar.imp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Matthijs Laan
 */
public class KV9ValidationError {
    private boolean fatal;
    private String code;
    private String xmlContext;
    private String context;
    private String value;
    private String message;
    
    public KV9ValidationError(boolean fatal, String code, String xmlContext, String context, String value, String message) {
        this.fatal = fatal;
        this.code = code;
        this.xmlContext = xmlContext;
        this.context = context;
        this.value = value;
        this.message = message;
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getXmlContext() {
        return xmlContext;
    }

    public void setXmlContext(String xmlContext) {
        this.xmlContext = xmlContext;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public JSONObject toJSONObject() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("fatal", fatal);
        j.put("code", code);
        j.put("xmlContext", xmlContext);
        j.put("context", context);
        j.put("value", value);
        j.put("message", message);
        return j;
    }
}
