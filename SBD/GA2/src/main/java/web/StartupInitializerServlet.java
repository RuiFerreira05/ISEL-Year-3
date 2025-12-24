package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

// Este servlet nunca é chamado por ninguem, chama simplesmente o iniciador das classes 
// que precisem ser iniciadas quando são usadas em contexto Web
// O valor 1 garante que este Servlet é carregado e inicializado primeiro
@WebServlet(
    urlPatterns = {"/app-initializer"},
    loadOnStartup = 1 
)
public class StartupInitializerServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    /**
     * 🚀 Este método é chamado imediatamente no arranque da aplicação.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        // 1. Obtém o caminho real do sistema de ficheiros para a raiz da aplicação Web
        String realPath = getServletContext().getRealPath("/");
        
        // 2. Chama o método de inicialização da sua classe utilitária
        util.Name.initialize(realPath);
        
        System.out.println("✅ Inicialização automática da classe Name concluída.");
        
        // Não é necessário implementar doGet() ou doPost(), pois o único propósito é o init().
    }
}