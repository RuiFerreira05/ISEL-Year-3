<%@page pageEncoding="UTF-8"%>
<%@page import="gestor.Relatorios"%>
<%@page import="util.Configura"%>
<%@page import="util.DataTransfer"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page errorPage="error.jsp"%>
<!DOCTYPE html>
<html lang="pt">
<head>  
    <meta charset="UTF-8">
	<meta name="keywords" content="ISEL, JSP, Relatórios">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Content-Language" content="pt">
	<meta name="title" content="Gestão de relatórios">
	<meta name="keywords" content="ISEL, DEI">
	<meta name="description" content="Gestão de relatórios">
	<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
	<meta name="copyright" content="ISEL/DEETC/2012">
	<meta name="createdate" content="06Dec2012">
	<meta name="lastupdate" content="07Dec2025">
	<meta http-equiv="Cache-control" content="no-store">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="stylesheet" type="text/css" href="styles.css" media="all"/>
	<title>Relatórios: Documentos e Estatísticas</title>
</head>
<body>
<%! 
    // Variáveis de estado e mensagens
    private String statusMessage = "Escolha um documento ou estatística para gerar.";
    private String statusType = "info"; // info, success, error
	
    // Simulação do Console.writeLine() para coletar a saída para a web
    private List<String> consoleOutput = new ArrayList<>();
    
    /**
     * Executa a ação de relatório solicitada.
     * @param action O código da ação ('a', 'b', 'c', 'd', 'e').
     */
    private void executeAction(String action, HttpServletRequest req) {
        statusMessage = "";
        statusType = "info";
        consoleOutput.clear();
        String result = "";
            switch (action) {
                case "a":
                    statusMessage = "✅ Estatística Global de ARP gerada com sucesso.";
                    result = Relatorios.obterARP();
                    break;
                case "b":
                    statusMessage = "✅ Estatística de Notas por Disciplina gerada com sucesso.";
                    result = Relatorios.obterMMM();
                    break;
                case "c":
                    statusMessage = "✅ Pauta gerada com sucesso.";
                    result = Relatorios.obterPauta(req.getParameter("codigo"), 
                    		          Integer.parseInt(req.getParameter("ano")));
                    break;
                case "d":
                    statusMessage = "✅ Certificado gerado com sucesso.";
                    result = Relatorios.obterCertificado(Integer.parseInt(req.getParameter("numero")));
                    break;
                default:
                    statusMessage = "❌ Ação de relatório inválida.";
                    statusType = "error";
                    return; // Sai sem processar o resultado
            }
            
            // Divide o resultado por linha para simular a saída da consola
            if (result != null) {
                String[] lines = result.split("\\r?\\n");
                for (String line : lines) {
                    consoleOutput.add(line);
                }
                statusType = "success";
            }
    }
%>
<%
    // Lógica para processar a submissão do formulário
    String action = request.getParameter("action");
    if (action != null && !action.isEmpty()) {
        executeAction(action,request);
    }
%>

    <div class="container">
        <h1>Documentos e Estatísticas Académicos</h1>
        <!-- Ajusta as classes para usar os estilos definidos em styles.css (status.info, status.success, status.error) -->
        <div class="status <%= statusType %>"><%= statusMessage %></div>

        <div class="menu-grid">
            
            <!-- Opção 'a' -->
            <div class="menu-item">
                <form method="post" class="form-action" title="Estatística de Aprovação/Reprovação e Percentagem.">
                    <input type="hidden" name="action" value="a">
                    <div class="button-group"><button type="submit" class="btn btn-primary">ARP 📈</button></div>
                </form>
                <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
            <!-- Opção 'b' -->
                <form method="post" class="form-action" title="Notas Minima, Máxima e Média por Disciplina.">
                    <input type="hidden" name="action" value="b">
                    <div class="button-group"><button type="submit" class="btn btn-primary">MMM 🧮</button></div>
                </form>
            </div>
            
            <!-- Opção 'c' -->
            <div class="menu-item">
                <form method="post" class="form-action" title="Gera a Pauta com as notas finais.">
                    <input type="hidden" name="action" value="c">
                    <div class="button-group"><button type="submit" class="btn btn-primary">Pauta 📋</button></div>
                    <div>
                         <label for="disciplina">Disciplina:</label>
                			 <input maxlength="4" size="6" required title="Código da Disciplina" type="text" id="codigo" name="codigo" placeholder="Ex: SBD">
                    		 <br>
                    		 <label for="ano">Ano:</label>
                			 <input maxlength="4" size="6" required title="Ano Letivo" type="number" id="ano" name="ano" placeholder="Ex: 2025">
					</div>
                </form>
            </div>
            
            <!-- Opção 'd' -->
            <div class="menu-item" >
                <form method="post" class="form-action" title="Gera o Certificado de Habilitações.">
                    <input type="hidden" name="action" value="d">
                    <div class="button-group"><button type="submit" class="btn btn-primary">Certificado 📜 </button></div>
                    <div>
                    		 <label for="numero">Número:</label>
                			 <input maxlength="5" size="9" required title="Número do aluno" type="number" id="numero" name="numero" placeholder="Ex: 99999">
            			</div>
                </form>
            </div>
            
            <!-- Opção 'e' -->
            <div class="menu-item">
                <form method="post" class="form-action" action="explorador.jsp" title=' Explorador de Tabelas ou Vistas.'>
                    <div class="button-group">
                    		<button type="submit" class="btn btn-primary">Explorar 🔭</button>
                    </div>
                </form>
			<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
                <form method="post" class="form-action" action="resumo.jsp" title="Resumo textual dos dados dos alunos.">
                    <div class="button-group">
                    		<button type="submit" class="btn btn-primary">Resumo 📋</button>
                    </div>
                </form>
            </div>
        </div>

        <%-- Output da Consola (Simulação de retorno de Relatorios.java) --%>
        <% if (!consoleOutput.isEmpty()) { %>
            <h2>Output do Relatório</h2>
            <!-- Usa a classe 'console-output' de styles.css -->
            <div class="console-output">
            		
                <% out.println(); // limpa desfazamento
                    // Mostra as linhas de output
                    for (String line : consoleOutput) { 
                        // O 'out.println' é mais seguro em blocos de scriptlet
     					out.println(line);
                         System.out.println(line);
                    } 
                %>
            </div>
        <% } %>
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Relatórios
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
    </div>
</body>
</html>