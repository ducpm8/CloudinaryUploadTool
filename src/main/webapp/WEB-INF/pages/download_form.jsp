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
    <h1>Download Product Photo from AliExpress</h1>
    <c:if test="${!empty message}">
    	 	<h2>${message}</h2>
   	 </c:if>
   	 <c:if test="${empty message}">
    	 	<img id="loading" style="display:none" src="<c:url value="/img/Loading_icon.gif"/>">
   	 </c:if>
    <form:form method="post" action="downloadImage" commandName="photoUpload" enctype="multipart/form-data">
        <c:if test="${empty photoUpload.publicId}">
            <div class="form_line">
                <label for="file">List file:</label>
                <div class="form_controls">
                    <input type="file" name="file" id="file"/>
                </div>
            </div>
        </c:if>
        <div class="form_line">
            <div class="form_controls">
                <input type="submit" value="Submit File" onclick="unhide()"/>
            </div>
        </div>
        <form:hidden path="preloadedFile"/>
        <form:errors path="signature" extraClasses="error" />
    </form:form>

</div>

<a href="<c:url value="/"/>" class="back_link">Back to home</a>
<a href="<c:url value="/traceDownload"/>" class="back_link">Go to Fail list</a>
<%@include file="post.jsp"%>

