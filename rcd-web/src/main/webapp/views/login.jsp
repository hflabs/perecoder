<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"   uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"    uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>
        <fmt:message key='page.common.title'>
            <fmt:param>
                <fmt:message key='page.login.title'/>
            </fmt:param>
        </fmt:message>
    </title>

    <tiles:insertDefinition name="_styles" flush="false"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/login.css"/>"/>
</head>
<body
        data-locale="<c:out value="${fn:substring(pageContext.response.locale, 0, 2)}" />"
        data-action="<c:url value="/admin/j_security_check"/>"
        data-error-message="<c:out value="${errorMessage}"/>"  >
    <div id="page-wrapper"></div>
    <script data-main="<c:url value="/resources/js/app/login-main"/>" src="<c:url value="/resources/webjars/requirejs/2.1.5/require.js"/>"></script>
</body>
</html>
