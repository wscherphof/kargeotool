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
package nl.b3p.geojson;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Hulpklasse voor geometry conversie van en naar JSON objecten
 *
 * @author Matthijs Laan
 * @author Meine Toonen
 */
public class GeoJSON {

    /**
     * SRID van het Rijksdriehoekstelsel
     */
    public static final int SRID_RIJKSDRIEHOEKSTELSEL = 28992;

    /**
     * Methode om een geometry object om te zetten naar coordinaten binnen een
     * JSON object.
     *
     * @param g het Geometry object
     * @return het JSON object met de coordinaten
     * @throws JSONException Wordt gegooid als er geen geojsonobject gemaakt kan worden.
     */
    public static JSONObject toGeoJSON(Geometry g) throws JSONException {
        if (!(g instanceof Point || g instanceof LineString)) {
            throw new UnsupportedOperationException();
        }
        JSONObject jg = new JSONObject();
        JSONArray coordinates = new JSONArray();
        if (g instanceof Point) {
            jg.put("type", "Point");
            coordinates.put(g.getCoordinate().x);
            coordinates.put(g.getCoordinate().y);
        } else if (g instanceof LineString) {
            jg.put("type", "LineString");
            Coordinate[] coords = g.getCoordinates();
            for (int i = 0; i < coords.length; i++) {
                JSONArray coordPair = new JSONArray();
                Coordinate coordinate = coords[i];
                coordPair.put(coordinate.x);
                coordPair.put(coordinate.y);
                coordinates.put(coordPair);
            }
        }
        jg.put("coordinates", coordinates);
        return jg;
    }

    /**
     * Methode om coordinaten binnen JSON object om te zetten naar een Geometry
     * object.
     *
     * @param j het JSON object
     * @return Geometry object
     * @throws JSONException Wordt gegooid als er geen geojsonobject gelezen kan worden.
     */
    public static Point toPoint(JSONObject j) throws JSONException {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID_RIJKSDRIEHOEKSTELSEL);
        JSONArray coords = j.getJSONArray("coordinates");
        return gf.createPoint(new Coordinate(coords.getDouble(0), coords.getDouble(1)));
    }
}
