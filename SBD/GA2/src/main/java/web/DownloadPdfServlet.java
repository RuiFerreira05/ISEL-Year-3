package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// 🚨 ATUALIZADO PARA JAKARTA EE (Java 25)
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 🚨 IMPORTAÇÕES DA BIBLIOTECA DE CONVERSÃO (Flying Saucer / OpenPDF)
// Se usar o Flying Saucer com OpenPDF, certifique-se de que os JARs estão em WEB-INF/lib.
import org.xhtmlrenderer.pdf.ITextRenderer;
import util.Calendario;

@WebServlet("/downloadPdf")
public class DownloadPdfServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Lida com os pedidos de download (os links na JSP usam o método GET).
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. RECEBER E VALIDAR PARÂMETROS
        int ano = 0;
        int opcaoModelo = 4;

        try {
            ano = Integer.parseInt(request.getParameter("ano"));
            String modeloParam = request.getParameter("modelo");
            if (modeloParam != null) {
                opcaoModelo = Integer.parseInt(modeloParam);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parâmetros 'Ano' ou 'Modelo' inválidos.");
            return;
        }

        // 2. GERAR O CONTEÚDO HTML COMPLETO
        String htmlContent;
        try {
            htmlContent = Calendario.gerarCalendarioHTMLtoString(ano, opcaoModelo);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o conteúdo HTML: " + e.getMessage());
            return;
        }
        
        // 3. CONVERSÃO DE HTML PARA PDF USANDO ITEXt/FLYING SAUCER
        byte[] pdfBytes;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            ITextRenderer renderer = new ITextRenderer();
            
            // É comum o Flying Saucer necessitar do prefixo de URI para resolver recursos (como fontes).
            // Aqui assumimos que ele não necessita, pois o calendário só usa estilos CSS internos.
            renderer.setDocumentFromString(htmlContent);
            
            renderer.layout();

            renderer.createPDF(baos);
            
            pdfBytes = baos.toByteArray();

        } catch (Exception e) {
            // Em ambientes Jakarta EE, erros de biblioteca podem ser complexos. 
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro de conversão de HTML para PDF. Verifique a compatibilidade do iText/Flying Saucer com o Java 25. Detalhe: " + e.getMessage());
            return;
        }
        
        // 4. CONFIGURAR OS HEADERS HTTP PARA DOWNLOAD
        String[] nomes = {"", "luxo", "premium", "purpura", "impressao", "terminal", "gotico", "celta", "ascii","culinaria","hotelaria","veterinaria"};
        String modeloNome = (opcaoModelo > 0 && opcaoModelo < nomes.length) ? nomes[opcaoModelo] : "calendario";
        String fileName = "Calendario_" + modeloNome + "_" + ano + ".pdf";
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(pdfBytes.length);

        // 5. ENVIAR O FICHEIRO AO CLIENTE
        OutputStream out = response.getOutputStream();
        out.write(pdfBytes);
        out.flush();
        out.close();
    }
}