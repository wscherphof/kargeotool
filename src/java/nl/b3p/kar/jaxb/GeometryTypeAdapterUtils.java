package nl.b3p.kar.jaxb;

import com.vividsolutions.jts.geom.Geometry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.geotools.gml.producer.GeometryTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Matthijs Laan
 */
public class GeometryTypeAdapterUtils {

    public static Geometry unmarshal(Element vt, String elementName, String namespace) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static Element marshal(Geometry g, String elementName, String namespace) throws Exception {
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
