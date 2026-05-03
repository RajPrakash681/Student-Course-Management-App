<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>${pageTitle}</h2>
    <p>Fill in the details below to ${empty course.id ? 'add a new' : 'update the'} course.</p>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">❌ ${errorMessage}</div>
</c:if>

<div class="card" style="max-width:620px;">
    <form:form action="${formAction}" method="post" modelAttribute="course" id="course-form">
        <form:hidden path="id"/>

        <div class="form-group">
            <label for="title">Course Title *</label>
            <form:input path="title" id="title" placeholder="e.g. Data Structures & Algorithms" cssClass="form-control"/>
            <form:errors path="title" cssClass="field-error"/>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:20px;">
            <div class="form-group">
                <label for="credits">Credits *</label>
                <form:input path="credits" id="credits" type="number" min="1" max="6"
                            placeholder="1–6" cssClass="form-control"/>
                <form:errors path="credits" cssClass="field-error"/>
            </div>

            <div class="form-group">
                <label for="duration">Duration *</label>
                <form:input path="duration" id="duration" placeholder="e.g. 16 weeks" cssClass="form-control"/>
                <form:errors path="duration" cssClass="field-error"/>
            </div>
        </div>

        <div class="form-group">
            <label for="instructor">Instructor Name *</label>
            <form:input path="instructor" id="instructor" placeholder="e.g. Dr. Alan Turing" cssClass="form-control"/>
            <form:errors path="instructor" cssClass="field-error"/>
        </div>

        <div style="display:flex; gap:12px; margin-top:8px;">
            <button type="submit" class="btn btn-primary" id="btn-submit-course">
                ${empty course.id ? '➕ Add Course' : '💾 Update Course'}
            </button>
            <a href="/courses" class="btn btn-secondary">✖ Cancel</a>
        </div>
    </form:form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
