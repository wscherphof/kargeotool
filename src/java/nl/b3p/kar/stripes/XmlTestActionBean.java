package nl.b3p.kar.stripes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.jaxb.KarNamespacePrefixMapper;
import nl.b3p.kar.jaxb.TmiPush;
import nl.b3p.kar.jaxb.TmiResponse;

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
    
    @DefaultHandler
    public Resolution test() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(TmiPush.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new KarNamespacePrefixMapper());
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        TmiPush push = new TmiPush("B3P", Arrays.asList(new RoadsideEquipment[] { rseq }));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        m.marshal(push, bos);
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
    
    @DontValidate
    public Resolution testResponse() throws JAXBException {
        
        JAXBContext ctx = JAXBContext.newInstance(TmiResponse.class);
        Unmarshaller u = ctx.createUnmarshaller();
        
        TmiResponse response = (TmiResponse)u.unmarshal(new File("kv9-RSP.xml"));
        
        Marshaller m = ctx.createMarshaller();
        
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        m.marshal(response, bos);
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
    
    @DontValidate
    public Resolution testLoad() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(TmiPush.class);
        Unmarshaller u = ctx.createUnmarshaller();
        
        TmiPush push = (TmiPush)u.unmarshal(new File("/home/matthijsln/Downloads/geo-ov_kv9_B3P_235.xml"));
        
        Marshaller m = ctx.createMarshaller();
        
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        m.marshal(push, bos);
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
}
