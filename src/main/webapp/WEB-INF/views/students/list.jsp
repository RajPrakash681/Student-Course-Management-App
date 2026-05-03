<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="All Students" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>All Students</h2>
    <p>Manage your student records — add, view, or edit students.</p>
</div>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success">✅ ${successMessage}</div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">❌ ${errorMessage}</div>
</c:if>

<div class="action-row">
    <span style="color:var(--text-muted); font-size:.85rem;">
        Total: <strong style="color:var(--text-main);">${students.size()} students</strong>
    </span>
    <div style="display:flex; gap:10px;">
        <a href="/students/enrollments" class="btn btn-secondary">🔗 View Enrollments</a>
        <a href="/students/new" class="btn btn-primary" id="btn-add-student">➕ Add Student</a>
    </div>
</div>

<div class="card">
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Department</th>
                    <th>Enroll Year</th>
                    <th>Courses</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty students}">
                        <tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:32px;">
                            No students found.
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="s" items="${students}" varStatus="loop">
                            <tr>
                                <td>${loop.index + 1}</td>
                                <td><strong>${s.name}</strong></td>
                                <td style="color:var(--accent3);">${s.email}</td>
                                <td><span class="badge badge-purple">${s.department}</span></td>
                                <td>${s.enrollmentYear}</td>
                                <td><span class="badge badge-blue">${s.courses.size()} enrolled</span></td>
                                <td>
                                    <a href="/students/edit/${s.id}" class="btn btn-edit btn-sm" id="edit-student-${s.id}">✏️ Edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
