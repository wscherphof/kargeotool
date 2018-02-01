/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.*;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.imp.KV9ValidationError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

@StrictBinding
@UrlBinding("/action/validateAll")
public class ValidateAllRseqsActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(EditorActionBean.class);
    
    private ActionBeanContext context;

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    
    public Gebruiker getGebruiker() {
        final String attribute = this.getClass().getName() + "_GEBRUIKER";
        Gebruiker g = (Gebruiker)getContext().getRequest().getAttribute(attribute);
        if(g != null) {
            return g;
        }
        Gebruiker principal = (Gebruiker) context.getRequest().getUserPrincipal();
        g = Stripersist.getEntityManager().find(Gebruiker.class, principal.getId());
        getContext().getRequest().setAttribute(attribute, g);
        return g;
    }
    
    @DefaultHandler
    public Resolution validate() throws Exception {

        if(!getGebruiker().isBeheerder()) {
            throw new IllegalArgumentException();
        }
        
        final EntityManager em = Stripersist.getEntityManager();
        
        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                PrintWriter out = new PrintWriter(response.getWriter());
                
                List<RoadsideEquipment> rseqs = em.createQuery("from RoadsideEquipment order by id").getResultList();

                List<Object[]> results = new ArrayList();
                
                for(RoadsideEquipment rseq: rseqs) {
                    out.format("Valideren rseq #%d: %s: ", rseq.getId(), rseq.getDescription());
                    
                    int validationErrors = rseq.validateKV9(new ArrayList<KV9ValidationError>());      
                    
                    results.add(new Object[] {rseq.getId(), validationErrors});

                    out.println(validationErrors);
                }
                // Rollback omdat bij afkappen rseq gewijzigd wordt
                em.getTransaction().rollback();
                em.getTransaction().begin();
                out.flush();
                out.print("Updating database...");
                out.flush();
                for(Object[] r: results) {
                    em.createQuery("update RoadsideEquipment set validationErrors = :res where id = :id")
                            .setParameter("res", r[1])
                            .setParameter("id", r[0])
                            .executeUpdate();
                }
                em.getTransaction().commit();
                out.println(" done.");
                out.flush();
            }            
        };        
    }    
}
