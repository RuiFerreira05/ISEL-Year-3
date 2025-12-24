package disciplina;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/save")
public class save extends HttpServlet {

	private static final long serialVersionUID = 1156573442522918800L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ⚙️ Configuração da Resposta
        response.setContentType("text/html");
        
        // 🔒 Bloco try-with-resources: Garante o fecho automático e seguro do PrintWriter
        try (PrintWriter out = response.getWriter()) {
            
            // 🔎 Recolha e Preparação dos Dados
            // Cria um objeto 'disc' (d) com o Código e Designacao submetidos.
            Disc d = new Disc(request.getParameter("codigo"), request.getParameter("designacao"));
            d.print(); // 💡 Linha de debug/registo (logging) para ver o que vai ser acrescentado.
            
            // 🔄 Lógica de Gravação (DAO - Data Access Object)
            // Tenta guardar o novo registo na base de dados.
            if (DiscDAO.save(d) == 1) {
                // ✅ Lógica de Sucesso
                // Envia um script JavaScript para exibir uma mensagem de sucesso no browser.
                out.print("<script>alert('Record saved successfully!');</script>");
                
            } else {
                // ❌ Lógica de Falha
                out.println("<h3>Sorry! 🚫 Unable to save record...</h3>");
            }
            
            // ➡️ Reencaminhamento Interno (include)
            // Inclui o conteúdo do JSP (disciplina.jsp) na resposta, 
            // seja para mostrar o formulário novamente 
            // ou para apresentar a lista após a operação. 
            // A mensagem de sucesso/erro (seja o script ou o <h2>) será mostrada *acima* do conteúdo do JSP.
            request.getRequestDispatcher("disciplina.jsp").include(request, response);

        }// 🔚 Fim do try-with-resources: O PrintWriter 'out' é automaticamente fechado aqui,
         // garantindo que não há fuga de recursos, mesmo que ocorram exceções.
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
