package web;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Mapeia o servlet para a Diretoria Raiz da Aplicação Web (Caminho Útil)
@WebServlet("/WebRootPath")
public class WebRootServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * 📥 Processa os pedidos HTTP GET.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Obter o caminho real do sistema de ficheiros para a raiz da sua aplicação (o diretório do .war)
        String webAppRootPath = request.getServletContext().getRealPath("/");
        
        // 2. Configurar a resposta
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        // 3. Devolver o caminho real
        try (PrintWriter out = response.getWriter()) {
            out.print(webAppRootPath);
        }
    }
    
    /**
     * 📤 Processa os pedidos HTTP POST, encaminhando a lógica para o doGet().
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 🔄 Simplesmente chama o método doGet para executar a mesma lógica
        doGet(request, response);
    }
}