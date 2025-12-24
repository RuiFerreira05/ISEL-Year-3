<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="util.Calendario" %>
<%
    // 🚨 Nota: Assumimos que a classe "Calendario" está acessível no classpath (ex: WEB-INF/classes).
    // Se estiver num pacote, altere o import acima (ex: com.seuprojeto.Calendario).
%>

<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🇵🇹 Gerador de Calendário Web</title>
    
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background-color: #f4f7f6; }
        .container { max-width: 900px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        h1 { color: #007bff; text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; color: #333; }
        input[type="number"], select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        
        /* Estilos para os botões e grupo de botões */
        .button-group { display: flex; gap: 10px; margin-top: 10px; }
        .button-group button, .button-group a { flex-grow: 1; padding: 12px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; text-align: center; text-decoration: none; display: inline-block; }
        
        .btn-gerar { background-color: #28a745; color: white; } /* Verde para visualização */
        .btn-gerar:hover { background-color: #218838; }
        
        .btn-pdf { background-color: #dc3545; color: white; } /* Vermelho para download */
        .btn-pdf:hover { background-color: #c82333; }
        
        .resultado { margin-top: 30px; border-top: 1px dashed #ccc; padding-top: 20px; }
        .modelos-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Gerador de Calendário Português 📅</h1>

        <%
            // 1. Configuração inicial
            int anoAtual = LocalDate.now().getYear();
            int anoAlvo = anoAtual;
            int opcaoModelo = 4; // 4 é 'impressao' (default)

            // Arrays de nomes de modelos (baseado na sua classe Calendario)
            String[] nomesModelos = new String[]{"", "Luxo 👑", "Premium ✨", "Púrpura 🔮", "Impressão 🖨️", "Terminal 📟", "Gótico 🏰", "Celta 🍀", "ASCII Art 🧱", "Culinária 👨‍🍳", "Hotelaria 🛎️", "Veterinária 🐾"};
            
            // 2. Processar o formulário
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                try {
                    String anoParam = request.getParameter("ano");
                    String modeloParam = request.getParameter("modelo");
                    
                    if (anoParam != null && !anoParam.trim().isEmpty()) {
                        anoAlvo = Integer.parseInt(anoParam);
                    }
                    if (modeloParam != null && !modeloParam.trim().isEmpty()) {
                        opcaoModelo = Integer.parseInt(modeloParam);
                    }
                } catch (NumberFormatException e) {
                    out.println("<p style='color: red;'>Erro: O ano deve ser um número válido.</p>");
                }
            }
        %>
        
        <form method="POST" action="calendario.jsp">
            <div class="form-group">
                <label for="ano">Ano do Calendário:</label>
                <input type="number" id="ano" name="ano" value="<%= anoAlvo %>" required min="1900" max="2100">
            </div>

            <div class="form-group">
                <label>Modelo Temático:</label>
                <div class="modelos-grid">
                    <%
                        // Gera as opções de rádio baseadas no array de nomes
                        for (int i = 1; i < nomesModelos.length; i++) {
                            String nome = nomesModelos[i];
                            String checked = (i == opcaoModelo) ? "checked" : "";
                    %>
                            <label style="display: inline-block; font-weight: normal; margin-right: 15px;">
                                <input type="radio" name="modelo" value="<%= i %>" <%= checked %> required>
                                <%= nome %>
                            </label>
                    <%
                        }
                    %>
                </div>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn-gerar">Gerar Visualização HTML</button>

                <%
                    // Cria o URL com os parâmetros ano e modelo que o DownloadPdfServlet irá receber
                    String servletUrl = "downloadPdf?ano=" + anoAlvo + "&modelo=" + opcaoModelo;
                %>
                <a href="<%= servletUrl %>" class="btn-pdf">Download PDF</a>
            </div>
        </form>

        <%
            // 3. Exibir o calendário na página (apenas após o POST)
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                try {
                    // Chama o método da classe Java para gerar o HTML como uma String
                    String htmlCalendario = Calendario.gerarCalendarioHTMLtoString(anoAlvo, opcaoModelo); 
                    
                    out.println("<div class='resultado'>");
                    out.println("<h2>Calendário " + nomesModelos[opcaoModelo] + " - " + anoAlvo + "</h2>");
                    
                    // Imprime o HTML gerado diretamente na página
                    out.println(htmlCalendario);
                    
                    out.println("</div>");
                } catch (Exception e) {
                    out.println("<p style='color: red;'>Ocorreu um erro ao gerar o calendário: " + e.getMessage() + "</p>");
                }
            }
        %>

    </div>
</body>
</html>