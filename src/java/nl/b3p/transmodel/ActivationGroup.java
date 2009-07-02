package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
    private Integer metersAfterStopLine;
    private Date inactiveFrom;
    private Double angleToNorth;
    private boolean followDirection;
    private String description;
    private Point stopLineLocation; /* locatie van stopstreep */
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

    public Integer getMetersAfterStopLine() {
        return metersAfterStopLine;
    }

    public void setMetersAfterStopLine(Integer metersAfterStopLine) {
        this.metersAfterStopLine = metersAfterStopLine;
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

    public Point getStopLineLocation() {
        return stopLineLocation;
    }

    public void setStopLineLocation(Point stopLineLocation) {
        this.stopLineLocation = stopLineLocation;
    }

    public String getStopLineLocationString() {
        if(stopLineLocation != null) {
            NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
            nf.setGroupingUsed(false);
            return nf.format(stopLineLocation.getCoordinate().x) + ", " + nf.format(stopLineLocation.getCoordinate().y);
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

    public List getActivations() {
        return activations;
    }

    public void setActivations(List activations) {
        this.activations = activations;
    }

    @Override
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
        return serializeToJson(true);
    }

    public JSONObject serializeToJson(boolean includeChildren) throws Exception {
        JSONObject j = new JSONObject();
        j.put("type", "ag");
        j.put("id", "ag:" + getId());
        j.put("description", getDescription());
        String richting = "";
        switch(getDirectionAtIntersection()) {
            case 1: richting = "Rechtsaf"; break;
            case 2: richting = "Rechtdoor"; break;
            case 3: richting = "Linksaf"; break;
            case 4: richting = "Rechtsaf, rechtdoor, linksaf"; break;
            case 5: richting = "Rechtsaf, rechtdoor"; break;
            case 6: richting = "Rechtdoor, linksaf"; break;
            case 7: richting = "Linksaf, rechtsaf"; break;
            default: richting = "Onbekend"; break;
        }
        j.put("icon", new Integer(0).equals(getMetersAfterStopLine()) ? "stop" : "nostop");
        j.put("name", getKarSignalGroup() + " " + richting + " " + (getDescription() == null ? "" : getDescription()));
        j.put("point", getStopLineLocationString());
        if(includeChildren) {
            if(!getActivations().isEmpty()) {
                JSONArray children = new JSONArray();
                j.put("children", children);
                for(Iterator it = getActivations().iterator(); it.hasNext();) {
                    children.put( ((Activation)it.next()).serializeToJson() );
                }
            }
        }
        return j;
    }
}
