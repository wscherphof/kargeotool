/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2013 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.kar.stripes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.jaxb.KarNamespacePrefixMapper;
import nl.b3p.kar.jaxb.TmiPush;

/**
 * Exporteren van KV9 gegevens. 
 * 
 * @author Matthijs Laan
 */
@UrlBinding("/action/export")
@StrictBinding
public class ExportActionBean implements ActionBean {

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
    
    public Resolution exportXml() throws Exception {
        
        JAXBContext ctx = JAXBContext.newInstance(TmiPush.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new KarNamespacePrefixMapper());
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        /* TODO subscriberId per dataOwner of in gebruikersprofiel instellen/vragen oid */
        TmiPush push = new TmiPush("B3P", Arrays.asList(new RoadsideEquipment[] { rseq }));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        m.marshal(push, bos);
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()))
                .setAttachment(true)
                .setFilename("geo-ov_kv9_" + rseq.getDataOwner().getCode() + "_" + rseq.getKarAddress() + ".xml")
                .setLength(bos.size());
    }
}
