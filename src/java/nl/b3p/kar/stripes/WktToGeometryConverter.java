package nl.b3p.kar.stripes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.util.Collection;
import java.util.Locale;
import net.sourceforge.stripes.validation.TypeConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
public class WktToGeometryConverter implements TypeConverter {

    private static final Log log = LogFactory.getLog(WktToGeometryConverter.class);

    public WktToGeometryConverter(){

    }

    public Object convert(String wkt, Class point, Collection error) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geom = (Geometry) reader.read(wkt);
            geom.setSRID(28992);
            return geom;
        } catch (ParseException e) {
            log.error("Converting string to geometry failed. WKT tried to convert: " + wkt + ". Message: " + e.getMessage());
            error.add(e);
        }
        return null;
    }

    public void setLocale(Locale locale) {
        
    }

}
