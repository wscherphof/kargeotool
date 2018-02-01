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

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converteert een Date naar een TMI "tmidateType" in formaat YYYY-MM-DD, zie
 * kv9 schema.
 * 
 * @author Matthijs Laan
 */
public class TmiDateAdapter extends XmlAdapter<String, Date> {

    private static final String TMI_DATE_FORMAT = "yyyy-MM-dd";
    
    private SimpleDateFormat buildDateFormat() {
        return new SimpleDateFormat(TMI_DATE_FORMAT);
    }
    
    @Override
    public Date unmarshal(String date) throws Exception {
        return buildDateFormat().parse(date);
    }

    @Override
    public String marshal(Date date) throws Exception {
        return buildDateFormat().format(date);
    }
    
}
