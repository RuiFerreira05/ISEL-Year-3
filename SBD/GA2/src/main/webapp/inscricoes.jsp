<%@page pageEncoding="UTF-8"%>
<%@ page import="util.*, disciplina.*, java.sql.*"%> 
<%@ page errorPage="error.jsp"%> <%@ page session="true"%> 
<%@ page info="Visualização de Inscrições"%> 
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8"> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Inscrições do estudante">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Inscrições ativas do estudante">
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
	// 🔎 1. Processamento da Entrada de Dados
	// Cria um objeto Disc (disciplina) com os parâmetros 'codigo' e 'designacao' da requisição HTTP.
	Disc disc = new Disc(request.getParameter("codigo"), request.getParameter("designacao"));
	boolean encontrouRegistos = false; // 🚩 Flag para controlar se a consulta retornou dados.

	// 🚦 2. Validação dos Parâmetros de Entrada
	// Verifica se o objeto Disc é válido. Se não for, salta todo o bloco de lógica e vai para o 'else'.
	if(disc!=null && disc.valid()){
		%> 
		<h1 style="color: #007bff; text-align: center; margin-bottom: 5px;">Inscrições Ativas</h1> 
		<h2 style="color: #343a40; border-bottom: 2px solid #007bff; padding-bottom: 5px; margin-top: 25px;">
		<%=disc.getDesignacao()%> (<%=disc.getCodigo()%>)</h2>
		<br/>
		<%
		// 📜 3. Definição da Query SQL
		// Query para obter dados dos alunos inscritos na disciplina.
		String directiva = "SELECT ANO, A.NUMERO, NOME FROM INSCRICOES I, ALUNO A "+
		                    "WHERE I.NUMERO = A.NUMERO AND CODIGO = ? "+
		 					"ORDER BY 1 DESC,2";
		
		// 🔒 4. Execução do JDBC (Try-with-resources para fecho seguro)
		try (Connection con = new Configura().getConnection(); // Obtém e fecha a ligação (Connection).
			 PreparedStatement ps = con.prepareStatement(directiva)) { // Prepara e fecha a instrução (PreparedStatement).
             
			 ps.setString(1, disc.getCodigo()); // Define o parâmetro '?' com o código da disciplina.
			 
			 try (ResultSet rs = ps.executeQuery()) { // Executa a consulta e fecha o resultado (ResultSet).
				
				// 🔄 5. Lógica Otimizada de Apresentação (Sem rs.last())
				int nRows = 0; // Contador de linhas
				
				while (rs.next()) { // Move para o próximo registo.
					nRows+=1;
					encontrouRegistos = true;
					
					// 🖼️ Imprime o cabeçalho APENAS na primeira linha encontrada
					if (nRows==1) {%>
						<table class="styled-table"> 
							<tr> 
							    <th style="width:10%;">Ano</th>
								<th style="width:10%;">Número</th> 
								<th>Nome</th>
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
			} // Fim do try (ResultSet)
            
            // ⚠️ 7. Feedback Visual para Consulta Vazia (Resultado do BD = 0)
            if (!encontrouRegistos) {
                // 💡 Apresenta uma mensagem HTML amigável.
                %><p>Não foram encontradas inscrições ativas para a disciplina <%=disc.getCodigo()%>.</p><%
            }
            
		} catch (SQLException e) {
            // ❌ Log de erro no servidor (Boa prática)
            System.err.println("ERRO na consulta de inscrições: " + e.getMessage());
            // Apresentar uma mensagem de erro ao utilizador (opcional, dependendo do errorPage)
            %><p style="color:red;">Ocorreu um erro na base de dados ao consultar as inscrições.</p><%
		}

	} else { // 🛑 Condição: Parâmetros de Entrada Inválidos (disc.valid() é FALSE)
        %>
        <h2>Erro de Parâmetros</h2>
        <p>O código e/ou designação da disciplina não foram fornecidos ou são inválidos.</p>
    <%}%>   
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Inscrições
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br><br>
</html>