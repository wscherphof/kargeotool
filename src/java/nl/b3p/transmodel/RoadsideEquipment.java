package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Role;
import nl.b3p.kar.persistence.MyEMFDatabase;
import nl.b3p.kar.struts.EditorTreeObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class RoadsideEquipment implements EditorTreeObject {
    /* Waardes voor type property */
    public static final String TYPE_CROSS = "CROSS"; /* VRI */
    public static final String TYPE_CLOSE = "CLOSE"; /* afsluiting/poller/slagboom */
    public static final String TYPE_PIU = "PIU";     /* Passenger Information Unit (halteprocessor) */

    private Integer id;
    private DataOwner dataOwner;
    private int unitNumber;
    private Date validFrom;
    private String type;
    private String radioAddress;
    private String description;
    private String supplier;
    private String supplierTypeNumber;
    private Date installationDate;
    private boolean selectiveDetectionLoop;
    private Point location; /* algemene locatie van VRI */
    private Date inactiveFrom;
    private String updater;
    private Date updateTime;
    private String validator;
    private Date validationTime;

    private Set<ActivationGroup> activationGroups = new HashSet<ActivationGroup>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DataOwner getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DataOwner dataOwner) {
        this.dataOwner = dataOwner;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
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

    public String getRadioAddress() {
        return radioAddress;
    }

    public void setRadioAddress(String radioAddress) {
        this.radioAddress = radioAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSupplierTypeNumber() {
        return supplierTypeNumber;
    }

    public void setSupplierTypeNumber(String supplierTypeNumber) {
        this.supplierTypeNumber = supplierTypeNumber;
    }

    public Date getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(Date installationDate) {
        this.installationDate = installationDate;
    }

    public boolean isSelectiveDetectionLoop() {
        return selectiveDetectionLoop;
    }

    public void setSelectiveDetectionLoop(boolean selectiveDetectionLoop) {
        this.selectiveDetectionLoop = selectiveDetectionLoop;
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

    public Date getInactiveFrom() {
        return inactiveFrom;
    }

    public void setInactiveFrom(Date inactiveFrom) {
        this.inactiveFrom = inactiveFrom;
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

    public JSONObject serializeToJson(HttpServletRequest request) throws Exception {
        return serializeToJson(request, true);
    }

    public JSONObject serializeToJson(HttpServletRequest request, boolean includeChildren) throws Exception {
        EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
        JSONObject j = new JSONObject();
        j.put("type", "rseq");
        j.put("id", "rseq:" + getId());
        if(request.isUserInRole(Role.BEHEERDER)) {
            j.put("editable", true);
        } else {
            Gebruiker g = Gebruiker.getNonTransientPrincipal(request);
            j.put("editable", g.canEditDataOwner(getDataOwner()));
        }
        j.put("description", getDescription());
        j.put("rseqType", getType());
        j.put("name", getUnitNumber() + (getDescription() == null ? "" : ": " + getDescription()));
        j.put("point", getLocationString());
        Set groups = getActivationGroups();
        if(includeChildren) {
            if(!groups.isEmpty()) {
                JSONArray children = new JSONArray();
                j.put("children", children);
                for(Iterator it = groups.iterator(); it.hasNext();) {
                    children.put( ((ActivationGroup)it.next()).serializeToJson(request) );
                }
            }
        }
        return j;
    }

    public void validateAll(String user, Date time) {
        setValidator(user);
        setValidationTime(time);

        // Propagate to all children
        for (Iterator<ActivationGroup> it1 = activationGroups.iterator(); it1.hasNext();) {
            ActivationGroup activationGroup = it1.next();
            activationGroup.validateAll(validator, validationTime);
        }
    }

    public Set getActivationGroups() {
        return activationGroups;
    }

    public void setActivationGroups(Set activationGroups) {
        this.activationGroups = activationGroups;
    }

    public List<Integer> getActivationGroupIds(){
        List<Integer> agIds = new ArrayList<Integer>();

        Set ags = this.activationGroups;
        for (Iterator<ActivationGroup> it = ags.iterator(); it.hasNext();) {
            ActivationGroup activationGroup = it.next();
            agIds.add(activationGroup.getId());
        }
        return agIds;
    }


}
