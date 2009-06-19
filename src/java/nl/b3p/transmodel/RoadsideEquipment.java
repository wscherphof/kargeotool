package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
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

    public JSONObject serializeToJson() throws Exception {
        return serializeToJson(true);
    }

    public JSONObject serializeToJson(boolean includeChildren) throws Exception {
        JSONObject j = new JSONObject();
        j.put("type", "rseq");
        j.put("id", "rseq:" + getId());
        j.put("description", getDescription());
        j.put("name", getUnitNumber() + " " + getType() + (getDescription() == null ? "" : ": " + getDescription()));
        j.put("point", getLocationString());
        EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
        List groups = em.createQuery("from ActivationGroup where roadsideEquipment = :this")
                .setParameter("this", this)
                .getResultList();
        if(includeChildren) {
            if(!groups.isEmpty()) {
                JSONArray children = new JSONArray();
                j.put("children", children);
                for(Iterator it = groups.iterator(); it.hasNext();) {
                    children.put( ((ActivationGroup)it.next()).serializeToJson() );
                }
            }
        }
        return j;
    }
}
