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
    <h1>New Photo</h1>
    <c:if test="${!empty message}">
    	 	<h2>${message}</h2>
   	 </c:if>
   	 <c:if test="${empty message}">
    	 	<img id="loading" style="display:none" src="<c:url value="/img/Loading_icon.gif"/>">
   	 </c:if>
    <form:form method="post" action="upload" commandName="photoUpload" enctype="multipart/form-data">
		<div class="form_line">
		    <label for="folderPath">Folder Path:</label>
		    <div class="form_controls">
		        <input type="text" name="folderPath" id="folderPath"/>
		    </div>
		    <br>
		    <label for="folderPath">Store Name:</label>
		    <div class="form_controls">
		        <input type="text" name="fileName" id="fileName" value="result"/>
		    </div>
		</div>
        
        <div class="form_line">
            <div class="form_controls">
                <input type="submit" value="Submit Photo"/>
            </div>
        </div>
        <form:hidden path="preloadedFile"/>
        <form:errors path="signature" extraClasses="error" />
    </form:form>

</div>

<a href="<c:url value="/"/>" class="back_link">Back to home</a>
<a href="<c:url value="/traceUpload"/>" class="back_link">Go to Fail list</a>
<%@include file="post.jsp"%>

