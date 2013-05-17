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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import java.io.StringReader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.geojson.GeoJSON;
import nl.b3p.kar.hibernate.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Stripes klasse welke de edit functionaliteit regelt.
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/editor")
public class EditorActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(EditorActionBean.class);
    private static final String JSP = "/WEB-INF/jsp/viewer/editor.jsp";
    private static final int RIJKSDRIEHOEKSTELSEL = 28992;
    private ActionBeanContext context;
    private boolean magWalapparaatMaken;
    @Validate
    private RoadsideEquipment rseq;
    @Validate
    private String json;
    private JSONArray vehicleTypesJSON;
    private JSONArray dataOwnersJSON;
    private JSONArray ovInfoJSON;
    
    @Validate
    private String extent;

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
    
    /**
     * Stripes methode waarmee de view van het edit proces wordt voorbereid.
     *
     * @return Stripes Resolution view
     * @throws Exception
     */
    @DefaultHandler
    public Resolution view() throws Exception {

        EntityManager em = Stripersist.getEntityManager();
        boolean isBeheerder = getGebruiker().isBeheerder();

        Set s = getGebruiker().getEditableDataOwners();
        if (s.size() >= 1 || isBeheerder) {
            magWalapparaatMaken = true;
        } else {
            magWalapparaatMaken = false;
        }

        vehicleTypesJSON = new JSONArray();
        for (VehicleType vt : (List<VehicleType>) em.createQuery("from VehicleType order by nummer").getResultList()) {
            JSONObject jvt = new JSONObject();
            jvt.put("nummer", vt.getNummer());
            jvt.put("groep", vt.getGroep());
            jvt.put("omschrijving", vt.getOmschrijving());
            vehicleTypesJSON.put(jvt);
        }

        dataOwnersJSON = new JSONArray();
        Collection<DataOwner> dataOwners;
        if(isBeheerder) {
            dataOwners = (List<DataOwner>) em.createQuery("from DataOwner order by code").getResultList();
        } else {
            dataOwners = s;
        }
        for (DataOwner dao : dataOwners) {
            JSONObject jdao = new JSONObject();
            jdao.put("code", dao.getCode());
            jdao.put("classificatie", dao.getClassificatie());
            jdao.put("companyNumber", dao.getCompanyNumber());
            jdao.put("omschrijving", dao.getOmschrijving());
            dataOwnersJSON.put(jdao);
        }        
        
        ovInfoJSON = makeOvInfoJSON();
        
        return new ForwardResolution(JSP);
    }
    
    private JSONArray makeOvInfoJSON() {
        JSONArray ovInfo = new JSONArray();
        
        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource)initCtx.lookup("java:comp/env/jdbc/transmodel");            
            Connection conn = ds.getConnection();
            
            List<String> schemas = new QueryRunner().query(conn, "select schema_name from information_schema.schemata where schema_owner <> 'postgres'", new ColumnListHandler<String>(1));
            
            SortedMap<String,JSONObject> ovInfoMap = new TreeMap();
            for(String schema: schemas) {
                JSONObject ovSchema = new JSONObject();
                ovSchema.put("schema", schema);

                try {
                    Map<String,Object> meta = new QueryRunner().query(conn, "select * from " + schema + ".geo_ov_metainfo", new MapHandler());
                    
                    for(Map.Entry<String,Object> entry: meta.entrySet()) {
                        ovSchema.put(entry.getKey(), entry.getValue());
                    }
                    
                    Object[] extent = new QueryRunner().query(conn, "select st_xmin(ext),st_xmax(ext),st_ymin(ext),st_ymax(ext) from (select st_extent(the_geom) as ext from " + schema + ".jopa) e", new ArrayHandler());
                    JSONObject jExtent = new JSONObject();
                    jExtent.put("xmin", (Double)extent[0]);
                    jExtent.put("xmax", (Double)extent[1]);
                    jExtent.put("ymin", (Double)extent[2]);
                    jExtent.put("ymax", (Double)extent[3]);
                    ovSchema.put("extent", jExtent);
                    
                    ovInfoMap.put(ovSchema.getString("title"), ovSchema);
                } catch(Exception e) {
                    log.info("Fout bij opvragen geo_ov_metainfo tabel in schema in transmodel database \"" + schema + "\", geen ov info?", e);
                    // schema waarschijnlijk niet via geo-ov geimporteerd
                }
            }

            for(JSONObject ovSchema: ovInfoMap.values()) {
                ovInfo.put(ovSchema);
            }
        } catch(Exception e) {
            log.error("Kan geen ov info ophalen: ", e);
        }
        return ovInfo;
    }

    /**
     * Stripes methode waarmee de huidge roadside equipement wordt opgehaald.
     *
     * @return Stripes Resolution rseqJSON
     * @throws Exception
     */
    public Resolution rseqJSON() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            RoadsideEquipment rseq2;

            if (rseq != null) {
                rseq2 = rseq;
            } else {
                throw new IllegalArgumentException("RoadSideEquipment not defined.");
            }
            if(!getGebruiker().isBeheerder() && !getGebruiker().canEditDataOwner(rseq2.getDataOwner())&& !getGebruiker().canReadDataOwner(rseq2.getDataOwner())) {
                info.put("error", "De gebruiker is niet gemachtigd om dit verkeerssysteem te bewerken of lezen.");
            } else {
                info.put("roadsideEquipment", rseq2.getJSON());
                info.put("editable",getGebruiker().canEditDataOwner(rseq2.getDataOwner()));

                info.put("success", Boolean.TRUE);
            }
        } catch (Exception e) {
            log.error("rseqJSON exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    /**
     * Stripes methode waarmee alle roadside equipement wordt opgehaald.
     *
     * @return Stripes Resolution allRseqJSON
     * @throws Exception
     */
    public Resolution allRseqJSON() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            List<RoadsideEquipment> rseq2;

            if(getGebruiker().isBeheerder()) {
                if (rseq != null) {
                    rseq2 = (List<RoadsideEquipment>) em.createQuery("from RoadsideEquipment where id <> :id").setParameter("id", rseq.getId()).getResultList();
                } else {
                    rseq2 = (List<RoadsideEquipment>) em.createQuery("from RoadsideEquipment").getResultList();
                }
            } else {
                Set<DataOwner> dos = getGebruiker().getAvailableDataOwners();
                if(dos.isEmpty()) {
                    rseq2 = Collections.EMPTY_LIST;
                } else {
                    if (rseq != null) {
                        rseq2 = (List<RoadsideEquipment>) em.createQuery(
                                "from RoadsideEquipment "
                              + "where id <> :id "
                              + "and dataOwner in (:dos)")
                                .setParameter("id", rseq.getId())
                                .setParameter("dos", dos)
                                .getResultList();
                    } else {
                        rseq2 = (List<RoadsideEquipment>) em.createQuery(
                                "from RoadsideEquipment "
                              + "where dataOwner in (:dos)")
                                .setParameter("dos", dos)
                                .getResultList();
                    }
                }
            }
            Gebruiker g =getGebruiker();
            JSONArray rseqs = new JSONArray();
            for (RoadsideEquipment r : rseq2) {
                JSONObject rseqJson = r.getRseqGeoJSON();
                rseqJson.put("editable", g.canEditDataOwner(r.getDataOwner()));
                rseqs.put(rseqJson);
                
            }
            info.put("rseqs", rseqs);

            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("allRseqJSON exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    public Resolution roads() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {

            Point p = rseq.getLocation();
            Polygon buffer = (Polygon) p.buffer(300, 1);
            buffer.setSRID(RIJKSDRIEHOEKSTELSEL);
            Session session = (Session) em.getDelegate();

            Query q = session.createQuery("from Road where intersects(geometry, ?) = true");
            q.setMaxResults(100);
            Type geometryType = GeometryUserType.TYPE;
            q.setParameter(0, buffer, geometryType);
            List<Road> roads = (List<Road>)q.list();
          
            JSONArray rs = new JSONArray();
            for (Road r : roads) {
                rs.put(r.getGeoJSON());
            }
            info.put("roads", rs);

            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("roads exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    
    public Resolution surroundingPoints() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            
            GeometryFactory gf = new GeometryFactory(new PrecisionModel(), RIJKSDRIEHOEKSTELSEL);
            WKTReader reader= new WKTReader(gf);
            Polygon p = (Polygon)reader.read(extent);
          //  p.setSRID(RIJKSDRIEHOEKSTELSEL);
            Session session = (Session) em.getDelegate();

            Collection<DataOwner> dos = getGebruiker().getAvailableDataOwners();
            List<ActivationPoint> points;
            if(getGebruiker().isBeheerder()) {
                points = session.createQuery(
                        "from ActivationPoint "
                      + "where intersects(location, :pos) = true "
                      + "and roadsideEquipment <> :this")
                        .setParameter("pos", p, GeometryUserType.TYPE)
                        .setParameter("this", rseq)
                        .list();
            } else if(dos.isEmpty()) {
                points = Collections.EMPTY_LIST;
            } else {
                points = session.createQuery(
                        "from ActivationPoint "
                      + "where roadsideEquipment.dataOwner in (:dos) "
                      + "and intersects(location, :pos) = true and roadsideEquipment <> :this")
                        .setParameterList("dos", dos)
                        .setParameter("pos", p, GeometryUserType.TYPE)
                        .setParameter("this", rseq)
                        .list();
            }
          
            JSONArray rs = new JSONArray();
            for (ActivationPoint r : points) {
                
                rs.put(r.getGeoJSON());
            }
            info.put("points", rs);

            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("surroundingPoints exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }
    
    /**
     * Bepaalt of een JSON object nieuw gemaakt client-side. De door JavaScript
     * client-side gestuurde JSON om een RoadsideEquipment op te slaan kan nieuw
     * gemaakte objecten bevatten die nog geen persistente JPA entities zijn,
     * deze hebben een door ExtJS bepaald id dat begint met "ext-gen".
     *
     * @param j het JSON object
     * @return of het JSON object client-side nieuw gemaakt is
     */
    private static boolean isNew(JSONObject j) {
        String id = j.optString("id");
        return id != null && id.startsWith("ext-gen");
    }
    
    /**
     * Ajax handler om een RSEQ te verwijderen.
     * 
     */
    public Resolution removeRseq () throws JSONException{
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try{
            em.remove(rseq);
            em.getTransaction().commit();
            info.put("success", Boolean.TRUE);
        }catch(Exception e){
            log.error("Removing of rseq failed.",e);
            info.put("error",ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }
    /**
     * Ajax handler om een RoadsideEquipment die in de json parameter is
     * meegegeven op te slaan.
     */
    public Resolution saveOrUpdateRseq() throws Exception {
        EntityManager em = Stripersist.getEntityManager();

        JSONObject info = new JSONObject();
        info.put("success", Boolean.FALSE);
        try {
            JSONObject jrseq = new JSONObject(json);

            Gebruiker g = getGebruiker();
            
            if (isNew(jrseq)) {
                rseq = new RoadsideEquipment();
            } else {
                rseq = em.find(RoadsideEquipment.class, jrseq.getLong("id"));
                
                if(rseq == null) {
                    throw new IllegalStateException("Kan verkeerssysteem niet vinden, verwijderd door andere gebruiker?");
                }
            }

            rseq.setLocation(GeoJSON.toPoint(jrseq.getJSONObject("location")));
            rseq.setDescription(jrseq.optString("description"));
            rseq.setTown(jrseq.optString("town"));
            rseq.setType(jrseq.getString("type"));
            rseq.setKarAddress(jrseq.getInt("karAddress"));
            rseq.setCrossingCode(jrseq.optString("crossingCode"));
            rseq.setDataOwner(em.find(DataOwner.class, jrseq.getString("dataOwner")));
            if(rseq.getDataOwner() == null) {
                throw new IllegalArgumentException("Data owner is verplicht");
            }
            if(!g.isBeheerder()) {
                if(!g.canEditDataOwner(rseq.getDataOwner())) {
                    throw new IllegalStateException(
                            String.format("Gebruiker \"%s\" heeft geen rechten op data owner code %s (\"%s\")",
                            g.getUsername(),
                            rseq.getDataOwner().getCode(),
                            rseq.getDescription()));
                }
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            rseq.setValidFrom(jrseq.has("validFrom") ? sdf.parse(jrseq.getString("validFrom")) : null);
            rseq.setValidUntil(jrseq.has("validUntil") ? sdf.parse(jrseq.getString("validUntil")) : null);
            rseq.setMemo(jrseq.has("memo") ? jrseq.getString("memo") : null);
            
            rseq.getKarAttributes().clear();
            JSONObject attributes = jrseq.getJSONObject("attributes");
            for(Iterator it = attributes.keys(); it.hasNext();) {
                String serviceType = (String)it.next();
                JSONArray perCommandType = attributes.getJSONArray(serviceType);
                
                KarAttributes ka = new KarAttributes(
                        serviceType, 
                        ActivationPointSignal.COMMAND_INMELDPUNT, 
                        perCommandType.getJSONArray(0));
                if(ka.getUsedAttributesMask() != 0) {
                    rseq.getKarAttributes().add(ka);
                }
                ka = new KarAttributes(
                        serviceType, 
                        ActivationPointSignal.COMMAND_UITMELDPUNT, 
                        perCommandType.getJSONArray(1));
                if(ka.getUsedAttributesMask() != 0) {
                    rseq.getKarAttributes().add(ka);
                }
                ka = new KarAttributes(
                        serviceType, 
                        ActivationPointSignal.COMMAND_VOORINMELDPUNT, 
                        perCommandType.getJSONArray(2));
                if(ka.getUsedAttributesMask() != 0) {
                    rseq.getKarAttributes().add(ka);
                }
            }

            if (rseq.getId() == null) {
                em.persist(rseq);
            }

            JSONArray jpts = jrseq.getJSONArray("points");
            Integer highestPointNumber = 0;
            Map<String, ActivationPoint> pointsByJSONId = new HashMap();
            for (int i = 0; i < jpts.length(); i++) {
                JSONObject jpt = jpts.getJSONObject(i);

                ActivationPoint p = null;
                if (isNew(jpt)) {
                    p = new ActivationPoint();
                    p.setRoadsideEquipment(rseq);
                } else {
                    for (ActivationPoint ap2 : rseq.getPoints()) {
                        if (ap2.getId().equals(jpt.getLong("id"))) {
                            p = ap2;
                            break;
                        }
                    }
                }
                pointsByJSONId.put(jpt.getString("id"), p);
                p.setNummer(jpt.has("nummer") ? jpt.getInt("nummer") : null);
                p.setLabel(jpt.optString("label"));
                p.setLocation(GeoJSON.toPoint(jpt.getJSONObject("geometry")));
                if (p.getNummer() != null) {
                    highestPointNumber = Math.max(p.getNummer(), highestPointNumber);
                }
            }

            for (ActivationPoint ap : pointsByJSONId.values()) {
                if (ap.getNummer() == null) {
                    ap.setNummer(++highestPointNumber);
                }
                if (ap.getId() == null) {
                    rseq.getPoints().add(ap);
                    em.persist(ap);
                }

            }

            // XXX delete niet goed
            rseq.getPoints().retainAll(pointsByJSONId.values());

            Set<Long> mIds = new HashSet();
            for (Movement m : rseq.getMovements()) {
                m.getPoints().clear();
                mIds.add(m.getId());
            }
            em.flush();
            if (!mIds.isEmpty()) {
                em.createNativeQuery("delete from movement_activation_point where movement in (:m)")
                        .setParameter("m", mIds)
                        .executeUpdate();
            }
            rseq.getMovements().clear();
            em.flush();

            JSONArray jmvmts = jrseq.getJSONArray("movements");
            for (int i = 0; i < jmvmts.length(); i++) {
                JSONObject jmvmt = jmvmts.getJSONObject(i);
                Movement m = new Movement();
                m.setRoadsideEquipment(rseq);
                m.setNummer(jmvmt.has("nummer") ? jmvmt.getInt("nummer") : null);

                JSONArray jmaps = jmvmt.getJSONArray("maps");
                for (int j = 0; j < jmaps.length(); j++) {
                    JSONObject jmap = jmaps.getJSONObject(j);
                    MovementActivationPoint map = new MovementActivationPoint();
                    map.setMovement(m);
                    map.setBeginEndOrActivation(jmap.getString("beginEndOrActivation"));
                    map.setPoint(pointsByJSONId.get(jmap.getString("pointId")));

                    if (MovementActivationPoint.ACTIVATION.equals(map.getBeginEndOrActivation())) {
                        ActivationPointSignal signal = new ActivationPointSignal();
                        map.setSignal(signal);
                        String s = jmap.optString("distanceTillStopLine", "");
                        signal.setDistanceTillStopLine(!"".equals(s) ? new Integer(s) : null);
                        signal.setKarCommandType(jmap.getInt("commandType"));
                        s = jmap.optString("signalGroupNumber");
                        signal.setSignalGroupNumber(!"".equals(s) ? new Integer(s) : null);
                        s = jmap.optString("virtualLocalLoopNumber");
                        signal.setVirtualLocalLoopNumber(!"".equals(s) ? new Integer(s) : null);
                        signal.setTriggerType(jmap.getString("triggerType"));
                        JSONArray vtids = jmap.optJSONArray("vehicleTypes");
                        if (vtids != null) {
                            for (int k = 0; k < vtids.length(); k++) {
                                signal.getVehicleTypes().add(em.find(VehicleType.class, vtids.getInt(k)));
                            }
                        }
                        JSONArray dir = jmap.optJSONArray("direction");
                        if(dir != null){
                            String direction = dir.join(",");
                            signal.setDirection(direction);
                        }
                    }
                    m.getPoints().add(map);
                }
                if (!m.getPoints().isEmpty()) {
                    m.setNummer(rseq.getMovements().size() + 1);
                    em.persist(m);
                    rseq.getMovements().add(m);
                }
            }

            em.persist(rseq);
            em.getTransaction().commit();

            info.put("roadsideEquipment", rseq.getJSON());
            info.put("success", Boolean.TRUE);
        } catch (Exception e) {
            log.error("saveOrUpdateRseq exception", e);
            info.put("error", ExceptionUtils.getMessage(e));
        }
        return new StreamingResolution("application/json", new StringReader(info.toString(4)));
    }

    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    /**
     *
     * @return magWalapparaatMaken
     */
    public boolean isMagWalapparaatMaken() {
        return magWalapparaatMaken;
    }

    /**
     *
     * @param magWalapparaatMaken
     */
    public void setMagWalapparaatMaken(boolean magWalapparaatMaken) {
        this.magWalapparaatMaken = magWalapparaatMaken;
    }

    /**
     *
     * @return rseq
     */
    public RoadsideEquipment getRseq() {
        return rseq;
    }

    /**
     *
     * @param rseq
     */
    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }

    /**
     *
     * @return dataOwnersJSON
     */
    public JSONArray getDataOwnersJSON() {
        return dataOwnersJSON;
    }

    /**
     *
     * @param dataOwnersJSON
     */
    public void setDataOwnersJSON(JSONArray dataOwnersJSON) {
        this.dataOwnersJSON = dataOwnersJSON;
    }

    /**
     *
     * @return vehicleTypesJSON
     */
    public JSONArray getVehicleTypesJSON() {
        return vehicleTypesJSON;
    }

    /**
     *
     * @param vehicleTypesJSON
     */
    public void setVehicleTypesJSON(JSONArray vehicleTypesJSON) {
        this.vehicleTypesJSON = vehicleTypesJSON;
    }

    /**
     *
     * @return json
     */
    public String getJson() {
        return json;
    }

    /**
     *
     * @param json
     */
    public void setJson(String json) {
        this.json = json;
    }
    
    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }
    
    public JSONArray getOvInfoJSON() {
        return ovInfoJSON;
    }

    public void setOvInfoJSON(JSONArray ovInfoJSON) {
        this.ovInfoJSON = ovInfoJSON;
    }
    // </editor-fold>
}
