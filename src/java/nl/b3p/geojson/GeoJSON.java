package nl.b3p.geojson;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Matthijs Laan
 */
public class GeoJSON {
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
}
