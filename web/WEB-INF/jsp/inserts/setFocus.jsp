<%@include file="/WEB-INF/taglibs.jsp" %>

<c:if test="${!empty focus}">
<script type="text/JavaScript">
<!--
    function initFocus() {
        try {
            document.forms[0]['${focus}'].focus();
            <c:if test="${select}">
                document.forms[0]['${focus}'].select();
            </c:if>
        } catch(e) {
        }
    }
    
    setOnload(initFocus);
// -->   
</script>
</c:if>