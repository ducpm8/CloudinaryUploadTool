<!doctype html>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@include file="pre.jsp"%>
<script type='text/javascript'>
 function unhide() {
	document.getElementById("loading").setAttribute("style", "display: inline;width: 20%;");
}
</script>
<!-- A standard form for uploading images to your server -->
<div id='backend_upload'>
    <h1>Download Process History</h1>
    
		<div class="form_line">
		    <div class="form_controls">
		        <c:forEach items="${fileList}" var="fileSub">
					<a href="<c:url value="/download/1"/>/<c:out value="${fileSub}"/>"><c:out value="${fileSub}"/></a><br>
				</c:forEach>
		    </div>
		</div>
</div>

<a href="<c:url value="/"/>" class="back_link">Back to home</a>
<%@include file="post.jsp"%>

