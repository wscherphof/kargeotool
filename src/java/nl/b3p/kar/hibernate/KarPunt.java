package nl.b3p.kar.hibernate;

import com.vividsolutions.jts.geom.Point;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class KarPunt {
    private Integer id;
    private Point geom;
    private String type;

    /**
     * Waarde voor type indien punt een <b>inmeldpunt</b> is.
     */
    public static final String TYPE_ACTIVATION = "ACTIVATION";
    /**
     * Waarde voor type indien punt een <b>signaalgroep</b> is.
     */
    public static final String TYPE_ACTIVATION_GROUP = "ACTIVATIONGROUP";
    /**
     * Waarde voor type indien punt <b>walapparatuur</b> is.
     */
    public static final String TYPE_ROADSIDE_EQUIPMENT = "RoadSideEQuipment";
    /**
     * XXX Geen idee waarvoor dit type moet worden gebruikt
     */
    public static final String TYPE_POINT_ON_LINK = "Point On Link";
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
        nf.setGroupingUsed(false);
        return nf.format(geom.getCoordinate().x) + ", " + nf.format(geom.getCoordinate().y);
    }
}
