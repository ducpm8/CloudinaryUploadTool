<!doctype html>
<%@include file="pre.jsp"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="posterframe">
<!-- This will render the fetched Facebook profile picture using Cloudinary according to the
requested transformations. This also shows how to chain transformations -->

</div>

<h1>Welcome!</h1>
<br>
<h1>Drop Shipping Assistant -v2</h1>
<br>
<div class="actions">
	<a class="upload_link" href="download_form">Download photo</a>
    <a class="upload_link" href="upload_form">Upload photo</a>
    
    <!-- 
    <a class="upload_link" href="direct_upload_form">Add photo (direct upload)</a>
    <a class="upload_link" href="direct_unsigned_upload_form">Add photo (direct unsigned upload)</a>
     -->
</div>

<cl:jsinclude/>
<script type='text/javascript'>
    $('.toggle_info').click(function () {
        $(this).closest('.photo').toggleClass('show_more_info');
        return false;
    });
    $.cloudinary.responsive();
</script>
<%@include file="post.jsp"%>
