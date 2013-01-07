<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<!DOCTYPE HTML>

<stripes:layout-definition>
    <head>
        <title><fmt:message key="index.title"/></title>
        <script type="text/javascript" src="${contextPath}/js/json2.js"></script>
        <script type="text/javascript" src="${contextPath}/js/utils.js"></script>
        <script type="text/javascript" src="${contextPath}/js/simple_treeview.js"></script>
        <link rel="stylesheet" href="${contextPath}/styles/geo-ov.css" type="text/css" media="screen" />
        <!--[if IE 7]> <link href="${contextPath}/styles/geo-ov-ie7.css" rel="stylesheet" media="screen" type="text/css" /> <![endif]-->
    </head>
    <body>
        <stripes:layout-component name="headerlinks"/>
        <div id="contentcontainer">
            <stripes:layout-component name="content"/>
        </div>
    </body>
</html:html>
</stripes:layout-definition>
