<%@page pageEncoding="UTF-8"%>
<%@ page import="util.*, aluno.*, java.sql.*"%> 
<%@ page errorPage="error.jsp"%> 
<%@ page session="true"%> 
<%@ page info="Manipulação de Inscrições"%> 
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8"> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Inscrição de um estudante">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Inscrições ativas de um estudante">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="07dec2025">
<meta http-equiv="Cache-control" content="no-store"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0">  
<link rel="stylesheet" type="text/css" href="styleTable.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleA.css" media="all"/>  
<title>Inscrições Ativas</title>
</head>
<body style="width:75%; margin:auto; text-align:center;">
	<% 
	Aluno aluno = AlunoDAO.getByNumero(request.getParameter("Numero"));
	// 🚦 2. Validação dos Parâmetros de Entrada
	// Verifica se o objeto Disc é válido. Se não for, salta todo o bloco de lógica e vai para o 'else'.
	if(aluno!=null && aluno.valid()){
		%> 
		<h1 style="color: #007bff; text-align: center; margin-bottom: 5px;">Inscrições Ativas</h1> 
		<h2 style="color: #343a40; border-bottom: 2px solid #007bff; padding-bottom: 5px; margin-top: 25px;">
		<%=aluno.getNomeS()%> (<%=aluno.getNumeroS()%>)</h2>
		<br/>
		<%
		// 📜 3. Definição da Query SQL
		// Query para obter dados as disciplinas em que o aluno está inscrito.
		String directiva = "SELECT ANO, I.CODIGO, DESIGNACAO FROM INSCRICOES I "+
		                    "WHERE NUMERO = ? ORDER BY 1 DESC, 2";
		
		// 🔒 4. Execução do JDBC (Try-with-resources para fecho seguro)
		try (Connection con = new Configura().getConnection(); // Obtém e fecha a ligação (Connection).
			 PreparedStatement ps = con.prepareStatement(directiva)) { // Prepara e fecha a instrução (PreparedStatement).
             
			 ps.setInt(1, aluno.getNumero()); // Define o parâmetro '?' com o código da disciplina.
			 
			 try (ResultSet rs = ps.executeQuery()) { // Executa a consulta e fecha o resultado (ResultSet).
				
				// 🔄 5. Lógica Otimizada de Apresentação (Sem rs.last())
				int nRows = 0; // Contador de linhas
				
				while (rs.next()) { // Move para o próximo registo.
					nRows+=1;
					
					// 🖼️ Imprime o cabeçalho APENAS na primeira linha encontrada
					if (nRows==1) {%>
						<table class="styled-table"> 
							<tr> 
							    <th style="width:10%;">Ano</th>
								<th style="width:10%;">Código</th> 
								<th>Designação</th>
							</tr>		
					<%}%>
							<tr> 
								<td style="text-align:center;"><%=rs.getString(1)%></td>
								<td style="text-align:right;"><%=rs.getString(2)%></td>
								<td style="text-align:left;"><%=rs.getString(3)%> </td>
							</tr>
				<%} // Fim do while (rs.next())
				
				// 🖼️ 6. Fecho da Tabela
				if(nRows>0) {%>
					</table><% // Fim da Tabela
				}
				else {
					// ⚠️ 7. Feedback Visual para Consulta Vazia (Resultado do BD = 0)
	                // 💡 Apresenta uma mensagem HTML amigável.
	                %><p>ℹ️ O aluno Nº <%=aluno.getNumeroS()%> não tem inscrições ativas! 👀</p><%
				}
			} // Fim do try (ResultSet)           
		} catch (SQLException e) {
            // ❌ Log de erro no servidor (Boa prática)
            System.err.println("ERRO na consulta de inscrições: " + e.getMessage());
            // Apresentar uma mensagem de erro ao utilizador (opcional, dependendo do errorPage)
            %><p style="color:red;">👀 Ocorreu um erro na base de dados ao consultar as inscrições.</p><%
		}
	} else { // 🛑 Condição: Parâmetros de Entrada Inválidos (disc.valid() é FALSE)
        %>
        <h2>Erro de Parâmetros</h2>
        <p>O número do aluno não foi fornecido ou não é válido.</p>
    <%}%>   
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Inscrição
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br><br>
</html>