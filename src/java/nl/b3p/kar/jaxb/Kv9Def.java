package nl.b3p.kar.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 *
 * @author Matthijs Laan
 */
public class Kv9Def {

    @XmlElementWrapper(name="RSEQDEFS")
    @XmlElement(name="RSEQDEF")
    List<RoadsideEquipment> rseqs = new ArrayList();
    
    
}
