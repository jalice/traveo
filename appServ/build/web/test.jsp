<%-- 
    Document   : test
    Created on : 27.09.2011, 9:26:34
    Author     : user
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="resource.Vars"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>speed test</title>
    </head>
    <body>
        <h1>search results - EN</h1>
		<table>
		<%
			ArrayList res = (ArrayList)Vars.lGet("search_res_en");
			LinkedHashMap row;

			if (res!=null)
			for (int i=0; i<res.size(); i++) {
				out.print("<tr>");
				
				row = (LinkedHashMap)res.get(i);
				for (Object col : row.keySet()) {
					out.print("<td>"+((String)row.get(col)) +"</td>");
				}

				out.print("</tr>");
			}
		%>
		</table>

		<h1>search results - RU</h1>
		<table>
		<%
			if (res!=null)
			res = (ArrayList)Vars.lGet("search_res_ru");
			for (int i=0; i<res.size(); i++) {
				out.print("<tr>");

				row = (LinkedHashMap)res.get(i);
				for (Object col : row.keySet()) {
					out.print("<td>"+((String)row.get(col)) +"</td>");
				}

				out.print("</tr>");
			}
		%>
		</table>
    </body>
</html>
