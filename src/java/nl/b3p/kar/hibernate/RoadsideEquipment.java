/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten               
 *                                                                           
 * Copyright (C) 2009-2013 B3Partners B.V.                                   
 *                                                                           
 * This program is free software: you can redistribute it and/or modify      
 * it under the terms of the GNU Affero General Public License as            
 * published by the Free Software Foundation, either version 3 of the        
 * License, or (at your option) any later version.                           
 *                                                                           
 * This program is distributed in the hope that it will be useful,           
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 * GNU Affero General Public License for more details.                       
 *                                                                           
 * You should have received a copy of the GNU Affero General Public License  
 * along with this program. If not, see <http://www.gnu.org/licenses/>.      
 */

package nl.b3p.kar.hibernate;

import nl.b3p.kar.jaxb.Namespace;
import nl.b3p.kar.jaxb.TmiDateAdapter;
import com.vividsolutions.jts.geom.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nl.b3p.geojson.GeoJSON;
import nl.b3p.kar.jaxb.XmlB3pRseq;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Klasse voor het beschrijven van RoadsideEquipment zoals VRI's
 * 
 * @author Matthijs Laan
 */
@Entity
@XmlRootElement(name="RSEQDEF")
@XmlType(name="RSEQDEFType", 
        propOrder={
            "dataOwner",
            "karAddress",
            "type",
            "validFrom",
            "validUntil",
            "crossingCode",
            "town",
            "description",
            "karAttributes",
            "points",
            "movements",
            "delimiter",
            "extraXml"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class RoadsideEquipment {
    
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
    @XmlTransient
    private Long id;
    
    @ManyToOne(optional=false)
    @XmlElement(name="dataownercode")
    @XmlJavaTypeAdapter(DataOwner.class)
    private DataOwner dataOwner;
        
    @org.hibernate.annotations.Type(type="org.hibernatespatial.GeometryUserType")
    @XmlTransient
    private Point location;
    
    /**
     * Het KAR adres (SID) van het verkeerssysteem. Verplicht voor Kv9.
     */
    @XmlElement(name="karaddress")
    private Integer karAddress;
    
    /**
     * Datum vanaf wanneer het verkeerssysteem actief is (inclusief). Verplicht 
     * voor Kv9.
     */
    @Temporal(TemporalType.DATE)
    @XmlElement(name="validfrom")
    @XmlJavaTypeAdapter(TmiDateAdapter.class)
    private Date validFrom;
    
    /**
     * Datum tot aan wanneer het verkeersysteem actief is (exclusief).     
     */
    @Temporal(TemporalType.DATE)
    @XmlElement(name="validuntil")
    @XmlJavaTypeAdapter(TmiDateAdapter.class)
    private Date validUntil;
    
    /**
     * De functie van het verkeerssysteem, zie de TYPE_ constanten in 
     * RoadsideEquipment.
     */
    @XmlElement(name="rseqtype")
    private String type;
    
    /**
     * Identificeert het kruispunt volgens codering domein DataOwner 
     * (wegbeheerder). Verplicht voor Kv9.
     */
    @XmlElement(name="crossingcode")
    private String crossingCode;
    
    /**
     * De plaats waar het verkeerssysteem staat. Verplicht voor Kv9.
     */
    @Column(length=50)
    @XmlElement(name="town")
    private String town;
    
    /**
     * Omschrijving van het verkeerssysteem.
     */
    @Column(length=255)
    @XmlElement(name="description")
    private String description;

    /**
     * Voor service en command types te versturen KAR attributen.
     */
    @ElementCollection
    //@JoinTable(inverseJoinColumns=@JoinColumn(name="roadside_equipment"))
    @OrderColumn(name="list_index")
    @XmlElement(name="KARATTRIBUTES")
    private List<KarAttributes> karAttributes = new ArrayList<KarAttributes>();
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="roadsideEquipment", orphanRemoval=true) 
    @Sort(type=SortType.NATURAL)
    @XmlElement(name="MOVEMENT")
    private SortedSet<Movement> movements = new TreeSet<Movement>();
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="roadsideEquipment", orphanRemoval=true) 
    @XmlElement(name="ACTIVATIONPOINT")
    @Sort(type=SortType.NATURAL)
    private SortedSet<ActivationPoint> points = new TreeSet<ActivationPoint>();
    
    @Column(length=4096)
    @XmlTransient
    private String memo;

    @XmlElement(name="b3pextra")
    public XmlB3pRseq getExtraXml() {
        
        XmlB3pRseq extra = new XmlB3pRseq(this);
        
        if(extra.isEmpty()) {
            return null;
        } else {
            return extra;
        }
    }
    
    @Transient
    @XmlElement(namespace=Namespace.NS_BISON_TMI8_KV9_CORE)
    private String delimiter = "";
    
    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    /**
     *
     * @return id
     */
    public Long getId() {
        return id;
    }
    
    /**
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     *
     * @return dataOwner
     */
    public DataOwner getDataOwner() {
        return dataOwner;
    }
    
    /**
     *
     * @param dataOwner
     */
    public void setDataOwner(DataOwner dataOwner) {
        this.dataOwner = dataOwner;
    }
    
    /**
     *
     * @return karAddress
     */
    public Integer getKarAddress() {
        return karAddress;
    }
    
    /**
     *
     * @param karAddress
     */
    public void setKarAddress(Integer karAddress) {
        this.karAddress = karAddress;
    }
    
    /**
     *
     * @return validFrom
     */
    public Date getValidFrom() {
        return validFrom;
    }
    
    /**
     *
     * @param validFrom
     */
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }
    
    /**
     *
     * @return validUntil
     */
    public Date getValidUntil() {
        return validUntil;
    }
    
    /**
     *
     * @param validUntil
     */
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    /**
     *
     * @return type
     */
    public String getType() {
        return type;
    }
    
    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     *
     * @return crossingCode
     */
    public String getCrossingCode() {
        return crossingCode;
    }
    
    /**
     *
     * @param crossingCode
     */
    public void setCrossingCode(String crossingCode) {
        this.crossingCode = crossingCode;
    }
    
    /**
     *
     * @return town
     */
    public String getTown() {
        return town;
    }
    
    /**
     *
     * @param town
     */
    public void setTown(String town) {
        this.town = town;
    }
    
    /**
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     *
     * @return location
     */
    public Point getLocation() {
        return location;
    }

    /**
     *
     * @param location
     */
    public void setLocation(Point location) {
        this.location = location;
    }

    /**
     *
     * @return karAttributes
     */
    public List<KarAttributes> getKarAttributes() {
        return karAttributes;
    }

    /**
     *
     * @param karAttributes
     */
    public void setKarAttributes(List<KarAttributes> karAttributes) {
        this.karAttributes = karAttributes;
    }

    /**
     *
     * @return movements
     */
    public SortedSet<Movement> getMovements() {
        return movements;
    }

    /**
     *
     * @param movements
     */
    public void setMovements(SortedSet<Movement> movements) {
        this.movements = movements;
    }

    /**
     *
     * @return points
     */
    public SortedSet<ActivationPoint> getPoints() {
        return points;
    }

    /**
     *
     * @param points
     */
    public void setPoints(SortedSet<ActivationPoint> points) {
        this.points = points;
    }
    //</editor-fold>
    
    public ActivationPoint getPointByNumber(int number) {
        for(ActivationPoint p: points) {
            if(p.getNummer() == number) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Methode converteert dit RoadsideEquipment object in een JSON object
     * 
     * @return JSON object
     * @throws JSONException
     */
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
        p.put("memo", memo);
        p.put("id",id);
        
        // geen kar attributes
        
        gj.put("properties", p);
        return gj;
    }
    
    /**
     * Methode bouwt het volledige JSON object op voor dit RoadsideEquipment 
     * object.
     * 
     * @return JSON Object
     * @throws JSONException
     */
    public JSONObject getJSON() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("id", id);
        j.put("dataOwner", dataOwner.getCode());
        j.put("crossingCode", crossingCode);
        j.put("description", description);
        j.put("karAddress", karAddress);
        j.put("town", town);
        j.put("type", type);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        j.put("validFrom", sdf.format(validFrom));
        j.put("validUntil", validUntil == null ? null : sdf.format(validUntil));
        j.put("location", GeoJSON.toGeoJSON(location));
        j.put("memo", memo);
        
        JSONObject jattrs = new JSONObject();
        j.put("attributes", jattrs);
        jattrs.put(KarAttributes.SERVICE_PT, new JSONArray("[ [], [], [] ]"));
        jattrs.put(KarAttributes.SERVICE_ES, new JSONArray("[ [], [], [] ]"));
        jattrs.put(KarAttributes.SERVICE_OT, new JSONArray("[ [], [], [] ]"));
        
        for(KarAttributes attribute: karAttributes) {
            JSONArray attributes = jattrs.getJSONArray(attribute.getServiceType()).getJSONArray(attribute.getCommandType()-1);
            
            if(attributes.length() != 0) {
                attributes = new JSONArray();
                jattrs.getJSONArray(attribute.getServiceType()).put(attribute.getCommandType()-1, attributes);
            }
            int mask = attribute.getUsedAttributesMask();
            for(int i = 0; i < 24; i++) {
                boolean set = (mask & (1 << i)) != 0;
                attributes.put(set);
            }
        }
             
        
        JSONArray jmvmts = new JSONArray();
        j.put("movements", jmvmts);
        for(Movement m: movements) {
            JSONObject jm = m.getJSON();
            jmvmts.put(jm);
        }
        
        Map<ActivationPoint,List<MovementActivationPoint>> mapsByAp2 = new HashMap();
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

        List<JSONObject> jpoints = new ArrayList();
        
        for(ActivationPoint ap2: points) {
            JSONObject pj = ap2.getGeoJSON();
            
            
            SortedSet<Integer> movementNumbers = new TreeSet();
            
            JSONArray mns = new JSONArray();
            for(Integer i: movementNumbers) { mns.put(i); };
            pj.put("movementNumbers", mns);
            
            jpoints.add(pj);
            Collections.sort(jpoints, new Comparator<JSONObject>() {
                public int compare(JSONObject lhs, JSONObject rhs) {
                    try {
                        return new Integer(lhs.getInt("nummer")).compareTo(rhs.getInt("nummer"));
                    } catch (JSONException ex) {
                        return 0;
                    }
                }
            });
            
            JSONArray jp = new JSONArray();
            for(JSONObject jo: jpoints) {
                jp.put(jo);
            }
            j.put("points", jp);
            
        }       
        
        return j;
    }
}
