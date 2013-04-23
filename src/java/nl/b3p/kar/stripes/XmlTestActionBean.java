package nl.b3p.kar.stripes;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.jaxb.Namespace;
import nl.b3p.kar.hibernate.RoadsideEquipment;

/**
 *
 * @author Matthijs Laan
 */
@UrlBinding("/action/xml")
@StrictBinding
public class XmlTestActionBean implements ActionBean {
    private ActionBeanContext context;
    
    @Validate(required=true)
    private RoadsideEquipment rseq;

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public RoadsideEquipment getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }
    
    public Resolution test() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(RoadsideEquipment.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if(Namespace.NS_BISON_TMI8_KV9_MSG.equals(namespaceUri)) {
                    return "tmi8";
                } else if(Namespace.NS_B3P_GEO_OV.equals(namespaceUri)) {
                    return "b3p";
                } else {
                    return suggestion;
                }
            }
        });
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        m.marshal(rseq, bos);
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
    
}
