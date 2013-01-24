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
    /**
     *
     * @return serviceType
     */
    public String getServiceType() {
        return serviceType;
    }
    
    /**
     *
     * @param serviceType
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    /**
     *
     * @return commandType
     */
    public int getCommandType() {
        return commandType;
    }
    
    /**
     *
     * @param commandType
     */
    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }
    
    /**
     *
     * @return usedAttributesMask
     */
    public int getUsedAttributesMask() {
        return usedAttributesMask;
    }
    
    /**
     *
     * @param usedAttributesMask
     */
    public void setUsedAttributesMask(int usedAttributesMask) {
        this.usedAttributesMask = usedAttributesMask;
    }
    //</editor-fold>
}
