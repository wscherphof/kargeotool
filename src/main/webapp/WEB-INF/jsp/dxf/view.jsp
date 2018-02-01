<%--
 KAR Geo Tool - applicatie voor het registreren van KAR meldpunten               
                                                                           
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

       
        <h1>Overzicht DXF-bestanden</h1>
        <stripes:messages/>
        <stripes:errors/>
            <div id="gebruikerScroller">
        <table class="tableheader" style="width: 100%;">
            <tr>
                <th>Bestandsnaam</th><th>VRI</th><th>Gemaakt door</th><th>Wegbeheerder</th><th>Gemaakt op</th><th>Verwijder</th>
            </tr>
            <c:forEach items="${actionBean.uploads}" var="upload">
                <tr>
                    <td>${upload.filename}</td>
                    <td>${upload.rseq.karAddress}: ${upload.rseq.description} (${upload.rseq.town})</td>
                    <td>${upload.user_.fullname} (${upload.user_.username})</td>
                    <td>${upload.dataOwner.omschrijving}</td>
                    <td>${upload.uploaddate}</td>
                    <td><stripes:link beanclass="nl.b3p.kar.stripes.UploadDXFActionBean" event="remove"> <stripes:param name="uploadFile" value="${upload.id}" />Verwijder</stripes:link></td>
                    </tr>
            </c:forEach>
        </table>
    </div>
        
    </stripes:layout-component>
</stripes:layout-render>