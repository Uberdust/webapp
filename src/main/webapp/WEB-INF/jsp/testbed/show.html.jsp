<%@ page import="java.util.TimeZone" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@page session="false" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:useBean id="testbed" scope="request" class="eu.wisebed.wisedb.model.Testbed"/>
<jsp:useBean id="nodes" scope="request" class="java.util.ArrayList"/>
<jsp:useBean id="links" scope="request" class="java.util.ArrayList"/>
<jsp:useBean id="capabilities" scope="request" class="java.util.ArrayList"/>
<jsp:useBean id="slses" scope="request" class="java.util.ArrayList"/>

<html>
<head>
    <META NAME="Description" CONTENT="ÜberDust"/>
    <META http-equiv="Content-Language" content="en"/>
    <META http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>ÜberDust - Show testbed : <c:out value="${testbed.name}"/></title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/css/styles.css"/>"/>
</head>
<body>
<%@include file="/header.jsp" %>
<p>
    /<a href="<c:url value="/rest/testbed"/>">testbeds</a>/
    <a href="<c:url value="/rest/testbed/${testbed.id}"/>">testbed</a>/
</p>

<table>
    <tbody>
    <tr>
        <td>Testbed ID</td>
        <td><c:out value="${testbed.id}"/></td>
    </tr>
    <tr>
        <td>Testbed Description</td>
        <td><c:out value="${testbed.description}"/></td>
    </tr>
    <tr>
        <td>Testbed Name</td>
        <td><c:out value="${testbed.name}"/></td>
    </tr>
    <tr>
        <td>Testbed URN prefix</td>
        <td><c:out value="${testbed.urnPrefix}"/></td>
    </tr>
    <tr>
        <td>Testbed Timezone</td>
        <c:set var="testbedZone" value="<%= testbed.getTimeZone().getDisplayName() %>"/>
        <td><c:out value="${testbedZone}"/></td>
    </tr>
    <tr>
        <td>Testbed URL</td>
        <td><a href="<c:out value="${testbed.url}"/>"><c:out value="${testbed.url}"/></a></td>
    </tr>
    <tr>
        <td>Testbed SNAA URL</td>
        <td><a href="<c:out value="${testbed.snaaUrl}"/>"><c:out value="${testbed.snaaUrl}"/></a></td>
    </tr>
    <tr>
        <td>Testbed RS URL</td>
        <td><a href="<c:out value="${testbed.rsUrl}"/>"><c:out value="${testbed.rsUrl}"/></a></td>
    </tr>
    <tr>
        <td>Testbed Session Management URL</td>
        <td><a href="<c:out value="${testbed.sessionUrl}"/>"><c:out value="${testbed.sessionUrl}"/></a></td>
    </tr>
    <tr>
        <td>Federated Testbed</td>
        <td><c:out value="${testbed.federated}"/></td>
    </tr>
    <tr>
        <td>Testbed Status Page</td>
        <td>
            <a href="<c:url value="/rest/testbed/${testbed.id}/status"/>">Status
                page</a></td>
    </tr>
    <tr>
        <td>Testbed GeoRSS feed</td>
        <td>
            <a href="<c:url value="/rest/testbed/${testbed.id}/georss"/>">GeoRSS feed</a>
            (<a href="http://maps.google.com/maps?q=<c:url value="${baseURL}/rest/testbed/${testbed.id}/georss"/>">View On Google
            Maps</a>)
        </td>
    </tr>
    <tr>
        <td>Testbed KML feed</td>
        <td>
            <a href="<c:url value="/rest/testbed/${testbed.id}/kml"/>">KML feed</a>
            (<a href="http://maps.google.com/maps?q=<c:url value="${baseURL}/rest/testbed/${testbed.id}/kml"/>">View On Google
            Maps</a>)
            <span style="color : red">not implemented yet</span>
        </td>

    </tr>
    <tr>
        <td>Testbed WiseML</td>
        <td>
            <a href="<c:url value="/rest/testbed/${testbed.id}/wiseml"/>">WiseML</a>
            <span style="color : red">not implemented yet</span>
        </td>
    </tr>
    </tbody>
