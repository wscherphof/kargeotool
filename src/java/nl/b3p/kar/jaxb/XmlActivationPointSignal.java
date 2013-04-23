package nl.b3p.kar.jaxb;

import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.VehicleType;

/**
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
