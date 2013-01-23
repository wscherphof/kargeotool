package nl.b3p.kar.hibernate;

import javax.persistence.*;

/**
 * Entity voor de punten van een Movement.
 * 
 * @author Matthijs Laan
 */
@Entity
public class MovementActivationPoint {

    /**
     * Waarde om aan te geven dat dit punt van een movement een beginpunt is. 
     * Een enkel beginpunt is optioneel, maar hoeft te worden vastgelegd indien
     * er geen (voor)inmeldpunten zijn.
     */
    public static final String BEGIN = "BEGIN";
    
    /**
     * Waarde om aan te geven dat dit punt van een movement een eindpunt is.
     * Een enkel eindpunt is verplicht.
     */
    public static final String END = "END";
    
    /**
     * Waarde om aan te geven dat dit punt van een movement een meldpunt is, wat
     * een voorinmeldpunt, inmeldpunt of uitmeldpunt kan zijn.
     * Er kunnen meerdere meldpunten per movement zijn, voor een geldige voorrang
     * is er een inmeldpunt en een uitmeldpunt voor dezelfde signaalgroep.
     */
    public static final String ACTIVATION = "ACTIVATION";
    
    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. 
     * Niet zichtbaar in Kv9 XML export.
     */        
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign-key backreference.
     */
    @ManyToOne
    @JoinColumn(name="movement",updatable=false,nullable=false)
    private Movement movement;

    /**
     * Locatie van het punt van dit movement; kan ook een punt zijn dat in een
     * andere movement wordt gebruikt.
     */
    @ManyToOne
    @Basic(optional=false)
    private ActivationPoint2 point;
    
    /**
     * Geeft aan of dit een beginpunt, eindpunt of meldpunt is.
     */
    private String beginEndOrActivation;
    
    /**
     * Verplicht indien dit punt een meldpunt is, bevat de o.a. signaalgroep.
     */
    @OneToOne(cascade= CascadeType.ALL)
    private ActivationPointSignal signal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }
    
    public ActivationPoint2 getPoint() {
        return point;
    }

    public void setPoint(ActivationPoint2 point) {
        this.point = point;
    }

    public String getBeginEndOrActivation() {
        return beginEndOrActivation;
    }

    public void setBeginEndOrActivation(String beginEndOrActivation) {
        this.beginEndOrActivation = beginEndOrActivation;
    }

    public ActivationPointSignal getSignal() {
        return signal;
    }

    public void setSignal(ActivationPointSignal signal) {
        this.signal = signal;
    }
}