</table>

<table style="margin-top: 50px">
    <tr>
        <td style="vertical-align:top">
            <p>
                <a href="<c:url value="/rest/testbed/${testbed.id}/node"/>">Nodes</a>
                (<a href="<c:url value="/rest/testbed/${testbed.id}/node/raw"/>">raw</a>,
                <a href="<c:url value="/rest/testbed/${testbed.id}/node/json"/>">json</a>)
            </p>
            <c:choose>
                <c:when test="${nodes == null || fn:length(nodes) == 0}">
                    <p style="color : red">No nodes found for testbed <c:out value="${testbed.name}"/></p>
                </c:when>
                <c:otherwise>
                    <table>
                        <tbody>
                        <c:forEach items="${nodes}" var="node">
                            <tr>
                                <td>
                                    <a href="<c:url value="/rest/testbed/${testbed.id}/node/${node}"/>"><c:out value="${node}"/></a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
        <td style="vertical-align:top">
            <p>
                <a href="<c:url value="/rest/testbed/${testbed.id}/link"/>">Links</a>
                (<a href="<c:url value="/rest/testbed/${testbed.id}/link/raw"/>">raw</a>)
            </p>
            <c:choose>
                <c:when test="${links == null || fn:length(links) == 0 }">
                    <p style="color : red">No links found for <c:out value="${testbed.name}"/></p>
                </c:when>
                <c:otherwise>
                    <table>
                        <c:forEach items="${links}" var="link">
                            <tr>
                                <td>
                                    <a href="<c:url value="/rest/testbed/${testbed.id}/link/${link.source}/${link.target}"/>"><c:out value="${link.source},${link.target}"/></a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
        <td style="vertical-align:top">
            <p>
                <a href="<c:url value="/rest/testbed/${testbed.id}/capability"/>">Capabilities</a>
                (<a href="<c:url value="/rest/testbed/${testbed.id}/capability/raw"/>">raw</a>)
            </p>
            <c:choose>
                <c:when test="${capabilities == null || fn:length(capabilities) == 0 }">
                    <p style="color : red">No capabilities found for <c:out value="${testbed.name}"/></p>
                </c:when>
                <c:otherwise>
                    <table>
                        <c:forEach items="${capabilities}" var="capability">
                            <tr>
                                <td>
                                    <a href="<c:url value="/rest/testbed/${testbed.id}/capability/${capability}"/>"><c:out value="${capability}"/></a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
        <td style="vertical-align:top">
            <p>
                <a href="<c:url value="/rest/testbed/${testbed.id}/slse"/>">Slses</a>
                (<a href="<c:url value="/rest/testbed/${testbed.id}/slse/raw"/>">raw</a>
                <a href="<c:url value="/rest/testbed/${testbed.id}/createslse"/>">create</a>)
            </p>
            <c:choose>
                <c:when test="${slses == null || fn:length(slses) == 0 }">
                    <p style="color : red">No slses found for <c:out value="${testbed.name}"/></p>
                </c:when>
                <c:otherwise>
                    <table>
                        <c:forEach items="${slses}" var="slse">
                            <tr>
                                <td>
                                    <a href="<c:url value="/rest/testbed/${testbed.id}/slse/${slse}/"/>"><c:out value="${slse}"/></a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
        <td style="vertical-align:top">
            <p>
                <a href="<c:url value="/rest/testbed/${testbed.id}/inse"/>">Inses</a>
                (<a href="<c:url value="/rest/testbed/${testbed.id}/inse/raw"/>">raw</a>)
            </p>
            <c:choose>
                <c:when test="${inses == null || fn:length(inses) == 0 }">
                    <p style="color : red">No inses found for <c:out value="${testbed.name}"/></p>
                </c:when>
                <c:otherwise>
                    <table>
                        <c:forEach items="${inses}" var="inse">
                            <tr>
                                <td>
                                    <a href="<c:url value="/rest/testbed/${testbed.id}/inses/${inse}/"/>"><c:out value="${inse}"/></a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
<%@include file="/footer.jsp" %>
</body>
</html>
