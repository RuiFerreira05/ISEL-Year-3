package prototype;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/directoryInfo")
public class DirectoryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public  static String rootPath="";

    @Override
    public void init() throws ServletException {
        // O método init() é o local ideal para obter o contexto.
        ServletContext context = this.getServletContext();
        
        // Chama o método de utilidade:
        rootPath = context.getRealPath("/");
        
        System.out.println("Caminho Raiz: " + rootPath);
    }

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        
        // 1. Diretoria de Trabalho da JVM (CWD)
        // É a pasta a partir de onde o servidor (ex: Tomcat/bin) foi iniciado.
        String jvmWorkingDirectory = System.getProperty("user.dir");
        
        // 2. Diretoria Raiz da Aplicação Web (Caminho Útil)
        // É o caminho real do sistema de ficheiros para a raiz da sua aplicação (o diretório do .war).
        // Este é o caminho mais relevante para um Servidor Web.
        String webAppRootPath = request.getServletContext().getRealPath("/");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Diretorias do Servidor Web</title></head><body>");
            out.println("<h1>Diretorias de Execução no Servidor</h1>");
            
            out.println("<h2>1. Diretoria de Trabalho da JVM (System Property)</h2>");
            out.println("<p><b>user.dir:</b> " + jvmWorkingDirectory + "</p>");
            out.println("<p><i>Isto é onde o processo Java do servidor (ex: Tomcat) foi iniciado.</i></p>");
            
            out.println("<hr>");
            
            out.println("<h2>2. Diretoria Raiz da Aplicação Web (Context Path)</h2>");
            out.println("<p><b>ServletContext.getRealPath(\"/\"):</b> " + webAppRootPath + "</p>");
            out.println("<p><i>Isto é o caminho real do seu projeto no servidor (ex: pasta 'webapps/seuapp').</i></p>");
            
            out.println("</body></html>");
        }
    }
}