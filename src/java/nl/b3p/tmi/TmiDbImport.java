package nl.b3p.tmi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbutils.QueryRunner;
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
            new TmiField("displaypublicline","s"),
            new TmiField("productformulatype","l")
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
            new TmiField("description","s"),
            new TmiField("transporttype","s")
        });        
    }
    
    private String dataOwner;
    private PrintWriter out;
    private String jndiName;
    private String schema;
    private int batch;
    
    private Connection connection;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    public TmiDbImport(PrintWriter out, String jndiName, String schema, int batch) {
        this.out = out;
        this.jndiName = jndiName;
        this.schema = schema;
        this.batch = batch;
    }
    
    private Connection getConnection() throws NamingException, SQLException {
        if(connection == null) {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource)initCtx.lookup(jndiName);            
            connection = ds.getConnection();
            
            connection.setAutoCommit(false);
            
            if("automatisch".equals(schema)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                schema = dataOwner + "_" + sdf.format(new Date());
            }
            new QueryRunner().update(connection, "drop schema if exists " +  schema + " cascade");
            new QueryRunner().update(connection, "create schema " +  schema);
            new QueryRunner().update(connection, "set search_path = " + schema);
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
        if("l".equals(t)) {
            return Long.parseLong(value);
        } else if("d".equals(t)) {
            return new java.sql.Date(sdf.parse(value).getTime());
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
            if(count % 100 == 0) {
                out.print(" " + count + "..." + (count % 1500 == 0 ? "\n" : ""));
                out.flush();
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        out.format("\nAantal records: %d, snelheid: %d per seconde\n", count, duration > 0 ? Math.round(count/(duration/1000.0)) : 0);
    }            

    public void loadFromZip(InputStream input, String encoding) {

        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(input);

            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                String path = entry.getName();
                int i = path.lastIndexOf('\\');
                if(i == -1) {
                    i = path.lastIndexOf('/');
                }
                if(i != -1) {
                    path = path.substring(i+1);
                }

                if(!path.toLowerCase().endsWith(".tmi") || path.startsWith("._")) {
                    continue;
                }

                TmiField[] fields = null;
                String table = StringUtils.remove(StringUtils.removeEnd(path.toLowerCase(),".tmi"),'x');
                fields = tmiData.get(table);
                if(fields == null) {
                    out.format("Overslaan \"%s\"\n", path);
                    continue;
                }

                out.format("Inlezen \"%s\"...\n", path);

                readTmi(zip, table, fields);
                zip.closeEntry();
            }
            connection.commit();
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
                    connection.rollback();
                    connection.close();
                }
            } catch(Exception e) {
            }
        } 
    }
}
