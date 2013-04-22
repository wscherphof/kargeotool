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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse welke de edit functionaliteit regelt.
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/search")
public class SearchActionBean implements ActionBean {

    private static final String GEOCODER_URL = "http://geodata.nationaalgeoregister.nl/geocoder/Geocoder?zoekterm=";
    private static final Log log = LogFactory.getLog(SearchActionBean.class);
    private ActionBeanContext context;
    @Validate
    private String term;

    public Resolution rseq() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {

            Session sess = (Session) em.getDelegate();
            Criteria criteria = sess.createCriteria(RoadsideEquipment.class);

            Disjunction dis = Restrictions.disjunction();
            dis.add( Restrictions.ilike("description", term, MatchMode.ANYWHERE));
          
            try {
                int karAddress = Integer.parseInt(term);
                dis.add( Restrictions.eq("karAddress", karAddress));
            } catch (NumberFormatException e) {
            }
            
            dis.add(Restrictions.ilike("crossingCode", term, MatchMode.ANYWHERE));

            List<RoadsideEquipment> l = criteria.list();
            JSONArray rseqs = new JSONArray();
            for (RoadsideEquipment roadsideEquipment : l) {
                rseqs.put(roadsideEquipment.getRseqGeoJSON());
            }
            info.put("rseqs", rseqs);
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("search rseq exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    public Resolution road() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {

            Session sess = (Session) em.getDelegate();
            Query q = sess.createSQLQuery("SELECT ref,name,astext(st_union(geometry)) FROM Road where ref ilike :ref group by ref,name");
            q.setParameter("ref", "%"+term+"%");

            List<Object[]> l = (List<Object[]>) q.list();
            JSONArray roads = new JSONArray();
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 28992);
            WKTReader reader = new WKTReader(gf);
            for (Object[] road : l) {
                JSONObject jRoad = new JSONObject();
                jRoad.put("weg", road[0]);
                if(road[1] != null){
                    jRoad.put("name", road[1]);
                }
                try {
                    Geometry g = reader.read((String) road[2]);
                    Envelope env = g.getEnvelopeInternal();
                    if(env != null){
                        JSONObject jEnv = new JSONObject();
                        jEnv.put("minx",env.getMinX());
                        jEnv.put("miny",env.getMinY());
                        jEnv.put("maxx",env.getMaxX());
                        jEnv.put("maxy",env.getMaxY());
                        jRoad.put("envelope", jEnv);
                    }
                } catch (ParseException ex) {}
                roads.put(jRoad);
            }
            info.put("roads",roads);
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("search road exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    /**
     *
     * @return Stripes Resolution geocode
     * @throws Exception
     */
    public Resolution geocode() throws Exception {
        InputStream in = new URL(GEOCODER_URL + URLEncoder.encode(term, "UTF-8")).openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        in.close();
        bos.close();
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
    // <editor-fold desc="Getters and Setters">

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
    // </editor-fold>
}
