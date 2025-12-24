package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger; 
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspFactory;
import util.Configura;

/**
 * @title getProperties
 * @description Servlet de Diagnóstico do Sistema. Apresenta todas as
 * propriedades da JVM (System Properties), informações sobre o
 * container (Tomcat) e a versão da base de dados.
 * @version 2.3 (Formatado para exibir carateres de line.separator)
 */
@WebServlet("/getProperties")
public class getProperties extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(getProperties.class.getName());
    
    // ---------------------------------------------

    public getProperties() {
        super();
    }
    
	/**
	 * Processa pedidos GET e gera um relatório de diagnóstico formatado em estilo retro/terminal.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
        
        LOGGER.info("Geração do relatório de propriedades do sistema iniciada.");

        // 2. Estilo Retro/Terminal (CRT Effect)
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>SYSTEM REPORT V.1.0 (RETRO)</title>");
        out.println("    <style>");
        out.println("        body {");
        out.println("            background-color: #000;");
        out.println("            color: #0F0; /* Verde brilhante */");
        out.println("            font-family: 'Monospace', 'Courier New', 'Courier', sans-serif;");
        out.println("            font-size: 14px;");
        out.println("            text-shadow: 0 0 5px #0F0; /* Efeito de 'glow' CRT */");
        out.println("            padding: 20px;");
        out.println("        }");
        out.println("        .terminal-block {");
        out.println("            border-left: 2px solid #0F0;");
        out.println("            padding-left: 10px;");
        out.println("            margin-top: 15px;");
        out.println("            margin-bottom: 15px;");
        out.println("        }");
        out.println("        h1 {");
        out.println("            color: #FF0; /* Amarelo/Âmbar para títulos */");
        out.println("            text-shadow: 0 0 10px #FF0;");
        out.println("            border-bottom: 1px dashed #FF0;");
        out.println("            padding-bottom: 5px;");
        out.println("            font-size: 18px;");
        out.println("        }");
        out.println("        .error {");
        out.println("            color: #F00; /* Vermelho para erros */");
        out.println("            text-shadow: 0 0 5px #F00;");
        out.println("        }");
        out.println("        a.retro-link {");
        out.println("            display: inline-block;");
        out.println("            color: #0FF; /* Cyan para links */");
        out.println("            text-decoration: none;");
        out.println("            text-shadow: 0 0 5px #0FF;");
        out.println("            margin-top: 20px;");
        out.println("            border: 1px solid #0FF;");
        out.println("            padding: 5px 10px;");
        out.println("            transition: background-color 0.1s;");
        out.println("        }");
        out.println("        a.retro-link:hover {");
        out.println("            background-color: rgba(0, 255, 255, 0.1);");
        out.println("        }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");

        // 3. Cabeçalho Retro
        out.println("<h1>::: J2EE SYSTEM DIAGNOSTIC REPORT :::</h1>");
        out.println("<div class='terminal-block'>");
		out.println("Developed with:<br>");
		out.println("Eclipse IDE for Enterprise Java and Web Developers<br>");
		out.println("(includes Incubating components)<br>");
		out.println("Version: 2022-09 (4.25.0)<br>");
		out.println("Build id: 20220908-1902<br><br>");
		out.println("Served at: " + request.getContextPath());
        out.println("</div>");
        
        out.println("<h1>::: JVM PROPERTIES (java.lang.System) :::</h1>");
        out.println("<pre class='terminal-block'>");

		Properties prop = System.getProperties();

		/*
         * ✅ CORREÇÃO: Iteramos manualmente e formatamos a propriedade 'line.separator'.
         * Isto exibe os carateres de controlo visíveis, em vez de uma quebra de linha.
         */
        
        out.println("-- listing properties --"); 

		// Iterar por todas as propriedades (ordenadas alfabeticamente para consistência)
        prop.stringPropertyNames().stream().sorted().forEach(key -> {
            String value = prop.getProperty(key);
            String displayValue = value;

            if ("line.separator".equals(key)) {
                // Substitui os carateres de controlo invisíveis pelas suas representações visíveis e descrições.
                displayValue = displayValue.replace("\r", "\\r (CR)") // CR = Carriage Return
                                           .replace("\n", "\\n (LF)"); // LF = Line Feed
            }
            
            // Output no formato key=value
            out.println(key + "=" + displayValue);
        });

        out.println("</pre>");

        // 4. Informações do Servidor
        out.println("<h1>::: SERVER/CONTAINER INFO :::</h1>");
        out.println("<div class='terminal-block'>");
		out.println("Tomcat Version = " + getServletContext().getServerInfo() + "<br>");
		out.println("Servlet Specification Version = " + getServletContext().getMajorVersion() + "."
				+ getServletContext().getMinorVersion() + "<br>");
        
        try {
            /*
             * Obtém a versão do JSP.
             */
		    out.println("JSP Specification Version = " + JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion() + "<br>");
        } catch (Exception e) {
             out.println("<span class='error'>JSP Specification Version = N/A (Factory Error)</span><br>");
             LOGGER.warning("Erro ao obter a versão do JSP: " + e.getMessage());
        }
        out.println("</div>");

		// 5. Informações da Base de Dados (Utiliza try-with-resources)
        out.println("<h1>::: DATABASE INFO (SQL) :::</h1>");
        
        try {

            /* * CONEXÃO SEGURA: try-with-resources garante que a Connection é fechada. */
		    try (Connection con = new Configura().getConnection()) {
                if (con != null) {
                    DatabaseMetaData meta = con.getMetaData();
                    out.println("<div class='terminal-block'>");
                    out.println("Database Product Name = " + meta.getDatabaseProductName() + "<br>");
                    out.println("Database Product Version = " + meta.getDatabaseProductVersion() + "<br>");
                    out.println("JDBC Driver Version = " + meta.getDriverVersion() + "<br>");
                    out.println("</div>");
                } else {
                    out.println("<div class='terminal-block error'>ERRO: Conexão BD é nula. Verificar a classe Manipula.</div>");
                    LOGGER.severe("A conexão de dados retornou NULL.");
                }
		    }
        } catch (SQLException e) {
            /*
             * TRATAMENTO DE ERRO: Apresenta a mensagem de erro no output retro.
             */
            out.println("<div class='terminal-block error'>SQL ERROR: Falha na conexão/metadata. Detalhes: " + e.getMessage() + "</div>");
            LOGGER.severe("Exceção SQL: " + e.getMessage());
        } catch (Exception e) {
            // Captura quaisquer outras exceções
            out.println("<div class='terminal-block error'>FATAL ERROR: Classe de BD inacessível. Detalhes: " + e.getMessage() + "</div>");
            LOGGER.severe("Erro Fatal de Instanciação: " + e.getMessage());
        }

        // 6. Etiqueta para voltar à página anterior
        out.println("<p>");
        out.println("PROCESS COMPLETED.");
        out.println("</p>");
        out.println("<a href='javascript:history.back()' class='retro-link'>[🔙] VOLTAR AO MENU ANTERIOR</a>");


        // 7. Fechar HTML
		out.println("</body>");
        out.println("</html>");
		out.close();
	}

	/**
	 * Encaminha pedidos POST para o método doGet.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}