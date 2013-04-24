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

package nl.b3p.kar.jaxb;

import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.VehicleType;

/**
 * Class voor kv9 XML omdat de JPA entity niet handig is om met JAXB te 
 * gebruiken.
 * 
 * @author Matthijs Laan
 */
@XmlType(name="ACTIVATIONPOINTSIGNALType", propOrder={
    "activationpointnumber",
    "karvehicletype",
    "karcommandtype",
    "triggertype",
    "distancetillstopline",
    "signalgroupnumber",
    "virtuallocalloopnumber"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlActivationPointSignal {
    
    private int activationpointnumber, karvehicletype, karcommandtype;
    private Integer distancetillstopline, signalgroupnumber, virtuallocalloopnumber;
    private String triggertype;

    public XmlActivationPointSignal() {
    }
    
    public XmlActivationPointSignal(MovementActivationPoint map, VehicleType vt) {
        activationpointnumber = map.getPoint().getNummer();
        karvehicletype = vt.getNummer();
        ActivationPointSignal s = map.getSignal();
        karcommandtype = s.getKarCommandType();
        triggertype = s.getTriggerType();
        distancetillstopline = s.getDistanceTillStopLine();
        signalgroupnumber = s.getSignalGroupNumber();
        virtuallocalloopnumber = s.getVirtualLocalLoopNumber();
    }
}
