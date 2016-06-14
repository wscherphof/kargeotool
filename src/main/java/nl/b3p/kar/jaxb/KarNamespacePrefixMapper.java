package nl.b3p.kar.jaxb;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 *
 * @author Matthijs Laan
 */
public class KarNamespacePrefixMapper extends NamespacePrefixMapper {
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if(Namespace.NS_BISON_TMI8_KV9_MSG.equals(namespaceUri)) {
            return "tmi8";
        } else if(Namespace.NS_BISON_TMI8_KV9_CORE.equals(namespaceUri)) {
            return "tmi8c";
        } else if(Namespace.NS_B3P_GEO_OV_KV9.equals(namespaceUri)) {
            return "b3p";
        } else {
            return suggestion;
        }
    }
}    
