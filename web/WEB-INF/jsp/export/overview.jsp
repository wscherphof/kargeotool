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


        <script type="text/javascript">

            var exportActionBeanUrl = "<stripes:url beanclass="nl.b3p.kar.stripes.ExportActionBean" />";
            var profile = {
            };

            <c:if test="${!empty actionBean.gebruiker.profile}">
            profile = ${actionBean.gebruiker.profile};
            </c:if>
        </script>

        <script type="text/javascript" src="${contextPath}/js/profilestate.js"></script>
        <script type="text/javascript" src="${contextPath}/js/settings.js"></script>
        <script type="text/javascript" src="${contextPath}/js/export.js"></script>
        <h1>Exporteer verkeerssystemen</h1>
        <div id="body" class="exportBody">
            <stripes:form beanclass="nl.b3p.kar.stripes.ExportActionBean"  >
                <stripes:messages/>
                <stripes:errors/>
                <p>
                    Deelgebied
                    <stripes:select name="filter" onchange="deelgebiedChanged();" id="deelgebied" >
                        <stripes:option>Selecteer een deelgebied</stripes:option>
                        <stripes:options-collection collection="${actionBean.deelgebieden}" label="name" value="id" />
                    </stripes:select>
                    <stripes:submit name="maakDeelgebied">Nieuw</stripes:submit>
                    <stripes:submit name="bewerkDeelgebied">Bewerk</stripes:submit>
                    <stripes:submit name="removeDeelgebied">Verwijder</stripes:submit>
                </p><br/>
                <p>
                    <div id="rseqGrid"></div> <br/>
                </p>
                <p>
                    Exporttype
                    <stripes:select name="exportType">
                        <stripes:option value="">Selecteer een type</stripes:option>
                        <stripes:option value="incaa" >INCAA .ptx</stripes:option>
                        <stripes:option value="kv9" >KV9 XML</stripes:option>
                    </stripes:select>
                    <stripes:hidden name="rseqs" id="rseqs"/>
                    <stripes:submit name="export" disabled="true" id="exportSubmit">Exporteer</stripes:submit>
                </p>
            </stripes:form>

        </div>


    </stripes:layout-component>
</stripes:layout-render>