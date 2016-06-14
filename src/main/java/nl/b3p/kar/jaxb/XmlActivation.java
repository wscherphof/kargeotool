package nl.b3p.kar.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.VehicleType;

/**
 *
 * @author Matthijs Laan
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlActivation {
    @XmlElement(name="ACTIVATIONPOINTSIGNAL")
    List<XmlActivationPointSignal> signals = new ArrayList();
    
    public XmlActivation() {
    }
    
    public XmlActivation(MovementActivationPoint map) {
        for(VehicleType vt: map.getSignal().getVehicleTypes()) {
            signals.add(new XmlActivationPointSignal(map, vt));
        }
    }

    public List<XmlActivationPointSignal> getSignals() {
        return signals;
    }

    public void setSignals(List<XmlActivationPointSignal> signals) {
        this.signals = signals;
    }
}
