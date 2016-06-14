package nl.b3p.kar.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 *
 * @author Matthijs Laan
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RseqDefs {
    // Multiple RSEQDEF per RSEQDEFS are supported although not allowed in schema
    // Should only be used to support import, only add a single RoadsideEquipment
    // to the list for export
    @XmlElement(name="RSEQDEF")
    List<RoadsideEquipment> rseqs = new ArrayList();

    public RseqDefs() {
    }
    
    public RseqDefs(RoadsideEquipment rseq) {
        this.rseqs.add(rseq);
    }
    
    public RoadsideEquipment getRseq() {
        return rseqs.get(0);
    }

    public List<RoadsideEquipment> getRseqs() {
        return rseqs;
    }

    public void setRseqs(List<RoadsideEquipment> rseqs) {
        this.rseqs = rseqs;
    }
}
