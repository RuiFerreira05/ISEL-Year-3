package web;

import util.DataTransfer; 							// 📥 Classe responsável pela lógica de importação de dados
import java.net.URLEncoder; 							// ⚙️ Usado para codificar URLs para mensagens de status
import java.nio.charset.StandardCharsets; 			// 🧱 Para garantir a codificação UTF-8
import java.lang.reflect.Method; 					// 🔍 Elemento chave para a Reflection
import java.lang.reflect.InvocationTargetException; 	// 💥 Exceção para erros no método invocado
import java.io.InputStream; 							// 🔓 Usado para ler o conteúdo do ficheiro carregado
import java.io.IOException;

import java.util.Map; 								// 🗺️ Usado para o mapeamento de tipos MIME
import java.util.Optional; 							// ✨ Usado para o retorno do formato determinado, indicando a sua presença

import jakarta.servlet.annotation.MultipartConfig; 	// 📎 Anotação crucial para uploads de ficheiros (multipart/form-data)
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part; // 📄 Representa uma parte do formulário multipart (neste caso, o ficheiro)

@WebServlet("/Upload") // 🌐 Mapeia o Servlet para o URL '/Upload'
@MultipartConfig(
    fileSizeThreshold 	= 1024 * 1024, 			// 1 MB: Tamanho mínimo do ficheiro para ser mantido em memória antes de ser escrito no disco
    maxFileSize 			= 1024 * 1024 * 145,  	// 145 MB: Tamanho máximo permitido para um único ficheiro
    maxRequestSize 		= 1024 * 1024 * 150 		// 150 MB: Tamanho máximo da totalidade do pedido (ficheiro + outros dados)
)
public class UploadServlet extends HttpServlet { 

    private static final long serialVersionUID = 1L;
    private final String RETURN_PAGE = "importar.jsp"; // 🔙 Página para onde o utilizador é redirecionado após o processamento

    // 🛑 MAPA DE TIPOS MIME E EXTENSÕES PARA O NOME DO FORMATO
    private static final Map<String, String> MIME_TO_FORMAT_MAP = Map.of(
        // Tipos MIME comuns
        "application/sql", "sql", 
        "text/x-sql", "sql",
        "text/csv", "csv", 
        "application/vnd.ms-excel", "csv", // CSV no Windows/MS Office
        "application/xml", "xml", 
        "text/xml", "xml",
        "application/json", "json",
        "application/javascript", "json", // Algumas configs para JSON (não ideal, mas suportado)
        "text/plain", "sql" // Fallback para TXT/PLANO (assumindo ser SQL)
    );
    
    // 🔄 Helper: Unificado para redirecionar com mensagens de Status
    private void redirectWithStatus(HttpServletResponse response, String message, String statusType) throws IOException {
        try {
            response.reset(); // 🗑️ Limpa o output anterior para garantir que o redirecionamento funciona
            String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
            // ➡️ Redireciona com parâmetros GET para que a JSP possa exibir a mensagem
            response.sendRedirect(RETURN_PAGE + "?statusType=" + statusType + "&statusMessage=" + encodedMsg);
            
        } catch (IllegalStateException ise) {
            // ❌ Tratamento de erro se a resposta já tiver sido enviada/commitada
            System.err.println("❌ Não foi possível redirecionar o status. Detalhe: " + ise.getMessage());
        }
    }

