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

import com.vividsolutions.jts.geom.Point;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import nl.b3p.geojson.GeoJSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Entity voor persisteren van een Activation Point zoals bedoeld in BISON 
 * koppelvlak 9.
 *
 * @author Matthijs Laan
 */
@Entity
@XmlType(name="ACTIVATIONPOINTType", 
        propOrder={
            "nummer",
            "x",
            "y",
            "label"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivationPoint {
    
    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. Niet zichtbaar
     * in Kv9 XML export.
     */
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @XmlTransient
    private Long id;

    /**
     * Roadside equipment waarbij deze movement hoort.
     */
    @ManyToOne(optional=false)
    @XmlTransient
    private RoadsideEquipment roadsideEquipment;
    
    /**
     * Nummer van het activation point binnen een enkele VRI.
     */
    @Basic(optional=false)
    @XmlElement(name="activationpointnumber")
    private Integer nummer;
    
    /**
     * Geografische locatie van het activation point.
     */
    @Basic(optional=false)
    @org.hibernate.annotations.Type(type="org.hibernatespatial.GeometryUserType")
    @XmlTransient
    private Point location;
    
    @XmlElement(name="rdx-coordinate")
    public double getX() {
        return location.getCoordinate().x;
    }
    
    @XmlElement(name="rdy-coordinate")
    public double getY() {
        return location.getCoordinate().y;
    }
    
    /**
     * Tekstuele aanduiding van het activation point, te tonen als label op de kaart.
     */
    private String label;
    
    public JSONObject getGeoJSON() throws JSONException{
        JSONObject pj = new JSONObject();
        pj.put("id",getId());
        pj.put("geometry", GeoJSON.toGeoJSON(getLocation()));
        pj.put("label", getLabel());
        pj.put("nummer", getNummer());
        
        
        EntityManager em = Stripersist.getEntityManager();
        if(em != null){
            
            // Zoek soort punt op, wordt in movement.points bepaald maar is 
            // voor alle movements die dit punt gebruiken altijd hetzelfde
            // soort (combi beginEndOrActivation en commandType)
            List<MovementActivationPoint> maps = (List<MovementActivationPoint>) em.createQuery("from MovementActivationPoint where point = :point").setParameter("point", this)
                        .getResultList();
            String apType = null;
            if(maps != null && !maps.isEmpty()) {
                // de waardes beginEndOrActivation en karCommandType moeten voor
                // alle MAP's voor deze AP2 hetzelfde zijn
                MovementActivationPoint map1 = maps.get(0);
                 apType = map1.getBeginEndOrActivation();
                if(map1.getSignal() != null) {
                    assert(MovementActivationPoint.ACTIVATION.equals(apType));
                    apType = apType + "_" + map1.getSignal().getKarCommandType();
                }
            }
            pj.put("type",apType);
        }
        
        return pj;
    }

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
     * @return roadsideEquipment
     */
    public RoadsideEquipment getRoadsideEquipment() {
        return roadsideEquipment;
    }

    /**
     *
     * @param roadsideEquipment
     */
    public void setRoadsideEquipment(RoadsideEquipment roadsideEquipment) {
        this.roadsideEquipment = roadsideEquipment;
    }

    /**
     *
     * @return nummer
     */
    public Integer getNummer() {
        return nummer;
    }

    /**
     *
     * @param nummer
     */
    public void setNummer(Integer nummer) {
        this.nummer = nummer;
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
     * @return label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     *
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }
    //</editor-fold>
 
}
