<%@page pageEncoding="UTF-8"%>
<%@page import="inscricao.InscricaoDAO"%>
<%@ page import="disciplina.*,util.Configura, java.io.IOException, java.util.List"%>
<%@ page errorPage="error.jsp"%>
<%@ page session="true"%>
<%@ page info="Visualização de disciplinas"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="keywords" content="ISEL, JSP, Disciplinas">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Visualização de disciplinas">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Visualização de disciplinas">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="07Dec2025">
<meta http-equiv="Cache-control" content="no-store">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Visualização de Disciplinas</title>
<link rel="stylesheet" type="text/css" href="styleA.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleTable.css" media="all"/>
<style>
  	iframe {color:blue;
  		  	display:none; 
  		  	width:100%;
  		  	height:215px; 
  		  	margin:auto; 
  		  	text-align:center;}
 
</style>
</head>
<body style="width:75%; margin:auto; text-align:center;">
<h1>Disciplinas</h1>
<%
List<Disc> list=DiscDAO.getAll();
%>
<script>
function go (url, target, c, d) {
	if(d=="")
		document.getElementById("ano").value = prompt("Introduza o ano:");
	document.getElementById("codigo").value=c;
	document.getElementById("designacao").value=d;
	document.getElementById("frm").action=url;
	document.getElementById("frm").target=target;
	document.getElementById("frm").submit();
	if(target!="_self")
		document.getElementById(target).style.display="block";
}

</script>

<form style="display:none" id="frm" name="frm" method="post">
			<input id="codigo" name="codigo">
			<input id="designacao" name="designacao">
			<input id="ano" name="ano">
</form>

<table class="styled-table" >
	<tr>
		<th>Código</th>
	    <th><a onclick="style.display='none';" title="Clique para acrescenter uma disciplina nova" 
	           href='javascript:go("disciplina.jsp","iDisc", "","");'>Designação</a>
	           <iframe id="iDisc" name="iDisc"></iframe>
	    </th>
	    <th>Inscrições</th>  
	</tr>
<%
for(Disc d:list){
%>
	<tr>
		<td style="text-align: left" title="Clique para consultar pauta">
		<a href='javascript:go("pauta.jsp","_self","<%=d.getCodigo()%>","");'>
		<%=d.getCodigo()%></a>
		</td>
		<td style="text-align: left" title="Clique para editar">
			<a onclick="style.display='none';" 
			<% String aux= d.getDesignacao();
				// Substitui aspas duplas (");
				aux=aux.replaceAll("\"","\\\\\"");
				// Substitui aspas simples (') pela entidade decimal;
				aux = aux.replaceAll("\'", "&#39;");%>
			href='javascript:go("disciplina.jsp","iDisc<%=d.getCodigo()%>", "<%=d.getCodigo()%>","<%=aux%>");'>
			<%=d.getDesignacao()%></a>
			<iframe id="iDisc<%=d.getCodigo()%>" name="iDisc<%=d.getCodigo()%>"></iframe>
		</td>
		<td style="text-align: right" title="Clique para visualizar inscrições ativas">
		<a href='javascript:go("inscricoes.jsp","_self","<%=d.getCodigo()%>","<%=aux%>");'>
				<%=inscricao.InscricaoDAO.contaAtivas(d.getCodigo()).toString()%></a>
		</td>
	</tr>
<%}%>
	</table>
	<br>
	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Disciplinas
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br><br>
</body>
</html>