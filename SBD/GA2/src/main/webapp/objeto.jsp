<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="util.Configura" %>
<%@ page import="util.DataTransfer" %>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <title>Visualização do Objeto SQL</title>
    <style>
        /* -------------------------------------------------- */
        /* --- 🎨 ESTILO (Consistente) ---                    */
        /* -------------------------------------------------- */
        body { 
            font-family: 'Courier New', Courier, monospace; 
            margin: 0; 
            padding: 20px;
            background-color: #212529; /* Fundo Escuro Principal */
            color: #f8f9fa; /* Texto Claro Principal */
            font-size: 14px;
        }
        .container { 
            max-width: 900px; 
            margin: auto; 
            border: 1px solid #6c757d; 
            padding: 15px;
            box-shadow: none;
        }
        h1 { 
            color: #f8f9fa; 
            border-bottom: 1px solid #495057; 
            padding-bottom: 5px; 
            text-transform: uppercase;
            font-size: 1.8em;
            margin-top: 0;
        }
        
        /* Mensagens de Estado */
        .status-message { 
            font-weight: bold; 
            margin-top: 15px; 
            padding: 10px;
            border-left: 5px solid;
        }
        .success { 
            color: #28a745; 
            border-color: #28a745;
            background-color: #15381e;
        }
        .error-eloquente { 
            display: flex; 
            align-items: center; 
            padding: 15px 20px; 
            margin-top: 25px; 
            margin-bottom: 25px; 
            border: 1px solid #dc3545; 
            border-left: 8px solid #dc3545; 
            background-color: #380c10; 
            color: #ffc107; 
            font-size: 1.1em; 
            font-weight: bold;
        }
        .error-eloquente .icon { 
            font-size: 1.8em; 
            margin-right: 15px; 
            color: #dc3545; 
            line-height: 1; 
        }
        
        .parameter { 
            background-color: #343a40; 
            padding: 10px; 
            border: 1px dashed #6c757d; 
            margin-top: 15px; 
        }
        
        /* Links */
        a { 
            color: #17a2b8; 
            text-decoration: none; 
        }
        a:hover { 
            text-decoration: underline; 
            color: #f8f9fa;
        }
        
        /* 🎯 CORREÇÃO DE VISIBILIDADE DA TABELA 🎯 */
        table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 20px;
            color: #f8f9fa; /* Cor do texto da tabela (Clara) */
        }
        th, td {
            border: 1px solid #495057;
            padding: 8px;
            text-align: left;
            /* 💡 Fix: Define o fundo escuro para as células de dados */
            background-color: #212529; 
            color: #f8f9fa; /* Garante que o texto é claro */
        }
        th {
            /* Cabeçalho da tabela com cor de acento */
            background-color: #343a40;
            color: #17a2b8;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Explorador</h1>
        
        <%
            String nomeObjeto = request.getParameter("nome");
        %>
        
        <% if (nomeObjeto != null && !nomeObjeto.trim().isEmpty()) { %>            
            <div class="parameter">
                Objeto SQL: <strong><%= nomeObjeto %></strong>
            </div>

            <div style="margin-top: 20px; overflow-x: auto;">
                <%=DataTransfer.obterHtml(nomeObjeto) %>
            </div>
            
        <% } else { %>
            <p class="error-eloquente">
                <span class="icon">🔑</span> 
                <span>ERRO DE PARÂMETRO: O parâmetro **'nome'** é um requisito obrigatório para a visualização.</span>
            </p>
        <% } %>
        
        <p style="text-align: center; margin-top: 30px; color: #495057;">
            <%=Configura.infoApp(null)%>
            | Detalhe do Explorador
        </p>
        
        <div style="width: 95%; margin: auto; text-align: center;">
            <a href="explorador.jsp">[Voltar ao Explorador]</a>
            <span style="color:#495057;"> | </span>
            <a href="javascript:window.history.back()">[Voltar Atrás]</a>
        </div>
        <br><br>
    </div>
</body>
</html>