package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import nl.b3p.kar.struts.EditorTreeObject;
import org.json.JSONObject;

public class Activation implements EditorTreeObject {
    public static final String TYPE_PRQA = "PRQA"; /* automatisch */
    public static final String TYPE_PRQM = "PRQM"; /* halteknop */
    public static final String TYPE_SDCAS = "SDCAS"; /* start deur sluiten */
    public static final String TYPE_PRQAA = "PRQAA"; /* altijd automatisch */
    public static final String TYPE_PRQI = "PRQI"; /* via koppelkabel */
    //public static final String TYPE_SHNJ = "SHNJ";
    //public static final String TYPE_SSED = "SSED";
    //public static final String TYPE_AABS = "AABS";

    /* "Emergency Services (Alarm)" */
    public static final String KAR_USAGE_TYPE_ES = "ES";
    /* "Public Transport" */
    public static final String KAR_USAGE_TYPE_PT = "PT";
    /* "Emergency Services Without Alarm" */
    public static final String KAR_USAGE_TYPE_ESWA = "ESWA";
    /* "Demand response Service" */
    //public static final String KAR_USAGE_TYPE_DS = "DS";
    /* "Emergency Services and Public Transport" */
    //public static final String KAR_USAGE_TYPE_ESPT = "ESPT";
    /* "Emergency Services with and without Alarm AND Public Transport" */
    //public static final String KAR_USAGE_TYPE_ESWAPT = "ESWAPT";
    /* "All Categories" */
    public static final String KAR_USAGE_TYPE_ALL = "ALL";

    /* Inmelding */
    //public static final Integer COMMAND_TYPE_ENTERING_ANNOUNCEMENT = 1;
    /* Uitmelding */
    //public static final Integer COMMAND_TYPE_LEAVE_ANNOUNCEMENT = 2;
    /* Vooraanmelding */
    //public static final Integer COMMAND_TYPE_PRE_ANNOUNCEMENT = 3;

    private Integer id;
    private ActivationGroup activationGroup;
    private Integer index;
    private Date validFrom;
    private String karUsageType;
    private String triggerType;
    private Integer commandType;
    private Double karDistanceTillStopLine;
    private Double karTimeTillStopLine;
    private Double karRadioPower;
    private Double metersBeforeRoadsideEquipmentLocation;
    private Double angleToNorth;
    private Point location;
    private String updater;
    private Date updateTime;
    private String validator;
    private Date validationTime;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ActivationGroup getActivationGroup() {
        return activationGroup;
    }

    public void setActivationGroup(ActivationGroup activationGroup) {
        this.activationGroup = activationGroup;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public String getKarUsageType() {
        return karUsageType;
    }

    public void setKarUsageType(String karUsageType) {
        this.karUsageType = karUsageType;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getCommandType() {
        return commandType;
    }

    public void setCommandType(Integer commandType) {
        this.commandType = commandType;
    }

    public Double getKarDistanceTillStopLine() {
        return karDistanceTillStopLine;
    }

    public void setKarDistanceTillStopLine(Double karDistanceTillStopLine) {
        this.karDistanceTillStopLine = karDistanceTillStopLine;
    }

    public Double getKarTimeTillStopLine() {
        return karTimeTillStopLine;
    }

    public void setKarTimeTillStopLine(Double karTimeTillStopLine) {
        this.karTimeTillStopLine = karTimeTillStopLine;
    }

    public Double getKarRadioPower() {
        return karRadioPower;
    }

    public void setKarRadioPower(Double karRadioPower) {
        this.karRadioPower = karRadioPower;
    }

    public Double getMetersBeforeRoadsideEquipmentLocation() {
        return metersBeforeRoadsideEquipmentLocation;
    }

    public void setMetersBeforeRoadsideEquipmentLocation(Double metersBeforeRoadsideEquipmentLocation) {
        this.metersBeforeRoadsideEquipmentLocation = metersBeforeRoadsideEquipmentLocation;
    }

    public Double getAngleToNorth() {
        return angleToNorth;
    }

    public void setAngleToNorth(Double angleToNorth) {
        this.angleToNorth = angleToNorth;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getLocationString() {
        if(location != null) {
            NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
            nf.setGroupingUsed(false);
            return nf.format(location.getCoordinate().x) + ", " + nf.format(location.getCoordinate().y);
        } else {
            return null;
        }
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public Date getValidationTime() {
        return validationTime;
    }

    public void setValidationTime(Date validationTime) {
        this.validationTime = validationTime;
    }

    public JSONObject serializeToJson() throws Exception {
        return serializeToJson(true);
    }

    public JSONObject serializeToJson(boolean includeChildren) throws Exception {
        JSONObject j = new JSONObject();
        j.put("type", "a");
        j.put("id", "a:" + getId());
        NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
        nf.setGroupingUsed(false);
        String namePart = "";
        if(commandType != null) {
            switch(commandType.intValue()) {
                case 1: namePart = "Inm"; break;
                case 2: namePart = "Uit"; break;
                case 3: namePart = "Voor"; break;
            }
        }
        j.put("name", namePart +
                  (getKarDistanceTillStopLine() == null ? ""
                : " " + nf.format(getKarDistanceTillStopLine()) + "m"
                + (getKarTimeTillStopLine() == null ? ""
                : " " + nf.format(getKarTimeTillStopLine()) + "s")));

        j.put("index", getIndex());
        j.put("point", getLocationString());
        return j;
    }
}
