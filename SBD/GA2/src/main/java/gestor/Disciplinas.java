package gestor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.Configura;
import util.IOx;
import util.DataFormatter;

/**
 * Classe responsável pela gestão (CRUD) da entidade Disciplina.
 * ⚠️ AVISO: Esta classe não adere ao Princípio da Responsabilidade Única (SRP).
 * Utiliza métodos estáticos que podem dificultar a manutenção e testes.
 */
public final class Disciplinas {
    
    // ----------------------------------------------------------------------
    // MÉTODOS DE NEGÓCIO (API Pública)
    // ----------------------------------------------------------------------
    
    /**
     * Pergunta o código e a designação da disciplina ao utilizador
     * e insere a nova disciplina na tabela 'disciplina', usando PreparedStatement.
     * @return true se a inserção for bem-sucedida, false caso contrário.
     */
    public static boolean nova() {
        Configura configuradorBD = new Configura();
        
        // 1. Obtenção de dados do utilizador.
        String codigo = Input.getCCodDis();
        if(codigo==null ||codigo.isEmpty()) {
        		System.out.println("❌ Código inválido!");
        		return false;
        }
        String designacao = Input.getCDsgDis();
        if(designacao==null ||designacao.isEmpty()) {
    			System.out.println("❌ Designação inválida!");
    			return false;
        }
        
        // SQL SEGURO: Usando marcadores de posição (?)
        String sqlInsert = "INSERT INTO disciplina (codigo, designacao) VALUES (?, ?)";

        // Try-with-resources para Connection e PreparedStatement
        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlInsert)) {
            
            // 2. Vincula os valores aos marcadores de posição
            preparacao.setString(1, codigo.toUpperCase());
            preparacao.setString(2, designacao);
            
            // 3. Execução da inserção
            if (1 == preparacao.executeUpdate()) {
                System.out.println("✅ Disciplina inserida com sucesso.");
                apresentarFicha(codigo);
                return true;
            } else {
            		System.out.println("❌ Erro inesperado ao inserir a disciplina."); 
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao inserir a disciplina. (Código e/ou designação duplicados).");
            System.err.println("----- SQLException de Inserção -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Código:  " + e.getErrorCode());
        }
        return false;
    }
    
    /**
     * Pede ao utilizador um código de disciplina e invoca o método 
     * para apresentar a ficha da disciplina correspondente.
     */
    public static void procurarCodigo() {
        // 1. Solicita o código.
        String codigo = Input.getCCodDis();
        
        // 2. Apresenta a ficha.
        apresentarFicha(codigo);
    }

    /**
     * Pede ao utilizador uma expressão de pesquisa e lista todas as disciplinas
     * que correspondem ao padrão na coluna 'designacao'.
     * Inclui validação de input para detetar tentativas de SQL Injection.
     */
    public static void procurarPorDesignacao() {
        System.out.println("🔎 Indique a expressão para pesquisar pela designação (usa % ou _):");
        String designacao = Input.getCDsgDis();
        if(designacao==null ||designacao.isEmpty()) {
    			System.out.println("❌ Designação inválida!");
    			return;
        }
        apresentarTabela(designacao);
      }
    /**
     * Apresenta uma tabela formatada com as disciplinas cuja designação corresponda 
     * ao padrão de procura.
     * * @param Designacao O padrão de designação a procurar (e.g., "%Matemática%").
     */
    public static void apresentarTabela(String Designacao) {
        // Chama o novo método que contém toda a lógica e formatação.
        String tabelaFormatada = gerarTabelaTXT(Designacao);
        
        // Imprime o resultado formatado diretamente no Console.
        System.out.println(tabelaFormatada);
    }

    /**
     * Gera e retorna uma tabela HTML com as disciplinas cuja designação corresponda 
     * ao padrão de procura.
     * O cálculo do layout é feito pelo navegador (CSS).
     * @param Designacao O padrão de designação a procurar (e.g., "%Sistemas%").
     * @return Uma String contendo a tabela HTML ou uma mensagem de aviso/erro HTML.
     */
    public static String gerarTabelaHTML(String Designacao) {
        Configura configuradorBD = new Configura(); 
        StringBuilder html = new StringBuilder();
        
        String sqlSelect = "SELECT codigo, designacao FROM disciplina WHERE designacao LIKE ? ORDER BY codigo";
        
        int rowCount = 0;
        List<String[]> dataRows = new ArrayList<>(); 

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) { 

            // 1. Execução Segura (SQL Injection Prevention)
            preparacao.setString(1, Designacao);
            
            try (ResultSet rs = preparacao.executeQuery()) { 
                
                // 2. Recolher dados
                while (rs.next()) {
                    String codigo = rs.getString("codigo").trim();
                    String designacao = rs.getString("designacao").trim();
                    dataRows.add(new String[]{codigo, designacao});
                    rowCount++;
                }
            } 
            
            // 3. Gerar a saída HTML

            // Caso 1: Sem resultados
            if (rowCount == 0) {
                return "<p class=\"aviso\">⚠️ Aviso: Não foram encontradas disciplinas com a designação <strong>'" + Designacao + "'</strong>.</p>";
            }

            // Caso 2: Resultados encontrados
            
            // Título e Abertura da Tabela
            html.append("<h3>Disciplinas Encontradas (").append(rowCount).append(")</h3>");
            // Adiciona uma classe para styling via CSS (ex: .disciplinas-table)
            html.append("<table class=\"disciplinas-table\">"); 

            // Cabeçalho (<thead>)
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th align=\"right\">CÓDIGO</th>");
            html.append("<th>DESIGNAÇÃO</th>");
            html.append("</tr>");
            html.append("</thead>");

            // Corpo da Tabela (<tbody>)
            html.append("<tbody>");
            for (String[] row : dataRows) {
                String codigo = row[0];
                String designacao = row[1];

                html.append("<tr>");
                // Sugestão: alinhar o código à direita para melhor leitura em tabelas
                html.append("<td style=\"text-align: right;\">").append(codigo).append("</td>"); 
                html.append("<td>").append(designacao).append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody>");
            html.append("</table>");
            
            return html.toString();

        } catch (SQLException e) {
            // Regista o erro completo no log do servidor
            System.err.println("❌ Erro ao listar as disciplinas.");
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Vendor:  " + e.getErrorCode());
            
            // Retorna uma mensagem de erro simples para o utilizador web
            return "<p class=\"erro\">❌ Erro interno ao processar a consulta de disciplinas. Por favor, tente novamente.</p>";
        }
    }
    
    /**
     * Gera uma tabela formatada em modo texto (ASCII/Box-Drawing Characters) 
     * com as disciplinas cuja designação corresponda ao padrão de procura.
     * O cálculo das larguras é dinâmico, ajustando-se aos dados mais longos.
     *
     * @param Designacao O padrão de designação a procurar (e.g., "%Matemática%").
     * @return String contendo a tabela formatada, ou uma mensagem de aviso/erro.
     */
    public static String gerarTabelaTXT(String Designacao) {
        Configura configuradorBD = new Configura();
        
        // StringBuilder para coletar todo o output em vez de imprimir no console
        StringBuilder output = new StringBuilder(); 
        
        String sqlSelect = "SELECT codigo, designacao FROM disciplina WHERE designacao LIKE ? ORDER BY codigo";
                           
        // --- Constantes de Largura e Preenchimento ---
        final int PADDING = 2; 
        final int W_MIN_CODE = 8; // Mínimo para " CÓDIGO"
        final int W_MIN_DSG = 30; // Mínimo para " DESIGNAÇÃO"
        
        int maxWCode = W_MIN_CODE;
        int maxWDesignacao = W_MIN_DSG;
        int rowCount = 0;

        List<String[]> dataRows = new ArrayList<>(); 

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) { 

            preparacao.setString(1, Designacao);
            
            try (ResultSet rs = preparacao.executeQuery()) { 

                // 1. PRIMEIRA ITERAÇÃO: Recolher dados e calcular larguras dinâmicas
                while (rs.next()) {
                    String codigo = rs.getString("codigo").trim();
                    String designacao = rs.getString("designacao").trim();
                    
                    dataRows.add(new String[]{codigo, designacao});
                    rowCount++;

                    if (codigo.length() > maxWCode) 
                    		maxWCode = codigo.length();
                    if (designacao.length() > maxWDesignacao) 
                    		maxWDesignacao = designacao.length();
                }
            } 

            if (rowCount == 0) {
                // Retorna a mensagem de aviso em vez de imprimir
                return "⚠️ Aviso: Não foram encontradas disciplinas com a designação '" + Designacao + "'.";
            }

            // 2. CALCULAR LARGURAS FINAIS E BORDAS
            
            final int W_CODE = maxWCode + PADDING;
            final int W_DSG = maxWDesignacao + PADDING;
            final int W_INNER = W_CODE + W_DSG + 3; // +3 para as 3 barras verticais "║ ║ ║"
            
            // Definições de Linhas e Bordas
            final String LINE_CODE = "═".repeat(W_CODE);   
            final String LINE_DSG = "═".repeat(W_DSG);     
            final String SIMPLE_CODE = "─".repeat(W_CODE); 
            final String SIMPLE_DSG = "─".repeat(W_DSG);   
            
            final String TITLE_TEXT = "Disciplinas Encontradas (" + rowCount + ")";
            
            // Bordas da Tabela
            final String BORDER_TOP = "╔" + LINE_CODE + "═" + LINE_DSG + "╗";
            final String HEADER_SEP = "╠" + LINE_CODE + "╦" + LINE_DSG + "╣";
            final String BORDER_BOT = "╚" + LINE_CODE + "╩" + LINE_DSG + "╝";
            
            // Separador entre Linhas de Dados
            final String ROW_SEP = "╟" + SIMPLE_CODE + "╫" + SIMPLE_DSG + "╢"; 

            // 3. GERAÇÃO DA TABELA (Substituir System.out.println por output.append)

            output.append("\n").append(BORDER_TOP).append("\n");
            output.append("║").append(DataFormatter.padCenter(TITLE_TEXT, W_INNER - 2)).append("║").append("\n");
            output.append(HEADER_SEP).append("\n");

            String headerLine = "║" 
                              + DataFormatter.padCenter("CÓDIGO", W_CODE) 
                              + "║" 
                              + DataFormatter.padCenter("DESIGNAÇÃO", W_DSG) 
                              + "║";
            output.append(headerLine).append("\n");
            output.append(HEADER_SEP.replace("╦", "╬")).append("\n"); 

            // 4. Desenhar as linhas de dados com separador simples
            for (int i = 0; i < rowCount; i++) {
                String[] row = dataRows.get(i);
                String codigo = row[0];
                String designacao = row[1];

                // Preenchimento à esquerda para o código (alinhamento à direita)
                String codeRight = DataFormatter.padRight(codigo,W_CODE-1)+" ";
                // Preenchimento à direita para a designação (alinhamento à esquerda)
                String dsgLeft = " "+DataFormatter.padLeft(designacao, W_DSG-1);
                
                String rowLine = "║"
                               + codeRight
                               + "║"
                               + dsgLeft
                               + "║";
                
                output.append(rowLine).append("\n");
                
                // Imprime o novo separador ╟╫╢
                if (i < rowCount - 1) {
                    output.append(ROW_SEP).append("\n");
                }
            }

            output.append(BORDER_BOT).append("\n");
            
            return output.toString();

        } catch (SQLException e) {
            // Retorna a mensagem de erro simples para o utilizador web
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("Message:  " + e.getMessage());
            return "❌ Erro ao listar as disciplinas. Detalhes de erro no log do sistema.";
        }
    }
    /**
     * Pede ao utilizador o código e da disciplina que vai ser apagada,
     * usando PreparedStatement.
     * @return true se a atualização for bem-sucedida, false caso contrário.
     */
    public static boolean apagar() {
        Configura configuradorBD = new Configura();
        
        String codigo = Input.getCCodDis();
        apresentarFicha(codigo);
        
        // SQL SEGURO: Usando marcadores de posição (?)
        String sqlUpdate = "DELETE FROM disciplina WHERE codigo = ?";
        
        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlUpdate)) {
            
            // 1. Vincula o valor
            preparacao.setString(1, codigo);
            
            // 2. Execução da atualização
            if (1 == preparacao.executeUpdate()) {
                System.out.println("✅ Disciplina apagada com sucesso.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao apagar a disciplina.");
            System.err.println("----- SQLException -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Menssage:  " + e.getMessage());
            System.err.println("Código:  " + e.getErrorCode());
        }
        return false;
    }
    
    /**
     * Pede ao utilizador o código e a nova designação, procedendo à atualização,
     * usando PreparedStatement.
     * @return true se a atualização for bem-sucedida, false caso contrário.
     */
    public static boolean alterarDesignacao() {
        Configura configuradorBD = new Configura();
        
        String codigo = Input.getCCodDis();
        if(codigo==null ||codigo.isEmpty()) {
    			System.out.println("❌ Código inválido!");
    			return false;
        }
        apresentarFicha(codigo);
        
        String designacao = Input.getCDsgDis();
        if(designacao==null ||designacao.isEmpty()) {
			System.out.println("❌ Designação inválida!");
			return false;
        }
        // SQL SEGURO: Usando marcadores de posição (?)
        String sqlUpdate = "UPDATE disciplina SET designacao = ? WHERE codigo = ?";
        
        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlUpdate)) {
            
            // 1. Vincula os valores
            preparacao.setString(1, designacao);
            preparacao.setString(2, codigo);
            
            // 2. Execução da atualização
            if (1 == preparacao.executeUpdate()) {
                System.out.println("✅ Designação alterada com sucesso.");
                apresentarFicha(codigo);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao alterar a designação da disciplina: '"+designacao+"'.");
            System.err.println("----- SQLException -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Vendor:  " + e.getErrorCode());
        }
        return false;
    }
    
   /**
     * Apresenta a ficha detalhada de uma disciplina formatada em HTML, 
     * incluindo CSS no corpo do HTML gerado.
     */
    public static String gerarFichaHTML(String codigo) {
    		Configura configuradorBD=new Configura(); 

        // 1. Definição do SQL
        String sqlSelect = "SELECT codigo, designacao FROM disciplina WHERE codigo = ?";

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {

            preparacao.setString(1, codigo);

            try (ResultSet rs = preparacao.executeQuery()) {
                
                // 2. Início do HTML e Estilos
                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append("<!DOCTYPE html>\n");
                htmlBuilder.append("<html lang=\"pt\">\n");
                htmlBuilder.append("<head>\n");
                htmlBuilder.append("<meta charset=\"UTF-8\">\n");
                htmlBuilder.append("<title>Ficha da Disciplina</title>\n");
                
                // CSS para a formatação da ficha
                htmlBuilder.append("<style>\n");
                htmlBuilder.append("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n");
                htmlBuilder.append(".disciplina-ficha { max-width: 500px; margin: 20px auto; border: 2px solid #0056b3; border-radius: 8px; background-color: #ffffff; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }\n");
                htmlBuilder.append(".disciplina-header { background-color: #0056b3; color: white; padding: 10px 0; border-radius: 6px 6px 0 0; text-align: center; }\n");
                htmlBuilder.append(".disciplina-header h2 { margin: 0; font-size: 1.4em; }\n");
                htmlBuilder.append(".disciplina-body { padding: 15px; }\n");
                htmlBuilder.append(".disciplina-tabela { width: 100%; border-collapse: collapse; }\n");
                htmlBuilder.append(".disciplina-tabela tr:nth-child(even) { background-color: #f9f9f9; }\n");
                htmlBuilder.append(".disciplina-tabela th, .disciplina-tabela td { padding: 12px; border-bottom: 1px solid #eee; }\n");
                htmlBuilder.append(".disciplina-tabela th { background-color: #eef; text-align: left; color: #333; width: 150px; font-weight: bold; }\n");
                htmlBuilder.append(".valor { font-size: 1.1em; }\n");
                htmlBuilder.append(".align-right { text-align: right; font-weight: bold; }\n");
                htmlBuilder.append(".erro-mensagem { color: #cc0000; font-weight: bold; padding: 15px; border: 1px solid #ffcccc; background-color: #ffeeee; border-radius: 4px; max-width: 500px; margin: 20px auto; }\n");
                htmlBuilder.append("</style>\n");
                htmlBuilder.append("</head>\n");
                htmlBuilder.append("<body>\n");

                if (rs != null && rs.next()) {

                    // 3. Obter Dados
                    String cod = rs.getString("codigo").trim();
                    String dsg = rs.getString("designacao").trim();

                    // 4. Montar o HTML da Ficha
                    htmlBuilder.append("<div class=\"disciplina-ficha\">");
                    
                    // Título (Cabeçalho da Ficha)
                    htmlBuilder.append("<div class=\"disciplina-header\">");
                    htmlBuilder.append("<h2>📚 Ficha da Disciplina 📝</h2>");
                    htmlBuilder.append("</div>");
                    
                    htmlBuilder.append("<div class=\"disciplina-body\">");
                    
                    // Tabela de Dados
                    htmlBuilder.append("<table class=\"disciplina-tabela\">");
                    
                    // Linha 1: Código
                    htmlBuilder.append("<tr>");
                    htmlBuilder.append("<th>🆔 Código:</th>");
                    htmlBuilder.append("<td class=\"valor align-right\">")
                               .append(cod)
                               .append("</td>");
                    htmlBuilder.append("</tr>");
                    
                    // Linha 2: Designação
                    htmlBuilder.append("<tr>");
                    htmlBuilder.append("<th>🏷️ Designação:</th>");
                    htmlBuilder.append("<td class=\"valor\">")
                               .append(dsg)
                               .append("</td>");
                    htmlBuilder.append("</tr>");
                    
                    htmlBuilder.append("</table>");
                    htmlBuilder.append("</div>"); // .disciplina-body
                    htmlBuilder.append("</div>"); // .disciplina-ficha

                } else {
                    // 5. Caso a disciplina não seja encontrada
                    htmlBuilder.append("<p class=\"erro-mensagem\">❌ Não foi encontrada a disciplina com o código: <strong>")
                               .append(codigo)
                               .append("</strong>.</p>");
                }
                
                htmlBuilder.append("</body>\n");
                htmlBuilder.append("</html>");
                return htmlBuilder.toString();

            }

        } catch (SQLException e) {
            // 6. Tratamento de Erro de Consulta
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:   " + e.getMessage());
            
            // Retorna uma mensagem de erro formatada em HTML
            return "<body><p class=\"erro-mensagem\">❌ Erro ao consultar a ficha da disciplina. Tente novamente mais tarde.</p></body></html>";
        }
    }
    
    /**
     * Gera a ficha detalhada de uma disciplina formatada de forma dinâmica, 
     * utilizando caracteres de desenho de caixa para um visual "retro/terminal".
     * * @param codigo Código da disciplina a procurar.
     * @return String contendo a ficha formatada ou uma mensagem de aviso/erro.
     */
    public static String gerarFichaTXT(String codigo) {
        Configura configuradorBD = new Configura();
        StringBuilder output = new StringBuilder(); // Objeto para coletar a saída formatada

        String sqlSelect = "SELECT codigo, designacao FROM disciplina WHERE codigo = ?";
        
        // --- Constantes de Largura ---
        final int W_LABEL 		= 18;     	// Largura fixa para as etiquetas
        final int PADDING 		= 2;      	// Padding interno para os dados
        final int W_MIN_DATA 	= 25;  		// Largura mínima legível para a coluna de dados

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {

            preparacao.setString(1, codigo);

            try (ResultSet rs = preparacao.executeQuery()) {
                if (rs != null && rs.next()) {
                    
                    // Obter e formatar dados como Strings finais
                    String cod = rs.getString("codigo").trim();
                    String dsg = rs.getString("designacao").trim(); 
                    
                    // 1. CALCULAR LARGURAS DINÂMICAS
                    int w_data = Math.max(cod.length(), dsg.length());
                    w_data = Math.max(w_data + PADDING, W_MIN_DATA); 

                    // 2. CALCULAR LARGURAS TOTAIS E BORDAS
                    final int W_TOTAL = W_LABEL + w_data + 3; 
                    final int W_INNER = W_TOTAL - 2;          
                    
                    final String BORDER_LINE = "═".repeat(W_INNER);
                    final String BORDER_TOP = "╔" + BORDER_LINE + "╗";
                    final String BORDER_SEP = "╠" + BORDER_LINE + "╣";
                    final String BORDER_BOT = "╚" + BORDER_LINE + "╝";

                    // 3. CONSTRUIR A FICHA (Usando output.append)

                    // Desenhar Título e Separador
                    output.append(BORDER_TOP).append("\n");
                    String titleText = "📚 Ficha da Disciplina 📝";
                    // Assumindo DataFormatter.fill() para preenchimento
                    String centeredTitle = DataFormatter.padCenter(titleText, W_INNER); 
                    output.append("║").append(centeredTitle).append("║").append("\n"); 
                    output.append(BORDER_SEP).append("\n"); 
                    
                    String codigoLabel 	= DataFormatter.padRight("Código", W_LABEL-4)+" 🆔:";
                    String codigoData 	= " "+DataFormatter.padLeft(cod, w_data-1);                    
                    output.append("║ ") 
                        .append(codigoLabel) 
                        .append(codigoData)
                        .append("║").append("\n"); 

                    String designacaoLabel 	= DataFormatter.padRight("Designação", W_LABEL-4)+" 🏷️:";
                    String designacaoData 	= " "+DataFormatter.padLeft(dsg, w_data-2);
                                    
                    output.append("║ ") 
                        .append(designacaoLabel)
                        .append(designacaoData) 
                        .append(" ║").append("\n");
                                    
                    output.append(BORDER_BOT).append("\n");
                    
                    // Retorna a String final
                    return output.toString();
                
                } else {
                    // Caso a disciplina não seja encontrada
                    return " ❌ Não foi encontrada a disciplina com o código: " + codigo + ".";
                }
            }

        } catch (SQLException e) {
            // Retorna a mensagem de erro amigável para a interface web
            System.err.println("----- SQLException de Consulta Ficha -----");
            System.err.println("Message:  " + e.getMessage());
            return "❌ Erro ao consultar a ficha da disciplina. Detalhes de erro no log do sistema.";
        } 
    }
    
    /**
     * Apresenta a ficha de uma disciplina formatada de forma dinâmica .
     * @param configuradorBD Objeto de configuração para obter a conexão (Parâmetro mantido
     * por compatibilidade, mas o código não o utiliza diretamente).
     * @param codigo Código da disciplina a procurar.
     */
    public static void apresentarFicha(String codigo) {
        System.out.println(gerarFichaTXT(codigo));
    }
    // ----------------------------------------------------------------------
    // MÉTODO MAIN E MENU DE TESTE
    // ----------------------------------------------------------------------
    
    /**
     * Método principal (Entry Point) da classe, implementando um menu de teste.
     * @param args Argumentos de linha de comandos (não utilizados).
     */
    public static void main(String[] args) {
        
        System.out.println("=============================================");
        System.out.println("        🧪 Menu - Disciplina       ");
        System.out.println("=============================================");
        char opcao;
        do {
            System.out.println("\n📚 > Disciplinas:");
            System.out.println("a. ✨ Inserir Nova Disciplina");
            System.out.println("b. 🆔 Procurar Disciplina por Código");
            System.out.println("c. 🔍 Procurar Disciplinas por Designação");
            System.out.println("d. 🔄 Alterar Designação de Disciplina");
            System.out.println("e. ❌ Apagar Disciplina");
            System.out.println("z. 🚪 Sair");
            System.out.println("---------------------------------------------");
            
            System.out.println("Opção: "); // Ajustado para Console.write
            opcao = Character.toLowerCase(IOx.inChar());

            switch (opcao) {
                case 'a':
                    System.out.println("\n--- ✨ INSERIR NOVA DISCIPLINA ---");
                    nova(); 
                    break;
                case 'b':
                    System.out.println("\n--- 🆔 PROCURAR POR CÓDIGO ---");
                    procurarCodigo();
                    break;
                case 'c':
                    System.out.println("\n--- 🔍 PROCURAR POR DESIGNAÇÃO ---");
                    procurarPorDesignacao();
                    break;
                case 'd':
                    System.out.println("\n--- 🔄 ALTERAR DESIGNAÇÃO ---");
                    alterarDesignacao();
                    break;
                case 'e':
                		System.out.println("\n--- ❌ APAGAR DISCIPLINA ---");
                    apagar();
                    break;
                case 'z':
                    System.out.println("\n👋 Saiu do menu!");
                    break;
                default:
                    System.out.println("❌ Opção inválida. Por favor, tente outra vez.");
            }
            
            if (opcao != 'z') {
                 System.out.println("\n[Pressione ENTER para continuar...]");
                 IOx.in();
            }
            
        } while (opcao != 'z');
    }
}