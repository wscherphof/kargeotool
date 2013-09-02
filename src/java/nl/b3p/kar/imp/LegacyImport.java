package nl.b3p.kar.imp;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.geotools.geometry.jts.WKTReader2;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
public class LegacyImport {
    private PrintWriter out;
    
    private Map<Integer,String> daoCodes = new HashMap();
    
    public LegacyImport(PrintWriter out) {
        this.out = out;
    }
    
    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        props.put("user", "geo_ov");
        props.put("password", "YYPlDvZMkrOyYFB94rZ");
        return DriverManager.getConnection("jdbc:postgresql://x17/geo_ov_imported", props);
    }
    
    public void doImport() {
        out.println("Opening connection...");
    
        Connection c = null;
        try {
            c = getConnection();
            EntityManager em = Stripersist.getEntityManager();
            
            initDaoCodes(c);
            
            List<Map<String,Object>> rseqs = new QueryRunner().query(c, "select * from ovitech_rseq limit 1", new MapListHandler());
            
            for(Map<String,Object> rseq: rseqs) {
                out.printf("Importing rseq #%d \"%s\"...\n", rseq.get("id"), rseq.get("description"));
                
                RoadsideEquipment newRseq = new RoadsideEquipment();
                newRseq.setDataOwner(em.find(DataOwner.class, daoCodes.get((Integer)rseq.get("dataownerid"))));
                
                GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 28992);
                WKTReader reader = new WKTReader2(gf);
                Point p = (Point)reader.read((String)rseq.get("location"));
                newRseq.setLocation(p);

                newRseq.setKarAddress((Integer)rseq.get("radioaddress"));                
                newRseq.setValidFrom((Date)rseq.get("validfrom"));                
                newRseq.setValidUntil((Date)rseq.get("inactivefrom"));
                
                String oldType = (String)rseq.get("type");
                if("CROSS".equals(oldType)) {
                    newRseq.setType(RoadsideEquipment.TYPE_CROSSING);
                } else if("CLOSE".equals(oldType)) {
                    newRseq.setType(RoadsideEquipment.TYPE_BAR);
                } else if("PIU".equals(oldType)) {
                    newRseq.setType(RoadsideEquipment.TYPE_GUARD);
                } else {
                    out.printf("Invalid type: %s, skipping\n", oldType);
                    continue;
                }
                newRseq.setCrossingCode((String)rseq.get("suppliertypenumber"));
                newRseq.setDescription((String)rseq.get("description"));
                //newRseq
                out.println("JSON format: " + newRseq.getJSON().toString(4));
            }
            
        } catch(Exception e) {
            e.printStackTrace(out);
        } finally {
            if(c != null) {
                out.println("Closing connection.");
                DbUtils.closeQuietly(c);
            }
        }
    }
    
    private void initDaoCodes(Connection c) throws Exception {
        List<Map<String,Object>> dataOwners = new QueryRunner().query(c, "select * from ovitech_dao", new MapListHandler());
        for(Map<String,Object> dao: dataOwners) {
            Integer id = (Integer)dao.get("id");
            String code = (String)dao.get("code");
            
            if(code.equals("RWSZH")) {
                code = "RWSDZH";
            } else if(code.equals("RWSNH")) {
                code = "RWSDNH";
            } else if(code.equals("RWSZL")) {
                code = "RWSDZL";
            } else if(code.equals("RWSIJS")) {
                code = "RWSDIJG";
            } else if(code.equals("OVITECH")) {
                code = "B3P";
            } else if(code.equals("CBSPV27")) {
                code = "CBSPV0007";
            } else if(code.equals("CBSPV28")) {
                code = "CBSPV0008";
            } else if(code.equals("CBSPV26")) {
                code = "CBSPV0006";
            } else if(code.equals("CBSPV24")) {
                code = "CBSPV0012";
            } else if(code.equals("CBSPV29")) {
                code = "CBSPV0009";
            } else if(code.equals("YLA")) {
                code = "GOVI";
            }
            daoCodes.put(id, code);
        }
        
        out.println("daoCodes: " + daoCodes);
    }
}
