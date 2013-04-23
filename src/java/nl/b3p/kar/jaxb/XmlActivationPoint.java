package nl.b3p.kar.jaxb;

import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.MovementActivationPoint;

/**
 *
 * @author Matthijs Laan
 */
@XmlType(name="BEGINType")
public class XmlActivationPoint {
    
    @XmlElement
    private Integer activationpointnumber;
    
    public XmlActivationPoint() {
    }
    
    public XmlActivationPoint(MovementActivationPoint map) {
        activationpointnumber = map.getPoint().getNummer();
    }
}
