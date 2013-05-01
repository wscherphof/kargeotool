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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse welke de zoek functionaliteit regelt.
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/search")
public class SearchActionBean implements ActionBean {

    private static final String GEOCODER_URL = "http://geodata.nationaalgeoregister.nl/geocoder/Geocoder?zoekterm=";
    private static final Log log = LogFactory.getLog(SearchActionBean.class);
    private static final int RIJKSDRIEHOEKSTELSEL = 28992;
    private ActionBeanContext context;
    @Validate
    private String term;
    
    private static final String TRANSMODEL_JNDI_NAME = "java:comp/env/jdbc/transmodel";

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
     * Doorzoekt de KV1 database. Gebruikt de parameter term voor publicnumber en name uit de jopa tabel. Alleen resultaten met een geometrie worden teruggegeven.
     * @return Resolution Resolution met daarin een JSONObject met de gevonden buslijnen (bij succes) of een fout (bij falen).
     * @throws Exception 
     * 
     */
    public Resolution busline()throws Exception {

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        
        Connection c = getConnection();
        try {
            
            String sql = "select j.oid,l.name,l.publicnumber,st_astext(j.the_geom) ,j.lineplanningnumber,j.code,j.destinationcode,j.direction from jopa j left join line l on (j.lineplanningnumber = l.planningnumber) "
                    + "where j.the_geom is not null and l.name ilike ? ";
            
            ResultSetHandler<JSONArray> h = new ResultSetHandler<JSONArray>() {
                public JSONArray handle(ResultSet rs) throws SQLException {
                    JSONArray lines = new JSONArray();
                    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), RIJKSDRIEHOEKSTELSEL);
                    WKTReader reader = new WKTReader(gf);
                    while(rs.next()) {
                        JSONObject line = new JSONObject();
                        try{
                        line.put("publicnumber", rs.getString(3));
                        line.put("name", rs.getString(2));
                        try {
                            String wkt = rs.getString(4);
                            Geometry g = reader.read(wkt);
                            if(g != null){
                                Envelope env = g.getEnvelopeInternal();
                                if(env != null){
                                    JSONObject jEnv = new JSONObject();
                                    jEnv.put("minx",env.getMinX());
                                    jEnv.put("miny",env.getMinY());
                                    jEnv.put("maxx",env.getMaxX());
                                    jEnv.put("maxy",env.getMaxY());
                                    line.put("envelope", jEnv);
                                }
                            }
                        } catch (ParseException ex) {}
                        lines.put(line);
                        }catch(JSONException je){
                            log.error("Kan geen buslijn ophalen: ", je);
                        }
                    }                    
                    return lines;
                }
            };
            JSONArray lines = new QueryRunner().query(c, sql, h,"%"+term+"%");
            info.put("buslines", lines);
            info.put("success", Boolean.TRUE);
        } catch(Exception e){
            log.error("Cannot execute query:",e);
        }finally {
            DbUtils.closeQuietly(c);
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
    
    
    private Connection getConnection() throws NamingException, SQLException {
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup(TRANSMODEL_JNDI_NAME);

        return ds.getConnection();        
    }
    // </editor-fold>
}
