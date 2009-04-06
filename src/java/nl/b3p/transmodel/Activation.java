package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.util.Date;

public class Activation {
    public static final String TYPE_PRQA = "PRQA";
    public static final String TYPE_PRQM = "PRQM";
    public static final String TYPE_SDCAS = "SDCAS";
    public static final String TYPE_PRQAA = "PRQAA";
    public static final String TYPE_PRQI = "PRQI";
    public static final String TYPE_SHNJ = "SHNJ";
    public static final String TYPE_SSED = "SSED";
    public static final String TYPE_AABS = "AABS";

    /* "Emergency Services (Alarm)" */
    public static final String KAR_USAGE_TYPE_ES = "ES";
    /* "Public Transport" */
    public static final String KAR_USAGE_TYPE_PT = "PT";
    /* "Emergency Services Without Alarm" */
    public static final String KAR_USAGE_TYPE_ESWA = "ESWA";
    /* "Demand response Service" */
    public static final String KAR_USAGE_TYPE_DS = "DS";
    /* "Emergency Services and Public Transport" */
    public static final String KAR_USAGE_TYPE_ESPT = "ESPT";
    /* "Emergency Services with and without Alarm AND Public Transport" */
    public static final String KAR_USAGE_TYPE_ESWAPT = "ESWAPT";
    /* "All Categories" */
    public static final String KAR_USAGE_TYPE_ALL = "ALL";

    /* Inmelding */
    public static final int COMMAND_TYPE_ENTERING_ANNOUNCEMENT = 1;
    /* Uitmelding */
    public static final int COMMAND_TYPE_LEAVE_ANNOUNCEMENT = 2;
    /* Vooraanmelding */
    public static final int COMMAND_TYPE_PRE_ANNOUNCEMENT = 3;

    private Integer id;
    private ActivationGroup activationGroup;
    private Integer index;
    private Date validFrom;
    private String karUsageType;
    private String type;
    private int commandType;
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
    
    private String concessionHolderLink;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
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

    /**
     * @return the concessionHolderLink
     */
    public String getConcessionHolderLink() {
        return concessionHolderLink;
    }

    /**
     * @param concessionHolderLink the concessionHolderLink to set
     */
    public void setConcessionHolderLink(String concessionHolderLink) {
        this.concessionHolderLink = concessionHolderLink;
    }
}
