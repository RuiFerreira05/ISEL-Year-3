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

@WebServlet("/edit")
public class edit extends HttpServlet {

	private static final long serialVersionUID = -4617797030215810878L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ⚙️ Configuração da Resposta
        response.setContentType("text/html");
        
        // 🔒 Bloco try-with-resources: Garante o fecho automático e seguro do PrintWriter
        try (PrintWriter out = response.getWriter()) {
            
            // 🔎 Recolha e Preparação dos Dados (Dados Novos)
            // Cria um objeto 'disc' (d) com o código e designação submetidos no formulário
            // Nota: Se o formulário tiver sido submetido via POST, o ideal seria usar o método doPost().
            Disc d = new Disc(request.getParameter("codigo"), request.getParameter("designacao"));
            d.print(); // 💡 Linha de debug/registo (logging) para ver o que vai ser atualizado.
            
            // 🏷️ Recolha e Preparação dos Dados (Dados Antigos)
            // Cria um objeto 'disc' (o) com os dados originais (geralmente passados em campos ocultos - hidden fields)
            Disc o = new Disc(request.getParameter("codigoOld"), request.getParameter("designacaoOld"));
            o.print(); // 💡 Linha de debug/registo (logging) para ver o registo original.

            // 🔄 Lógica de Atualização (DAO - Data Access Object)
            // Tenta atualizar o registo na base de dados, comparando 'o' (original) com 'd' (novo).
            if (DiscDAO.update(d, o) != 1) {
                
                // ❌ Lógica de Falha na Atualização
                out.println("<h3>Sorry! Unable to update record...</h3>");
                
                // ⚠️ Codificação de URL (URL Encoding)
                // É essencial codificar os valores dos parâmetros para evitar que caracteres especiais
                // (como espaços ou acentos) causem problemas na URL.
                String codigoEncoded = URLEncoder.encode(o.getCodigo(), StandardCharsets.UTF_8.toString());
                String designacaoEncoded = URLEncoder.encode(o.getDesignacao(), StandardCharsets.UTF_8.toString());
                
                // Constrói a URL para retornar à página de edição com os dados originais
                String urlErro = "disciplina.jsp?codigo=" + codigoEncoded + "&designacao=" + designacaoEncoded;
                
                // ➡️ Reencaminhamento Interno (include)
                // Inclui o conteúdo do JSP (disc.jsp) na resposta atual do Servlet.
                // Isso permite mostrar a mensagem de erro acima ('out.println...') e manter a URL do Servlet no browser.
                request.getRequestDispatcher(urlErro).include(request, response);
                
            } else {
                
                // ✅ Lógica de Sucesso na Atualização
                // Redirecionamento HTTP (sendRedirect) para a página principal ou de listagem.
                // O 'parent=true' é um parâmetro de controlo.
                response.sendRedirect("disciplina.jsp?parent=true");
            }

        } 
        // 🔚 Fim do try-with-resources: O PrintWriter 'out' é automaticamente fechado aqui,
        // garantindo que não há fuga de recursos, mesmo que ocorram exceções.
    } // As exceções não tratadas (ServletException, IOException) são propagadas para o contentor (container) do Servlet.
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
