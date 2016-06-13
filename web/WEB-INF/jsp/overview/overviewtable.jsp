
<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<stripes:layout-component name="messagesTable">
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