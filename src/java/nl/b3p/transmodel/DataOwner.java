package nl.b3p.transmodel;

public class DataOwner {
    private String code;
    private String type;
    private String name;
    private String description;
    private boolean validationRequired;

    /**
     * Waarde voor type indien data owner een <b>wegbeheerder</b> is.
     */
    public static final String TYPE_ROOW = "ROOW";
    /**
     * Waarde voor type indien data owner een <b>vervoerder</b> is.
     */
    public static final String TYPE_PUCO = "PUCO";
    /**
     * Waarde voor type indien data owner een <b>concessieverlener</b> is.
     */
    public static final String TYPE_COPR = "COPR";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
    }
}
