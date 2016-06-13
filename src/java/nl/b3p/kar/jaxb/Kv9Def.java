package nl.b3p.kar.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 *
 * @author Matthijs Laan
 */
public class Kv9Def {

    @XmlElement(name="RSEQDEFS")
    List<RseqDefs> rseqs = new ArrayList();

    public List<RseqDefs> getRoadsideEquipments() {
        return rseqs;
    }
}
