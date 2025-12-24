<%@page pageEncoding="UTF-8"%>
<%@ page import="inscricao.*,util.Configura"%> 
<%@ page import="java.sql.*"%>

<%@ page isThreadSafe="false"%>
<%@ page session="true"%>
<%@ page info="Inscrição na disciplina"%>
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8"> 
<meta http-equiv="Content-Type" content="text/html; charset=IUTF-8">
<meta http-equiv="Content-Language" content="pt">
<meta name="title" content="Inscrição de um aluno">
<meta name="keywords" content="ISEL, DEI">
<meta name="description" content="Inscrição na disciplina num determinado ano">
<meta name="owner" content="ISEL/DEI - Doutor Porfírio Filipe">
<meta name="copyright" content="ISEL/DEETC/2012">
<meta name="createdate" content="06Dec2012">
<meta name="lastupdate" content="10dec2025">
<meta http-equiv="Cache-control" content="no-store"> 
<meta name="viewport" content="width=device-width, initial-scale=1.0"> 
<link rel="stylesheet" type="text/css" href="styleTable.css" media="all"/>
<link rel="stylesheet" type="text/css" href="styleA.css" media="all"/>
<title>Inscrição na Disciplina</title>
</head>
<body style="text-align:center;">
<h2>Inscrição na Disciplina</h2>
<%
String numero=request.getParameter("Numero");
String codigo=request.getParameter("Codigo");
String ano=request.getParameter("Ano");
if(numero==null) 
	numero="";
if(codigo==null) 
	codigo="";
if(ano==null) 
	ano="";
if(!numero.isEmpty() && !codigo.isEmpty() && !ano.isEmpty()){
	Inscricao ins = new Inscricao(request);
	if (InscricaoDAO.save(ins)==1) 
		%> <script>alert("Inscrição realizada com sucesso!");</script><%
	else
		%> <script>alert("Falha na concretização da inscrição...")</script><%
}  // else {

%>
<div id="myDIV" style="background-image: linear-gradient(to bottom left, goldenrod, gold); 
					   margin: auto; width: 95%; border: 2px solid #0000FF; padding: 10px;">
<br>
<form name="inscricao" method="post" autocomplete="off">
<label for="Numero">Número: </label> 
<input 	id="Numero" name="Numero" 
		value="<%=numero%>" 
		onchange="getAluno(this.value); chkInscricao(inscricao.Numero.value, inscricao.Codigo.value, inscricao.Ano.value);"
        size="5" maxlength="5" min="1" max="99999" 
		type="number" pattern="[0-9]{5}" 
		title="Número até cinco digitos." placeholder="00000" required>
&nbsp;&nbsp;
<!--  implementar o min e o max dinamicamente a partir do ano atual  -->
<%int anoAtual=new Configura().today().getYear(); 
  int anoMin=anoAtual;
  int anoMax=anoAtual+3;
%>
<label for="Codigo">Disciplina </label>
<select title="📚 Designação da disciplina 📝" 
		name="Codigo" id="Codigo" required
	onchange="inscricao.Ano.value=''; getAnosLivres('<%=anoMin%>', '<%=anoMax%>',inscricao.Numero.value, inscricao.Codigo.value); window.setTimeout(() => inscricao.Ano.focus(), 0);">
	<%	
	String directiva="SELECT CODIGO, DESIGNACAO FROM DISCIPLINA D ORDER BY DESIGNACAO";
	try (Connection con = new Configura().getConnection();
	 Statement st = con.createStatement();
	 ResultSet rs = st.executeQuery(directiva)) {
		while (rs.next()) {
			%><option value='<%=rs.getString("CODIGO")%>'><%=rs.getString("DESIGNACAO")%></option><%
		}
	}
	%>
</select>

&nbsp;&nbsp;
<label for="Ano">Ano </label>
<input 
	name="Ano" id="Ano"
	list="Anos" 
	value="<%=ano%>" 
	onchange="chkInscricao(inscricao.Numero.value, inscricao.Codigo.value, this.value);" size="4" 
	min="<%=anoMin%>" max="<%=anoMax%>" 
	maxlength="4" type="number" 
	pattern="[0-9]{4,4}" title="⏳ Anos sem inscrição 👴" 
	placeholder="0000" required>
<datalist id="Anos" title="⏳ Anos sem inscrição 👴">
<%for(int i=anoMin; i<=anoMax; i++) {%>
	<option value='<%=i%>'/>
<%}%>
</datalist>
<button>Executar</button>
<p id="msg"></p>
</form>
</div>
<script>

//seleciona a opção do item, select
function setValue(sel, inVal){
	var dl = document.getElementById(sel);
	if(dl!=null) {
		var i = 0;
		for (; i < dl.options.length; i++)
		  if (dl.options[i].value == inVal)
		    break;
		dl.selectedIndex = i;
	}	
}
setValue('Codigo', '<%=codigo%>');
//Atualiza por AJAX o paragrafo com os dados do aluno
function getAluno(numero) {
  const xhttp = new XMLHttpRequest();
  xhttp.onload = function() {
    document.getElementById("msg").innerHTML = this.responseText;
  }
  xhttp.open("GET", "getAluno?Numero="+numero);
  xhttp.send();
}
//Atualiza o paragrafo com mensagem relativa à inscrição já existir 'duplicado'
function chkInscricao(numero, codigo, ano) {
  const xhttp = new XMLHttpRequest();
  xhttp.onload = function() {
    document.getElementById("msg").innerHTML = this.responseText;
  }
  xhttp.open("GET", "chkInscricao?Numero="+numero+"&Codigo="+codigo+"&Ano="+ano);
  xhttp.send();
}

//Anos no intervalo que ainda não têm inscricoes, faz pedido AJAX
function getAnosLivres(min, max, numero, codigo) {
  const xhttp = new XMLHttpRequest();
  xhttp.onload = function() {
    document.getElementById("Anos").innerHTML = this.responseText;
  }
  xhttp.open("GET", "getAnosLivres?Min="+min+"&Max="+max+"&Numero="+numero+"&Codigo="+codigo);
  xhttp.send();
}

window.setTimeout(() => inscricao.Numero.focus(), 0);

</script>

	<p style="text-align: center; margin-top: 30px; color: #6c757d;">
		<%=Configura.infoApp(null)%>
		👉 Inscrever
	</p>
	<div style="width: 95%; margin: auto; text-align: center;">
		<a href="javascript:window.history.back()">Voltar</a>
	</div>
	<br>
	<br>

</body>
</html>
