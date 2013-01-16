package nl.b3p.kar.hibernate;

import java.util.List;
import javax.persistence.*;

/**
 * Entity voor een activation point in een beweging welke een KAR-signaal moet
 * sturen uit BISON koppelvlak 9.
 * 
 * @author Matthijs Laan
 */
@Entity
public class ActivationPointSignal {
    /**
     * Waarde voor het KAR commandtype voor een inmeldpunt bij een movement.
     */
    public static final int COMMAND_INMELDPUNT = 1;
    
    /**
     * Waarde voor het KAR commandtype voor een uitmeldpunt bij een movement.
     */
    public static final int COMMAND_UITMELDPUNT = 2;
    
    /**
     * Waarde voor het KAR commandtype voor een voorinmeldpunt bij een movement.
     */
    public static final int COMMAND_VOORINMELDPUNT = 3;
    
    /**
     * Waarde voor het KAR triggertype welke betekent dat de vervoerder welke de
     * lijn exploiteert bepaalt of het bericht direct verstuurd wordt of dat er 
     * een aanvullende conditie geldt.
     */
    public static final String TRIGGER_STANDARD = "STANDARD";
    
    /**
     * Waarde voor het KAR triggertype welke betekent dat er altijd een 
     * automatische inmelding wordt geforceerd ongeacht het soort lijn en de 
     * eventuele aanwezigheid van een halte voor het verkeerssysteem.
     */
    public static final String TRIGGER_FORCED = "FORCED";
    
    /**
     * Waarde voor het KAR triggertype voor "altijd handmatig aanmelden", de
     * chauffeur bepaalt wanneer er aangemeld wordt.
     */
    public static final String TRIGGER_MANUAL = "MANUAL";

    /**
     * Automatisch gegenereerde unieke sleutel volgens een sequence. Niet zichtbaar
     * in Kv9 XML export.
     */        
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Soort melding, zie ActivationPointSignal.COMMAND_ constantes.
     */
    private int karCommandType;
    
    /**
     * Voor welke voertuigtypes dit activation point signal geldt; in Kv9 XML
     * is er per vehicle type een ActivationPointSignal XML element. Indien voor
     * bepaalde vehicle types de waardes van triggerType, distanceTillStopLine
     * of signalGroupNumber of virtualLocalLoopNumber anders is dient een nieuwe
     * movement aangemaakt te worden.
     */
    @ManyToMany
    private List<VehicleType> vehicleTypes;
    
    /**
     * Soort aanmelding, zie ActivationPointSignal.TRIGGER_ constantes.
     */
    private String triggerType;
    
    /**
     * Afstand tot aan de stopstreep in meters, waarde voorbij stopstreep zijn 
     * negatief.
     */
    private Integer distanceTillStopLine;
    
    /**
     * Signaalgroepnummer. Een signaalgroep kan gelden voor meerdere movements.
     * Een movement kan in een complexe situatie zoals het "Ei van Frans" 
     * (Edisonlaan in Apeldoorn) meerdere uitmeldpunten hebben voor andere 
     * signaalgroepen dan waarvoor werd ingemeld.
     */
    private Integer signalGroupNumber;
    
    /**
     * Het virtuele lusnummer.
     */
    private Integer virtualLocalLoopNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getKarCommandType() {
        return karCommandType;
    }

    public void setKarCommandType(int karCommandType) {
        this.karCommandType = karCommandType;
    }

    public List<VehicleType> getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(List<VehicleType> vehicleTypes) {
        this.vehicleTypes = vehicleTypes;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getDistanceTillStopLine() {
        return distanceTillStopLine;
    }

    public void setDistanceTillStopLine(Integer distanceTillStopLine) {
        this.distanceTillStopLine = distanceTillStopLine;
    }

    public Integer getSignalGroupNumber() {
        return signalGroupNumber;
    }

    public void setSignalGroupNumber(Integer signalGroupNumber) {
        this.signalGroupNumber = signalGroupNumber;
    }

    public Integer getVirtualLocalLoopNumber() {
        return virtualLocalLoopNumber;
    }

    public void setVirtualLocalLoopNumber(Integer virtualLocalLoopNumber) {
        this.virtualLocalLoopNumber = virtualLocalLoopNumber;
    }
    
    
}
