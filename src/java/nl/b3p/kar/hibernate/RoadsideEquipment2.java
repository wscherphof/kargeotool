package nl.b3p.kar.hibernate;

import com.vividsolutions.jts.geom.Point;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.*;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
/**
 *
 * @author Matthijs Laan
 */
@Entity
public class RoadsideEquipment2 {
    
    /**
     * Waarde voor de functie van het verkeerssysteem welke een verkeersregel-
     * installatie (VRI) aanduidt.
     */
    public static final String TYPE_CROSSING = "CROSSING";
    
    /**
     * Waarde voor de functie van het verkeerssysteem, duidt een bewaking aan
     * die een alarmsignaal geeft bij het naderen van een OV of hulpdienst
     * voertuig.
     */
    public static final String TYPE_GUARD = "GUARD";
    
    /**
     * Waarde voor de functie van het verkeerssysteem om een afsluiting aan te 
     * duiden, een wegafsluitend object dat bij het naderen van een OV of 
     * hulpdienst voertuig tijdelijk verdwijnt (in de weg verzinkt), en na
     * passage van het voertuig weer verschijnt. 
     */
    public static final String TYPE_BAR = "BAR";
    
    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. Niet zichtbaar
     * in Kv9 XML export.
     */    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional=false)
    private DataOwner2 dataOwner;
    
    private Point location;
    
    /**
     * Het KAR adres (SID) van het verkeerssysteem. Verplicht voor Kv9.
     */
    private Integer karAddress;
    
    /**
     * Datum vanaf wanneer het verkeerssysteem actief is (inclusief). Verplicht 
     * voor Kv9.
     */
    @Temporal(TemporalType.DATE)
    private Date validFrom;
    
    /**
     * Datum tot aan wanneer het verkeersysteem actief is (exclusief).     
     */
    @Temporal(TemporalType.DATE)
    private Date validUntil;
    
    /**
     * De functie van het verkeerssysteem, zie de TYPE_ constanten in 
     * RoadsideEquipment.
     */
    private String type;
    
    /**
     * Identificeert het kruispunt volgens codering domein DataOwner 
     * (wegbeheerder). Verplicht voor Kv9.
     */
    private String crossingCode;
    
    /**
     * De plaats waar het verkeerssysteem staat. Verplicht voor Kv9.
     */
    @Column(length=50)
    private String town;
    
    /**
     * Omschrijving van het verkeerssysteem.
     */
    @Column(length=255)
    private String description;

    /**
     * Voor service en command types te versturen KAR attributen.
     */
    @ElementCollection
    //@JoinTable(inverseJoinColumns=@JoinColumn(name="roadside_equipment"))
    @OrderColumn(name="list_index")
    private List<KarAttributes> karAttributes;
    
    @OneToMany(cascade=CascadeType.PERSIST, mappedBy="roadsideEquipment") 
    @Sort(type=SortType.NATURAL)
    private SortedSet<Movement> movements = new TreeSet<Movement>();

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public DataOwner2 getDataOwner() {
        return dataOwner;
    }
    
    public void setDataOwner(DataOwner2 dataOwner) {
        this.dataOwner = dataOwner;
    }
    
    public Integer getKarAddress() {
        return karAddress;
    }
    
    public void setKarAddress(Integer karAddress) {
        this.karAddress = karAddress;
    }
    
    public Date getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }
    
    public Date getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCrossingCode() {
        return crossingCode;
    }
    
    public void setCrossingCode(String crossingCode) {
        this.crossingCode = crossingCode;
    }
    
    public String getTown() {
        return town;
    }
    
    public void setTown(String town) {
        this.town = town;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<KarAttributes> getKarAttributes() {
        return karAttributes;
    }

    public void setKarAttributes(List<KarAttributes> karAttributes) {
        this.karAttributes = karAttributes;
    }

    public SortedSet<Movement> getMovements() {
        return movements;
    }

    public void setMovements(SortedSet<Movement> movements) {
        this.movements = movements;
    }
    //</editor-fold>
    
}
