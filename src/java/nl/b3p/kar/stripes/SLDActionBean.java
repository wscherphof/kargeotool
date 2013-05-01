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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/sld")
public class SLDActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(SLDActionBean.class);
    private static final String ENCODING = "UTF-8";
    private ActionBeanContext context;
    @Validate
    private String publicnumber;
    public static final String OGCNS = "http://www.opengis.net/ogc";
    public static final String SLDNS = "http://www.opengis.net/sld";
    public static final String SENS = "http://www.opengis.net/se";
    public static final String STROKEOPACITY = "stroke-opacity";
    public static final String STROKEWIDTH = "stroke-width";
    public static final String STROKECOLOR = "stroke";
    public static final String TEXTCOLOR = "fill";
    public static final String HALOCOLOR = "halofill";
    public static final String FILLCOLOR = "fill-color";
    public static final String FILLOPACITY = "fill-opacity";
    public static final String FONTFAMILY = "font-family";
    public static final String FONTSIZE = "font-size";

    public Resolution generate() {

        return new StreamingResolution("text/xml; charset=" + ENCODING) {
            @Override
            public void stream(HttpServletResponse response) throws Exception {

                OutputStream out = response.getOutputStream();
                try {
                    Document doc = getDefaultSld();

                    Node root = doc.getDocumentElement();

                    String laag = "buslijnen";
                    createLayer(doc, root, laag);
                    DOMSource domSource = new DOMSource(root);
                    Transformer optimusPrime = TransformerFactory.newInstance().newTransformer();

                    optimusPrime.transform(domSource, new StreamResult(out));
                } catch (Exception e) {
                    log.error("Fout bij maken sld: ", e);
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter pw = new PrintWriter(out);
                    pw.write(e.getMessage());
                } finally {
                    out.close();
                }
            }
        };
    }

    private void createLayer(Document doc, Node root, String laag) throws Exception {
        Node namedLayer = doc.createElementNS(SLDNS, "NamedLayer");
        Node name = doc.createElementNS(SLDNS, "Name");
        name.appendChild(doc.createTextNode(laag));
        namedLayer.appendChild(name);

        Node userStyle = createUserstyle(doc, namedLayer);

        Node rule = doc.createElementNS(SLDNS, "Rule");
        Node filter = doc.createElementNS(OGCNS, "Filter");

        Node propertyIsEqualTo = doc.createElementNS(OGCNS, "PropertyIsEqualTo");
        Node propertyName = doc.createElementNS(OGCNS, "PropertyName");
        propertyName.appendChild(doc.createTextNode("publicnumber"));
        propertyIsEqualTo.appendChild(propertyName);
        Node literal = doc.createElementNS(OGCNS, "Literal");
        literal.appendChild(doc.createTextNode(publicnumber));
        propertyIsEqualTo.appendChild(literal);

        filter.appendChild(propertyIsEqualTo);
        rule.appendChild(filter);

        userStyle.appendChild(rule);

        Node polygonSymbolizer = createStyleLine(doc);
        rule.appendChild(polygonSymbolizer);

        root.appendChild(namedLayer);
    }

    private Node createUserstyle(Document doc, Node namedLayer) {
        Node userStyle = doc.createElementNS(SLDNS, "UserStyle");
        Node styleName = doc.createElementNS(SLDNS, "Name");
        Node featureTypeStyle = doc.createElementNS(SLDNS, "FeatureTypeStyle");

        styleName.appendChild(doc.createTextNode("nieuweStyle"));
        userStyle.appendChild(styleName);
        userStyle.appendChild(featureTypeStyle);
        namedLayer.appendChild(userStyle);
        return featureTypeStyle;
    }

    private Node createStyleLine(Document doc) throws Exception {
        // String geoProperty = "geom";
        /*<LineSymbolizer>
         <Stroke>
         <CssParameter name="stroke">#000000</CssParameter>
         <CssParameter name="stroke-width">3</CssParameter>
         </Stroke>
         </LineSymbolizer>*/
        Map<String, String> styles = getStyleMap();

        Node lineSymbolizer = doc.createElementNS(SLDNS, "LineSymbolizer");
        //  Node geo = doc.createElementNS(SLDNS, "Geometry");
        //    Node propName = doc.createElementNS(OGCNS, "PropertyName");
        //   propName.appendChild(doc.createTextNode(geoProperty));
        // geo.appendChild(propName);


        Node stroke = doc.createElementNS(SLDNS, "Stroke");

        Element cssParamStrokeColor = doc.createElementNS(SLDNS, "CssParameter");
        cssParamStrokeColor.setAttribute("name", "stroke");
        cssParamStrokeColor.appendChild(doc.createTextNode(styles.get(STROKECOLOR)));
        stroke.appendChild(cssParamStrokeColor);

        Element cssParamStrokeOpacity = doc.createElementNS(SLDNS, "CssParameter");
        cssParamStrokeOpacity.setAttribute("name", STROKEOPACITY);
        cssParamStrokeOpacity.appendChild(doc.createTextNode(styles.get(STROKEOPACITY)));
        stroke.appendChild(cssParamStrokeOpacity);

        Element cssParamStrokeWidth = doc.createElementNS(SLDNS, "CssParameter");
        cssParamStrokeWidth.setAttribute("name", STROKEWIDTH);
        cssParamStrokeWidth.appendChild(doc.createTextNode(styles.get(STROKEWIDTH)));
        stroke.appendChild(cssParamStrokeWidth);

        lineSymbolizer.appendChild(stroke);

        return lineSymbolizer;
    }

    private Map<String, String> getStyleMap() throws Exception {
        Map<String, String> styles = new HashMap<String, String>();

        styles.put(FILLCOLOR, "#FF0000");
        styles.put(FILLOPACITY, "0.6");
        styles.put(STROKEOPACITY, "1.0");
        styles.put(STROKEWIDTH, "2.0");
        styles.put(STROKECOLOR, "#FF0000");
        styles.put(TEXTCOLOR, "#000000");
        styles.put(HALOCOLOR, "#FFFFFF");
        styles.put(FONTFAMILY, "arial");
        styles.put(FONTSIZE, "10");

        return styles;
    }

    private Document getDefaultSld() {
        Document doc = null;

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            ServletContext sc = getContext().getServletContext();
            String p = sc.getRealPath("/WEB-INF/DefaultSld.xml");
            File f = new File(p);

            doc = db.parse(f);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return doc;
    }

    // <editor-fold desc="Getters and setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
 
    public String getPublicnumber() {
        return publicnumber;
    }

    public void setPublicnumber(String publicnumber) {
        this.publicnumber = publicnumber;
    }
    
    
    // </editor-fold>


}
