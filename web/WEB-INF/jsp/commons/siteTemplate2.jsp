<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<!DOCTYPE HTML>

<stripes:layout-definition>
    <html>
    <head>
        <title><fmt:message key="index.title"/></title>
        <script type="text/javascript" src="${contextPath}/js/edit.js"></script>
        <script type="text/javascript" src="${contextPath}/js/json2.js"></script>
        <script type="text/javascript" src="${contextPath}/js/utils.js"></script>
        <script type="text/javascript" src="${contextPath}/js/simple_treeview.js"></script>
        <script src="${contextPath}/js/jquery-1.3.2.min.js"></script>
        <script src="${contextPath}/js/jquery-ui-1.7.2.custom.min.js"></script>
        <script type="text/javascript" src="${contextPath}/openlayers/OpenLayers.js"></script>
        <script type="text/javascript" src="${contextPath}/js/ext/ext-all-debug.js"></script>
        <script type="text/javascript" src="${contextPath}/js/models.js" ></script>
        <script type="text/javascript" src="${contextPath}/js/OpenLayersController.js" ></script>
        <script type="text/javascript" src="${contextPath}/js/contextmenu.js" ></script>
        <link rel="stylesheet" href="${contextPath}/js/ext/ext-all.css" type="text/css" media="screen" />
        <link rel="stylesheet" href="${contextPath}/styles/geo-ov.css" type="text/css" media="screen" />
        
    </head>
    <body class="editor" id="editorBody">
        <div id="viewportcontainer">
            <stripes:layout-component name="headerlinks"/>
            <div id="contentcontainer">
                <stripes:layout-component name="content"/>
            </div>
        </div>
    </body>
</html>
</stripes:layout-definition>
