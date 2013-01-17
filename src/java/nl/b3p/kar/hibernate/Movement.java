package nl.b3p.kar.hibernate;

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
    private RoadsideEquipment2 roadsideEquipment;

    /**
     * Volgnummer van movement binnen het verkeerssysteem.
     */
    private Integer nummer;
    
    /**
     * Geordende lijst met punten (begin, eind en meldpunten) voor deze movement.
     */
    @OneToMany(cascade= CascadeType.PERSIST, mappedBy="movement")
    @OrderColumn(name="list_index")    
    private List<MovementActivationPoint> points;

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RoadsideEquipment2 getRoadsideEquipment() {
        return roadsideEquipment;
    }
    
    public void setRoadsideEquipment(RoadsideEquipment2 roadsideEquipment) {
        this.roadsideEquipment = roadsideEquipment;
    }
    
    public Integer getNummer() {
        return nummer;
    }
    
    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }
    
    public List<MovementActivationPoint> getPoints() {
        return points;
    }
    
    public void setPoints(List<MovementActivationPoint> points) {
        this.points = points;
    }
    //</editor-fold>

    public int compareTo(Object t) {
        return nummer.compareTo(((Movement)t).nummer);
    }
}
