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
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RoadsideEquipment getRoadsideEquipment() {
        return roadsideEquipment;
    }
    
    public void setRoadsideEquipment(RoadsideEquipment roadsideEquipment) {
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
