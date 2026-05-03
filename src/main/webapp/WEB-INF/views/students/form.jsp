<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>${pageTitle}</h2>
    <p>Fill in the details below to ${empty student.id ? 'add a new' : 'update the'} student.</p>
</div>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">❌ ${errorMessage}</div>
</c:if>

<div class="card" style="max-width:680px;">
    <form:form action="${formAction}" method="post" modelAttribute="student" id="student-form">
        <form:hidden path="id"/>

        <div class="form-group">
            <label for="name">Full Name *</label>
            <form:input path="name" id="name" placeholder="e.g. Alice Johnson" cssClass="form-control"/>
            <form:errors path="name" cssClass="field-error"/>
        </div>

        <div class="form-group">
            <label for="email">Email Address *</label>
            <form:input path="email" id="email" type="email" placeholder="e.g. alice@bits.edu" cssClass="form-control"/>
            <form:errors path="email" cssClass="field-error"/>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:20px;">
            <div class="form-group">
                <label for="department">Department *</label>
                <form:select path="department" id="department" cssClass="form-control">
                    <form:option value="" label="— Select Department —"/>
                    <form:option value="Computer Science" label="Computer Science"/>
                    <form:option value="Mathematics" label="Mathematics"/>
                    <form:option value="Physics" label="Physics"/>
                    <form:option value="Chemistry" label="Chemistry"/>
                    <form:option value="Engineering" label="Engineering"/>
                    <form:option value="Biology" label="Biology"/>
                    <form:option value="Economics" label="Economics"/>
                    <form:option value="Management" label="Management"/>
                </form:select>
                <form:errors path="department" cssClass="field-error"/>
            </div>

            <div class="form-group">
                <label for="enrollmentYear">Enrollment Year *</label>
                <form:input path="enrollmentYear" id="enrollmentYear" type="number"
                            placeholder="e.g. 2022" min="2000" max="2030" cssClass="form-control"/>
                <form:errors path="enrollmentYear" cssClass="field-error"/>
            </div>
        </div>

        <div class="form-group">
            <label for="courses">Enroll in Courses (hold Ctrl/Cmd to select multiple)</label>
            <form:select path="courses" id="courses" multiple="true" cssClass="form-control"
                         items="${allCourses}" itemValue="id" itemLabel="title"/>
        </div>

        <div style="display:flex; gap:12px; margin-top:8px;">
            <button type="submit" class="btn btn-primary" id="btn-submit-student">
                ${empty student.id ? '➕ Add Student' : '💾 Update Student'}
            </button>
            <a href="/students" class="btn btn-secondary">✖ Cancel</a>
        </div>
    </form:form>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
