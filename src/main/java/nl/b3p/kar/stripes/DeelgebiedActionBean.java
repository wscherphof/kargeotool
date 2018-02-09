/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2018 B3Partners B.V.
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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import nl.b3p.kar.hibernate.Deelgebied;
import nl.b3p.kar.hibernate.Gebruiker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen
 */
@UrlBinding("/action/deelgebied")
@StrictBinding
public class DeelgebiedActionBean implements ActionBean{
  
    private static final Log log = LogFactory.getLog(DeelgebiedActionBean.class);
    private static final String NIEUW_DEELGEBIED = "/WEB-INF/jsp/export/deelgebied.jsp";
    private final int SRID = 28992;
    
    private ActionBeanContext context;
    
    @Validate(on = "saveDeelgebied", required = true)
    private String geom;

    @Validate(converter = EntityTypeConverter.class, on = {"saveDeelgebied"})
    @ValidateNestedProperties({
        @Validate(field = "name")
    })
    private Deelgebied deelgebied;

    @Validate(converter = EntityTypeConverter.class, on = {"bewerkDeelgebied"})
    private Deelgebied filter;
    
    @DefaultHandler
    public Resolution maakDeelgebied() {
        deelgebied = new Deelgebied();
        return new ForwardResolution(NIEUW_DEELGEBIED);
    }

    public Resolution bewerkDeelgebied() {
        deelgebied = filter;
        return new ForwardResolution(NIEUW_DEELGEBIED);
    }
    
    
    public Resolution saveDeelgebied() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID);
        WKTReader reader = new WKTReader(gf);
        try {
            Polygon geometrie = (Polygon) reader.read(geom);
            deelgebied.setGeom(geometrie);
            deelgebied.setGebruiker(getGebruiker());
            em.persist(deelgebied);
            em.getTransaction().commit();
            filter = deelgebied;
        } catch (ParseException ex) {
            log.error(ex);
        }
        return new ForwardResolution(ExportActionBean.class);
    }


    public Resolution removeDeelgebied() throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        deelgebied = filter;
        em.remove(deelgebied);
        em.getTransaction().commit();
        return new ForwardResolution(ExportActionBean.class);
    }


    // <editor-fold desc="Getters and setters" defaultstate="collapsed">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    
    public Deelgebied getDeelgebied() {
        return deelgebied;
    }

    public void setDeelgebied(Deelgebied deelgebied) {
        this.deelgebied = deelgebied;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
    
    public Gebruiker getGebruiker() {
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        Gebruiker g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
        return g;
    }
    
    
    // </editor-fold>

    public Deelgebied getFilter() {
        return filter;
    }

    public void setFilter(Deelgebied filter) {
        this.filter = filter;
    }
    
 
}
