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
<body data-locale="<c:out value="${fn:substring(pageContext.response.locale, 0, 2)}" />">
<div id="page-wrapper">
    <div class="" id="form-login">

        <h1 class="brand"><i></i><span><fmt:message key='page.login.sing'/></span></h1>

        <form class="form-horizontal login" method="POST" action="<c:url value="/admin/j_security_check"/>">
            <fieldset>
                <div class="control-group js-control-wrap js-username-control">
                    <input id="j_username" type="text" name="j_username" placeholder="<fmt:message key='page.login.username'/>" class="input-large"
                           autofocus required>
                    <span class="help-inline js-username-error js-field-error"></span>
                </div>

                <div class="control-group js-control-wrap js-password-control">
                    <input id="j_password" type="password" name="j_password" placeholder="<fmt:message key='page.login.password'/>"
                           class="input-large" required>
                    <span class="help-inline js-password-error js-field-error"></span>
                </div>

                <div class="text-error" data-field="error">
                    <c:out value="${errorMessage}"/>
                </div>

                <div class="control-group">
                    <input type="submit" data-login="true" class="btn-login" value="<fmt:message key='page.login.action'/>"/>
                </div>
            </fieldset>
        </form>

    </div>
</div>
</body>
</html>
