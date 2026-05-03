<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} — Student Course Manager</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        :root {
            --bg-dark:    #0d0d1a;
            --bg-card:    #13132b;
            --bg-sidebar: #0f0f22;
            --accent1:    #6c63ff;
            --accent2:    #a855f7;
            --accent3:    #38bdf8;
            --text-main:  #e8e8f0;
            --text-muted: #8888aa;
            --border:     rgba(108,99,255,.25);
            --success:    #22c55e;
            --danger:     #ef4444;
            --warning:    #f59e0b;
            --radius:     12px;
            --sidebar-w:  240px;
        }

        body {
            font-family: 'Inter', sans-serif;
            background: var(--bg-dark);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
        }

        /* ── Sidebar ───────────────────────────────────────── */
        .sidebar {
            width: var(--sidebar-w);
            background: var(--bg-sidebar);
            border-right: 1px solid var(--border);
            display: flex;
            flex-direction: column;
            padding: 24px 0;
            position: fixed;
            top: 0; left: 0; bottom: 0;
            z-index: 100;
        }
        .sidebar-brand {
            padding: 0 24px 28px;
            border-bottom: 1px solid var(--border);
            margin-bottom: 16px;
        }
        .sidebar-brand h1 {
            font-size: 1rem;
            font-weight: 700;
            background: linear-gradient(135deg, var(--accent1), var(--accent2));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .sidebar-brand p { font-size: .72rem; color: var(--text-muted); margin-top: 4px; }

        .nav-section { padding: 0 16px; margin-bottom: 8px; }
        .nav-label { font-size: .65rem; font-weight: 600; color: var(--text-muted);
                     text-transform: uppercase; letter-spacing: .1em; padding: 0 8px 8px; }
        .nav-link {
            display: flex; align-items: center; gap: 10px;
            padding: 10px 12px; border-radius: 8px;
            color: var(--text-muted); text-decoration: none;
            font-size: .85rem; font-weight: 500;
            transition: background .2s, color .2s;
            margin-bottom: 2px;
        }
        .nav-link:hover, .nav-link.active {
            background: rgba(108,99,255,.15);
            color: var(--accent1);
        }
        .nav-link .icon { font-size: 1rem; width: 20px; text-align: center; }

        /* ── Main content ──────────────────────────────────── */
        .main {
            margin-left: var(--sidebar-w);
            flex: 1;
            padding: 32px;
            min-height: 100vh;
        }
        .page-header {
            margin-bottom: 28px;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--border);
        }
        .page-header h2 {
            font-size: 1.6rem; font-weight: 700;
            background: linear-gradient(135deg, var(--text-main), var(--accent3));
            -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
        }
        .page-header p { color: var(--text-muted); font-size: .88rem; margin-top: 4px; }

        /* ── Cards ─────────────────────────────────────────── */
        .card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            padding: 24px;
            margin-bottom: 24px;
        }

        /* ── Tables ────────────────────────────────────────── */
        .table-wrapper { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        thead tr { background: rgba(108,99,255,.12); }
        th {
            padding: 12px 16px; text-align: left;
            font-size: .78rem; font-weight: 600;
            color: var(--accent1); text-transform: uppercase; letter-spacing: .07em;
            border-bottom: 1px solid var(--border);
        }
        td {
            padding: 13px 16px; font-size: .88rem;
            border-bottom: 1px solid rgba(255,255,255,.04);
            color: var(--text-main);
        }
        tbody tr { transition: background .15s; }
        tbody tr:hover { background: rgba(108,99,255,.07); }

        /* ── Buttons ───────────────────────────────────────── */
        .btn {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 9px 18px; border-radius: 8px; border: none;
            font-size: .83rem; font-weight: 600; cursor: pointer;
            text-decoration: none; transition: transform .15s, box-shadow .15s, opacity .15s;
        }
        .btn:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(0,0,0,.4); }
        .btn:active { transform: translateY(0); }
        .btn-primary {
            background: linear-gradient(135deg, var(--accent1), var(--accent2));
            color: #fff;
        }
        .btn-secondary { background: rgba(255,255,255,.08); color: var(--text-main); }
        .btn-edit { background: rgba(56,189,248,.15); color: var(--accent3); }
        .btn-danger { background: rgba(239,68,68,.15); color: var(--danger); }
        .btn-success { background: rgba(34,197,94,.15); color: var(--success); }
        .btn-sm { padding: 6px 12px; font-size: .78rem; }

        /* ── Forms ─────────────────────────────────────────── */
        .form-group { margin-bottom: 20px; }
        label { display: block; font-size: .82rem; font-weight: 500; color: var(--text-muted); margin-bottom: 6px; }
        input[type=text], input[type=email], input[type=number], select {
            width: 100%; padding: 11px 14px;
            background: rgba(255,255,255,.05);
            border: 1px solid var(--border);
            border-radius: 8px; color: var(--text-main);
            font-size: .88rem; font-family: 'Inter', sans-serif;
            transition: border-color .2s, box-shadow .2s;
            outline: none;
        }
        input:focus, select:focus {
            border-color: var(--accent1);
            box-shadow: 0 0 0 3px rgba(108,99,255,.2);
        }
        select option { background: var(--bg-card); }
        .field-error { color: var(--danger); font-size: .78rem; margin-top: 5px; }

        /* ── Alerts ────────────────────────────────────────── */
        .alert {
            padding: 13px 18px; border-radius: 9px;
            font-size: .85rem; margin-bottom: 20px;
            display: flex; align-items: center; gap: 10px;
        }
        .alert-success { background: rgba(34,197,94,.12); border: 1px solid rgba(34,197,94,.3); color: var(--success); }
        .alert-danger  { background: rgba(239,68,68,.12); border: 1px solid rgba(239,68,68,.3); color: var(--danger); }

        /* ── Badge ─────────────────────────────────────────── */
        .badge {
            display: inline-block; padding: 3px 9px; border-radius: 20px;
            font-size: .72rem; font-weight: 600;
        }
        .badge-blue { background: rgba(56,189,248,.15); color: var(--accent3); }
        .badge-purple { background: rgba(168,85,247,.15); color: var(--accent2); }

        /* ── Action row ────────────────────────────────────── */
        .action-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

        /* ── Multi-select ──────────────────────────────────── */
        select[multiple] { height: 160px; }
    </style>
</head>
<body>
<nav class="sidebar">
    <div class="sidebar-brand">
        <h1>🎓 SCM App</h1>
        <p>Student Course Manager</p>
    </div>
    <div class="nav-section">
        <div class="nav-label">Students</div>
        <a href="/students" class="nav-link"><span class="icon">👩‍🎓</span> All Students</a>
        <a href="/students/new" class="nav-link"><span class="icon">➕</span> Add Student</a>
        <a href="/students/enrollments" class="nav-link"><span class="icon">🔗</span> Enrollments</a>
    </div>
    <div class="nav-section">
        <div class="nav-label">Courses</div>
        <a href="/courses" class="nav-link"><span class="icon">📚</span> All Courses</a>
        <a href="/courses/new" class="nav-link"><span class="icon">➕</span> Add Course</a>
        <a href="/courses/enrolled" class="nav-link"><span class="icon">🔗</span> Enrolled Courses</a>
    </div>
    <div class="nav-section" style="margin-top:auto;">
        <a href="/h2-console" class="nav-link" target="_blank"><span class="icon">🛢️</span> H2 Console</a>
    </div>
</nav>
<div class="main">
