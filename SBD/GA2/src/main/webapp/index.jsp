<%@page pageEncoding="UTF-8"%>
<%@page import="gestor.Gestor"%>
<%@page import="util.Configura"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.io.File"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page errorPage="error.jsp"%>
<!DOCTYPE html>
<html lang="pt">
<head>
    	<meta charset="UTF-8">
	<meta name="keywords" content="ISEL, JSP, Gestor Acámico">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Content-Language" content="pt">
	<meta name="title" content="Menu Principal">
	<meta name="keywords" content="ISEL, DEI">
	<meta name="description" content="Sistema para Gestão Académica">
	<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
	<meta name="copyright" content="ISEL/DEETC/2012">
	<meta name="createdate" content="06Dec2012">
	<meta name="lastupdate" content="07Dec2025">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestor Académico - Menu Principal</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<%! 

    // Variáveis de estado e mensagens para a interface
    private String statusMessage = Configura.infoApp("Bem-vindo(a) ao ");
    private String statusType = "info"; // info, success, error

    // Simulação do Console.writeLine() para coletar a saída para a web
    private List<String> consoleOutput = new ArrayList<>();
    private void executeAction(String action) {
        statusMessage = "";
        statusType = "info";
        consoleOutput.clear();
        try {
            switch (action) {
                // Lógica de administração mantida em scriptlet para processar eventuais redirects
                case "criar_db":
                    Configura cfg = new Configura();
                    if (cfg.criarBaseDeDados()) {
                        statusMessage = "✅ Base de Dados criada com sucesso.";
                        statusType = "success";
                    } else {
                        statusMessage = "❌ Falha ao criar a Base de Dados. Verifique os logs do servidor.";
                        statusType = "error";
                    }
                    break;
                case "criar_tabelas":
                    if (Gestor.criarTabelas() && Gestor.criarVistas()) {
                        statusMessage = "✅ Tabelas e Vistas criadas com sucesso.";
                        statusType = "success";
                    } else {
                        statusMessage = "❌ Falha na criação de Tabelas/Vistas. Verifique os logs do servidor.";
                        statusType = "error";
                    }
                    break;
                // ... outras ações de administração (omitidas para brevidade, mas presentes no arquivo anterior)
                default:
                    statusMessage = "Ação desconhecida.";
                    statusType = "info";
            }
        } catch (Exception e) {
            statusMessage = "❌ ERRO CRÍTICO DURANTE A EXECUÇÃO: " + e.getMessage();
            statusType = "error";
            e.printStackTrace(); 
        }
    }
%>
<%
    String action = request.getParameter("action");
    if (action != null && !action.isEmpty()) {
        executeAction(action);
    }
%>
    <div class="container">
        <h1>Sistema para Gestão Académica</h1>

        <%-- Exibir Mensagem de Status --%>
        <% if (statusMessage!=null && !statusMessage.isEmpty()) { %>
            <div class="alert alert-<%= statusType %>">
                <%= statusMessage %>
                <script> alert("<%= statusMessage %>")</script>
            </div>
        <% 
        } %>

        <h2>Menu Principal</h2>
        <p>Acesso aos módulos de gestão específicos.</p>
       
        <div class="menu-grid">
            <div class="menu-item" title="** Gestão de Alunos**">      
                <form method="post" action="alunos.jsp">
                	<button type="submit" class="btn btn-primary">🎓 Alunos</button>
                </form>
            </div>
            <div class="menu-item" title="** Gestão de Disciplinas**">  
                <form method="post" action="disciplinas.jsp">
                 	<button type="submit" class="btn btn-primary">📚 Disciplinas</button>
                 </form>
            </div>
            <div class="menu-item" title="** Gestão de Avaliações **">
                <form method="post" action="lancar.jsp">
                 	<button type="submit" class="btn btn-primary">💯 Avaliações</button>
                 </form>
            </div>
            <div class="menu-item" title="** Gestão de Inscrições **">
                <form method="post" action="inscrever.jsp">
                 	<button type="submit" class="btn btn-primary">🏷️ Inscrições</button>
                 </form>
            </div>
            <div class="menu-item" title="** Relatórios: Documentos e Estatísticas **">
                 <form method="post" action="relatorios.jsp">
                		<button type="submit" class="btn btn-primary">📊 Relatórios</button>
                 </form>
            </div>
            <div class="menu-item" title="** Manutenção da Base de Dados **">
                 <form method="get" action="administracao.jsp">
                		<button type="submit" class="btn btn-warning">⚙️ Administração</button>
                 </form>
            </div> 
        </div>
        
        <%-- Output da Consola --%>
        <% if (!consoleOutput.isEmpty()) { %>
            <h2>Output da Consola</h2>
            <div class="console-output">
                <% for (String line : consoleOutput) { %>
                    <%= line %><br>
                <% } %>
            </div>
        <% } %>
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Home
	</p>
    </div>
    <br><br>
</body>
</html>