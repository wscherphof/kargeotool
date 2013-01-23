package nl.b3p.geojson;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Matthijs Laan
 */
public class GeoJSON {
    public static final int SRID_RIJKSDRIEHOEKSTELSEL = 28992;
    
    public static JSONObject toGeoJSON(Geometry g) throws JSONException {
        if(!(g instanceof Point)) {
            throw new UnsupportedOperationException();
        }
        JSONObject jg = new JSONObject();
        jg.put("type","Point");
        JSONArray coordinates = new JSONArray();
        coordinates.put(g.getCoordinate().x);
        coordinates.put(g.getCoordinate().y);
        jg.put("coordinates", coordinates);
        return jg;
    }
    
    public static Point toPoint(JSONObject j) throws JSONException {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID_RIJKSDRIEHOEKSTELSEL);
        JSONArray coords = j.getJSONArray("coordinates");
        return gf.createPoint(new Coordinate(coords.getDouble(0), coords.getDouble(1)));
    }
}
