package nl.b3p.kar.hibernate;

import javax.persistence.*;

/**
 * De voor een RoadsideEquipment voor een bepaald service type en command type
 * te versturen attributen in het KAR bericht.
 * 
 * @author Matthijs Laan
 */
@Embeddable
public class KarAttributes {

    /**
     * Waarde voor KAR service type voor openbaar vervoer (public transport).
     */
    public static final String SERVICE_PT = "PT";
    
    /**
     * Waarde voor KAR service type voor hulpdiensten (emergency services).
     */
    public static final String SERVICE_ES = "ES";
    
    /**
     * Waarde voor KAR service type voor andersoortig vervoer, bijvoorbeeld taxi
     * (other transport).
     */
    public static final String SERVICE_OT = "OT";
    
    /**
     * Service type waarvoor de attributen gelden; zie SERVICE_ constanten in
     * KarAttributesKey.
     */
    @Basic(optional=false)
    private String serviceType;
    
    /**
     * Command type waarvoor de attributen gelden; zie KAR_COMMAND_ constanten 
     * in Movement.
     */
    @Basic(optional=false)
    private int commandType;

    /**
     * Bitmask voor de te vullen attributen in het KAR bericht. LSB duidt 
     * attribuut 1 aan en de MSB (van een 24-bit waarde) attribuut 24. Verplicht
     * in Kv9.
     */
    @Basic(optional=false)
    private int usedAttributesMask;

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public int getCommandType() {
        return commandType;
    }
    
    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }
    
    public int getUsedAttributesMask() {
        return usedAttributesMask;
    }
    
    public void setUsedAttributesMask(int usedAttributesMask) {
        this.usedAttributesMask = usedAttributesMask;
    }
    //</editor-fold>
}
