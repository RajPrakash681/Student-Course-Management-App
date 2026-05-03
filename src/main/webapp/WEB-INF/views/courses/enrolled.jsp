<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Enrolled Courses" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-header">
    <h2>Courses With Enrolled Students — Inner Join</h2>
    <p>Only courses that have at least one enrolled student are displayed (INNER JOIN result).</p>
</div>

<div class="action-row">
    <span style="color:var(--text-muted); font-size:.85rem;">
        Total: <strong style="color:var(--text-main);">${courses.size()} courses with students</strong>
    </span>
    <a href="/courses" class="btn btn-secondary">← Back to Courses</a>
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
                    <th>Enrolled Students</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty courses}">
                        <tr><td colspan="6" style="text-align:center; color:var(--text-muted); padding:32px;">
                            No courses with enrollments found.
                        </td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="course" items="${courses}" varStatus="loop">
                            <tr>
                                <td>${loop.index + 1}</td>
                                <td><strong>${course.title}</strong></td>
                                <td><span class="badge badge-blue">${course.credits} cr</span></td>
                                <td>${course.instructor}</td>
                                <td>${course.duration}</td>
                                <td>
                                    <span class="badge badge-purple">${course.students.size()} students</span>
                                    &nbsp;
                                    <c:forEach var="st" items="${course.students}" varStatus="stLoop">
                                        ${st.name}<c:if test="${!stLoop.last}">, </c:if>
                                    </c:forEach>
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
