<%@page pageEncoding="UTF-8"%>
<%@page import="util.Configura"%>
<%@page import="util.Manipula"%>
<%@page import="aluno.*"%>
<%@page import="java.util.StringJoiner"%>
<%@page import="java.sql.*"%>
<%@ page import="java.math.RoundingMode, java.math.BigDecimal"%>
<%@ page errorPage="error.jsp"%>
<%@ page isThreadSafe="false"%>
<%@ page info="Resumo dos registos"%>

<!DOCTYPE html>
<%@page import="util.Name"%>
<html lang="pt">
<head>
<meta charset="UTF-8">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Resumo dos registos">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Resumo dos registos dos estudantes">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="08Dec2025">
<meta http-equiv="Cache-control" content="no-store">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" type="text/css" href="styleTable.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleText.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleLink.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleFotoPasse.css" media="all"/>
<title>Resumo</title>
</head>
<body style="text-align:center;">
<h2>Resumo dos Registos 🗃️</h2>
<%=AlunoDAO.geraFiltro("numero",8)%>
<br>
<br>
<%=AlunoDAO.geraFiltro("nome",7)%>
<br>
<br>
<%=AlunoDAO.geraFiltro("nascido",6)%>
<br>
<br>
<%
String coluna 	= request.getParameter("Coluna")==null?"numero":request.getParameter("Coluna");
String superior 	= request.getParameter("Superior")==null?"":request.getParameter("Superior");
String inferior 	= request.getParameter("Inferior")==null?"":request.getParameter("Inferior");
%>
<form id="MyFrm" method="post">
	<input type="hidden" id="Superior" 	name="Superior" 	value="<%=superior%>"/>
	<input type="hidden" id="Inferior" 	name="Inferior" 	value="<%=inferior%>"/>
	<input type="hidden" id="Coluna"		name="Coluna" 	value="<%=coluna%>"/>	
</form>
<script>
	function ativar(coluna, inferior, superior) {
		document.getElementById("Coluna").value=coluna;
		document.getElementById("Inferior").value=inferior;
		document.getElementById("Superior").value=superior;
		document.getElementById("MyFrm").submit();
	}
</script>
<%
String where="";
String limite = "";
if (superior.length() > 0 && inferior.length() > 0)
	where = " where " + coluna + ">='" + inferior.replaceAll("'", "''") + "' AND " + 
						coluna + " <='"+ superior.replaceAll("'", "''") + "'";
else {%>
	<script>
	
	document.body.style.cursor = 'wait';
	
	setTimeout(() => {
		document.getElementsByName('filtro')[0].click();
	}, 100);	
	</script>
<%
}
System.out.println("Filtro/Where: "+where);
String sql = "select numero from aluno " + where + " order by 1";
try (Connection con = new Configura().getConnection();
		Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery(sql)) {
	int rowCount = (rs.last()) ? rs.getRow() : 0;
	if (rowCount > 0) {
		rs.beforeFirst();
%>
		<table style="width:60%" class="styled-table"><%
		while (rs.next()) {
			String numero=rs.getString("numero");%>
		<tr>
			<td>
				<a href="#" onclick="ck('<%=numero%>');">
					<img class 	=	"foto-passe" 
						   src 	=	'<%="FotoDown?numero="+numero%>' 
						  title 	=	"Fotografia do estudante Nº <%=numero%>"/>
				</a>
			</td> 					
			<td style="text-align: justify;  text-justify: inter-word; font-family: 'Altura'; font-size: 2.0em;">
				<%=AlunoDAO.resumo(numero)%>
			</td>
		</tr>
		<%} // while%>
		</table><%
	} // if
	else {%>
		<script>alert("Não existem estudantes registados!")</script>
	<%}
} // try%>
	
<script>
function ck(numero) {
	document.getElementById("Numero").value=numero;
	document.getElementById("myForm").submit();
}
</script>
<form id="myForm" action="alunos.jsp" method="post" onsubmit="document.body.style.cursor = 'wait'; return true;">
  <input type="hidden" name="Numero" id="Numero"/>
  <input type="hidden" name="Comando" id="Comando" value="S"/>
</form>
<br>
<p style="text-align: center; margin-top: 30px; color: #6c757d;">
	<%=Configura.infoApp(null)%>
	👉 Resumo
</p>
<div style="width: 95%; margin: auto; text-align: center;">
	<a href="javascript:window.history.back()">Voltar</a>
</div>
<br><br>
</body>
</html>
