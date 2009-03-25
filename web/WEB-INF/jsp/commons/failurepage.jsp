<%@include file="/WEB-INF/taglibs.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Failure Page</title>
    </head>
    <body>

        <h1>Er is een fout opgetreden</h1>
        <%-- foutmeldingen --%>
        <tiles:insert definition="infoblock"/>

    </body>
</html>
