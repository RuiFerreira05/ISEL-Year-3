<%@page pageEncoding="UTF-8"%>
<%@ page import="util.Configura,util.Name,util.DataFormatter,java.sql.*,disciplina.*"%>
<%@ page errorPage="error.jsp"%>
<%@ page session="true"%>
<%@ page info="Pauta da disciplina"%>
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8"> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Pauta da Disciplina">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Pauta da disciplina num determinado ano">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="08dec2025">
<meta http-equiv="Cache-control" content="no-store"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0"> 
<link rel="stylesheet" type="text/css" href="styleTable.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleA.css" media="all"/>
<title>Pauta da Disciplina</title>
</head>
<body style="width:65%; margin:auto; text-align:center;">
<h1>Pauta de Avaliação</h1>
<%
String codigo=request.getParameter("codigo");
String ano=request.getParameter("ano");
boolean encontrouRegistos = false; // 🚩 Flag para controlar se a consulta retornou dados.
if(codigo!=null && ano!=null){
	Disc disc=DiscDAO.getByCodigo(codigo);
%>
	<h2><%=disc.getDesignacao()%> em <%=ano%></h2>
	<br/>
	<%
	String directiva=
		"select a.numero, nome, MAX(NOTA) NOTA "
				+ "from aluno as a, inscricao as i where a.numero=i.numero and i.codigo = ?"
				+ " and ano = ? GROUP BY A.NUMERO, CODIGO order by a.numero";

		try (Connection con = new Configura().getConnection(); // Obtém e fecha a ligação (Connection).
			 PreparedStatement ps = con.prepareStatement(directiva)) { // Prepara e fecha a instrução (PreparedStatement).
	         
			 ps.setString(1, codigo); 
			 ps.setString(2, ano);
			 try (ResultSet rs = ps.executeQuery()) { // Executa a consulta e fecha o resultado (ResultSet).
		
		// 🔄 Lógica Otimizada de Apresentação (Sem rs.last())
		int nRows = 0; // Contador de linhas
		
		while (rs.next()) { // Move para o próximo registo.
			nRows+=1;
			encontrouRegistos = true;
			
			// 🖼️ Imprime o cabeçalho APENAS na primeira linha encontrada
			if (nRows==1) {
	%>
						<table class="styled-table">
							<tr>
								<th>Número</th>
								<th>Nome</th>
								<th>Nota</th>
							</tr>
	
				<%
					}
					%>
							<tr>
								<td style="text-align: right"><%=rs.getString("numero")%></td>
								<td style="text-align: left"><%=Name.normalize(rs.getString("nome"))%></td>
								<%String nota=rs.getString("nota");%>
								<td style="text-align: right"><%=(nota==null)?"__,__":DataFormatter.formatDecimal(nota)%></td>
							</tr>
			<%}	
			if(nRows>0) {%>
							</table><% // Fim da Tabela
			}
		}             // Feedback Visual para Consulta Vazia (Resultado do BD = 0)
         if (!encontrouRegistos) {
             // 💡 Apresenta uma mensagem HTML amigável
             %><p>Não foram encontradas inscrições para a disciplina '<%=disc.getCodigo()%>' no '"+ano+"'.</p><%
         }
		 
	} catch (SQLException e) {
        // ❌ Log de erro no servidor (Boa prática)
        System.err.println("ERRO na consulta de inscrições: " + e.getMessage());
        // Apresentar uma mensagem de erro ao utilizador (opcional, dependendo do errorPage)
        %><p style="color:red;">Ocorreu um erro na base de dados ao consultar as inscrições.</p><%
	} 
}	 
	else { // 🛑 Condição: Parâmetros de Entrada Inválidos (disc.valid() é FALSE)
        %>
        <h2>Erro de Parâmetros</h2>
        <p>O código e/ou ano da disciplina não foram fornecidos ou são inválidos.</p>
    <%}%>   
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Pauta
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br><br>
</body>
</html>
