package nl.b3p.kar.hibernate;

import com.vividsolutions.jts.geom.Point;
import javax.persistence.*;
import nl.b3p.transmodel.DataOwner;

/**
 * Entity voor persisteren van een Activation Point zoals bedoeld in BISON 
 * koppelvlak 9.
 *
 * @author Matthijs Laan
 */
@Entity
public class ActivationPoint2 {
    
    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. Niet zichtbaar
     * in Kv9 XML export.
     */
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    /**
     * Nummer van het activation point binnen een enkele VRI.
     */
    @Basic(optional=false)
    private Integer nummer;
    
    /**
     * Geografische locatie van het activation point.
     */
    @Basic(optional=false)
    private Point location;
    
    /**
     * Tekstuele aanduiding van het activation point, te tonen als label op de kaart.
     */
    private String label;

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNummer() {
        return nummer;
    }

    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }
    
    public Point getLocation() {
        return location;
    }
    
    public void setLocation(Point location) {
        this.location = location;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    //</editor-fold>
    
}
