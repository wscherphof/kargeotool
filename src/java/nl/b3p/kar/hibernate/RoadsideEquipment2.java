package nl.b3p.kar.hibernate;

import com.vividsolutions.jts.geom.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.*;
import nl.b3p.geojson.GeoJSON;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    
    @org.hibernate.annotations.Type(type="org.hibernatespatial.GeometryUserType")
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
    
    @OneToMany(cascade=CascadeType.PERSIST, mappedBy="roadsideEquipment") 
    @Sort(type=SortType.NATURAL)
    private SortedSet<ActivationPoint2> points = new TreeSet<ActivationPoint2>();

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

    public SortedSet<ActivationPoint2> getPoints() {
        return points;
    }

    public void setPoints(SortedSet<ActivationPoint2> points) {
        this.points = points;
    }
    //</editor-fold>
    
    public JSONObject getRseqGeoJSON() throws JSONException {
        JSONObject gj = new JSONObject();
        gj.put("type","Feature");
        gj.put("geometry", GeoJSON.toGeoJSON(location));
        JSONObject p = new JSONObject();
        p.put("crossingCode", crossingCode);
        p.put("description", description);
        p.put("karAddress", karAddress);
        p.put("town", town);
        p.put("type", type);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        p.put("validFrom", sdf.format(validFrom));
        p.put("dataOwner", dataOwner.getCode());
        p.put("validUntil", validUntil == null ? null : sdf.format(validUntil));
        
        // geen kar attributes
        
        gj.put("properties", p);
        return gj;
    }
    
    public JSONObject getPointsGeoJSON() throws JSONException {
        JSONObject gjc = new JSONObject();
        gjc.put("type","FeatureCollection");
        JSONArray f = new JSONArray();
        
        Map<ActivationPoint2,List<MovementActivationPoint>> mapsByAp2 = new HashMap();
        for(Movement m: movements) {
            for(MovementActivationPoint map: m.getPoints()) {
                List<MovementActivationPoint> maps = mapsByAp2.get(map.getPoint());
                if(maps == null) {
                    maps = new ArrayList();
                    mapsByAp2.put(map.getPoint(), maps);
                }
                maps.add(map);                
            }
        }
        
        for(ActivationPoint2 ap2: points) {
            JSONObject gj = new JSONObject();
            gj.put("type","Feature");
            gj.put("geometry", GeoJSON.toGeoJSON(ap2.getLocation()));
            JSONObject p = new JSONObject();
            p.put("id", ap2.getId());
            p.put("label", ap2.getLabel());
            p.put("type", "CheckInPoint");  // Dummy type. For prototyping
            p.put("nummer", ap2.getNummer());
            
            // Zoek soort punt op, wordt in movement.points bepaald maar is 
            // voor alle movements die dit punt gebruiken altijd hetzelfde
            // soort (combi beginEndOrActivation en commandType)
            
            
            List<MovementActivationPoint> maps = mapsByAp2.get(ap2);
            String ap2type = null;
            
            SortedSet<Integer> movementNumbers = new TreeSet();
            SortedSet<Integer> signalGroupNumbers = new TreeSet();
            
            if(maps != null && !maps.isEmpty()) {
                // de waardes beginEndOrActivation en karCommandType moeten voor
                // alle MAP's voor deze AP2 hetzelfde zijn
                MovementActivationPoint map1 = maps.get(0);
                 ap2type = map1.getBeginEndOrActivation();
                if(map1.getSignal() != null) {
                    assert(MovementActivationPoint.ACTIVATION.equals(ap2type));
                    ap2type = ap2type + "_" + map1.getSignal().getKarCommandType();
                }
                
                for(MovementActivationPoint map: maps) {
                    movementNumbers.add(map.getMovement().getNummer());
                    if(map.getSignal() != null) {
                        signalGroupNumbers.add(map.getSignal().getSignalGroupNumber());
                    }
                    if(MovementActivationPoint.END.equals(ap2type)) {
                        for(int i = map.getMovement().getPoints().size()-1;  i >= 0; i--) {
                            ActivationPointSignal aps = map.getMovement().getPoints().get(i).getSignal();
                            if(aps != null) {
                                signalGroupNumbers.add(aps.getSignalGroupNumber());
                            }
                        }
                    }
                }
            }
            p.put("type", ap2type);
            JSONArray mns = new JSONArray();
            for(Integer i: movementNumbers) { mns.put(i); };
            p.put("movementNumbers", mns);
            JSONArray sgns = new JSONArray();
            for(Integer i: signalGroupNumbers) { sgns.put(i); };
            p.put("signalGroupNumbers", sgns);
            
            gj.put("properties", p);
            f.put(gj);
        }
        gjc.put("features", f);
        return gjc;
    }
    
}
