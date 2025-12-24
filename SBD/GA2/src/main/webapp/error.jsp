<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8"
	language="java"%>
<%--
    Página de erro padrão (error.jsp).
    O atributo isErrorPage="true" permite aceder à variável implícita 'exception'.
--%>
<!DOCTYPE html>
<html lang="pt">
<head>
<meta charset="UTF-8">
<title>❌ Ocorreu um erro</title>
<style>
/* Estilos gerais do corpo da página */
body {
	font-family: Arial, sans-serif;
	background: #f5f1ec; 
	color: #3e2a28; 
	display: flex;
	/* CORREÇÃO (1): Uso de 'center' */
	justify-content: center; /* Centraliza horizontalmente o conteúdo */
	align-items: center; /* Centraliza verticalmente o conteúdo */
	height: 100vh; 
	margin: 0;
}

/* Estilos da caixa de conteúdo principal */
.container {
	background: #ffffff;
	padding: 40px;
	width: 420px;
	box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
	border-radius: 10px;
	/* CORREÇÃO (2): Uso de 'center' */
	text-align: center; 
}

/* Estilos do emoji de café */
.emoji {
	font-size: 60px;
}

/* Estilos dos links */
a {
	color: #7b3f20; 
	font-weight: bold;
	text-decoration: none;
}

a:hover {
	text-decoration: underline;
}

/* Estilos para os detalhes técnicos/debug (Ocultos por Segurança) */
.tech {
	margin-top: 25px;
	font-size: 12px;
	color: #6b544f;
	text-align: left;
	word-wrap: break-word; 
	background: #eee6e3;
	padding: 10px;
	border-radius: 6px;
	
	/* SEGURANÇA: Mantém os detalhes técnicos ocultos por padrão */
	/* display: none; */
		display: block; 
}

.tech pre {
    /* Alinhamento: GARANTE que o texto começa à esquerda (como na imagem) */
    text-align: left;      
    
    /* Quebra de Linha: Permite quebras de linha em formato pré-formatado */
    white-space: pre-wrap; 
    
    /* Quebra de Palavra: Força a quebra de sequências longas (URLs, paths) */
    word-break: break-all; 
    
    /* Remove margens e padding padrões do PRE que podem deslocar o conteúdo */
    margin: 0;
    padding: 0;
}
</style>
</head>
<body>
	<div class="container">
		<div class="emoji">☕</div>
		<h2>Ups! Parece que algo correu mal…</h2>
		<p>Ocorreu um erro inesperado.</p>

		<p>⬅️ 
			<!--  <a href="index.jsp" title="Voltar à página inicial">Voltar</a> -->
			<a href="#" 
				onclick="history.back(); return false;" 
				title="Voltar à página anterior">Voltar</a>
		</p>

		<div class="tech">
			<strong>Detalhes técnicos (Debug):</strong><br>
				<%-- O conteúdo aqui é o tratamento da exceção (exception.getMessage()) --%>
				<%String msg = "❌ " + (exception != null? exception.getMessage() : "Erro desconhecido ou indisponível");%>
				<%="<script>alert('"+msg+"');</script>"%>
				<pre><%=msg%></pre>
		</div>
	</div>
</body>
</html>