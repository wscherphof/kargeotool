/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2014 B3Partners B.V.
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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
@StrictBinding
@UrlBinding("/action/overview")
public class OverviewActionBean implements ActionBean{
    private static final Log log = LogFactory.getLog(DetermineAllVRITypeActionBean.class);


    private static final String OVERVIEW_CARRIER = "/WEB-INF/jsp/overview/carrier.jsp";
    private static final String OVERVIEW_DATAOWNER = "/WEB-INF/jsp/overview/dataowner.jsp";

    private ActionBeanContext context;


    @DefaultHandler
    public Resolution overview(){

    }

    public Resolution readMessage(){
          return new ForwardResolution(OVERVIEW_CARRIER);
    }

    public Resolution carrier(){
        return new ForwardResolution(OVERVIEW_CARRIER);
    }

    // <editor-fold desc="Getters and setters" defaultstate="collapsed">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    // </editor-fold>

}
