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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 * 
 * @author Matthijs Laan
 */
@XmlRootElement(name="VV_TM_PUSH")
@XmlType(propOrder={"defs", "ends"})
@XmlAccessorType(XmlAccessType.FIELD)
public class TmiPush extends TmiMessage {
    @XmlElement(name="KV9tlcdef")
    List<Kv9Def> defs = null;
    @XmlElement(name="KV9tlcend")
    List<Kv9End> ends = null;
    
    public TmiPush() {
    }
    
    public TmiPush(String subscriberId, List<RoadsideEquipment> rseqs) {
        super(subscriberId);
     
        Kv9Def def = new Kv9Def();
        Kv9End end = new Kv9End();
        
        for(RoadsideEquipment rseq: rseqs) {
            if(rseq.getValidUntil() == null) {
                def.rseqs.add(rseq);
            } else {
                end.rseqs.add(new RseqEnd(rseq));
            }
        }
        
        if(!def.rseqs.isEmpty()) {
            defs = Collections.singletonList(def);
        }
        if(!end.rseqs.isEmpty()) {
            ends = Collections.singletonList(end);
        }
    }
}
