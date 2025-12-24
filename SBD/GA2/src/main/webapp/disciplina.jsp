<%@page pageEncoding="UTF-8"%>
<%@ page import="disciplina.*"%>
<%@ page errorPage="error.jsp"%>
<%@ page info="Manutenção de Disciplinas"%>
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8">
<meta name="keywords" content="ISEL, JSP, Disciplinas">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Manutenção de disciplinas">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Gestão de disciplinas">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="07Dec2025">
<meta http-equiv="Cache-control" content="no-store">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" type="text/css" href="styles.css" media="all"/>
<title>Manutenção de Disciplinas</title>
<body>
<a style="display:none;" id="go" target="_parent" href="disciplinas.jsp">Fechar</a>
<%
String codigo = request.getParameter("codigo");
String designacao = request.getParameter("designacao");
if(designacao!=null) {
	designacao = designacao.replaceAll("\"","&quot;");
	designacao = designacao.replaceAll("\'", "&apos;");
}
String parent = request.getParameter("parent");
if(parent!=null && parent.compareTo("true")==0){%>
	<script>
		document.getElementById("go").click();
	</script>
<%
} 
String titulo="";
String accao="";
if(codigo!=null && !codigo.isEmpty()) {
	titulo="Edição da Disciplina";
	accao="edit";
}
else {
	codigo="";
	designacao="";
	titulo="Nova Disciplina";
	accao="save";
}
%>
<script>
	function gerarCodigo(dsg) {
		if("<%=codigo%>"=="")
			document.getElementById("codigo").value=dsg.match(/\b(\w)/g).join('').toUpperCase();
	}
	
	window.setTimeout(() => document.getElementById("myForm").focus(), 0);
	
	String.prototype.toTitleCase = function() {
		  var i, j, str, lowers, uppers;
		  str = this.replace(/([^\W_]+[^\s-]*) */g, function(txt) {
			if(txt==='de '||txt==='dos '||txt==='do '||txt==='das '||txt==='da '||txt==='e ')
				return txt;
		    return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
		  });		  
		  return str; 
	}
</script>
<h2 style="line-height: 0.75; color: #007bff; text-align: center; margin-bottom: 20px;"><%=titulo%></h2>

<form id="myForm" name="myForm" action="<%=accao%>" method="post">
<table>
	<tr><td style="text-align:right"><label for="Designacao">Designação:</label></td>
		<td>
			<input value="<%=designacao%>" type="hidden" id="designacaoOld" name="designacaoOld"/>
			<input onkeyup="gerarCodigo(this.value);" 
				   onchange="gerarCodigo(this.value);this.value=this.value.toTitleCase();" 
				   value="<%=designacao%>" maxlength="60" size="60" type="text" 
				   id="designacao" name="designacao" 
				   placeholder="Alfanumérico" pattern="[a-zA-Z0-9 ÁÉÍÓÚàáãâéêíóõôúç\-]{6,60}" 
				   title="Designação da Disciplina" required>
	    </td>
	</tr>
	<tr><td style="text-align:right"><label for="codigo">Código:  </label></td>
		<td>
			<input value="<%=codigo%>" type="hidden" id="codigoOld" name="codigoOld"/>
			<input value="<%=codigo%>" maxlength="4" size="4" type="text" id="codigo" name="codigo" 
					placeholder="A-Z0-9" pattern="[a-zA-Z0-9àáãâéêíóõôúç\-]{2,4}" 
					title="Código da Disciplina" required>
		</td>
	</tr>
	<tr>
		<td colspan="2" style="text-align:right">
		<br>
		<%if(codigo!=null && codigo.length()!=0) {%>
			<button title="Atualiza a Disciplina"
					onclick="document.getElementById('myForm').action='edit'">Atualizar</button>
			<button title="Apaga a Disciplina" 
					onclick="document.getElementById('myForm').action='delete'">Apagar</button>
			<%}%>
		<button title="Acrescenta Disciplina Nova" 
				onclick="document.getElementById('myForm').action='save';">Acrescentar</button>
		<button title="Fecha o formulário" 
				onclick="document.getElementById('go').click(); return false;">Fechar</button></td>
	</tr>
</table>
</form>
</body>
</html>