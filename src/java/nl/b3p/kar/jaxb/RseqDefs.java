package nl.b3p.kar.jaxb;

import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 *
 * @author Matthijs Laan
 */
public class RseqDefs {
    @XmlElement(name="RSEQDEF")
    RoadsideEquipment rseq;

    public RseqDefs() {
    }
    
    public RseqDefs(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }
    
    public RoadsideEquipment getRseq() {
        return rseq;
    }
}
