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

import org.locationtech.jts.geom.Geometry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.geotools.gml.producer.GeometryTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility classe met static methodes voor het gebruik in @XmlJavaTypeAdapters
 * die Geometry types converteren.
 * 
 * @author Matthijs Laan
 */
public class GeometryTypeAdapterUtils {

    public static Geometry unmarshal(Element vt, String elementName, String namespace) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static Element marshal(Geometry g, String elementName, String namespace) throws Exception {
        
        if(g == null) {
            return null;
        }
        
        GeometryTransformer gt = new GeometryTransformer();
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        gt.transform(g, xml);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.toByteArray()));

        Document doc2 = db.newDocument();
        Node n = doc2.createElementNS(namespace, elementName);
        doc2.appendChild(n);
        
        n.appendChild(doc2.adoptNode(doc.getDocumentElement()));
        
        return doc2.getDocumentElement();
    }
}
