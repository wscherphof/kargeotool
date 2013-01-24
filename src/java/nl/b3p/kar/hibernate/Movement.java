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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 * Entity voor persisteren van een Movement zoals bedoeld in BISON koppelvlak 9.
 *
 * @author Matthijs Laan
 */
@Entity
public class Movement implements Comparable {
    
    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. Niet zichtbaar
     * in Kv9 XML export.
     */        
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Roadside equipment waarbij deze movement hoort.
     */
    @ManyToOne(optional=false)
    private RoadsideEquipment roadsideEquipment;

    /**
     * Volgnummer van movement binnen het verkeerssysteem.
     */
    private Integer nummer;
    
    /**
     * Geordende lijst met punten (begin, eind en meldpunten) voor deze movement.
     */
    @ManyToMany(cascade=CascadeType.ALL) // Actually @OneToMany, workaround for HHH-1268    
    @JoinTable(name="movement_points",inverseJoinColumns=@JoinColumn(name="point"))
    @OrderColumn(name="list_index")
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN) // cannot use orphanRemoval=true due to workaround
    private List<MovementActivationPoint> points = new ArrayList();

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
     * @return points
     */
    public List<MovementActivationPoint> getPoints() {
        return points;
    }
    
    /**
     *
     * @param points
     */
    public void setPoints(List<MovementActivationPoint> points) {
        this.points = points;
    }
    //</editor-fold>

    public int compareTo(Object t) {
        return nummer.compareTo(((Movement)t).nummer);
    }
}
