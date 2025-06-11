<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vježba 8 - zadaća 3 - Početna stranica</title>
    </head>
    <body>
        <h1>Vježba 8 - zadaća 3 - Početna stranica</h1>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/kraj">Šalji komandu za kraj</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Status poslužitelja Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/1">Start poslužitelja Tvrtka - registracija</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/1">Pauza poslužitelja Tvrtka - registracija</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/2">Start poslužitelja Tvrtka - za partnere</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/2">Pauza poslužitelja Tvrtka - za partnere</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna konzola Tvrtka</a>
            </li>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled partnera</a>
            </li>
        </ul>          
    </body>
</html>
