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

import java.util.Date;

import javax.xml.bind.annotation.*;

/**
 *
 * @author Matthijs Laan
 */
@XmlType(propOrder={
    "subscriberId",
    "version",
    "dossierName",
    "timestamp"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class TmiMessage {
    public static final String DOSSIER_NAME_DEF = "KV9tlcdef";
    public static final String DOSSIER_NAME_END = "KV9tlcend";
    
    public static final String VERSION_8_1_0_0 = "BISON 8.1.0.0";
    
    @XmlElement(name="SubscriberID")
    private String subscriberId;
    
    @XmlElement(name="Version")
    private String version = VERSION_8_1_0_0;
    
    @XmlElement(name="DossierName")
    private String dossierName = DOSSIER_NAME_DEF;
    
    @XmlElement(name="Timestamp")
    private Date timestamp = new Date();

    public TmiMessage() {
    }
    
    public TmiMessage(String subscriberId) {
        this.subscriberId = subscriberId;
    }
    
    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDossierName() {
        return dossierName;
    }

    public void setDossierName(String dossierName) {
        this.dossierName = dossierName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
