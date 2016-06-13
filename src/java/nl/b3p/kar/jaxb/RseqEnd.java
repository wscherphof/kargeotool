package nl.b3p.kar.jaxb;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 *
 * @author Matthijs Laan
 */
@XmlType(name="RSEQENDType", 
        propOrder={
            "dataownercode",
            "karaddress",
            "invalidfrom"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class RseqEnd {
    private String dataownercode;
    private Integer karaddress;
    
    @XmlJavaTypeAdapter(TmiDateAdapter.class)
    private Date invalidfrom;
    
    public RseqEnd() {
    }
    
    public RseqEnd(RoadsideEquipment rseq) {
        this.dataownercode = rseq.getDataOwner().getCode();
        this.karaddress = rseq.getKarAddress();
        this.invalidfrom = rseq.getValidUntil();
    }
}
