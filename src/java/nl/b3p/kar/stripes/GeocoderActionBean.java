package nl.b3p.kar.stripes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
@UrlBinding("/action/geocoder")
public class GeocoderActionBean implements ActionBean {
    
    private static final String GEOCODER_URL = "http://geodata.nationaalgeoregister.nl/geocoder/Geocoder?zoekterm=";
    
    private ActionBeanContext context;

    @Validate
    private String search;
    
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
    
    public Resolution geocode() throws Exception {
        InputStream in = new URL(GEOCODER_URL + URLEncoder.encode(search, "UTF-8")).openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        in.close();
        bos.close();
        return new StreamingResolution("text/xml", new ByteArrayInputStream(bos.toByteArray()));
    }
}
