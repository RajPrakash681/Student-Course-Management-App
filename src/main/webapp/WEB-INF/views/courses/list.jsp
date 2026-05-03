<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="All Courses" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>All Courses</h2>
    <p>Browse and manage all available courses in the system.</p>
</div>

<c:if test="${not empty successMessage}">
    <div class="alert alert-success">✅ ${successMessage}</div>
</c:if>
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">❌ ${errorMessage}</div>
</c:if>

<div class="action-row">
    <span style="color:var(--text-muted); font-size:.85rem;">
        Total: <strong style="color:var(--text-main);">${courses.size()} courses</strong>
    </span>
    <div style="display:flex; gap:10px;">
        <a href="/courses/enrolled" class="btn btn-secondary">🔗 Enrolled Courses</a>
        <a href="/courses/new" class="btn btn-primary" id="btn-add-course">➕ Add Course</a>
    </div>
</div>

<div class="card">
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Course Title</th>
                    <th>Credits</th>
                    <th>Instructor</th>
                    <th>Duration</th>
                    <th>Students</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty courses}">
                        <tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:32px;">
                            No courses found.
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="c" items="${courses}" varStatus="loop">
                            <tr>
                                <td>${loop.index + 1}</td>
                                <td><strong>${c.title}</strong></td>
                                <td><span class="badge badge-blue">${c.credits} cr</span></td>
                                <td>${c.instructor}</td>
                                <td>${c.duration}</td>
                                <td><span class="badge badge-purple">${c.students.size()} students</span></td>
                                <td>
                                    <a href="/courses/edit/${c.id}" class="btn btn-edit btn-sm" id="edit-course-${c.id}">✏️ Edit</a>
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
