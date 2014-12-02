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

        <h1>Overzicht vervoerder</h1>
        <div id="gebruikerScroller">
            <table class="tableheader" style="width: 100%;">
                <tr>
                    <th>Kruispunt</th><th>Vervoerder</th><th>Gemaakt op</th><th>Verzonden</th><th>Verwerkt</th><th>Bekijk</th>
                </tr>
                <c:forEach items="${actionBean.messages}" var="message">
                    <tr>
                        <td>${message.rseq.karAddress}: ${message.rseq.description} (${message.rseq.town})</td>
                        <td>${message.vervoerder.username} (${message.vervoerder.fullname})</td>
                        <td><fmt:formatDate value="${message.createdAt}" pattern="HH:mm dd-MM-yyyy"/></td>
                        <td><c:choose><c:when test="${message.mailSent}"><img src="${contextPath}/images/silk/accept.png"/> (<fmt:formatDate value="${message.sentAt}" pattern="HH:mm dd-MM-yyyy"/>)</c:when><c:otherwise><img src="${contextPath}/images/silk/cancel.png"/></c:otherwise></c:choose></td>
                        <td><c:choose><c:when test="${message.mailProcessed}"><img src="${contextPath}/images/silk/accept.png"/> (<fmt:formatDate value="${message.processedAt}" pattern="HH:mm dd-MM-yyyy"/>)</c:when><c:otherwise><img src="${contextPath}/images/silk/cancel.png"/></c:otherwise></c:choose></td>
                        <td><stripes:link beanclass="nl.b3p.kar.stripes.EditorActionBean" event="view" anchor="rseq=${message.rseq.id}&x=${message.rseq.location.x}&y=${message.rseq.location.y}&zoom=12"> Bekijk op kaart</stripes:link></td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </stripes:layout-component>
</stripes:layout-render>