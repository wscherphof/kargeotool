/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.kar.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author meine
 */
public class SldServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(SldServlet.class);
    public static final String OGCNS = "http://www.opengis.net/ogc";
    public static final String SLDNS = "http://www.opengis.net/sld";
    public static final String SENS = "http://www.opengis.net/se";
    private String defaultSldPath = "WEB-INF/DefaultSld.xml";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OutputStream out = response.getOutputStream();
        try {

            // Haal de variabelen uit de request

            // signaalgroepen
            // Haal waarden op
            String agVisibleValues[] = new String[0];
            if (request.getParameter("agVisibleValues") != null) {
                // splits waarden in array
                agVisibleValues = request.getParameter("agVisibleValues").split(",");
            }

            String aVisibleValues[] = new String[0];
            if (request.getParameter("aVisibleValues") != null) {
                // splits waarden in array
                aVisibleValues = request.getParameter("aVisibleValues").split(",");
            }

            String rseqVisibleValues[] = new String[0];
            if (request.getParameter("rseqVisibleValues") != null) {
                // splits waarden in array
                rseqVisibleValues = request.getParameter("rseqVisibleValues").split(",");
            }
            // Haal het bronbestand op
            Document doc = getDefaultSld();
            Node root = doc.getDocumentElement();

            // Voeg eigen info toe
            createTypeNode(doc, agVisibleValues, "signaalgroepen");
            createTypeNode(doc, rseqVisibleValues, "walapparatuur");
            createTypeNode(doc, aVisibleValues, "triggerpunten");

            // Schrijf de sld naar de outputstream
            //StringWriter sw= new StringWriter();
            DOMSource domSource = new DOMSource(doc);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            //t.transform(domSource,new StreamResult(out));
            response.setContentType("text/xml");
            t.transform(domSource, new StreamResult(out));
        } catch (Exception e) {
            log.error("Fout bij maken sld: ", e);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter pw = new PrintWriter(out);
            pw.write(e.getMessage());
        } finally {
            out.close();
        }
    }

    private void createTypeNode(Document root, String[] visibleValues, String type) {
        if (visibleValues.length != 0) {

            List<Node> rules = getRulelistByName(type, root);
            for (Iterator<Node> it = rules.iterator(); it.hasNext();) {
                Node rule = it.next();
                Node filter = getFilterFromRule(rule);

                // And filter toevoegen
                Node and = root.createElement("ogc:And");

                // vorige <ogc:PropertyIsEqualTo> uit filter halen en aan And filter toevoegen
                NodeList nl = filter.getChildNodes();
                Node typeCheck = null;
                for (int i = 0; i < nl.getLength(); i++) {
                    typeCheck = nl.item(i);
                    String nn = typeCheck.getNodeName();
                    if (nn.equals("ogc:PropertyIsEqualTo")) {
                        break;
                    }
                }

                and.appendChild(typeCheck);
                filter.appendChild(and);

                // checken of er een Or filter moet komen
                Node temp = null;
                if (visibleValues.length > 1) {
                    temp = root.createElement("ogc:Or");
                    and.appendChild(temp);
                } else {
                    temp = and;
                }


                // Voeg alle waarden toe aan het filter
                for (int i = 0; i < visibleValues.length; i++) {
                    Node propertyEqual = root.createElement("ogc:PropertyIsEqualTo");

                    Node propertyName = root.createElement("ogc:PropertyName");
                    propertyName.setTextContent("id");
                    Node literal = root.createElement("ogc:Literal");
                    literal.setTextContent(visibleValues[i]);

                    propertyEqual.appendChild(propertyName);
                    propertyEqual.appendChild(literal);

                    temp.appendChild(propertyEqual);
                }
            }
        }
    }

    private List<Node> getRulelistByName(String name, Document doc) {
        NodeList list = doc.getElementsByTagName("NamedLayer");

        Node layer = null;
        for (int i = 0; i < list.getLength(); i++) {
            layer = list.item(i);
            Node nameNode = layer.getFirstChild().getNextSibling();
            String text = nameNode.getTextContent();
            if (text.equals(name)) {
                break;
            }
        }

        NodeList nl = layer.getChildNodes();

        Node userStyle = null;
        for (int i = 0; i < nl.getLength(); i++) {
            userStyle = nl.item(i);
            String nn = userStyle.getNodeName();
            if (nn.equals("UserStyle")) {
                break;
            }
        }

        nl = userStyle.getChildNodes();
        Node featureTypeStyle = null;
        for (int i = 0; i < nl.getLength(); i++) {
            featureTypeStyle = nl.item(i);
            String nn = featureTypeStyle.getNodeName();
            if (nn.equals("FeatureTypeStyle")) {
                break;
            }
        }

        nl = featureTypeStyle.getChildNodes();
        List<Node> rules = new ArrayList<Node>();
        Node rule = null;
        for (int i = 0; i < nl.getLength(); i++) {
            rule = nl.item(i);
            String nn = rule.getNodeName();
            if (nn.equals("Rule")) {
                rules.add(rule);
            }
        }

        return rules;/*
        nl = rule.getChildNodes();
        Node filter = null;
        for (int i = 0; i < nl.getLength(); i++) {
        filter = nl.item(i);
        String nn = filter.getNodeName();
        int a = 0;
        if (nn.equals("ogc:Filter")) {
        break;
        }
        }


        return filter;*/
    }

    private Node getFilterFromRule(Node rule) {
        NodeList nl = rule.getChildNodes();
        Node filter = null;
        for (int i = 0; i < nl.getLength(); i++) {
            filter = nl.item(i);
            String nn = filter.getNodeName();
            int a = 0;
            if (nn.equals("ogc:Filter")) {
                break;
            }
        }
        return filter;
    }

    private Document getDefaultSld() throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        //URL u = new URL("http://geo.zuid-holland.nl/geo-loket/config/sld_status_concept.xml");
        FileInputStream fi = new FileInputStream(getServletContext().getRealPath(defaultSldPath));
        Document doc = db.parse(fi);
        return doc;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
