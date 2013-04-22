package nl.b3p.kar.hibernate;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Matthijs Laan
 */
public class TMIDateConverter extends XmlAdapter<String, Date> {

    private static final String TMI_DATE_FORMAT = "yyyy-MM-dd";
    
    private SimpleDateFormat buildDateFormat() {
        return new SimpleDateFormat(TMI_DATE_FORMAT);
    }
    
    @Override
    public Date unmarshal(String date) throws Exception {
        return buildDateFormat().parse(date);
    }

    @Override
    public String marshal(Date date) throws Exception {
        return buildDateFormat().format(date);
    }
    
}
