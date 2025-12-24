<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="one.Gestor" %>
<%@ page import="one.User" %>
<%@ page import="java.util.*" %>

<%
    // --- 🛠️ Gestão de Sessão ---
    String user = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");
    String lang = (String) session.getAttribute("lang");
    if (lang == null) lang = "pt-PT";

    // --- 🔑 Lógica de Login ---
    if (request.getParameter("login") != null) {
        String inputUser = request.getParameter("username");
        String foundRole = User.getGroup(inputUser);
        if (foundRole != null && !foundRole.equals("Guest")) {
            session.setAttribute("user", inputUser);
            session.setAttribute("role", foundRole);
            user = inputUser;
            role = foundRole;
        }
    }

    // --- 🚪 Logout e 🌐 Idioma ---
    if (request.getParameter("logout") != null) {
        session.invalidate();
        response.sendRedirect("index.jsp");
        return;
    }
    if (request.getParameter("setLang") != null) {
        lang = request.getParameter("setLang");
        session.setAttribute("lang", lang);
    }

    // --- 🚩 Mapeamento de Bandeiras ---
    Map<String, String> flags = new HashMap<>();
    flags.put("pt-PT", "🇵🇹"); flags.put("en-US", "🇺🇸"); flags.put("fr-FR", "🇫🇷");
    flags.put("de-DE", "🇩🇪"); flags.put("it-IT", "🇮🇹"); flags.put("el-GR", "🇬🇷");
%>

<!DOCTYPE html>
<html lang="<%= lang %>">
<head>
    <meta charset="UTF-8">
    <title>VetSystem Web</title>
    <style>
        :root { --main: #2980b9; --bg: #f5f6fa; --txt: #2f3640; }
        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: var(--bg); color: var(--txt); margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 8px 25px rgba(0,0,0,0.1); }
        
        /* Login */
        .login-card { text-align: center; border: 2px dashed var(--main); padding: 40px; border-radius: 15px; }
        .user-list-text { color: #7f8c8d; font-size: 0.9em; margin-bottom: 20px; letter-spacing: 0.5px; }

        /* Header & Langs */
        .top-bar { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid var(--main); padding-bottom: 15px; margin-bottom: 25px; }
        .lang-btn { background: none; border: 1px solid #ddd; padding: 8px 12px; cursor: pointer; border-radius: 8px; font-size: 1.1em; transition: 0.2s; }
        .lang-btn:hover { background: #ebf2f7; }
        .lang-active { border-color: var(--main); background: #e1f0fa; box-shadow: inset 0 0 5px rgba(0,0,0,0.05); }

        /* Tasks */
        .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(450px, 1fr)); gap: 15px; }
        .card { border: 1px solid #dcdde1; padding: 18px; border-radius: 10px; background: #fff; cursor: pointer; transition: 0.2s; display: flex; align-items: flex-start; }
        .card:hover { transform: translateY(-3px); border-color: var(--main); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
        .code-tag { background: var(--main); color: white; padding: 2px 8px; border-radius: 4px; margin-right: 12px; font-weight: bold; font-size: 0.85em; }

        .btn-exit { background: #e84118; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold; float: right; }
    </style>
</head>
<body>

<div class="container">

    <% if (user == null) { %>
        <div class="login-card">
            <h2 style="margin-top:0;">--- VETERINARY SYSTEM LOGIN ---</h2>
            <div class="user-list-text">
                Username (admin_user, vet_silva, reception_ana, tutor_joao)
            </div>
            <form method="POST">
                <input type="text" name="username" placeholder="Enter username" required 
                       style="padding:12px; width:280px; border:1px solid #ccc; border-radius:6px; margin-bottom:15px;">
                <br>
                <button type="submit" name="login" style="padding:12px 30px; background:var(--main); color:white; border:none; border-radius:6px; cursor:pointer; font-weight:bold;">LOGIN</button>
            </form>
        </div>
    <% } else { 
        Map<String, String> txt = Gestor.LANGUAGES.get(lang);
    %>
        <div style="text-align: right; margin-bottom: 10px;">
            <form method="POST">
                <% for (String code : Gestor.LANGUAGES.keySet()) { %>
                    <button name="setLang" value="<%= code %>" 
                            class="lang-btn <%= code.equals(lang) ? "lang-active" : "" %>" 
                            title="<%= code %>">
                        <%= flags.getOrDefault(code, "🌐") %> <%= code.split("-")[0].toUpperCase() %>
                    </button>
                <% } %>
            </form>
        </div>

        <div class="top-bar">
            <div>
                <h1 style="margin:0;"><%= txt.get("header") %></h1>
                <div style="margin-top:5px; color: #718093;">
                    👤 <%= txt.get("logged_as") %>: <strong><%= user %></strong> | 
                    🔑 <%= txt.get("role") %>: <strong><%= role %></strong>
                </div>
            </div>
        </div>

        <div class="grid">
            <% 
                for (String key : Gestor.COMMANDS.keySet()) {
                    if (Gestor.COMMANDS.get(key).contains(role)) {
            %>
                <div class="card" onclick="alert('<%= txt.get("executing") %>: <%= key %>')">
                    <span class="code-tag"><%= key %></span>
                    <span><%= txt.get(key) %></span>
                </div>
            <% 
                    }
                }
            %>
        </div>

        <div style="margin-top:40px; overflow: hidden;">
            <form method="POST">
                <button type="submit" name="logout" class="btn-exit"><%= txt.get("logout") %></button>
            </form>
        </div>
    <% } %>

</div>

</body>
</html>