package nl.b3p.transmodel;

import com.vividsolutions.jts.geom.Point;
import java.util.Date;

public class RoadsideEquipment {
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
    private Point location;

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

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
