<%-- 
    Document   : index
    Created on : 11.08.2011, 12:27:30
    Author     : PK
--%>

<%@page import="resource.Vars"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
		<%= (String)Vars.lGet("asd") %>
    </body>
</html>
