<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div id="leftspacecontent">
    <h1>Contactpersonen bij gemeenten en regio's (<b>${fn:length(gebruikers)}</b>)</h1>
    <p>
    <c:if test="${!empty gebruikers}">
        <table border="1" cellpadding="3" style="border-collapse: collapse" class="table-autosort:0 table-stripeclass:alternate">
            <thead>
                <tr>
                    <th class="table-sortable:default">Gebruikersnaam</th>
                    <th class="table-sortable:default">Naam</th>
                    <th class="table-sortable:default">E-mail</th>
                    <th class="table-sortable:numeric">Telefoonnummer</th>
                    <th colspan="2">Organisatie</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="g" varStatus="status" items="${gebruikers}">
                    <c:set var="col" value=""/>
                    <tr bgcolor="${col}">
                        <td><c:out value="${g.username}"/></td>
                        <td><c:out value="${g.fullName}"/></td>
                        <td><c:out value="${g.email}"/></td>
                        <td><c:out value="${g.phone}"/></td>
                        <c:if test="${!empty g.gemeente}">
                            <td>gemeente</td><td><c:out value="${g.gemeente.naam}"/></td>
                        </c:if>
                        <c:if test="${!empty g.regio}">
                            <td>regio</td><td><c:out value="${g.regio.naam}"/></td>
                        </c:if>
                        <c:if test="${g.beheerder}">
                            <td colspan="2">beheerder</td>
                        </c:if>
                        <c:if test="${!g.beheerder && !empty g.provincie}">
                            <td>provincie</td><td><c:out value="${g.provincie.naam}"/></td>
                        </c:if>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>
</div>