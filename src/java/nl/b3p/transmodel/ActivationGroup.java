package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivationGroup {
    /* "Priority ReQuest Automatic" */
    public static final String TYPE_PRQA = "PRQA";

    private Integer id;
    private RoadsideEquipment roadsideEquipment;
    private int karSignalGroup;
    private Date validFrom;
    private String type;
    private int directionAtIntersection;
    private Integer metersBeforeRoadsideEquipmentLocation;
    private Date inactiveFrom;
    private Double angleToNorth;
    private Boolean followDirection;
    private String description;
    private Point stopLineLocation;
    private String updater;
    private Date updateTime;
    private String validator;
    private Date validationTime;

    private List activations = new ArrayList();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RoadsideEquipment getRoadsideEquipment() {
        return roadsideEquipment;
    }

    public void setRoadsideEquipment(RoadsideEquipment roadsideEquipment) {
        this.roadsideEquipment = roadsideEquipment;
    }

    public int getKarSignalGroup() {
        return karSignalGroup;
    }

    public void setKarSignalGroup(int karSignalGroup) {
        this.karSignalGroup = karSignalGroup;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDirectionAtIntersection() {
        return directionAtIntersection;
    }

    public void setDirectionAtIntersection(int directionAtIntersection) {
        this.directionAtIntersection = directionAtIntersection;
    }

    public Integer getMetersBeforeRoadsideEquipmentLocation() {
        return metersBeforeRoadsideEquipmentLocation;
    }

    public void setMetersBeforeRoadsideEquipmentLocation(Integer metersBeforeRoadsideEquipmentLocation) {
        this.metersBeforeRoadsideEquipmentLocation = metersBeforeRoadsideEquipmentLocation;
    }

    public Date getInactiveFrom() {
        return inactiveFrom;
    }

    public void setInactiveFrom(Date inactiveFrom) {
        this.inactiveFrom = inactiveFrom;
    }

    public Double getAngleToNorth() {
        return angleToNorth;
    }

    public void setAngleToNorth(Double angleToNorth) {
        this.angleToNorth = angleToNorth;
    }

    public Boolean getFollowDirection() {
        return followDirection;
    }

    public void setFollowDirection(Boolean followDirection) {
        this.followDirection = followDirection;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point getStopLineLocation() {
        return stopLineLocation;
    }

    public void setStopLineLocation(Point stopLineLocation) {
        this.stopLineLocation = stopLineLocation;
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

    public List getActivations() {
        return activations;
    }

    public void setActivations(List activations) {
        this.activations = activations;
    }
    public String toString(){
        String returnValue="";
        returnValue+=this.getType();
        if (this.getRoadsideEquipment()!=null){
            returnValue+=" "+this.getRoadsideEquipment().getDescription();
        }
        returnValue+="(KAR-signal: "+this.getKarSignalGroup()+")";
        return returnValue;
    }
}
