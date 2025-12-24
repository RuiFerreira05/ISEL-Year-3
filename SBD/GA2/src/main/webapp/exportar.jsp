<%@page pageEncoding="UTF-8"%>
<%@page import="util.Configura"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.util.Collections"%>
<%@ page errorPage="error.jsp"%>
<%
    // 🚀 INÍCIO DO SCRIPTLET PRINCIPAL: Lógica de Negócio por Pedido (Request-Scoped)

    // 🛑 Variáveis de estado LOCAIS: Crucial para evitar erros de concorrência entre utilizadores.
    String statusMessage = "";
    String statusType 	= "info"; 
    String currentTable = "ALUNO"; // Tabela por omissão inicial
    
    // Parâmetros recebidos na URL:
    String newTableName = request.getParameter("tableName");
    String servletStatusType = request.getParameter("statusType");
    String servletStatusMessage = request.getParameter("statusMessage");
    
    // 🚨 1. TRATAMENTO DE MENSAGENS DE STATUS
    // Recebe mensagens do Servlet /Download (que redireciona via GET em caso de erro).
    if (servletStatusMessage != null && !servletStatusMessage.isEmpty()) {
        statusType = servletStatusType != null ?
        servletStatusType : "error"; // Assume 'error' se o tipo não for especificado.
        statusMessage = servletStatusMessage;
    }
    
    // ⚙️ 2. OBTENÇÃO DA LISTA DE TABELAS DISPONÍVEIS
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
    		statusMessage="🚫 Não existem tabelas para exportar!";
    }
%>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Exportação de Dados</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

    <div class="container">
        <h1 style="text-align: center;">📤 Exportação de Dados</h1>
        <p><a href="administracao.jsp">⬅️ Voltar ao Menu Principal</a></p>

        <%-- 💬 4. ÁREA DE EXIBIÇÃO DE MENSAGENS --%>
        <div style="<%=statusMessage.isEmpty()?"display: none":"display: block"%>" id="statusMessage" class="alert alert-<%= statusType %>">
            <%if(!statusMessage.isEmpty()) { %>
            		<%= statusMessage %>
            		<script> alert("<%= statusMessage %>")</script>
            	<%}%>
        </div> 

        <div class="form-section">
            <form id="exportForm" method="post" action="Download" class="form-transfer">
            		<div class="menu-section">
                <h4>1. Selecione a Tabela/Vista:</h4>
                <div class="button-group-row download-buttons">
	                <select title="Tabela selecionada..." id="tableName" name="tableName" required>
	                <% 
	                    // 📋 Loop para popular a lista de tabelas
	                    for (String tableName : availableTables) {
	                        String selected = tableName.equals(currentTable) ?
	                        "selected" : "";
	                %>		<option value="<%= tableName %>" <%= selected %>>
	                            <%= tableName %>
	                        </option>
	                <%
	                    }
	                %>
	                </select>
                </div>
       			</div>
                 <div class="menu-section">
                    <h4>2. Escolha o Formato:</h4>
                    <div class="button-group-row download-buttons">
                        <%-- 💾 BOTÕES DE SUBMISSÃO (Method POST, Envia o parâmetro 'format') --%>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta instruções Structured Query Language DML INSERT **"
                        			type="submit" value="sql" class="btn btn-warning">SQL 📜</button>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta Valores Separados por Vírgulas (Comma Separated Values) **"
                        		    type="submit" value="csv" class="btn btn-warning">CSV 📊</button>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta na Linguagem de Marcação Extensível (eXtensible Markup Language) **"
                        		    type="submit" value="xml" class="btn btn-warning">XML 🏗️</button>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta no formato JavaScript Object Notation **"
                        			type="submit" value="json" class="btn btn-warning">JSON 🧩</button>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta no Formato de Documento Portátil (Portable Document Format) **"
                        			type="submit" value="pdf" class="btn btn-warning">PDF 📎</button>
                        <button onclick="return clickButton(this);"
                        			title="** Exporta na Linguagem de Marcação de Hipertexto (HyperText Markup Language) **"
                        			type="submit" value="html" class="btn btn-warning">HTML 🌐</button>   
                        <button onclick="return clickButton(this);" 
                        			title="** Exporta texto simples (Plain Text) **"
                        			type="submit" value="txt" class="btn btn-warning">TXT 📝</button>
                        <input type = 'hidden' id="format" name = 'format'/>; 
                		</div>
                </div>
             </form>
        </div>
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Exportação
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	</div> 
	<br><br>
