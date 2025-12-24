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
	<meta name="keywords" content="ISEL, JSP, Administração">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Content-Language" content="pt">
	<meta name="title" content="Administração do gestor académico">
	<meta name="keywords" content="ISEL, DEI">
	<meta name="description" content="Administração">
	<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
	<meta name="copyright" content="ISEL/DEETC/2012">
	<meta name="createdate" content="06Dec2012">
	<meta name="lastupdate" content="07Dec2025">
	<meta http-equiv="Cache-control" content="no-store">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" type="text/css" href="styles.css" media="all"/>
	<title>Gestor Académico - Administração</title>
</head>
<body>
<%! 
	
    // Variáveis de estado e mensagens para a interface
    private String statusMessage = "Caminho: "+new Configura().getRealPath();
    private String statusType = "info"; // info, success, error

    // Simulação do Console.writeLine() para coletar a saída para a web
    private List<String> consoleOutput = new ArrayList<>();
    private void executeAction(String action) {
        statusMessage = "";
        statusType = "info";
        consoleOutput.clear();
        try {
            switch (action) {
                // --- ADMINISTRAÇÃO ---
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
                case "carregar_dados":
                    if (Gestor.carregarTabelas()) {
                        statusMessage = "✅ Dados carregados com sucesso.";
                        statusType = "success";
                    } else {
                        statusMessage = "❌ Falha ao carregar dados. Verifique os logs do servidor.";
                        statusType = "error";
                    }
                    break;
                case "listar_estrutura":
                		List<String> tableNames = new Configura().getObjects(true);
                    if (tableNames.isEmpty()) {
                    		consoleOutput.add("⚠️ Nenhuma tabela ou view encontrada, ou houve algum erro.");
                    } else {
                    		consoleOutput.add("🔍 A consultar tabelas na base de dados: ");
                    			for (String name : tableNames)
                    			consoleOutput.add("-> "+name);
                    			consoleOutput.add("⚙️ Tabelas e Views encontradas (" + tableNames.size() +")");
                        }
                    statusMessage = "✅ Listagem das tabelas existentes concluida.";
                    statusType = "info";
                    break;
                case "limpar_dados":
                    if (Gestor.apagarTabelas()) {
                        statusMessage = "✅ Dados de todas as tabelas removidos (TRUNCATE/DELETE).";
                        statusType = "success";
                    } else {
                        statusMessage = "❌ Falha ao limpar os dados. Verifique os logs do servidor.";
                        statusType = "error";
                    }
                    break;
                case "remover_tabelas":
                    if (Gestor.removerTabelas() && Gestor.removerVistas()) {
                        statusMessage = "✅ Tabelas e Vistas removidas com sucesso (DROP).";
                        statusType = "success";
                    } else {
                        statusMessage = "❌ Falha ao remover Tabelas/Vistas. Verifique os logs do servidor.";
                        statusType = "error";
                    }
                    break;
                case "eliminar_db":
                    Configura cfg_del = new Configura();
                    if (cfg_del.eliminarBaseDeDados()) { 
                        statusMessage = "✅ Base de Dados eliminada.";
                        statusType = "warning";
                    } else {
                         statusMessage = "❌ Falha ao eliminar a Base de Dados.";
                        statusType = "error";
                    }
                    break;
                default:
                    statusMessage = "Ação desconhecida.";
                    statusType = "info";
            }
        } catch (Exception e) {
            statusMessage = "❌ ERRO CRÍTICO DURANTE A EXECUÇÃO: " + e.getMessage();
            statusType = "error";
            e.printStackTrace(); // Imprime a exceção completa no log do servidor
        }
    }
%>
<%
    // Lógica de processamento do pedido
    String action = request.getParameter("action");
    if (action != null && !action.isEmpty()) {
        executeAction(action);
    }