    /**
     * Tenta determinar o formato (sql, csv, xml, json) pelo MIME ou pela extensão.
     * @param filePart A parte do ficheiro do pedido HTTP.
     * @return Um Optional contendo o formato ("sql", "csv", etc.) se for reconhecido.
     */
    private Optional<String> determineFormat(Part filePart) {
        String mimeType = filePart.getContentType();
        String fileName = filePart.getSubmittedFileName();

        // 1. Tentar mapear pelo Tipo MIME (prioritário, mas fornecido pelo cliente)
        if (mimeType != null && MIME_TO_FORMAT_MAP.containsKey(mimeType.toLowerCase())) {
            return Optional.of(MIME_TO_FORMAT_MAP.get(mimeType.toLowerCase()));
        }

        // 2. Tentar mapear pela Extensão (Fallback)
        if (fileName != null && fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            
            // Mapeamento de Extensões
            if (extension.equals("sql")) return Optional.of("sql");
            if (extension.equals("csv")) return Optional.of("csv");
            if (extension.equals("xml")) return Optional.of("xml");
            if (extension.equals("json")) return Optional.of("json");
        }

        return Optional.empty(); // Formato não reconhecido
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        try {
    			// ⚠️ A chamada a getPart é o ponto de falha para SizeExceededException (se o ficheiro for demasiado grande)
            Part filePart = request.getPart("fileUpload"); 
            
            if (filePart == null || filePart.getSize() == 0) {
                 redirectWithStatus(response, "🛑 Erro: Nenhum ficheiro foi selecionado ou o ficheiro está vazio.", "error");
                 return;
            }
            
	    		// ⚠️ Nota: A leitura dos parâmetros pode falhar/retornar null se o limite de RequestSize for atingido.
			String tableName = request.getParameter("tableName");
			System.out.println("Tabela Selecionada: "+tableName); // 🖨️ Log de depuração
			// 🧹 Limpa o nome da tabela (remove possíveis metadados)
			String nomeTabela = tableName != null ? tableName.replaceAll("\\[.*?\\]", "").trim() : "";
			if (nomeTabela.isEmpty()) {
				redirectWithStatus(response, "🛑 Erro: Tabela alvo não especificada.", "error");
				return;
			}
		
            // 🛑 DETERMINAR O FORMATO E FILTRAR FICHEIROS INVÁLIDOS 🛑
            Optional<String> formatOpt = determineFormat(filePart);
            
            if (!formatOpt.isPresent()) {
                // 🚫 Formato não reconhecido (Nem por MIME nem por extensão)
                String fileName = filePart.getSubmittedFileName() != null ? filePart.getSubmittedFileName() : "Ficheiro desconhecido";
                String mimeType = filePart.getContentType() != null ? filePart.getContentType() : "N/A";

                String msg = String.format("🚫 Ficheiro Bloqueado: Não foi possível determinar o formato (MIME: %s, Ficheiro: %s). Apenas são suportados ficheiros .sql, .csv, .xml ou .json.",
                                            mimeType, fileName);
                redirectWithStatus(response, msg, "error");
                System.err.println(msg);
                return; 
            }
            
            String formatoImportacao = formatOpt.get(); // O formato determinado (ex: "xml")

            // 3. Execução do Método de Importação (Reflection)
            // 🔄 Capitaliza o formato (ex: 'xml' -> 'Xml')
            String capitalizedFormat = formatoImportacao.substring(0, 1).toUpperCase() + formatoImportacao.substring(1);
            // 🛠️ Constrói o nome do método esperado (ex: importFromXml)
            String methodName = "importFrom" + capitalizedFormat; 
            
            // 🔓 Obtém o stream de entrada do ficheiro
            try (InputStream input = filePart.getInputStream()) {
                
                Class<?> dataTransferClass = DataTransfer.class;
                // 🧱 Define os tipos de parâmetros esperados ('String nomeTabela', 'InputStream')
                Class<?>[] paramTypes = new Class<?>[]{String.class, InputStream.class};

                // 🔍 Encontra o método na classe DataTransfer
                Method importMethod = dataTransferClass.getMethod(methodName, paramTypes);
                
                // 🚀 Executa o método estático e converte o resultado para boolean
                boolean importSuccess = (Boolean) importMethod.invoke(null, nomeTabela, input);
                
                // 📢 Prepara as mensagens de status
                String successMsg = String.format("🎉 Sucesso! A importação de dados **%s** para a tabela '%s' terminou.", 
                        formatoImportacao.toUpperCase(), nomeTabela);
                String errorMsg = String.format("❌ Erro ao importar dados **%s** para a tabela '%s'. Reveja os logs do servidor.",
                                                formatoImportacao.toUpperCase(), nomeTabela);
                
                // 🏁 Redireciona com base no resultado do método (true/false)
                redirectWithStatus(response, importSuccess ? successMsg : errorMsg, importSuccess ? "success" : "error");

            } catch (NoSuchMethodException e) {
                // ❌ Tratamento de erro se o método de importação não existir (falha de configuração)
                String detailedMsg = String.format("❌ Falha de Configuração (Reflection): O método '%s(String, InputStream)' não foi encontrado em DataTransfer.", methodName);
                System.err.println(detailedMsg + " Detalhe: " + e.getMessage());
                redirectWithStatus(response, detailedMsg, "error");
                
            } catch (InvocationTargetException e) {
                // 💥 Tratamento de erro se o método de importação lançar uma exceção (falha de negócio/dados)
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                String detailedMsg = String.format("💥 Erro de Execução (%s): Falha na importação para %s. Causa: %s.",
                                                    formatoImportacao.toUpperCase(), nomeTabela, cause.getClass().getSimpleName());
                System.err.println(detailedMsg + " Detalhe Técnico: " + cause.getMessage());
                redirectWithStatus(response, detailedMsg, "error");
                
            } 
           } catch (Exception e) {
            // ⛔ Tratamento de qualquer outra exceção genérica (ex: limite de tamanho excedido, falha ao ler partes)
            String detailedMsg = "❌ Erro grave ao processar o upload: " + e.getMessage();
            System.err.println(detailedMsg);
            redirectWithStatus(response, detailedMsg, "error");
        }
    }
    
    // 🚫 Método `doGet` para Bloquear Acesso Direto (por GET)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 🛑 Define o status como 405 (Método Não Permitido)
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        // 📢 Envia uma mensagem de erro ao cliente
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "A importação de dados requer o método POST.");
    }
}