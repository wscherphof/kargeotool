package nl.b3p.tmi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class TmiDbImport {
    private static final Log log = LogFactory.getLog(TmiDbImport.class);

    private static final Map<String, TmiField[]> tmiData = new HashMap();
    
    static {
        tmiData.put("point", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("code","s"),
            new TmiField("validfrom","d"),
            new TmiField("pointtype","s"),
            null, // coordinatesystemtype
            new TmiField("x","l"),
            new TmiField("y","l"),
            null, // z
            new TmiField("description","s")                
        });
        
        tmiData.put("pool", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("userstopcodebegin","s"),
            new TmiField("userstopcodeend","s"),
            new TmiField("linkvalidfrom","d"),
            new TmiField("pointdataownercode","s"),
            new TmiField("pointcode","s"),
            new TmiField("distancesincestartoflink","l"),
            new TmiField("segmentspeed","l"),
            new TmiField("localpointspeed","l"),
            new TmiField("description","s")//,                
            //new TmiField("transporttype","s")                
        });

        tmiData.put("jopatili", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("lineplanningnumber","s"),
            new TmiField("journeypatterncode","s"),
            new TmiField("timinglinkorder","l"),
            new TmiField("userstopcodebegin","s"),
            new TmiField("userstopcodeend","s"),
            new TmiField("confinrelcode","s"),
            new TmiField("destcode","s"),
            null,
            new TmiField("istimingstop","b"),
            new TmiField("displaypublicline","s")//,
            //new TmiField("productformulatype","l")
        });  

        tmiData.put("jopa", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("lineplanningnumber","s"),
            new TmiField("journeypatterncode","s"),
            new TmiField("journeypatterntype","s"),
            new TmiField("direction","s"),
            new TmiField("description","s")
        });
        
        tmiData.put("line", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("lineplanningnumber","s"),
            new TmiField("linepublicnumber","s"),
            new TmiField("linename","s"),
            new TmiField("linevetagnumber","l"),
            new TmiField("description","s")//,
            //new TmiField("transporttype","s")
        });  
        
        tmiData.put("usrstop", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("userstopcode","s"),
            new TmiField("timingpointcode","s"),
            new TmiField("getin","b"),
            new TmiField("getout","b"),
            null,
            new TmiField("name","s"),
            new TmiField("town","s"),
            new TmiField("userstopareacode","s"),
            new TmiField("stopsidecode","s"),
            new TmiField("roadsideeqdataownercode","s"),
            new TmiField("roadsideequnitnumber","l")
        });
        
        tmiData.put("usrstar", new TmiField[] {
            new TmiField("dataowner","s"),
            new TmiField("userstopareacode","s"),
            new TmiField("name","s"),
            new TmiField("town","s"),
            new TmiField("roadsideeqdataownercode","s"),
            new TmiField("roadsideequnitnumber","l"),
            new TmiField("description","s")
        });
    }
    
    private String dataOwner;
    private PrintWriter out;
    private String jndiName;
    private String schema;
    private int batch;
    private boolean transaction;
    
    private String filename, titel, description;
    private java.sql.Date validFrom, validUntil;
    
    private Connection connection;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
    
    public TmiDbImport(PrintWriter out, String jndiName, String schema, int batch, boolean transaction) {
        this.out = out;
        this.jndiName = jndiName;
        this.schema = schema;
        this.batch = batch;
        this.transaction = transaction;
    }
    
    private Connection getConnection() throws NamingException, SQLException {
        if(connection == null) {
            out.println("Openen database connectie...");
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource)initCtx.lookup(jndiName);            
            connection = ds.getConnection();
            
            connection.setAutoCommit(!transaction);
            
            if("automatisch".equals(schema)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                schema = dataOwner + "_" + sdf.format(new Date());
                schema = schema.toLowerCase();
            }
            out.printf("Geopend, aanmaken schema \"%s\"...\n", schema);
            new QueryRunner().update(connection, "drop schema if exists " +  schema + " cascade");
            new QueryRunner().update(connection, "create schema " +  schema);
            new QueryRunner().update(connection, "set search_path = " + schema + ",public");
            new QueryRunner().update(connection, "set standard_conforming_strings to on");
        }
        return connection;
    }
    
    private String getSqlType(String t) {
        if("l".equals(t)) {
            return "bigint";
        } else if("d".equals(t)) {
            return "date";
        } else if("b".equals(t)) {
            return "boolean";
        } else {
            return "varchar";
        }
    }
    
    private Object convert(String value, String t) throws ParseException {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        value = value.trim();
        if("l".equals(t)) {
            return Long.parseLong(value);
        } else if("d".equals(t)) {
            if(value.indexOf("-") == -1) {
                return new java.sql.Date(sdf2.parse(value).getTime());
            } else {
                return new java.sql.Date(sdf.parse(value).getTime());
            }
        } else if("b".equals(t)) {
            return "true".equalsIgnoreCase(value);
        } else {
            return value;
        }
    }
    
    private void createTable(String name, TmiField[] fields) throws Exception {
        
        String sql = "create table " + name + "(\n";
        
        for(TmiField f: fields) {
            if(f != null) {
                sql += "\t" + f.name + " " + getSqlType(f.type) + ",\n";
            }
        }
        sql = StringUtils.removeEnd(sql, ",\n") + "\n);\n";
        out.println(sql);
        out.flush();
        new QueryRunner().update(getConnection(), sql);
    }

    private void update(String sql, Object... params) throws Exception {
        long startTime = System.currentTimeMillis();
        out.println(sql);
        new QueryRunner().update(getConnection(), sql, (Object[])params);
        out.printf("Tijd: %.1f s\n", (System.currentTimeMillis() - startTime) / 1000.0);
    }
    
    private void selectPrint(String sql, Object... params) throws Exception {
        long startTime = System.currentTimeMillis();
        out.println(sql);
        out.flush();
        Map<String,Object> result = new QueryRunner().query(getConnection(), sql, new MapHandler(), (Object[])params);
        out.println("Resultaat: " + result);
        out.printf("Tijd: %.1f s\n", (System.currentTimeMillis() - startTime) / 1000.0);
    }
    
    private void process(Set<String> tables) throws Exception {
        if(!tables.containsAll(Arrays.asList(new String[] {
            "point", "pool", "jopatili", "jopa"
        }))) {
            out.println("Niet alle benodigde tabellen zijn aanwezig voor processing!");
            return;
        }
        
        Long zeroDistance = new QueryRunner().query(getConnection(), "select count(*) from pool where distancesincestartoflink <> 0", new ScalarHandler<Long>());
        if(zeroDistance == null || 0 == zeroDistance) {
            throw new Exception("Ongeldige TMI gegevens; geen \"Point On Link\" gegevens of geen \"Distance since start of link\" in \"Point On Link\"");
        }
        
        update("create index pool_begin_idx on pool (userstopcodebegin)");
        update("create index pool_end_idx on pool (userstopcodeend)");
        update("create index point_code_idx on point (code)");
        
        selectPrint(String.format("select addgeometrycolumn('%s', 'point', 'point_geom', 28992, 'POINT', 2)", schema));
        update("update point set point_geom = st_setsrid(st_makepoint(x, y), 28992)");
        
        selectPrint(String.format("select addgeometrycolumn('%s','jopa','the_geom',28992,'MULTILINESTRING',2)", schema));

        // Enkele LINESTRING op basis van alle coordinaten van pool van jopa samengevoegd
        update("alter table jopatili add column points text");
        update("update jopatili jt " +
            "set points = ( " +
            "    select substring(st_astext(st_makeline(point_geom)) from 'LINESTRING\\((.*)\\)') " +
            "    from ( " +
            "        select point_geom " +
            "        from pool " +
            "        join point on (point.code = pool.pointcode) " +
            "        where pool.userstopcodebegin = jt.userstopcodebegin " +
            "        and pool.userstopcodeend = jt.userstopcodeend " +
            "        order by distancesincestartoflink " +
            "    ) as points " +
            ") " +
            "where exists ( " +
            "    select 1  " +
            "    from pool p " +
            "    where jt.userstopcodebegin = p.userstopcodebegin " +
            "    and jt.userstopcodeend = p.userstopcodeend " +
            ")");
        update("update jopa " +
            "set the_geom = ( " +
            "	select st_geomfromtext('MULTILINESTRING((' || array_to_string(array_agg(points),',') || '))', 28992) " +
            "	from ( " +
            "		select points " +
            "		from jopatili jt " +
            "		where jt.lineplanningnumber = jopa.lineplanningnumber  " +
            "		and jt.journeypatterncode = jopa.journeypatterncode " +
            "		order by jt.timinglinkorder " +
            "	) pts " +
            ") " +
            "where exists ( " +
            "	select 1 " +
            "	from jopatili jt " +
            "	where jt.lineplanningnumber = jopa.lineplanningnumber  " +
            "	and jt.journeypatterncode = jopa.journeypatterncode  " +
            "	and jt.points is not null " +
            ")");
/*        
        // MULTILINESTRINGs, kan geen pool van enkel punt aan
        selectPrint(String.format("select addgeometrycolumn('%s', 'jopatili', 'the_geom', 28992, 'LINESTRING', 2)", schema));
        update("update jopatili jt " +
                "set the_geom = ( " +
                "    select st_makeline(points.point_geom) " +
                "    from ( " +
                "        select point_geom " +
                "        from pool " +
                "        join point on (point.code = pool.pointcode) " +
                "        where pool.userstopcodebegin = jt.userstopcodebegin " +
                "        and pool.userstopcodeend = jt.userstopcodeend " +
                "        order by distancesincestartoflink " +
                "    ) as points " +
                ") " +
                "where ( " +
                "    select count(*)  " +
                "    from pool p " +
                "    where jt.userstopcodebegin = p.userstopcodebegin " +
                "    and jt.userstopcodeend = p.userstopcodeend " +
                ") > 1"
        );
        update("update jopa " +
                "set the_geom = ( " +
                "    select st_collect(the_geom) from ( " +
                "        select jt.the_geom " +
                "        from jopatili jt " +
                "        where jt.lineplanningnumber = jopa.lineplanningnumber " +
                "        and jt.journeypatterncode = jopa.journeypatterncode " +
                "        order by timinglinkorder " +
                "    ) as line_pieces " +
                ") " +
                "where exists ( " +
                "    select 1 " +
                "    from jopatili jt " +
                "    where jt.lineplanningnumber = jopa.lineplanningnumber " +
                "    and jt.journeypatterncode = jopa.journeypatterncode " +
                "    and the_geom is not null " +
                ")"
        );
*/         
        // Index voor mapfile
        update("create index line_lineplanningnummber_idx on line(lineplanningnumber)");
        update("create index jopa_geom_idx on jopa using gist(the_geom)");

        // View voor mapfile met bushaltes
        update("create view bushaltes as select usrstop.*,point.point_geom from usrstop join point on (point.code=usrstop.userstopcode)");
        
        update("create table geo_ov_metainfo(data_owner_code varchar, title varchar, " +
            "original_filename varchar, valid_from date, valid_until date, " +
            "description text);");
        update("insert into geo_ov_metainfo(data_owner_code,title,original_filename,valid_from,valid_until,description) values (?,?,?,?,?,?)",
                dataOwner, titel, filename, validFrom, validUntil, description);
                
    }
    
    private void readTmi(InputStream input, String table, TmiField[] fields) throws Exception {
        String[] header = new String[fields.length+3];

        for(int i = 0; i < fields.length; i++) {
            header[i+3] = fields[i] == null ? "null" : fields[i].name;
        }
        Iterable<CSVRecord> parser = CSVFormat.newBuilder()
                .withCommentStart(';')
                .withDelimiter('|')
                .withHeader(header)
                .parse(new InputStreamReader(input, "UTF-8"));
        
        boolean haveTable = false;
        String insert = "insert into " + table + " (";
        String params = "";
        for(TmiField f: fields) {
            if(f != null) {
                insert += f.name + ", ";
                params += "?, ";
            }
        }
        insert = StringUtils.removeEnd(insert, ", ") + ") values (" + StringUtils.removeEnd(params, ", ") + ");\n";
        QueryRunner qr = new QueryRunner();
        int count = 0;
        
        List<Object[]> batchValues = new ArrayList();
        
        long startTime = System.currentTimeMillis();
        
        for(CSVRecord record: parser) {
            if(record.get(0).toLowerCase().indexOf("recordtype") != -1) {
                continue;
            }
            dataOwner = record.get(3);
            if(!haveTable) {
                createTable(table, fields);
                out.println(insert);
                out.println("Inserten records...");
                out.flush();
                haveTable = true;
            }
            List values = new ArrayList();
            for(TmiField f: fields) {
                if(f != null) {
                    values.add(convert(record.get(f.name), f.type));
                }
            }
            if(batch == 1) {
                qr.update(getConnection(), insert, values.toArray());
            } else {
                batchValues.add(values.toArray());
                
                if(batchValues.size() == batch) {
                    qr.batch(getConnection(), insert, (Object[][])batchValues.toArray(new Object[][]{}));
                    batchValues = new ArrayList();
                }
            }
            count++;
            if(count % 25000 == 0) {
                out.print(" " + count + "..." + (count % 250000 == 0 ? "\n" : ""));
                out.flush();
            }
        }
        if(!batchValues.isEmpty()) {
            qr.batch(getConnection(), insert, (Object[][])batchValues.toArray(new Object[][]{}));
        }
        long duration = System.currentTimeMillis() - startTime;
        out.format("\nAantal records: %d, snelheid: %d per seconde\n", count, duration > 0 ? Math.round(count/(duration/1000.0)) : 0);
    }            

    public void loadFromZip(InputStream input, String filename, String encoding, String titel, Date validFrom, Date validUntil, String description) {
        this.filename = filename;
        this.titel = titel;
        this.validFrom = validFrom == null ? null : new java.sql.Date(validFrom.getTime());
        this.validUntil = validUntil == null ? null: new java.sql.Date(validUntil.getTime());
        this.description = description;

        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(input);

            ZipEntry entry;
            Set<String> tables = new HashSet();
            while((entry = zip.getNextEntry()) != null) {
                String path = entry.getName();
                int i = path.lastIndexOf('\\');
                if(i == -1) {
                    i = path.lastIndexOf('/');
                }
                if(i != -1) {
                    path = path.substring(i+1);
                }

                if(!(path.toLowerCase().endsWith(".tmi") || path.toLowerCase().endsWith(".csv"))|| path.startsWith("._")) {
                    continue;
                }

                TmiField[] fields = null;
                String table = StringUtils.remove(StringUtils.removeEnd(StringUtils.removeEnd(path.toLowerCase(),".tmi"), ".csv"),'x');
                fields = tmiData.get(table);
                if(fields == null) {
                    out.format("Overslaan \"%s\"\n", path);
                    continue;
                }

                out.format("Inlezen \"%s\"...\n", path);
                readTmi(zip, table, fields);
                tables.add(table);
                zip.closeEntry();
            }
            process(tables);
            if(transaction) {
                connection.commit();
            }
            out.println("Klaar");
        } catch(Exception e) {
            log.error("Fout bij TMI import", e);
            out.format("Exception %s: %s\n", e.getClass().getName(), e.getMessage());
            e.printStackTrace(out);
        } finally {
            try {
                if(zip != null) {
                    zip.close();
                }
            } catch(Exception e) {
            }
            
            try {
                if(connection != null) {
                    if(transaction) {
                        connection.rollback();
                    }
                    connection.close();
                }
            } catch(Exception e) {
            }
        } 
    }
}