%>

    <div class="container">
        <h1>Sistema de Gestão Académica - Administração</h1>

        <%-- Exibir Mensagem de Status --%>
        <% if (statusMessage!=null && !statusMessage.isEmpty()) { %>
            <div class="alert alert-<%= statusType %>">
                <%= statusMessage %>
                <script>alert("<%= statusMessage %>")</script>
            </div>
        <% 
        } %>

        <p><a href="index.jsp">⬅️ Voltar ao Menu Principal</a></p>
        
        <h2>Operações da Base de Dados</h2>
        <p>Acesso a operações de instanciação, carga, transferência, limpeza e remoção da base de dados.</p>
        <div class="menu-grid"> 
         
            <div class="menu-item" title="** Criar Base de Dados **">
                <form method="post" class="form-action">
                    <input type="hidden" name="action" value="criar_db">
                    <div class="button-group">
            		    <button type="submit" class="btn btn-primary">🧱 Cria BD</button>
                    </div>
                </form>
            </div>
            <div class="menu-item" title="** Criar Tabelas/Vistas **">
                 <form method="post" class="form-action">
                    <input type="hidden" name="action" 
                    value="criar_tabelas">
                    <div class="button-group"><button type="submit" class="btn btn-primary">➕ Cria TV</button></div>
                </form>
            </div>
            <div class="menu-item" title="** Listar Tabelas/Vistas **">
                 <form method="post" class="form-action">
                    <input type="hidden" name="action" value="listar_estrutura">
                    <div class="button-group"><button type="submit" class="btn btn-primary">📑 Lista TV</button></div>
                </form>
             </div>
             <div class="menu-item"  title="** Carregar Dados Iniciais **">
                <form method="post" class="form-action">
                    <input type="hidden" name="action" value="carregar_dados">
                    <div class="button-group"><button type="submit" class="btn btn-primary">🗄️ Carrega</button></div>
                </form>
             </div>
            <div class="menu-item" title="** Limpar Tabelas (DELETE) **">
                 <form method="post" class="form-action" onsubmit="return confirm('ATENÇÃO: Deseja realmente APAGAR TODOS OS DADOS das tabelas? Esta ação é irreversível.')">
                    <input type="hidden" name="action" value="limpar_dados">
                    <div class="button-group">
                    		<button onclick="alert('🛑 Segurança: Bloqueado! 🔒'); return false;" type="submit" class="btn btn-danger">🗑️ Limpa</button>
                    	</div>
                </form>
            </div>
            <div class="menu-item" title="** Remover Tabelas/Vistas (DROP) **">
                <form method="post" class="form-action" onsubmit="return confirm('PERIGO: Deseja realmente REMOVER Tabelas e Vistas? Esta ação é irreversível.')">
                    <input type="hidden" name="action" value="remover_tabelas">
                    <div class="button-group"><button onclick="alert('🛑 Segurança: Bloqueado! 🔒'); return false;" type="submit" class="btn btn-danger">➖ Remove TV</button></div>
                </form>
            </div>
            <div class="menu-item" title="** Eliminar Base de Dados (DROP) **">          
                 <form method="post" class="form-action" onsubmit="return confirm('PERIGO: Deseja realmente ELIMINAR a Base de Dados? Esta ação é irreversível.')">
                    <input type="hidden" name="action" value="eliminar_db">
                    <div class="button-group"><button onclick="alert('🛑 Segurança: Bloqueado! 🔒'); return false;" type="submit" class="btn btn-danger">🧹 Elimina BD</button></div>
                </form>
            </div>
            
             <div class="menu-item" title=" ** Exportar Dados para Ficheiro **">
                 <form method="get" class="form-action" action="exportar.jsp">
                    <div class="button-group"><button type="submit" class="btn btn-warning">📤 Exporta</button></div>
                </form>
            </div>
             <div class="menu-item" title=" ** Importar Dados de Ficheiro **">
                 <form method="get" class="form-action" action="importar.jsp">
                    <div class="button-group"><button type="submit" class="btn btn-warning">📥 Importa</button></div>
                </form>
            </div>
             <div class="menu-item" title=" ** Informação sobre o projeto **">
                 <form method="get" class="form-action" action="sobre.html">
                    <div class="button-group"><button type="submit" class="btn btn-primary">👤 Sobre</button></div>
                </form>
            </div>
            
        </div>

        <%-- Output da Consola (Simulação de retorno de Gestor.java) --%>
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
		👉 Administração
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br><br>
    </div>
</body>
</html>