<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Student Enrollments" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>Student Enrollments — Inner Join</h2>
    <p>Shows every student–course pair via a JPQL INNER JOIN between Student and Course tables.</p>
</div>

<div class="action-row">
    <span style="color:var(--text-muted); font-size:.85rem;">
        Total records: <strong style="color:var(--text-main);">${enrollments.size()}</strong>
    </span>
    <a href="/students" class="btn btn-secondary">← Back to Students</a>
</div>

<div class="card">
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Student Name</th>
                    <th>Email</th>
                    <th>Department</th>
                    <th>Enroll Year</th>
                    <th>Course Title</th>
                    <th>Instructor</th>
                    <th>Credits</th>
                    <th>Duration</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty enrollments}">
                        <tr><td colspan="9" style="text-align:center; color:var(--text-muted); padding:32px;">
                            No enrollment data found. Students need to be assigned to courses.
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="e" items="${enrollments}" varStatus="loop">
                            <tr>
                                <td>${loop.index + 1}</td>
                                <td><strong>${e.studentName}</strong></td>
                                <td style="color:var(--accent3);">${e.studentEmail}</td>
                                <td><span class="badge badge-purple">${e.department}</span></td>
                                <td>${e.enrollmentYear}</td>
                                <td><strong>${e.courseTitle}</strong></td>
                                <td>${e.instructor}</td>
                                <td><span class="badge badge-blue">${e.credits} cr</span></td>
                                <td>${e.duration}</td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
