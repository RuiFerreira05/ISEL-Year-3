<%@page pageEncoding="UTF-8"%>
<%@page import="util.Configura"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.util.Collections"%>
<%@ page errorPage="error.jsp"%>

<%
    // ⚙️ LÓGICA DE PROCESSAMENTO DO PEDIDO HTTP ⚙️
    
    // 🛑 Variáveis de estado LOCAIS (Scope do Pedido)
    String statusMessage = "";
    String statusType 	= "info"; 
    String currentTable = "ALUNO"; 
    
    // Parâmetros recebidos: tableName (para re-seleção)
    String newTableName = request.getParameter("tableName"); 
    
    // 1. Capturar mensagens de status do Servlet (via GET params do redirect)
    String servletStatusType = request.getParameter("statusType");
    String servletStatusMessage = request.getParameter("statusMessage");
    
    if (servletStatusMessage != null && !servletStatusMessage.isEmpty()) {
        statusType = servletStatusType != null ? servletStatusType : "error";
        statusMessage = servletStatusMessage;
    }
    
    // 2. Obter a lista de tabelas da Base de Dados
    List<String> availableTables = new Configura().getObjects(true);

    if (!(availableTables == null || availableTables.isEmpty() || availableTables.get(0).contains("ERRO"))) { 
        availableTables.replaceAll(String::toUpperCase);
        Collections.sort(availableTables); // Ordenação alfabética
   // 🔄 3. DEFINIÇÃO DA TABELA ATUAL SELECIONADA
   if (newTableName != null && !newTableName.trim().isEmpty()) {
       currentTable = newTableName.trim().toUpperCase(); // Usa a tabela passada por parâmetro (se existir).
   } else {
       // Garante que uma tabela válida está selecionada se currentTable falhar.
       if (!availableTables.isEmpty() && !availableTables.contains(currentTable)) {
            currentTable = availableTables.get(0);
       }
   }
   } else {
   		currentTable = "";
   		statusMessage="🚫 Não existem tabelas para importar!";
   }
%>

<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Importar Dados</title>
    <link rel="stylesheet" href="styles.css"> 
</head>
<body>
    <div class="container">
        <h1 style="text-align: center;">📥 Importação de Dados</h1>
        <p><a href="administracao.jsp">⬅️ Voltar ao Menu Principal</a></p>

        <%-- 4. Exibir Mensagem de Status --%>
        <% if (statusMessage != null && !statusMessage.isEmpty()) { %>
            <div class="status-box status-<%=statusType%>">
                <%=statusMessage%>
            </div>
        <% } %>
		<div class="form-section">
        <%-- 5. Formulário de Importação: Unificado para Upload --%>
        <form id="importForm" onsubmit="return handleImportStart();"
        		  action="Upload" method="post" enctype="multipart/form-data" class="content-box">           
            <div class="menu-section">
                <h4>1. Selecione a Tabela:</h4>
                <div class="button-group-row download-buttons">
                <select title="Tabela selecionada..." required id="tableName" name="tableName" class="form-select">
                <%
                    for (String table : availableTables) {
                        String selected = table.equals(currentTable) ? "selected" : "";
                        if(!table.contains("VIEW"))
                        		out.println("<option value=\"" + table + "\" " + selected + ">" + table + "</option>");
                    }
                %>
                </select>
                </div> 
            </div>

            <div class="menu-section">
                <h4>2. Escolha o Ficheiro:</h4>
                <%-- CAMPO DE UPLOAD com validação required --%>
                <div class="button-group-row download-buttons">
	                <input type="file" name="fileUpload" id="fileUpload" 
	                       title="Selecione um ficheiro .sql, .csv, .xml, ou .json."
	                       accept=".sql,.csv,.xml,.json,.txt"
	                       required class="form-control"> 
                 </div>
            </div>

            <div class="menu-section">
                <h4>3. Iniciar Importação:</h4>
                <div class="button-group-row download-buttons">
                    <%-- BOTÃO ÚNICO DE SUBMISSÃO --%>
                    <button id="importButton" 
                            title="Inicia o upload do ficheiro, importa os dados para a tabela selecionada."
                            type="submit" 
                            class="btn btn-warning" 
                            style="width: 100%;">
                        📜📊🏗️🧩 Importar Dados
                    </button>
                </div>
            </div>
        
        </form> 
        </div>                
        <p class="io-path">
            O formato de importação (**SQL, CSV, XML, JSON**) é determinado **automaticamente** a partir do tipo MIME/extensão.
        </p>
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Importação
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
    </div>
    <br><br>
<script>
function handleImportStart() {
	// Obtém o elemento <body>
	const body = document.querySelector('body');
	// Define o cursor para 'wait' (ampulheta/espera)
	body.style.cursor = 'wait';
    var button = document.getElementById('importButton');
    var form = document.getElementById('importForm');
    var selectElement = document.getElementById('tableName');
    
    // 1. Cria e adiciona o campo oculto (para preservar o nome da tabela)
    var hiddenInput = document.createElement('input');
    hiddenInput.type = 'hidden';
    hiddenInput.name = 'tableName'; 
    hiddenInput.value = selectElement.value;
    form.appendChild(hiddenInput); 
    
    // 2. Feedback visual (o browser deve pintar isto agora)
    selectElement.disabled = true; // Desabilita o SELECT (valor já está seguro)
    
    // 🎨 Alterações que precisam de ser renderizadas
    button.innerHTML = '⚙️ Importação em Curso... Aguarde.'; 
    button.classList.remove('btn-warning'); 
    button.classList.add('btn-processing');
 	// Lê a altura do elemento (offsetHeight), forçando o browser a pintar as alterações.
    var triggerReflow = button.offsetHeight;
    return true; 
}
</script>
</body>
</html>