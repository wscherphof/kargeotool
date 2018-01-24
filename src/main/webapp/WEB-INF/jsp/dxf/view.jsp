<%--
 Geo-OV - applicatie voor het registreren van KAR meldpunten               
                                                                           
 Copyright (C) 2009-2013 B3Partners B.V.                                   
                                                                           
 This program is free software: you can redistribute it and/or modify      
 it under the terms of the GNU Affero General Public License as            
 published by the Free Software Foundation, either version 3 of the        
 License, or (at your option) any later version.                           
                                                                           
 This program is distributed in the hope that it will be useful,           
 but WITHOUT ANY WARRANTY; without even the implied warranty of            
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 GNU Affero General Public License for more details.                       
                                                                           
 You should have received a copy of the GNU Affero General Public License  
 along with this program. If not, see <http://www.gnu.org/licenses/>.      
--%>

<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<stripes:layout-render name="/WEB-INF/jsp/commons/siteTemplate.jsp">

    <stripes:layout-component name="headerlinks" >
        <%@include file="/WEB-INF/jsp/commons/headerlinks.jsp" %>

    </stripes:layout-component>
    <stripes:layout-component name="content">

       
        <h1>Upload DXF</h1>
        <stripes:messages/>
        <stripes:errors/>
        
        <stripes:form beanclass="nl.b3p.kar.stripes.UploadDXFActionBean">
            <stripes:hidden name="rseq"/>
            <table>
                <tr><td>Bestand</td><td><stripes:file name="bestand"/></td></tr>
                <tr><td>Omschrijving</td><td><stripes:text name="description"/></td></tr>
                <tr><td>VRI omschrijving</td><td><stripes:text name="rseq.description"/></td></tr>

                <tr><td>Wegbeheerder</td><td><stripes:select name="dataowner">
                    <stripes:option label="- Kies wegbeheerder -"/>
                    <stripes:options-collection collection="${actionBean.dataowners}" label="omschrijving" value="id"/>
                </stripes:select></td></tr>

                <tr><td><stripes:submit name="upload" value="Upload" /></td></tr>
            </table>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>