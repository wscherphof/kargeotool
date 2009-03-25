<%@include file="/WEB-INF/taglibs.jsp" %>

<tiles:importAttribute/>

<html:messages id="error" message="true">
    <div class="messages" style="padding-top: 5px">&#8594; <c:out value="${error}" escapeXml="false"/>&#160;&#160;</div>
</html:messages>
<p>
