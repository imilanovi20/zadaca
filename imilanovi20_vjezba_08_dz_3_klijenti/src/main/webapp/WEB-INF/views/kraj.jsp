<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vježba 8 - zadaća 3 - Kraj rada poslužitelja Tvrtka</title>
    </head>
    <body>
        <h1>Vježba 8 - zadaća 3 - Kraj rada poslužitelja Tvrtka</h1>
	<%
	String status = (String) request.getAttribute("status");
	%>        
		<p>Status poslužitelja: <%= status %> </p>
    </body>
</html>
