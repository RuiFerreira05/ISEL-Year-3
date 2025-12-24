package web;

import java.io.OutputStream; // ✍️ Usado para escrever os dados diretamente no corpo da resposta HTTP
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // 🏷️ Anotação para mapear o Servlet
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder; // ⚙️ Usado para codificar mensagens de status em URLs
import java.nio.charset.StandardCharsets; // 🧱 Para garantir a codificação UTF-8
import java.lang.reflect.Method; // 🔍 Elemento chave para a Reflection
import java.lang.reflect.InvocationTargetException; // 💥 Exceção para capturar erros no método invocado

@WebServlet("/Download") // 🌐 Mapeia o Servlet para o URL '/Download'
public class DownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final String RETURN_PAGE = "exportar.jsp"; // 🔙 Página para onde o utilizador é redirecionado em caso de ERRO ou SUCESSO (tentativa)

    // 🔄 Helper: Unificado para redirecionar com mensagens de Status (Sucesso ou Erro)
    private void redirectWithStatus(HttpServletResponse response, String message, String statusType) throws IOException {
        try {
            // 🛑 É crucial limpar qualquer output parcial ANTES de redirecionar
            response.reset(); 
            // 🔐 Codifica a mensagem para que possa ser transportada na URL sem problemas
            String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
            
            // Redireciona usando GET para que a JSP possa ler os parâmetros statusType e statusMessage
            response.sendRedirect(RETURN_PAGE + "?statusType=" + statusType + "&statusMessage=" + encodedMsg);
            
        } catch (IllegalStateException ise) {
            // ❌ Em caso de erro irrecuperável (ex: resposta já *committed* ou fechada)
            System.err.println("❌ Não foi possível redirecionar o status. Resposta comprometida. Detalhe: " + ise.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // 1. Receber Parâmetros
        String tableName = request.getParameter("tableName");
        System.out.println("Tabela Selecionada:"+tableName); // 🖨️ Log de depuração
        String format = request.getParameter("format");
        
        // Validação básica
        if (tableName == null || tableName.trim().isEmpty() || format == null || format.trim().isEmpty()) {
            String msg = "🛑 Erro: Tabela ou formato não especificados. Reveja os parâmetros do formulário.";
            redirectWithStatus(response, msg, "error"); // 🛠️ Utiliza o novo método para avisar o cliente
            return;
        }

        // 🧹 Limpa o nome da tabela (remove possíveis metadados como '[metadados]')
        String nomeTabela = tableName.replaceAll("\\[.*?\\]", "").trim(); 
        String formatoExportacao = format.toLowerCase();
        String tipoConteudo; 
        
        // --- REFLECTION SETUP ---
        // 🔄 Capitaliza a primeira letra do formato (ex: 'csv' -> 'Csv')
        String capitalizedFormat = formatoExportacao.substring(0, 1).toUpperCase() + formatoExportacao.substring(1);
        // 🛠️ Constrói o nome do método esperado na classe DataTransfer (ex: 'exportToCsv')
        String methodName = "exportTo" + capitalizedFormat;
        
        // 3. Determinação do Content-Type
        switch (formatoExportacao) {// tipo generico que força o download "application/octet-stream"
            case "sql": 		tipoConteudo = "application/sql"; 	break; // 📜 Tipo MIME para SQL
            case "csv": 		tipoConteudo = "text/csv"; 			break; // 📊 Tipo MIME para CSV
            case "xml": 		tipoConteudo = "application/xml"; 	break; // 🏗️ Tipo MIME para XML
            case "json": 	tipoConteudo = "application/json"; 	break; // 🧩 Tipo MIME para JSON
            case "pdf": 		tipoConteudo = "application/pdf"; 	break; // 📎 Tipo MIME para PDF
            case "html": 	tipoConteudo = "text/html"; 			break; // 🌐 Tipo MIME para HTML
            case "txt": 		tipoConteudo = "text/plain"; 		break; // 📝 Tipo MIME para Texto Simples
            default:
                // 🚫 Formato não reconhecido
                String msg = "🚫 Formato de exportação inválido: " + format + ". Formato esperado: " + methodName;
                redirectWithStatus(response, msg, "error"); // 🛠️ Notifica o erro
                return;
        }

        // 4. Configuração dos Headers de Resposta
        response.reset(); // 🗑️ Limpa os cabeçalhos e o buffer existentes (crucial antes de enviar o ficheiro)
        response.setContentType(tipoConteudo); // 📦 Define o tipo de ficheiro que está a ser enviado
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // ✍️ Define a codificação
        
        String fileName = tableName.trim() + "." + formatoExportacao;
        
        // 🎯 LÓGICA CONDICIONAL: Content-Disposition
        // o formato HTML é exibidos (inline).
        String down = "attachment"; // ⬇️ Padrão: Força o browser a FAZER o download (attachment)
        if ("html".equals(formatoExportacao)) 
        			down = "inline"; // 👁️ Exceção: Exibe o conteúdo diretamente no browser (inline)
        // 📎 Define o nome de ficheiro sugerido ao browser e a disposição
        response.setHeader("Content-Disposition", down+"; filename=\"" + fileName + "\"");

        // 5. Execução do Método de Exportação (Reflection)
        // 🔓 Obtém o stream de saída para escrever o ficheiro diretamente na resposta
        try (OutputStream os = response.getOutputStream()) {
            
            Class<?> dataTransferClass = util.DataTransfer.class;
            // 🧱 Define os tipos de parâmetros esperados no método ('String nomeTabela', 'OutputStream')
            Class<?>[] paramTypes = new Class<?>[]{String.class, OutputStream.class};

            // 🔍 Encontra o método na classe DataTransfer (ex: exportToCsv)
            Method exportMethod = dataTransferClass.getMethod(methodName, paramTypes);
            
            // 🚀 Executa o método estático (null como primeiro argumento) e passa os parâmetros
            exportMethod.invoke(null, nomeTabela, os); 
            
            os.flush(); // 💾 Garante que todos os dados são enviados

            // 🟢 DOWNLOAD BEM SUCEDIDO
            // ⚠️ NOTA: É IMPOSSÍVEL enviar uma mensagem de sucesso por Redirect AQUI.
            // A resposta (ficheiro) já está a ser enviada ao browser, impedindo o Redirect.
            // O sucesso é indicado pelo início do download no cliente.


        } catch (NoSuchMethodException e) {
            // ❌ Tratamento de erro se o método de exportação não existir
            String detailedMsg = String.format("❌ Falha de Configuração (Reflection): O método '%s' não foi encontrado na classe DataTransfer. Detalhe: %s",
                                                methodName, e.getMessage());
            System.err.println(detailedMsg);
            redirectWithStatus(response, detailedMsg, "error"); // 🛠️ Notifica o erro
            
        } catch (InvocationTargetException e) {
            // 💥 Tratamento de erro se o método de exportação lançar uma exceção
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            String detailedMsg = String.format("💥 Erro de Execução (%s): Falha ao gerar %s.%s. Causa: %s. Detalhe Técnico: %s",
                                                formatoExportacao.toUpperCase(), nomeTabela, formatoExportacao, cause.getClass().getSimpleName(), cause.getMessage());
            System.err.println(detailedMsg);
            redirectWithStatus(response, detailedMsg, "error"); // 🛠️ Notifica o erro
            
        } catch (Exception e) {
            // ⛔ Tratamento de qualquer outra exceção genérica (ex: IO Exception no stream)
            String detailedMsg = String.format("❌ Erro no Servlet: Falha ao iniciar o download de %s.%s. Detalhe: %s",
                                                nomeTabela, formatoExportacao, e.getMessage());
            System.err.println(detailedMsg);
            redirectWithStatus(response, detailedMsg, "error"); // 🛠️ Notifica o erro
        }
    }
    
    // 🚫 Método `doGet` para Bloquear Acesso Direto (por GET)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 🛑 Define o status como 405 (Método Não Permitido)
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        // 📢 Envia uma mensagem de erro ao cliente
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "A exportação de dados requer o método POST.");
    }
}