<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>
<script type="text/javascript" src="<html:rewrite module="" page="/js/swfobject.js"/>"></script>
<div id="flamingo" class="flamingoContainer">
    <font color="red"><strong>Flashplayer met Flamingo-viewer wordt geladen... Indien u geen viewer ziet, controleer dan of de Flash-plugin is geinstalleerd.</strong></font>
</div>
<div style="display: none;">
    <iframe name="submitter"></iframe>
</div>
<script type="text/javascript">    
    var so = new SWFObject("<html:rewrite module="" page="/flamingo/flamingo.swf"/>?config=/config.xml", "flamingoo", "100%", "100%", "8", "#FFFFFF");
    so.write("flamingo");
    var flamingo = document.getElementById("flamingoo");    
</script>
<script type="text/javascript" src="<html:rewrite module="" page="/js/viewer.js"/>"></script>
<form action="activation.do" method="post" id="activationForm" target="_blank">
    <input type="hidden" id="wktgeomfield" name="newWktgeom" value=""/>
    <input type="hidden" id="idfield" name="id" value=""/>
</form>
<script type="text/javascript">
    var bbox;
    <c:if test="${not empty bbox}">
        bbox='${bbox}';
    </c:if>
    var layers = '${layers}';
    var gebied = '${gebied}';
    var isGemeente=${f:isUserInRole(pageContext.request, 'gemeente')};
    var wmsUrl="${wmsUrl}";    
</script>
