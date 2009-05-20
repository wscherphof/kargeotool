package nl.b3p.transmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import nl.b3p.kar.hibernate.KarPunt;
import nl.b3p.kar.struts.EditorTreeObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class ActivationGroup implements EditorTreeObject {
    /* "Priority ReQuest Automatic" */
    public static final String TYPE_PRQA = "PRQA";

    private Integer id;
    private RoadsideEquipment roadsideEquipment;
    private int karSignalGroup;
    private Date validFrom;
    private String type;
    private int directionAtIntersection;
    private Integer metersBeforeRoadsideEquipmentLocation;
    private Integer metersAfterRoadsideEquipmentLocation;
    private Date inactiveFrom;
    private Double angleToNorth;
    private boolean followDirection;
    private String description;
    private KarPunt point; /* locatie van stopstreep */
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

    public Integer getMetersAfterRoadsideEquipmentLocation() {
        return metersAfterRoadsideEquipmentLocation;
    }

    public void setMetersAfterRoadsideEquipmentLocation(Integer metersAfterRoadsideEquipmentLocation) {
        this.metersAfterRoadsideEquipmentLocation = metersAfterRoadsideEquipmentLocation;
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

    public boolean isFollowDirection() {
        return followDirection;
    }

    public void setFollowDirection(boolean followDirection) {
        this.followDirection = followDirection;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KarPunt getPoint() {
        return point;
    }

    public void setPoint(KarPunt point) {
        this.point = point;
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

    public JSONObject serializeToJson() throws Exception {
        JSONObject j = new JSONObject();
        j.put("type", "ag");
        j.put("id", "ag:" + getId());
        j.put("description", getDescription());
        String richting = "";
        switch(getDirectionAtIntersection()) {
            case 1: richting = "rechtsaf"; break;
            case 2: richting = "rechtdoor"; break;
            case 3: richting = "linksaf"; break;
            case 4: richting = "rechtsaf,rechtdoor,linksaf"; break;
            case 5: richting = "rechtsaf,rechtdoor"; break;
            case 6: richting = "rechtdoor,linksaf"; break;
            case 7: richting = "linksaf,rechtsaf"; break;
            default: richting = "onbekend"; break;
        }
        j.put("name", getKarSignalGroup() + " " + richting + " " + (getDescription() == null ? "" : getDescription()));
        j.put("point", getPoint() == null ? null : getPoint().toString());
        if(!getActivations().isEmpty()) {
            JSONArray children = new JSONArray();
            j.put("children", children);
            for(Iterator it = getActivations().iterator(); it.hasNext();) {
                children.put( ((Activation)it.next()).serializeToJson() );
            }
        }
        return j;
    }
}
