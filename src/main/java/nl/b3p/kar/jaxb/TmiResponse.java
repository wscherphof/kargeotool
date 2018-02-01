/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten               
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

/**
 *
 * @author Matthijs Laan
 */
@XmlRootElement(name="VV_TM_RES")
@XmlType(propOrder={"code", "error"})
@XmlAccessorType(XmlAccessType.FIELD)
public class TmiResponse extends TmiMessage {
    public static final String CODE_OK = "OK";
    public static final String CODE_NOK = "NOK";
    public static final String CODE_SYNTAX_ERROR = "SE";
    public static final String CODE_NOT_ALLOWED = "NA";
    public static final String CODE_PROTOCOL_ERROR = "PE";

    @XmlElement(name="ResponseCode")
    private String code;
    
    @XmlElement(name="ResponseError")
    private String error;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
