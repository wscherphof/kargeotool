package nl.b3p.kar.jaxb;

import com.vividsolutions.jts.geom.Point;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Matthijs Laan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace=Namespace.NS_B3P_GEO_OV_KV9)
public class XmlB3pRseq {
    public static class LocationAdapter extends XmlAdapter<Element, Point> {
        @Override
        public Point unmarshal(Element e) throws Exception {
            return (Point)GeometryTypeAdapterUtils.unmarshal(e, "location", Namespace.NS_B3P_GEO_OV_KV9);
        }

        @Override
        public Element marshal(Point p) throws Exception {
            return GeometryTypeAdapterUtils.marshal(p, "location", Namespace.NS_B3P_GEO_OV_KV9);
        }
    }
    
    @XmlAnyElement
    @XmlJavaTypeAdapter(LocationAdapter.class)
    private Point location;
    
    private String memo;

    public XmlB3pRseq() {
    }
    
    public XmlB3pRseq(RoadsideEquipment rseq) {
        this.location = rseq.getLocation();
        this.memo = rseq.getMemo();
    }
    
    public boolean isEmpty() {
        return location == null && StringUtils.isBlank(memo);
    }
}
