package disciplina;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/delete")
public class delete extends HttpServlet {

	private static final long serialVersionUID = 2173812416937754960L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ⚙️ Configuração da Resposta
        response.setContentType("text/html");
        
        // 🔒 Bloco try-with-resources: Garante o fecho automático e seguro do PrintWriter
        try (PrintWriter out = response.getWriter()) {
            
            // 🔎 Recolha e Preparação dos Dados
            // Cria um objeto 'disc' (d) usando o código e designação passados como parâmetros.
            // Estes parâmetros identificam o registo a ser eliminado.
            Disc d = new Disc(request.getParameter("codigo"), request.getParameter("designacao"));
            d.print(); // 💡 Linha de debug/registo (logging) para ver o que vai ser apagado.
            // 🗑️ Lógica de Eliminação (DAO - Data Access Object)
            // Tenta eliminar o registo na base de dados. Assume-se que 'dao.delete(d)' 
            // retorna 1 em caso de sucesso (1 registo afetado).
            if (DiscDAO.delete(d) != 1) {
                
                // ❌ Lógica de Falha na Eliminação
                out.println("<h3>Sorry! 🚫 Unable to delete record...</h3>");
                
                // ⚠️ Codificação de URL (URL Encoding)
                // É crucial codificar os valores dos parâmetros para evitar que caracteres especiais 
                // causem problemas ao reencaminhar para o JSP.
                String codigoEncoded = URLEncoder.encode(d.getCodigo(), StandardCharsets.UTF_8.toString());
                String designacaoEncoded = URLEncoder.encode(d.getDesignacao(), StandardCharsets.UTF_8.toString());
                
                // Constrói a URL para retornar à página com os dados do registo que não pôde ser eliminado
                String urlErro = "disciplina.jsp?codigo=" + codigoEncoded + "&designacao=" + designacaoEncoded;
                
                // ➡️ Reencaminhamento Interno (include)
                // Inclui o conteúdo do JSP (disc.jsp) na resposta atual para mostrar a mensagem de erro.
                request.getRequestDispatcher(urlErro).include(request, response);
                
            } else {
                
                // ✅ Lógica de Sucesso na Eliminação
                // Redirecionamento HTTP (sendRedirect) para a página principal ou de listagem.
                // Isso impede que o utilizador tente reenviar a eliminação se atualizar a página.
                response.sendRedirect("disciplina.jsp?parent=true");
            }

        } 
        // 🔚 Fim do try-with-resources: O PrintWriter 'out' é automaticamente fechado.
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
