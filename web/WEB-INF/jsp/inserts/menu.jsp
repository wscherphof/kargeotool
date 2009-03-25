
<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<html:link module="" page="/index.do">Home</html:link>

<html:link module="/viewer" page="/viewer.do">Invoer- en visiemodule</html:link>

<html:link module="" page="/export.do">Servicemodule</html:link>

<c:choose>
    <c:when test="${pageContext.request.remoteUser == null}">
        <html:link module="/beheer" page="/gebruikers.do">Beheermodule</html:link>
    </c:when>
    <c:when test="${f:isUserInRole(pageContext.request, 'beheerder')}">
        <html:link module="/beheer" page="/gebruikers.do">Beheermodule</html:link>
        <html:link module="/beheer" page="/verwijderdeplannen.do">Verwijderde Plannen</html:link>
    </c:when>
</c:choose>

<html:link module="/viewer" page="/edituser.do">Gebruikersprofiel</html:link>

<html:link module="" page="/help.do">Help</html:link>

<html:link module="" page="/logout.do">Uitloggen</html:link>


