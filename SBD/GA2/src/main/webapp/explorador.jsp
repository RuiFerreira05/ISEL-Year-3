<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="util.Configura" %>
<%@ page import="util.Configura.SGBD" %>

<%!
    // 💡 Classe interna para simular o logger (mantido)
    private static final class Log {
        public static void info(String message) {
            System.out.println("LOG INFO: " + message);
        }
        public static void error(String message) {
            System.err.println("LOG ERROR: " + message);
        }
    }

    // 🧩 Classe interna para agrupar os dados do objeto BD (mantido)
    private static class ObjetoBD {
        public final String nome;
        public final String tipo; // "TABLE" ou "VIEW"
        public final String comentarios; // REMARKS do metadados

        public ObjetoBD(String nome, String tipo, String comentarios) {
            this.nome = nome;
            this.tipo = tipo;
            // Garantir que o comentário não é nulo para o atributo title
            this.comentarios = (comentarios != null && !comentarios.trim().isEmpty()) ? comentarios.trim() : "Sem descrição.";
        }
    }
%>

<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>SQL Explorer 🧭 - Console de Metadados</title>
    <style>
        /* -------------------------------------------------- */
        /* --- 🎨 ESTILO FORMAL RETRO (Monocromático com Acentos) --- */
        /* -------------------------------------------------- */
        body { 
            font-family: 'Courier New', Courier, monospace; 
            margin: 0; 
            padding: 20px;
            background-color: #212529; /* Preto/Cinza Escuro Formal */
            color: #f8f9fa; /* Texto Branco/Claro */
            font-size: 14px;
        }
        .container { 
            max-width: 900px; 
            margin: auto; 
            border: 1px solid #6c757d; /* Borda Cinza Sutil */
            padding: 15px;
            box-shadow: none; /* Sem brilho exagerado */
        }
        h1 { 
            color: #f8f9fa; 
            border-bottom: 1px solid #495057; /* Linha divisória discreta */
            padding-bottom: 5px; 
            text-transform: uppercase;
            font-size: 1.8em;
            margin-top: 0;
        }
        h2 {
            color: #ced4da; /* Cinza Claro */
            margin-top: 25px;
            margin-bottom: 15px;
            font-weight: bold;
        }
        .info-bar { 
            color: #adb5bd; /* Cinza Suave */
            margin-bottom: 15px;
        }
        
        /* Controlo: Listagem de Links */
        .table-list { 
            display: flex; 
            flex-wrap: wrap; 
            gap: 10px; 
            margin-top: 20px; 
        }
        .table-list a {
            flex-grow: 1; 
            max-width: calc(33.33% - 7px);
            
            display: block;
            padding: 8px 12px;
            text-decoration: none;
            border: 1px solid;
            transition: background-color 0.1s;
            text-align: left; /* Formal: Texto alinhado à esquerda */
            font-weight: normal;
        }
        
        /* 🔵 Estilo para TABELAS */
        .table-control {
            background-color: #0b2f4f; /* Azul Marinho Escuro */
            color: #17a2b8; /* Azul/Teal de Acento */
            border-color: #17a2b8;
        }
        .table-control:hover {
            background-color: #17a2b8; 
            color: #ffffff;
        }

        /* 🟡 Estilo para VISTAS */
        .view-control {
            background-color: #493300; /* Marrom Escuro */
            color: #ffc107; /* Amarelo/Dourado de Acento */
            border-color: #ffc107;
        }
        .view-control:hover {
            background-color: #ffc107; 
            color: #000000;
        }

        /* Estilo de Mensagem de Erro/Aviso */
        .error { 
            color: #dc3545; /* Vermelho Formal */
            border: 1px solid #dc3545;
            background-color: #380c10;
            padding: 10px;
            margin-top: 15px;
        }
        .warning {
            color: #ffc107;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><span style="color:#adb5bd;">[SQL.META]</span> Explorer 🧭</h1>
        <div class="info-bar">
            [+] Status: Conectado a <%= Configura.SGBD.MySQL %> (<span style="color:#17a2b8;">TABLES</span> / <span style="color:#ffc107;">VIEWS</span>)
        </div>
        
        <%
            // Lógica de acesso à BD (mantida do código anterior)
            Connection conn = null;
            ResultSet rs = null;
            List<ObjetoBD> objetos = new ArrayList<>();
            String erroMsg = null;
            
            try {
                Configura cfgMySQL = new Configura(SGBD.MySQL);
                conn = cfgMySQL.getConnection();
                
                if (conn == null) {
                    throw new SQLException("Falha ao obter a conexão: objeto nulo.");
                }
                
                DatabaseMetaData dbmd = conn.getMetaData();
                String[] tipos = {"TABLE", "VIEW"}; 
                
                rs = dbmd.getTables(Configura.getDTB_(), null, "%", tipos);
                
                while (rs.next()) {
                    String nomeTabela = rs.getString("TABLE_NAME");
                    String tipoTabela = rs.getString("TABLE_TYPE");
                    String comentarios = rs.getString("REMARKS"); // Obtém o comentário/descrição
                    
                    if (nomeTabela != null && !nomeTabela.startsWith("sys") && !nomeTabela.startsWith("information_schema")) {
                        objetos.add(new ObjetoBD(nomeTabela, tipoTabela, comentarios));
                    }
                }
                
            } catch (SQLException e) {
                erroMsg = "SQL ERROR [" + e.getErrorCode() + "]: " + e.getMessage();
                Log.error(erroMsg);
            } catch (Exception e) {
                erroMsg = "SYSTEM ERROR: " + e.getMessage();
                Log.error(erroMsg);
            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
                if (conn != null) try { conn.close(); Log.info("🔌 Conexão fechada."); } catch (SQLException ignore) {}
            }
        %>

        <%-- ---------------------------------------------------------------------- --%>
        <%-- --- 🖼️ APRESENTAÇÃO DOS RESULTADOS (HTML) ---                          --%>
        <%-- ---------------------------------------------------------------------- --%>
        
        <% if (erroMsg != null) { %>
            <div class="error">
                [ERRO] Falha na Conexão/Metadados:<br>
                > <%= erroMsg %>
            </div>
        <% } else if (objetos.isEmpty()) { %>
            <p class="warning">⚠️ Aviso: Nenhum objeto encontrado para exploração.</p>
        <% } else { %>
            <h2>OBJETOS DISPONÍVEIS (<%= objetos.size() %>)</h2>
            <div class="table-list">
                <% 
                    for (ObjetoBD obj : objetos) { 
                        String cssClass = obj.tipo.equals("TABLE") ? "table-control" : "view-control";
                %>
                        <a 
                            href="objeto.jsp?nome=<%= obj.nome %>" 
                            class="<%= cssClass %>" 
                            title="TIPO: <%= obj.tipo %> | DESCRIÇÃO: <%= obj.comentarios %>"
                        >
                            <span style="font-weight: bold; margin-right: 5px;">[<%= obj.tipo.substring(0, 1) %>]</span> <%= obj.nome %>
                        </a>
                <% 
                    } 
                %>
            </div>
        <% } %>
        
        <p style="text-align: right; margin-top: 50px; color: #495057; font-size: 0.8em;">
            <span style="color:#6c757d;"><%=Configura.infoApp(null)%></span>
            Explorador V1.0 - Executado em <%= new java.util.Date() %>
        </p>
    </div>
</body>
</html>