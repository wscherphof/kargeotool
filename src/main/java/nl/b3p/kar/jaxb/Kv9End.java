package nl.b3p.kar.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Matthijs Laan
 */
public class Kv9End {
    @XmlElement(name="RSEQEND")
    List<RseqEnd> rseqs = new ArrayList();    
}
