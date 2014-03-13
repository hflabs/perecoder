<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="tiles"  uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri ="http://www.springframework.org/tags" %>

<c:set var="errorCode" value="<%= response.getStatus() %>" />

<!DOCTYPE html>
<head>
    <meta charset="UTF-8">
    <title>
        <spring:message code="page.${errorCode}.title" text="${errorCode}"/>
    </title>

    <tiles:insertDefinition name="_styles" flush="false"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/error.css"/>" />
</head>
<body>
<section class="error">
    <article class="error-${errorCode}">
        <h1><spring:message code="page.${errorCode}.title" text="${errorCode}"/></h1>
        <p><spring:message code="page.${errorCode}.message" text="Seems like something unexpected happen. The error has been sent to support team"/></p>
        <p>
            <a href="<c:url value="/admin/" />" class="btn btn-large">
                <i class="icon-home"></i> <spring:message code="page.error.home"/>
            </a>
        </p>
    </article>
</section>
</body>
