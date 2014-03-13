<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"   uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec"   uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=9"/>

    <title>
        <fmt:message key='page.common.title'>
            <fmt:param>
                <fmt:message key='page.dashboard.title'/>
            </fmt:param>
        </fmt:message>
    </title>

    <link rel="icon" type="image/png" href="<c:url value="/resources/images/favicon.png"/>">
    <link rel="stylesheet" href="<c:url value="/resources/fonts/fonts.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/webjars/bootstrap/2.3.1/css/bootstrap.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/lib/bootstrap-fileupload.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/lib/bootstrap-switch/bootstrap-switch.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/lib/bootstrap-select.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/font-awesome/css/font-awesome.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/common.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/layout.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/navigation.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/aside.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/modal.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/table.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/common/locale.css"/>" />
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/groups.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/dictionaries.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/records.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/recodes.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/page/tasks.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/app/theme/hfl.css"/>" />
    <!--[if lt IE 10]><link rel="stylesheet" href="<c:url value="/resources/css/app/common/ie.css"/>" /><![endif]-->
</head>
<body class="loading" id="page-wrapper" data-username="<sec:authentication property="name"/>">

    <header id="header-wrapper"></header>

    <aside id="right-wrapper"></aside>

    <section id="content-wrapper" class="panel-wrapper clearfix width-wrapper"></section>

    <footer id="footer-wrapper"></footer>

    <script data-main="<c:url value="/resources/js/app/main"/>" src="<c:url value="/resources/webjars/requirejs/2.1.5/require.js"/>"></script>

</body>
</html>