<script>
function clickButton(botao) {
    // 1. Preenche o campo oculto com o formato correto
    document.getElementById('format').value = botao.value;

    // 2. Prepara a página (desativa botões, inicia contador)
    handleExportStart();
    
    // 3. Submete o formulário APÓS a preparação da UI
    document.getElementById('exportForm').submit();

    // 4. Retorna false para garantir que o onclick não tente submeter novamente
    return false;
}

function handleExportStart() {

	// Obtém o elemento <body>
	const body = document.querySelector('body');
	// Define o cursor para 'wait' (ampulheta/espera)
	body.style.cursor = 'wait';
    var selectElement = document.getElementById('tableName');
    // Cria e adiciona o campo oculto (para preservar o nome da tabela)
    var hiddenInput = document.createElement('input');
    hiddenInput.type = 'hidden';
    hiddenInput.name = 'tableName'; 
    hiddenInput.value = selectElement.value;
    var form = document.getElementById('exportForm');
    form.appendChild(hiddenInput);
 	// Seleciona todos os elementos <button> que têm o atributo name="format"
    const botoesParaDesativar = document.querySelectorAll('button[name="format"]');

    // Itera sobre a lista de botões encontrados
    botoesParaDesativar.forEach(botao => {
		// Define a propriedade 'disabled' como true para desativar o botão
		botao.disabled = true;
		botao.innerHTML = '⚙️ Exportação...'; 
		// Remove as classes de cor (Ex: btn-primary)
		// Assume que todos os botões têm a classe base 'btn' e uma classe de cor.
		botao.classList.remove('btn-primary', 'btn-success', 'btn-warning', 'btn-danger', 'btn-info');
		// Aplica o estilo de processamento/desativado
		botao.classList.add('btn-processing');
    });
    // Feedback visual (o browser deve pintar isto agora)
    selectElement.disabled = true; // Desabilita o SELECT (valor já está seguro)
    // ✅ SOLUÇÃO: Usa setTimeout para perguntar ao utilizador se quer fazer imedatamente o recarregamento.
    setTimeout(() => {
    		alert("↩️ Cancelar? 🛑");
        location.reload(true); // Recarrega a página
    }, 5000);    
    iniciarContador();
}

let intervaloID; 			// Variável para armazenar o ID do intervalo
let segundosRestantes=12; 	// Várivavel que implementa o contador
function iniciarContador() {
    // Para garantir que só um contador está ativo
    if (intervaloID) {
        clearInterval(intervaloID);
    }

    // A função que será executada a cada 1000 milissegundos (1 segundo)
    intervaloID = setInterval(() => {
        // Decrementa o tempo
        segundosRestantes--;

        // Atualiza a mensagem
        const divAlvo = document.getElementById('statusMessage');
        divAlvo.className = "alert alert-warning";
        divAlvo.innerHTML="A página irá recarregar em <strong>"+segundosRestantes+" segundos</strong>...⏳";
        divAlvo.style.display = 'block';  // // Se estiver escondida, mostra
        
        // Se o tempo chegar a zero
        if (segundosRestantes <= 0) {
        		divAlvo.className = "alert alert-success"; // Use .className para substituir
            divAlvo.innerHTML = "Exportação concluida! 🎊";

            // ✅ SOLUÇÃO: Usa setTimeout para atrasar a recarga.
            setTimeout(() => {
                location.reload(true); // Recarrega a página
            }, 1000);
        		
            clearInterval(intervaloID);  // Interrompe a execução repetida
        }
    }, 1000); // 1000 milissegundos = 1 segundo
}
</script>
</body>
</html>