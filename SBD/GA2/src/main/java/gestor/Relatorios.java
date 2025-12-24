package gestor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import util.Configura;
import util.IOx;
import util.DataFormatter;
import util.Name;

/**
 * Classe responsável pela geração de documentos e estatísticas académicas.
 */
public final class Relatorios {
	
	/** Condição de aprovação utilizada nas queries. */
	final static String condAprov = " nota >= 9.50 AND nota <= 20 ";
	
	// ----------------------------------------------------------------------
    // MÉTODOS DE NEGÓCIO (API Pública - Com Configura)
    // ----------------------------------------------------------------------
	/**
	 * Gera a estatística global de aprovação/reprovação (ARP) e retorna-a
	 * como uma String.
	 *
	 * @return Uma String contendo a estatística ARP completa e as mensagens de status.
	 */
	public static String obterARP() {
	    
	    // StringWriter é um buffer de memória que captura a saída como uma String.
	    StringWriter sw = new StringWriter();
	    
	    // StringBuilder para acumular as mensagens de status, se necessário, 
	    // mas vamos depender principalmente do StringWriter/PrintWriter.
	    // Para replicar as mensagens de início/fim:
	    // PrintWriter envolve o StringWriter, permitindo que os métodos de escrita
	    // (como os usados dentro de gerarARP) funcionem como se estivessem a escrever 
	    // num ficheiro ou na consola. O 'true' ativa o auto-flush.
	    try (PrintWriter writer =new PrintWriter(sw, true)) {
	        // Chama o método original, passando o novo PrintWriter de memória.
	        // Assumimos que 'gerarARP' trata internamente qualquer exceção do tipo SQLException.
	        gerarARP(writer);
	    }
	    // Retorna o que foi escrito, incluindo a mensagem de erro.
	    return sw.toString();
	}	
	
	
	/**
     * Gera e apresenta a estatística global de aprovação/reprovação (ARP) 
     * diretamente na consola (System.out).
     *
     * O PrintWriter é usado APENAS com flush() no final, para não fechar o System.out.
     */
    public static void apresentarARP() {
        
        // Cria o PrintWriter que aponta para a consola (System.out).
        // O argumento 'true' ativa o auto-flush, o que é útil.
    		// Não se pode fechar o output!
        System.out.println("\n--- Início da Geração da Estatística Global (ARP) ---");
        OutputStreamWriter output=new OutputStreamWriter(System.out);
        PrintWriter consoleWriter = new PrintWriter(output, true);
        // Chama o método original, passando o PrintWriter da consola.
        // Assumimos que gerarARP trata internamente qualquer SQLException (como no seu código original).
        gerarARP(consoleWriter);
        System.out.println("--- Fim da Geração da Estatística Global ---");
    }

	/**
	 * Gera uma estatística de todos os alunos aprovados e reprovados, 
	 * com a respetiva percentagem de aprovação, formatada em modo texto, 
	 * escrevendo-a diretamente num PrintWriter.
	 * * @param out O PrintWriter para onde a tabela formatada com os dados será escrita.
	 */
	public static void gerarARP(PrintWriter out) {
	    // A classe Configura, estatistica, condAprov e DataFormatter.fill() devem estar acessíveis
	    Configura configuradorBD = new Configura();

	    // --- Lógica de Cálculo (Mantida) ---
	    // 1. Conta Aprovados (Nota entre 10 e 20)
	    // Nota: A linha abaixo assume a existência da variável 'condAprov' e do método 'estatistica'
	    BigDecimal nAprov = estatistica(configuradorBD, null, condAprov, "COUNT(*)");
	    
	    // 2. Conta Reprovados (Nota fora da condição de aprovação E ano de inscrição <= ano atual)
	    BigDecimal nReprov = estatistica(configuradorBD, null, 
	                                " NOT (" + condAprov + ") AND ano <= " + Year.now().getValue(), 
	                                "COUNT(*)");
	                                
	    BigDecimal nTotal = nAprov.add(nReprov);
	    String sPer = "-"; // Default
	    
	    // Calcula a percentagem
	    if (BigDecimal.ZERO.compareTo(nTotal) != 0) {
	        // Divide com 3 casas decimais e arredonda para cálculo da percentagem
	        BigDecimal nPer = nAprov.divide(nTotal, 3, RoundingMode.HALF_UP);
	        sPer = NumberFormat.getPercentInstance().format(nPer);
	    }
	    
	    // --- Lógica de Formatação (Refatorada para PrintWriter.println()) ---

	    // Larguras das Colunas (Ajustadas do original)
	    final int COL_APROV = 17; // Inclui a margem interna
	    final int COL_REPROV = 18; 
	    final int COL_TAXA = 14; 
	    final int W_INNER = COL_APROV + COL_REPROV + COL_TAXA + 3; // +3 para as 3 barras verticais

	    // Linhas Horizontais
	    final String LINE_TOTAL = "═".repeat(W_INNER - 1);
	    final String LINE_APROV = "═".repeat(COL_APROV);
	    final String LINE_REPROV = "═".repeat(COL_REPROV);
	    final String LINE_TAXA = "═".repeat(COL_TAXA);

	    // Bordas
	    final String BORDER_TOP = "╔" + LINE_TOTAL + "╗";
	    final String HEADER_SEP = "╠" + LINE_APROV + "╦" + LINE_REPROV + "╦" + LINE_TAXA + "╣";
	    final String BODY_SEP = "╠" + LINE_APROV + "╬" + LINE_REPROV + "╬" + LINE_TAXA + "╣";
	    final String BORDER_BOT = "╚" + LINE_APROV + "╩" + LINE_REPROV + "╩" + LINE_TAXA + "╝";

	    // Título da Tabela
	    out.println(BORDER_TOP);
	    out.println("║" + DataFormatter.padCenter("Estatística Global de Avaliações", W_INNER - 1) + "║");
	    out.println(HEADER_SEP); 

	    // Cabeçalho da Tabela
	    String headerLine = "║" + DataFormatter.padCenter("Aprovações", COL_APROV)
	                      + "║" + DataFormatter.padCenter("Reprovações", COL_REPROV) 
	                      + "║" + DataFormatter.padCenter("Taxa (%)", COL_TAXA) + "║";
	    out.println(headerLine); 
	    out.println(BODY_SEP);
	    
	    // Linha dos Dados (Alinhamento à Direita com String.format)
	    String dataLine = "║" + DataFormatter.padRight(nAprov.toString(),COL_APROV-1)+" "
	                    + "║" + DataFormatter.padRight(nReprov.toString(),COL_REPROV-1)+" " 
	                    + "║" + DataFormatter.padRight(sPer,COL_TAXA-1) + " ║";
	                    
	    out.println(dataLine);
	                        
	    out.println(BORDER_BOT);
	}
	
	/**
	 * Gera uma estatística de todos os alunos aprovados e reprovados, 
	 * com a respetiva percentagem de aprovação, em formato HTML com estilos internos CSS.
	 *
	 * @return String contendo o HTML formatado da tabela ou uma mensagem de erro em HTML.
	 */
	public static String gerarARPHTML() {
	    // Configura e condAprov (condição de aprovação) são assumidos estarem disponíveis.
	    Configura configuradorBD = new Configura();
	    // Exemplo: deve ser a constante real definida no seu projeto.
	    final String condAprov = "nota >= 9.5"; 
	    
	    StringBuilder htmlOutput = new StringBuilder();
	    BigDecimal nAprov = BigDecimal.ZERO;
	    BigDecimal nReprov = BigDecimal.ZERO;
	    String sPer = "-";

	    try {
	        // 1. Conta Aprovados (Nota entre 10 e 20)
	        // O método estatistica(configuradorBD, null, condicao, "COUNT(*)") é uma dependência externa
	        // e assume-se que trata da ligação à BD e exceções SQL.
	        nAprov = estatistica(configuradorBD, null, condAprov, "COUNT(*)"); 
	        
	        // 2. Conta Reprovados (Nota fora da condição de aprovação E ano de inscrição <= ano atual)
	        nReprov = estatistica(configuradorBD, null, 
	                              " NOT (" + condAprov + ") AND ano <= " + Year.now().getValue(), 
	                              "COUNT(*)");
	                                
	        // 3. Calcula o Total e a Percentagem
	        BigDecimal nTotal = nAprov.add(nReprov);
	        
	        if (BigDecimal.ZERO.compareTo(nTotal) != 0) {
	            BigDecimal nPer = nAprov.divide(nTotal, 3, RoundingMode.HALF_UP);
	            // NumberFormat.getPercentInstance() formata o valor como percentagem (e.g., "62,5%")
	            sPer = NumberFormat.getPercentInstance().format(nPer); 
	        }

	        // --- GERAÇÃO DO HTML COM ESTILOS INTERNOS ---
	        
	        // 1. Contêiner principal para o layout
	        htmlOutput.append("<div style=\"font-family: Arial, sans-serif; margin: 20px 0;\">");
	        
	        // 2. Estilos CSS Internos
	        htmlOutput.append("<style>");
	        htmlOutput.append("/* Estilo para a tabela de estatísticas (aprovados/reprovados/taxa) */\n");
	        htmlOutput.append(".tabela-arp { border-collapse: collapse; width: 60%; min-width: 400px; text-align: center; margin: 0 auto; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }");
	        htmlOutput.append("/* Estilo para o título (caption) da tabela */\n");
	        htmlOutput.append(".tabela-arp caption { font-size: 1.4em; font-weight: bold; margin-bottom: 10px; color: #007bff; padding: 10px; border-bottom: 2px solid #ddd; }");
	        htmlOutput.append("/* Estilo para o cabeçalho da tabela */\n");
	        htmlOutput.append(".tabela-arp th { background-color: #007bff; color: white; padding: 12px 15px; border: 1px solid #0056b3; }");
	        htmlOutput.append("/* Estilo para as células de dados */\n");
	        htmlOutput.append(".tabela-arp td { border: 1px solid #ddd; padding: 10px 15px; font-weight: bold; font-size: 1.1em; }");
	        htmlOutput.append("/* Cor de fundo para as células de dados (linhas pares) */\n");
	        htmlOutput.append(".tabela-arp tbody tr:nth-child(even) { background-color: #f7f7ff; }");
	        htmlOutput.append("/* Estilo específico para a célula da taxa de aprovação, destacando a cor */\n");
	        htmlOutput.append(".tabela-arp td.taxa { color: #28a745; font-size: 1.3em; }");
	        htmlOutput.append("</style>");
	        
	        // 3. Tabela
	        htmlOutput.append("<table class=\"tabela-arp\">");
	        
	        // Título da Tabela
	        htmlOutput.append("<caption>Estatística Global de Avaliações</caption>");
	        
	        // Cabeçalho (Com Emojis para clareza)
	        htmlOutput.append("<thead><tr>");
	        htmlOutput.append("<th>✅ Aprovações</th>");
	        htmlOutput.append("<th>❌ Reprovações</th>");
	        htmlOutput.append("<th>📈 Taxa (%)</th>");
	        htmlOutput.append("</tr></thead>");
	        
	        // Corpo da Tabela (Linha única com os valores)
	        htmlOutput.append("<tbody><tr>");
	        
	        // Célula 1: Aprovados
	        htmlOutput.append("<td>").append(nAprov.toString()).append("</td>");
	        
	        // Célula 2: Reprovados
	        htmlOutput.append("<td>").append(nReprov.toString()).append("</td>");
	        
	        // Célula 3: Taxa de Aprovação (com classe CSS para destaque)
	        htmlOutput.append("<td class=\"taxa\">").append(sPer).append("</td>");
	        
	        htmlOutput.append("</tr></tbody>");
	        htmlOutput.append("</table>");
	        htmlOutput.append("</div>"); // Fecha o contêiner principal

	        return htmlOutput.toString();

	    } catch (Exception e) {
	        // Retorna uma mensagem de erro em HTML
	        System.err.println("----- Erro na Estatística ARP -----");
	        System.err.println("Message:  " + e.getMessage());
	        return "<p style=\"color: red; font-weight: bold; padding: 10px; font-family: Arial, sans-serif;\">❌ Erro ao calcular a Estatística Global de Avaliações. Detalhes de erro no log do sistema.</p>";
	    }
	}
	
	/**
	 * Gera uma tabela em HTML com estilos internos (CSS) apresentando as notas 
	 * mínima, máxima e média (MMM) das disciplinas aprovadas.
	 * ⚠️ NOTA: Este método utiliza propositadamente java.sql.Statement simples.
	 *
	 * @return String contendo o HTML formatado da tabela ou uma mensagem de erro em HTML.
	 */
	public static String gerarMMMHTML() {
	    // A classe Configura, DataFormatter e a constante condAprov devem estar acessíveis.
	    Configura configuradorBD = new Configura();
	    StringBuilder htmlOutput = new StringBuilder();
	    
	    // Query (segura pois não usa input de utilizador) 
	    String sqlSelect = "SELECT d.designacao, MIN(i.nota), MAX(i.nota), AVG(i.nota) "
	            + "FROM inscricao i JOIN disciplina d ON i.codigo = d.codigo "
	            + "WHERE " + condAprov + " GROUP BY d.designacao ORDER BY d.designacao";

	    // Início da String de Output HTML
	    htmlOutput.append("<div style=\"font-family: Arial, sans-serif; margin: 20px 0;\">");
	    
	    // Título da Tabela
	    htmlOutput.append("<h4 style=\"color: #333; border-bottom: 2px solid #ddd; padding-bottom: 5px; margin-bottom: 15px;\">")
	              .append("📊 Estatística de Notas por Disciplina (Aprovadas)")
	              .append("</h4>");

	    // Definição dos Estilos CSS internos (para a tabela)
	    htmlOutput.append("<style>");
	    htmlOutput.append("/* Estilo para a tabela principal */\n");
	    htmlOutput.append(".tabela-mmm { border-collapse: collapse; width: 100%; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }");
	    htmlOutput.append("/* Estilo para as células do cabeçalho */\n");
	    htmlOutput.append(".tabela-mmm th { background-color: #3498db; color: white; padding: 12px 15px; border: 1px solid #2980b9; }");
	    htmlOutput.append("/* Alinhamento para as colunas de notas (Min, Max, Média) */\n");
	    htmlOutput.append(".tabela-mmm th.nota { text-align: center; width: 12%; }");
	    htmlOutput.append("/* Estilo para as células do corpo da tabela */\n");
	    htmlOutput.append(".tabela-mmm td { border: 1px solid #ddd; padding: 10px 15px; }");
	    htmlOutput.append("/* Estilo para a coluna da designação */\n");
	    htmlOutput.append(".tabela-mmm td.designacao { font-weight: bold; }");
	    htmlOutput.append("/* Alinhamento para as células de notas */\n");
	    htmlOutput.append(".tabela-mmm td.nota-valor { text-align: center; font-family: 'Consolas', monospace; }");
	    htmlOutput.append("/* Efeito 'Zebra' para as linhas (Fundo cinzento claro) */\n");
	    htmlOutput.append(".tabela-mmm tbody tr:nth-child(even) { background-color: #f7f7f7; }");
	    htmlOutput.append("</style>");
	    
	    htmlOutput.append("<table class=\"tabela-mmm\">");

	    try (Connection conexao = configuradorBD.getConnection();
	         Statement stm = conexao.createStatement();
	         ResultSet rs = stm.executeQuery(sqlSelect)){
	        
	        // 3. Geração do Cabeçalho
	        htmlOutput.append("<thead><tr>");
	        htmlOutput.append("<th style=\"text-align: left;\">Disciplina</th>");
	        htmlOutput.append("<th class=\"nota\">Mín.</th>");
	        htmlOutput.append("<th class=\"nota\">Máx.</th>");
	        htmlOutput.append("<th class=\"nota\">Média</th>");
	        htmlOutput.append("</tr></thead>");
	        
	        htmlOutput.append("<tbody>");

	        boolean dataFound = false;
	        while (rs.next()) {
	            dataFound = true;
	            String designacao = rs.getString(1).trim();
	            
	            // Assume-se que DataFormatter.NotaToString(BigDecimal) formata corretamente (ex: "12,5")
	            String sMin = DataFormatter.NotaToString(rs.getBigDecimal(2)); 
	            String sMax = DataFormatter.NotaToString(rs.getBigDecimal(3));
	            String sMed = DataFormatter.NotaToString(rs.getBigDecimal(4));
	            
	            // Geração da Linha de Dados
	            htmlOutput.append("<tr>");
	            // Coluna 1: Designação (Alinhada à Esquerda)
	            htmlOutput.append("<td class=\"designacao\">").append(designacao).append("</td>");
	            // Colunas 2, 3, 4: Notas (Alinhadas ao Centro/Direita via CSS)
	            htmlOutput.append("<td class=\"nota-valor\">").append(sMin).append("</td>");
	            htmlOutput.append("<td class=\"nota-valor\">").append(sMax).append("</td>");
	            htmlOutput.append("<td class=\"nota-valor\">").append(sMed).append("</td>");
	            htmlOutput.append("</tr>");
	        }
	        
	        htmlOutput.append("</tbody></table>");
	        htmlOutput.append("</div>"); // Fecha o contêiner principal

	        if (!dataFound) {
	            return "<p style=\"color: orange; font-weight: bold; padding: 10px;\">⚠️ Aviso: Não foram encontradas notas de aprovação para calcular as estatísticas.</p>";
	        }
	        
	        return htmlOutput.toString();
	        
	    } catch (SQLException e) {
	        // Retorna a mensagem de erro em HTML
	        System.err.println("----- SQLException de MMM -----");
	        System.err.println("Message:  " + e.getMessage());
	        return "<p style=\"color: red; font-weight: bold; padding: 10px;\">❌ Erro ao listar Mínima, Máxima e Média das notas. Detalhes de erro no log do sistema.</p>";
	    }
	}
	/**
	 * Gera e retorna uma tabela formatada em modo texto (ASCII/Box-Drawing Characters) 
	 * com as notas mínima, máxima e média (MMM) das disciplinas como uma String.
	 * @return A String contendo a tabela de estatísticas.
	 * @throws SQLException Se ocorrer um erro ao aceder à base de dados.
	 */
	public static String obterMMM() {
	    // 1. Cria um StringWriter para capturar a saída de texto.
	    StringWriter sw = new StringWriter();
	    // 2. Envolve o StringWriter num PrintWriter. 
	    //    Este é o objeto 'writer' que o método original espera.
	    
	    try (PrintWriter writer = new PrintWriter(sw,true)) {
	        
	        // 3. Chama o método original, passando o novo PrintWriter.
	        //    Todo o output 'writer.println()' será escrito no StringWriter 'sw'.
	        gerarMMM(writer);
	    } 
	    return sw.toString(); // 4.Retorna o que foi escrito.
	}
	
	/**
     * Gera e escreve uma tabela formatada em modo texto (ASCII/Box-Drawing Characters) 
     * com as notas mínima, máxima e média (MMM) das disciplinas para um PrintWriter.
     *
     * @param writer O objeto PrintWriter para onde a tabela será escrita.
     * @throws SQLException Se ocorrer um erro ao aceder à base de dados.
     */
    // Modificações: Tipo de retorno 'void' e novo parâmetro 'PrintWriter writer'.
    public static void gerarMMM(PrintWriter writer) {
        Configura configuradorBD = new Configura();
        
        // Query (segura pois não usa input de utilizador)
        // Substitua 'condAprov' pela constante real ou defina-a no escopo se necessário
        // final String condAprov = "i.nota >= 9.5"; // Exemplo, deve ser a sua constante real.

        String sqlSelect = "SELECT d.designacao, MIN(i.nota), MAX(i.nota), AVG(i.nota) "
                + "FROM inscricao i JOIN disciplina d ON i.codigo = d.codigo "
                + "GROUP BY d.designacao ORDER BY d.designacao";

        // Larguras das Colunas Fixas e Variáveis
        final int COL_NOTA = 6;
        final int W_MIN_DESIGNACAO = 30; // Largura mínima para o campo designação
        
        int maxWDesignacao = W_MIN_DESIGNACAO;
        int rowCount = 0;

        // Lista para armazenar dados (para calcular largura e desenhar depois)
        List<String[]> dataRows = new ArrayList<>(); 

        // Try-with-resources para Connection, Statement e ResultSet
        try (Connection conexao = configuradorBD.getConnection();
             Statement stm = conexao.createStatement();
             ResultSet rs = stm.executeQuery(sqlSelect)){
            
            // 1. PRIMEIRA ITERAÇÃO: Recolher dados e calcular largura dinâmica
            while (rs.next()) {
                String designacao = rs.getString(1).trim();
                // Usa DataFormatter.NotaToString para obter as notas formatadas (ex: "12,5")
                // Assumimos a existência da classe DataFormatter com o método NotaToString
                String sMin = DataFormatter.NotaToString(rs.getBigDecimal(2)); 
                String sMax = DataFormatter.NotaToString(rs.getBigDecimal(3));
                String sMed = DataFormatter.NotaToString(rs.getBigDecimal(4));
                
                dataRows.add(new String[]{designacao, sMin, sMax, sMed});
                rowCount++;

                // Ajuste de largura da designação
                if (designacao.length() > maxWDesignacao) maxWDesignacao = designacao.length();
            }

            if (rowCount == 0) {
                // Em vez de 'return String', escreve no writer
                writer.println("⚠️ Aviso: Não foram encontradas notas de aprovação para calcular as estatísticas.");
                return;
            }

            // 2. CALCULAR LARGURAS FINAIS E BORDAS
            
            // Larguras Finais (dado + padding para a designação)
            final int PADDING = 2;
            final int W_DESIGNACAO = maxWDesignacao + PADDING; 
            
            // Largura Total Interna
            // W_DESIGNACAO + 3 x W_NOTA + 4 separadores internos (|)
            final int W_INNER = W_DESIGNACAO + (3 * COL_NOTA) + 5; 
            
            // Definições de Linhas
            final String L_DSG = "═".repeat(W_DESIGNACAO);
            final String L_NOTA = "═".repeat(COL_NOTA + 1); 
            final String S_DSG = "─".repeat(W_DESIGNACAO);
            final String S_NOTA = "─".repeat(COL_NOTA + 1); 
            final String TITLE_TEXT = "Estatística de Notas por Disciplina (" + rowCount + ")";
            
            // Bordas da Tabela
            final String BORDER_TOP = "╔" + L_DSG + "═" + L_NOTA + "═" + L_NOTA + "═" + L_NOTA + "╗";
            final String HEADER_SEP = "╠" + L_DSG + "╦" + L_NOTA + "╦" + L_NOTA + "╦" + L_NOTA + "╣";
            final String ROW_SEP    = "╟" + S_DSG + "╫" + S_NOTA + "╫" + S_NOTA + "╫" + S_NOTA + "╢"; 
            final String BORDER_BOT = "╚" + L_DSG + "╩" + L_NOTA + "╩" + L_NOTA + "╩" + L_NOTA + "╝";          
            // Separador entre Linhas de Dados
            
            // 3. APRESENTAÇÃO DA TABELA 

            // Substituir output.append() por writer.println() ou writer.print()
            writer.println();
            // Título centrado
            writer.println(BORDER_TOP);
            writer.println("║"+DataFormatter.padCenter(TITLE_TEXT, W_INNER+1)+"║");
            writer.println(HEADER_SEP);

            // Cabeçalho da Tabela
            String headerLine = "║" + DataFormatter.padCenter(" Disciplina", W_DESIGNACAO) 
                              + "║" + DataFormatter.padCenter("Mín.",COL_NOTA+1)
                              + "║" + DataFormatter.padCenter("Máx.",COL_NOTA+1) 
                              + "║" + DataFormatter.padCenter("Média",COL_NOTA+1) + "║";
            writer.println(headerLine);
            writer.println(HEADER_SEP.replace("╦","╬")); 

            // 4. Desenhar as linhas de dados
            for (int i = 0; i < rowCount; i++) {
                String[] row = dataRows.get(i);
                String designacao = row[0];
                String sMin = row[1];
                String sMax = row[2];
                String sMed = row[3];
                
                // Designação Alinhada à Esquerda (fill() faz isso)
                String dsgLeft = DataFormatter.fill(" " + designacao, W_DESIGNACAO, " ");
                
                // Notas Alinhadas à Direita (String.format faz isso)
                String rowLine = "║"
                               + dsgLeft
                               + "║" + String.format("%" + (COL_NOTA) + "s", sMin)+ " "
                               + "║" + String.format("%" + (COL_NOTA) + "s", sMax)+ " "
                               + "║" + String.format("%" + (COL_NOTA) + "s", sMed)+ " "
                               + "║";
                
                writer.println(rowLine);
                
                // Imprime o separador
                if (i < rowCount - 1) {
                    writer.println(ROW_SEP);
                }
            }

            writer.println(BORDER_BOT);
            writer.println();
            
            // Não há 'return output.toString()'

        } catch (SQLException e) {
            // Em vez de retornar uma String de erro, a exceção é propagada (throws SQLException)
            // ou pode escrever a mensagem de erro no log e no writer, se preferir o comportamento original:
            System.err.println("-----SQLException de MMM-----");
            System.err.println("Message:  " + e.getMessage());
            writer.println("❌ Erro ao listar Mínima, Máxima e Média das notas. Detalhes de erro no log do sistema.");
            // Re-lançar a exceção se quiser forçar o tratamento no chamador:
            // throw e; 
        } finally {
            // Opcional: Descarregar (flush) o buffer do writer para garantir que tudo é escrito imediatamente
            writer.flush();
        }
    }
    
    /**
     * Usa o método gerarMMM para gerar e imprimir a tabela diretamente
     * na saída padrão (System.out).
     *
     * @throws SQLException Se ocorrer um erro de acesso à base de dados.
     */
    public static void apresentarMMM() {
        // É recomendável envolver System.out num OutputStreamWriter para 
        // garantir a codificação correta (UTF-8) para os caracteres Box-Drawing.
        System.out.println("--- Estatísticas de Notas ---");
        // Cria um PrintWriter que escreve para System.out.
        PrintWriter consoleWriter = new PrintWriter(new OutputStreamWriter(System.out), true);
        // Chama o método principal, passando o PrintWriter da consola.
        // (Note que o método 'gerarMMM' deve estar acessível/existir na classe)
		gerarMMM(consoleWriter);
        System.out.println("-----------------------------");
    }

	/**
     * Gera e apresenta a pauta de uma disciplina/ano diretamente na consola (System.out).
     *
     * @param codigo O código da disciplina.
     * @param ano O ano de funcionamento da disciplina.
     */
    public static void apresentarPauta() {
	    // Obtenção de Inputs do Utilizador
	    String codigo = Input.getCCodDis().trim();
	    Integer ano = Input.getCAnoDis();
        
        // 1. Cria o PrintWriter, mas NÃO usa try-with-resources para evitar fechar o System.out
         // O 'true' ativa o auto-flush
        
        System.out.println("--- Início da Geração da Pauta (" + codigo + "/" + ano + ") ---");
        PrintWriter consoleWriter = new PrintWriter(new OutputStreamWriter(System.out), true);
        // 2. Chama o método original
        // Se o auto-flush for 'true', um flush() não é estritamente necessário 
        // após cada println, mas é uma boa prática garantir no final.
        gerarPauta(codigo, ano, consoleWriter); 
        System.out.println("--- Fim da Geração da Pauta ---");
    }
    /**
     * Gera a pauta de uma disciplina/ano no formato de texto e retorna o resultado como uma String.
     * @param codigo O código da disciplina.
     * @param ano O ano de funcionamento da disciplina.
     * @return A pauta formatada como uma String.
     */
    public static String obterPauta(String codigo, Integer ano) {
        // 1. Cria um StringWriter, que é um 'writer' em memória.
        StringWriter sw = new StringWriter();
        // 2. Cria um PrintWriter que escreve para o StringWriter.    
        try (PrintWriter printWriter = new PrintWriter(sw,true)) {
        	// 3. Chama o método original, passando o PrintWriter em memória.
            gerarPauta(codigo, ano, printWriter);
            
        } 
        // 4. Retorna o conteúdo acumulado no StringWriter como uma String.
        return sw.toString();
    }

    /**
   	 * Gera a pauta de uma disciplina/ano no formato de texto (caracteres de desenho de caixa),
   	 * escrevendo o resultado diretamente num PrintWriter.
   	 * * @param codigo O código da disciplina.
   	 * @param ano O ano de funcionamento da disciplina.
   	 * @param writer O objeto PrintWriter para onde a pauta será escrita.
   	 * @throws IOException Se ocorrer um erro de escrita no writer (embora o PrintWriter tipicamente englobe isso).
   	 */
   	public static void gerarPauta(String codigo, Integer ano, PrintWriter writer) {
   	    // Configura deve ser uma classe que fornece a conexão à BD.
   	    Configura configuradorBD = new Configura();
   	    
   	    // SQL para obter a designação (usado no cabeçalho)
   	    final String sqlSelectDisciplina = "SELECT designacao FROM disciplina WHERE codigo = ?";
   	    
   	    // SQL para obter os dados da pauta (alunos inscritos e nota mais alta)
   	    final String sqlSelectPauta = "SELECT a.numero, a.nome, MAX(i.nota) AS NOTA "
   	            + "FROM aluno a JOIN inscricao i ON a.numero = i.numero "
   	            + "WHERE i.codigo = ? AND i.ano = ? "
   	            + "GROUP BY a.numero, a.nome " 
   	            + "ORDER BY a.numero";

   	    // 2. Definição da Estrutura Visual (Hardcoded para consistência)
   	    final String BORDER_TOP1 = "╔═══════════════════════════════════════════════════════════════════════════════════╗";
   	    final String BORDER_TOP2 = "╠═════════╦════════════════════════════════════════════════════════════════╦════════╣";
   	    final String BORDER_SEP  = "╠═════════╬════════════════════════════════════════════════════════════════╬════════╣";
   	    final String BORDER_BOT  = "╚═════════╩════════════════════════════════════════════════════════════════╩════════╝";

   	    // Larguras das colunas (incluem as margens internas)
   	    final int NUMERO_W = 9;  
   	    final int NOME_W = 64;   
   	    final int NOTA_W = 8;    
   	    final int TITLE_FILL_WIDTH = BORDER_TOP1.length() - 2; 

   	    // 3. Obter Designação da Disciplina
   	    String designacao = "Disciplina Inválida";
   	    try (Connection conexao = configuradorBD.getConnection();
   	         PreparedStatement preparacao = conexao.prepareStatement(sqlSelectDisciplina)) {
   	        
   	        preparacao.setString(1, codigo); 
   	        try (ResultSet rs = preparacao.executeQuery()) {
   	            if (rs.next()) {
   	                designacao = rs.getString(1).trim();
   	            }
   	        }
   	    } catch (SQLException e) {
   	        // Ignora este erro e usa "Disciplina Inválida" no título, registando no log
   	        System.err.println("❌ Erro ao obter designação da disciplina: "+e.getMessage());
   	        // A pauta continua a ser gerada, mas com título 'Disciplina Inválida'
   	    }
   	    
   	    String titleText = "Pauta da Disciplina: " + designacao + " (" + ano + ")";

   	    // 4. Execução da Query Principal e Geração da Pauta
   	    try (Connection conexao = configuradorBD.getConnection();
   	         PreparedStatement preparacao = conexao.prepareStatement(sqlSelectPauta)) {
   	        
   	        preparacao.setString(1, codigo);
   	        preparacao.setInt(2, ano);

   	        try (ResultSet rs = preparacao.executeQuery()) {
   	            
   	            // 4.1. Início da escrita no PrintWriter
   	            
   	            // 4.2. Geração do Título e Cabeçalho
   	            writer.println(BORDER_TOP1);
   	            // Título: alinhado à esquerda
   	            writer.println("║" + DataFormatter.padCenter(" " + titleText, TITLE_FILL_WIDTH) + "║"); 
   	            writer.println(BORDER_TOP2);

   	            // Cabeçalho das Colunas: Nº e Nota alinhados à direita
   	            String headerLine = "║" 
   	                            + String.format("%" + NUMERO_W + "s", "Nº")           
   	                            + "║ " + DataFormatter.padLeft("Nome", NOME_W - 1) // Nome alinhado à esquerda
   	                            + "║" + String.format("%" + NOTA_W + "s", "Nota") + "║"; 
   	            writer.println(headerLine);
   	            
   	            writer.println(BORDER_SEP); // Separador
   	            
   	            boolean encontrou = false;
   	            while (rs.next()) {
   	                encontrou = true;
   	                String numeroStr = rs.getString("numero");
   	                String nomeStr = rs.getString("nome").trim();
   	                // Assumindo que DataFormatter.NotaToString sabe lidar com o tipo de nota
   	                String notaStr = DataFormatter.NotaToString(rs.getBigDecimal("NOTA")); 
   	                // 4.3. Dados da Pauta
   	                String dataLine = 
   	                		"║" + String.format("%" + NUMERO_W + "s", numeroStr) +      	// Nº alinhado à Direita
   	                		"║ " + DataFormatter.fill(util.Name.shorten(nomeStr, NOME_W - 2), NOME_W - 1, " ")+ 	// Nome alinhado à Esquerda
   	                		"║" + String.format("%" + NOTA_W + "s", notaStr) + "║"; // Nota alinhada à Direita
   	                writer.println(dataLine);
   	            }
   	            
   	            // 4.4. Rodapé e Mensagem de Finalização
   	            if (encontrou) {
   	                writer.println(BORDER_BOT);
   	            } else {
   	                // Se não encontrou inscrições, exibe mensagem
   	                writer.println("║" + DataFormatter.fill(" Não foram encontradas inscrições para esta disciplina/ano.", TITLE_FILL_WIDTH, " ") + "║");
   	                writer.println(BORDER_BOT);
   	            }
   	            
   	            // Não há retorno de String, o resultado foi escrito no writer.
   	            
   	        } // Fim do try com ResultSet
   	    } catch (SQLException e) {
   	        // Tratamento de erros da query principal (pauta)
   	        System.err.println("-----SQLException de Pauta-----");
   	        System.err.println("Message:  " + e.getMessage());
   	        // Escreve uma mensagem de erro simples no output
   	        writer.println("❌ Erro ao gerar a pauta. Detalhes de erro no log do sistema.");
   	    }
   	    // NOTA: Geralmente, é responsabilidade do chamador fechar o PrintWriter.
   	}
	/**
	 * Gera a pauta de uma disciplina em formato HTML com estilos CSS internos, 
	 * com base no código e ano de funcionamento fornecidos.
	 *
	 * @param codigo O código da disciplina.
	 * @param ano O ano de funcionamento da disciplina.
	 * @return String contendo o HTML formatado da pauta ou uma mensagem de aviso/erro em HTML.
	 */
	public static String gerarPautaHTML(String codigo, Integer ano) {
	    // As classes Configura, NameUtils e DataFormatter devem estar acessíveis.
	    Configura configuradorBD = new Configura();
	    StringBuilder htmlOutput = new StringBuilder();
	    
	    // Query SQL para obter os dados da pauta (Número, Nome e Nota Máxima).
	    String sqlSelect = "SELECT a.numero, a.nome, MAX(i.nota) AS NOTA "
	            + "FROM aluno a JOIN inscricao i ON a.numero = i.numero "
	            + "WHERE i.codigo = ? AND i.ano = ? "
	            + "GROUP BY a.numero, a.nome " 
	            + "ORDER BY a.numero";

	    // 1. Obter Designação da Disciplina (necessário para o título)
	    String designacao = "Disciplina Inválida";
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = 
	                 conexao.prepareStatement("SELECT designacao FROM disciplina WHERE codigo = ?")) {
	        
	        preparacao.setString(1, codigo);
	        try (ResultSet rs = preparacao.executeQuery()) {
	            if (rs.next()) {
	                designacao = rs.getString(1).trim();
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("❌ Erro ao obter designação da disciplina: " + e.getMessage());
	        designacao = "ERRO (Ver Log)";
	    }
	    
	    String titleText = "Pauta da Disciplina: " + designacao + " (" + ano + ")";

	    // 2. Inserir CSS Interno
	    htmlOutput.append("<style type=\"text/css\">");
	    htmlOutput.append("/* Estilos gerais do container da pauta */\n");
	    htmlOutput.append(".pauta-container { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px 0; max-width: 900px; }\n");
	    htmlOutput.append("/* Título da pauta */\n");
	    htmlOutput.append(".pauta-header { background-color: #007bff; color: white; padding: 12px 15px; border-radius: 5px 5px 0 0; margin: 0; font-size: 1.2em; }\n");
	    htmlOutput.append("/* Tabela principal */\n");
	    htmlOutput.append(".pauta-table { width: 100%; border-collapse: collapse; border: 1px solid #ddd; }\n");
	    htmlOutput.append("/* Cabeçalho da tabela (Nº, Nome, Nota) */\n");
	    htmlOutput.append(".pauta-table th { background-color: #f8f9fa; color: #333; border: 1px solid #ddd; padding: 12px 15px; text-align: left; font-weight: 600; border-bottom: 2px solid #ddd; }\n");
	    htmlOutput.append("/* Células de dados */\n");
	    htmlOutput.append(".pauta-table td { border: 1px solid #ddd; padding: 10px 15px; }\n");
	    htmlOutput.append("/* Estilo de linha alternada (Zebra Striping) */\n");
	    htmlOutput.append(".pauta-table tbody tr:nth-child(even) { background-color: #f2f2f2; }\n");
	    htmlOutput.append("/* Alinhamento da coluna de Número e Nota (à direita) */\n");
	    htmlOutput.append(".col-numero, .col-nota { text-align: right; }\n");
	    htmlOutput.append("/* Alinhamento da coluna de Nome (à esquerda) */\n");
	    htmlOutput.append(".col-nome { text-align: left; }\n");
	    htmlOutput.append("/* Aviso / Mensagem de erro */\n");
	    htmlOutput.append(".pauta-message { padding: 15px; border: 1px solid #ffc107; background-color: #fff3cd; color: #856404; border-radius: 4px; margin-top: 15px; }\n");
	    htmlOutput.append("</style>");
	    
	    // 3. Iniciar a estrutura HTML
	    htmlOutput.append("<div class=\"pauta-container\">");
	    htmlOutput.append("<h3 class=\"pauta-header\">").append(titleText).append("</h3>");

	    // 4. Execução da Query Principal e Geração da Tabela
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
	        
	        preparacao.setString(1, codigo);
	        preparacao.setInt(2, ano);

	        List<String[]> pautaData = new ArrayList<>();
	        try (ResultSet rs = preparacao.executeQuery()) {
	            while (rs.next()) {
	                String numeroStr = rs.getString("numero");
	                // Garante nome próprio com NameUtils.proper
	                String nomeStr = Name.normalize(rs.getString("nome")).trim(); 
	                // Assumindo DataFormatter.NotaToString converte BigDecimal para String (e trata NULL)
	                String notaStr = DataFormatter.NotaToString(rs.getBigDecimal("NOTA")); 
	                pautaData.add(new String[]{numeroStr, nomeStr, notaStr});
	            }
	        }
	        
	        if (pautaData.isEmpty()) {
	            // Mensagem se não houver resultados (em HTML)
	            htmlOutput.append("<div class=\"pauta-message\">⚠️ Aviso: Não foram encontradas inscrições para a disciplina **").append(designacao).append("** no ano **").append(ano).append("**.</div>");
	        } else {
	            // Início da Tabela
	            htmlOutput.append("<table class=\"pauta-table\">");
	            
	            // Cabeçalho
	            htmlOutput.append("<thead><tr>");
	            htmlOutput.append("<th class=\"col-numero\" style=\"width: 10%;\">Nº</th>");
	            htmlOutput.append("<th class=\"col-nome\" style=\"width: 75%;\">Nome</th>");
	            htmlOutput.append("<th class=\"col-nota\" style=\"width: 15%;\">Nota</th>");
	            htmlOutput.append("</tr></thead>");
	            
	            // Corpo da Tabela
	            htmlOutput.append("<tbody>");
	            for (String[] row : pautaData) {
	                String numero = row[0];
	                String nome = row[1];
	                String nota = row[2];
	                
	                htmlOutput.append("<tr>");
	                // Nº (alinhado à direita)
	                htmlOutput.append("<td class=\"col-numero\">").append(numero).append("</td>");
	                // Nome (alinhado à esquerda)
	                htmlOutput.append("<td class=\"col-nome\">").append(nome).append("</td>");
	                // Nota (alinhado à direita)
	                htmlOutput.append("<td class=\"col-nota\">").append(nota).append("</td>");
	                htmlOutput.append("</tr>");
	            }
	            htmlOutput.append("</tbody>");
	            htmlOutput.append("</table>");
	        }
	        
	    } catch (SQLException e) {
	        // Tratamento de erros da query principal (pauta)
	        System.err.println("❌ Erro ao gerar a pauta: " + e.getMessage());
	        htmlOutput.append("<div class=\"pauta-message\" style=\"background-color: #f8d7da; border-color: #f5c6cb; color: #721c24;\">");
	        htmlOutput.append("❌ Erro ao consultar a pauta. Detalhes de erro no log do sistema. Código SQL: ").append(e.getSQLState());
	        htmlOutput.append("</div>");
	    }

	    htmlOutput.append("</div>"); // Fecha pauta-container
	    return htmlOutput.toString();
	}
	/**
	 * Gera o certificado de habilitações do aluno em HTML, 
	 * com estilos CSS internos,
	 * apresentando anos, disciplinas e respetivas notas de aprovação.
	 * Utiliza PreparedStatement para evitar SQL Injection.
	 *
	 * @param numero O número de identificação do aluno a procurar.
	 * @return String contendo o HTML formatado do certificado ou uma mensagem de erro em HTML.
	 */
	public static String gerarCertificadoHTML(Integer numero) {
	    Configura configuradorBD = new Configura();
	    StringBuilder htmlOutput = new StringBuilder();
	    
	    // 1. Busca do Nome do Aluno
	    String nomeAluno = "Aluno Desconhecido";
	    String sqlNome = "SELECT nome FROM aluno WHERE numero = ?";
	    
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlNome)) {
	        
	        preparacao.setInt(1, numero);
	        try (ResultSet rs = preparacao.executeQuery()) {
	            if (rs.next()) {
	                // NomeUtils.proper(rs.getString(1)) é assumido para formatar o nome
	                nomeAluno = Name.normalize(rs.getString(1).trim()); 
	            }
	        }
	    } catch (SQLException e) {
	        // Ignoramos erros aqui e usamos o nome padrão, mas registamos.
	        System.err.println("❌ Erro ao obter nome do aluno para o título: " + e.getMessage());
	    }

	    // 2. Query SQL principal para obter o certificado
	    String sqlSelect = 
	            "SELECT MIN(X.ANO) AS ANO, D.DESIGNACAO, X.NOTA AS NTA "+ 
	                "FROM INSCRICAO X "+
	                "JOIN DISCIPLINA D ON X.CODIGO = D.CODIGO "+
	                "JOIN ( "+
	                	"SELECT NUMERO, CODIGO, MAX(NOTA) AS NOTA FROM INSCRICAO WHERE "+condAprov+" GROUP BY NUMERO, CODIGO"+
	                ") Y ON X.NUMERO = Y.NUMERO AND X.CODIGO = Y.CODIGO AND X.NOTA = Y.NOTA "+
	                "WHERE X.NUMERO = ? "+ // Marcador de posição para o número
	                "GROUP BY X.NUMERO, D.DESIGNACAO, X.NOTA "+
	                "ORDER BY 1, 2";

	    // 3. Início da Geração do HTML
	    
	    // Contêiner principal
	    htmlOutput.append("<div style=\"font-family: Arial, sans-serif; max-width: 800px; margin: 30px auto;\">");

	    // Estilos CSS internos (comentados em pt-PT)
	    htmlOutput.append("<style>");
	    htmlOutput.append("/* Estilos CSS internos para o Certificado de Habilitações (pt-PT) */\n");
	    
	    htmlOutput.append(".certificado-header { /* Estilo para o cabeçalho do certificado */\n");
	    htmlOutput.append("    background-color: #004c8c; /* Azul escuro oficial */\n");
	    htmlOutput.append("    color: white;\n");
	    htmlOutput.append("    padding: 15px;\n");
	    htmlOutput.append("    font-size: 1.2em;\n");
	    htmlOutput.append("    border-radius: 8px 8px 0 0;\n");
	    htmlOutput.append("    margin: 0;\n");
	    htmlOutput.append("    text-align: center;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append(".certificado-table { /* Estilo para a tabela de disciplinas */\n");
	    htmlOutput.append("    width: 100%;\n");
	    htmlOutput.append("    border-collapse: collapse;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append(".certificado-table th { /* Estilo para os cabeçalhos das colunas */\n");
	    htmlOutput.append("    background-color: #f0f8ff; /* Azul claro (fundo) */\n");
	    htmlOutput.append("    color: #004c8c;\n");
	    htmlOutput.append("    padding: 12px 8px;\n");
	    htmlOutput.append("    border: 1px solid #ddd;\n");
	    htmlOutput.append("    border-top: 2px solid #004c8c;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append(".certificado-table td { /* Estilo para as células de dados */\n");
	    htmlOutput.append("    padding: 10px 8px;\n");
	    htmlOutput.append("    border: 1px solid #ddd;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append(".col-ano, .col-nota { /* Alinhamento à direita para Ano e Nota */\n");
	    htmlOutput.append("    text-align: center;\n"); // Centralizado para uma melhor leitura da nota
	    htmlOutput.append("    width: 10%;\n"); 
	    htmlOutput.append("    font-weight: bold;\n");
	    htmlOutput.append("    font-family: 'Consolas', monospace;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append(".certificado-table tr:nth-child(even) { /* Efeito Zebra (linhas pares) */\n");
	    htmlOutput.append("    background-color: #f9f9f9;\n");
	    htmlOutput.append("}\n");

	    htmlOutput.append("</style>");
	    
	    // Título do Certificado
	    htmlOutput.append("<div class=\"certificado-header\">");
	    htmlOutput.append("📜 Certificado de Habilitações: Nº ").append(numero).append(" - ").append(nomeAluno);
	    htmlOutput.append("</div>");
	    
	    // Tabela
	    htmlOutput.append("<table class=\"certificado-table\">");
	    
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
	        
	        preparacao.setInt(1, numero); // Vincula o número do aluno

	        try (ResultSet rs = preparacao.executeQuery()) {
	            
	            // Cabeçalho da Tabela
	            htmlOutput.append("<thead><tr>");
	            htmlOutput.append("<th class=\"col-ano\">Ano</th>");
	            htmlOutput.append("<th style=\"text-align: left;\">Disciplina</th>");
	            htmlOutput.append("<th class=\"col-nota\">Nota</th>");
	            htmlOutput.append("</tr></thead>");
	            
	            htmlOutput.append("<tbody>");
	            
	            boolean encontrou = false;
	            while (rs.next()) {
	                encontrou = true;
	                String anoStr = rs.getString("ANO");
	                String designacaoStr = rs.getString("DESIGNACAO").trim();
	                // DataFormatter.NotaToString(BigDecimal) é assumido
	                String notaStr = DataFormatter.NotaToString(rs.getBigDecimal("NTA")); 
	                
	                // Linha de Dados
	                htmlOutput.append("<tr>");
	                htmlOutput.append("<td class=\"col-ano\">").append(anoStr).append("</td>");
	                htmlOutput.append("<td class=\"col-disc\">").append(designacaoStr).append("</td>");
	                htmlOutput.append("<td class=\"col-nota\">").append(notaStr).append("</td>");
	                htmlOutput.append("</tr>");
	            }
	            
	            htmlOutput.append("</tbody></table>");
	            htmlOutput.append("</div>"); // Fecha o contêiner principal

	            if (!encontrou) {
	                // Se não encontrou aprovações
	                return "<div class=\"msg-aviso\">⚠️ Aviso: Não foram encontradas aprovações para o aluno Nº " + numero + " - " + nomeAluno + ".</div>";
	            }
	            
	            return htmlOutput.toString();
	            
	        }
	    } catch (SQLException e) {
	        // Tratamento de erros de BD
	        System.err.println("-----SQLException de Certificado-----");
	        System.err.println("Message: " + e.getMessage());
	        return "<p style=\"color: red; font-weight: bold; padding: 15px;\">❌ Erro ao gerar o certificado. Detalhes de erro no log do sistema.</p>";
	    }
	}
	/**
     * Gera e apresenta o certificado de habilitações do aluno 
     * diretamente na consola (System.out).
     *
     * @param numero O número de identificação do aluno.
     */
    public static void apresentarCertificado() {
	    // Obtenção do Input do Utilizador
	    Integer numero = Input.getCNmrAluno();
        // 1. Cria o PrintWriter para a consola (System.out)
        // Usamos 'true' para ativar o auto-flush
        // Não se pode fechar o output!
        System.out.println("\n--- Início da Geração do Certificado (Nº " + numero + ") ---");
        PrintWriter consoleWriter = new PrintWriter(new OutputStreamWriter(System.out), true);
        // 2. Chama o método original, passando o PrintWriter da consola
        // Assume-se que o método 'gerarCertificado' está acessível.
        gerarCertificado(numero, consoleWriter); 
        System.out.println("--- Fim da Geração do Certificado ---");
    }

    /**
     * Retorna o certificado de habilitações do aluno como uma String, 
     * obtido através da chamada do método estático gerarCertificado.
     * @param numero O número de identificação do aluno.
     * @return O certificado formatado como String (texto ASCII/Box-Drawing).
     */
    public static String obterCertificado(Integer numero) {
        // 1. Cria um StringWriter para atuar como o destino de saída (buffer de String)
        StringWriter stringWriter = new StringWriter();
        
        // 2. Cria um PrintWriter que escreve no StringWriter
        // O PrintWriter é o objeto que o método original espera receber
        try (PrintWriter printWriter = new PrintWriter(stringWriter,true)) {
            
            // 3. Chama o método original, passando o PrintWriter
            gerarCertificado(numero, printWriter);
        }
        
        // 4. Retorna o conteúdo acumulado no StringWriter
        return stringWriter.toString();
    }
    
	/**
	 * Gera o certificado de habilitações do aluno, 
	 * formatado em modo texto (ASCII/Box-Drawing Characters) 
	 * com anos, disciplinas e respetivas notas de aprovação, 
	 * escrevendo o resultado diretamente num PrintWriter.
	 * @param numero O número de identificação do aluno a procurar.
	 * @param writer O objeto PrintWriter para onde o certificado será escrito.
	 */
	public static void gerarCertificado(Integer numero, PrintWriter writer) {
	    // É importante garantir que 'condAprov', 'Configura', 'NameUtils' e 'DataFormatter' 
	    // estejam acessíveis e definidos conforme o método original.
	    // Assumimos que estão disponíveis ou importados.
	    
	    Configura configuradorBD = new Configura(); 

	    // 1. Busca do Nome do Aluno (Necessário para o título)
	    String nomeAluno = "Aluno Desconhecido";
	    String sqlNome = "SELECT nome FROM aluno WHERE numero = ?";
	    
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlNome)) {
	        
	        preparacao.setInt(1, numero);
	        try (ResultSet rs = preparacao.executeQuery()) {
	            if (rs.next()) {
	                nomeAluno = Name.shorten(rs.getString(1).trim(),40);
	            } else {
	                // Se o aluno não existe, escreve a mensagem de erro e retorna
	                writer.println("❌ Erro: Não foi encontrado aluno com o número: " + numero + ".");
	                return;
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("❌ Erro ao obter nome do aluno para o título: " + e.getMessage());
	        // Permite continuar, usando o nome default, se falhar só a busca do nome
	    }

	    // Query SQL principal para obter o certificado (inalterada)
	    String sqlSelect = 
	            "SELECT MIN(X.ANO) AS ANO, D.DESIGNACAO, X.NOTA AS NTA "+ 
	                "FROM INSCRICAO X "+
	                "JOIN DISCIPLINA D ON X.CODIGO = D.CODIGO "+
	                "JOIN ( "+
	                	"SELECT NUMERO, CODIGO, MAX(NOTA) AS NOTA FROM INSCRICAO WHERE "+condAprov+" GROUP BY NUMERO, CODIGO"+
	                ") Y ON X.NUMERO = Y.NUMERO AND X.CODIGO = Y.CODIGO AND X.NOTA = Y.NOTA "+
	                "WHERE X.NUMERO = ? "+ 
	                "GROUP BY X.NUMERO, D.DESIGNACAO, X.NOTA "+
	                "ORDER BY 1, 2";

	    // 2. Definição da Estrutura Visual da Tabela (Bordas e Larguras)
	    final String BORDER_TOP1 = "╔═══════════════════════════════════════════════════════════════════════════════════╗";
	    final String BORDER_TOP2 = "╠═════════╦════════════════════════════════════════════════════════════════╦════════╣";
	    final String BORDER_SEP  = "╠═════════╬════════════════════════════════════════════════════════════════╬════════╣";
	    final String BORDER_BOT  = "╚═════════╩════════════════════════════════════════════════════════════════╩════════╝";

	    // Larguras das colunas (fixas)
	    final int W_ANO = 9;   
	    final int W_DISC = 64; 
	    final int W_NOTA = 8;  
	    final int W_TITLE_FILL = BORDER_TOP1.length() - 2;

	    // Título do certificado
	    String titleText = " Certificado de Habilitações: Nº " + numero + " - " + nomeAluno;

	    // 3. Execução da Query Principal e Geração do Output
	    try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
	        
	        preparacao.setInt(1, numero); // Vincula o número do aluno

	        try (ResultSet rs = preparacao.executeQuery()) {
	            
	            // 3.1. Geração do Título e Cabeçalho
	            writer.println(BORDER_TOP1);
	            // Título: alinhado à esquerda
	            writer.print("║");
	            writer.print(DataFormatter.fill(" " + titleText, W_TITLE_FILL, " "));
	            writer.println("║"); 
	            writer.println(BORDER_TOP2);

	            // Cabeçalho das Colunas: Ano e Nota alinhados à direita
	            String headerLine = "║" 
	                              + String.format("%" + W_ANO + "s", "Ano")           
	                              + "║ " + DataFormatter.fill("Disciplina", W_DISC - 1, " ") 
	                              + "║" + String.format("%" + W_NOTA + "s", "Nota") + "║"; 
	            writer.println(headerLine);
	            
	            writer.println(BORDER_SEP); // Separador
	            
	            boolean encontrou = false;
	            while (rs.next()) {
	                encontrou = true;
	                String anoStr = rs.getString("ANO");
	                String designacaoStr = rs.getString("DESIGNACAO").trim();
	                
	                // Conversão de Nota
	                BigDecimal notaBD = rs.getBigDecimal("NTA");
	                String notaStr = DataFormatter.NotaToString(notaBD);
	                
	                // 3.2. Dados do Certificado (Ano e Nota à Direita)
	                String dataLine = "║" 
	                                + String.format("%" + W_ANO + "s", anoStr)           
	                                + "║ " + DataFormatter.fill(designacaoStr, W_DISC - 1, " ") 
	                                + "║" + String.format("%" + W_NOTA + "s", notaStr) + "║";  
	                writer.println(dataLine);
	            }
	            
	            // 3.3. Rodapé e Mensagem de Finalização
	            if (encontrou) {
	                writer.println(BORDER_BOT);
	                writer.println(); // Linha em branco
	            } else {
	                // Se não encontrou aprovações, exibe mensagem
	                writer.print("║");
	                writer.print(DataFormatter.fill(" Não foram encontradas aprovações para o aluno Nº " + numero + ".", W_TITLE_FILL, " "));
	                writer.println("║");
	                writer.println(BORDER_BOT);
	                writer.println(); // Linha em branco
	            }
	            
	            // Não há 'return output.toString()' - o output foi direto para o writer.
	            
	        }
	    } catch (SQLException e) {
	        // Tratamento de erros de BD
	        System.err.println("-----SQLException de Certificado-----");
	        System.err.println("Message: " + e.getMessage());
	        writer.println("❌ Erro ao gerar o certificado. Detalhes de erro no log do sistema.");
	    }
	    
	    // Opcional: Garante que tudo o que foi escrito é descarregado para o destino.
	    writer.flush(); 
	}
	
	/**
	 * Faz um processamento estatístico sobre a tabela 'inscricao'.
	 * Usa PreparedStatement apenas para o código (se fornecido).
	 * @param configuradorBD Objeto de configuração da base de dados.
	 * @param codigo código da disciplina envolvida no processamento (pode ser null)
	 * @param cond   condição de selecção (assumida como segura/interna)
	 * @param func   função de agregação (assumida como segura/interna)
	 * @return valor da estatística (BigDecimal), ou ZERO em caso de erro.
	 */
	private static BigDecimal estatistica(Configura configuradorBD, String codigo, String cond, String func) {
		BigDecimal ret = BigDecimal.ZERO;

        // cond e func são gerados internamente (listarARP) ou são inputs confiáveis.
		StringBuilder sql = new StringBuilder("SELECT ").append(func).append(" FROM inscricao WHERE ").append(cond);
        
        // Verifica se o código da disciplina deve ser usado
        boolean usaCodigo = (codigo != null && !codigo.trim().isEmpty());
        if (usaCodigo) {
            sql.append(" AND codigo = ?"); // Adiciona o marcador de posição
        }
        
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sql.toString())) {
             
            if (usaCodigo) {
                preparacao.setString(1, codigo); // Vincula o código de forma segura
            }
            
			try (ResultSet valor = preparacao.executeQuery()) {
				if (valor.next()) {
					// Usa getBigDecimal para melhor precisão e trata o caso de ser nulo (funções de agregação sem resultados)
					ret = valor.getBigDecimal(1);
                    if (ret == null) ret = BigDecimal.ZERO;
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro no processamento estatístico.");
			System.err.println("-----SQLException de Estatística-----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		return ret;
	}

    // ----------------------------------------------------------------------
    // MÉTODO MAIN E MENU DE TESTE
    // ----------------------------------------------------------------------
    
    /**
     * Método principal (Entry Point) da classe, implementando um menu de teste
     * para interagir com a geração de relatórios.
     * @param args Argumentos de linha de comandos (não utilizados).
     */
    public static void main(String[] args) {
        
        System.out.println("=============================================");
        System.out.println("        🧪 Menu - Relatórios       ");
        System.out.println("=============================================");
        char opcao;
        do {
            System.out.println("\n📊 > Relatórios e Estatísticas:");
            System.out.println("a. 📈 Listar Aprovados, Reprovados e Percentagem");
            System.out.println("b. 🧮 Listar Notas Mínima, Máxima e Média");
            System.out.println("c. 📋 Gerar Pauta de Disciplina/Ano");
            System.out.println("d. 📜 Gerar Certificado de Habilitações");
            System.out.println("e. 📄 Listagem \"WIP\"");
            System.out.println("z. 🔙 Sair");
            System.out.println("---------------------------------------------");
            
            System.out.println("Opção: ");
            opcao = Character.toLowerCase(IOx.inChar());

            // Chama as versões de conveniência (sem Configura)
            switch (opcao) {
                case 'a':
                    System.out.println("\n--- 📈 ESTATÍSTICA GLOBAL ---");
                    apresentarARP(); 
                    break;
                case 'b':
                    System.out.println("\n--- 🧮 ESTATÍSTICA MMM ---");
                    apresentarMMM();
                    break;
                case 'c':
                    System.out.println("\n--- 📋 GERAR PAUTA ---");
                    apresentarPauta();
                    break;
                case 'd':
                    System.out.println("\n--- 📜 GERAR CERTIFICADO ---");
                    apresentarCertificado();
                    break;
                case 'e':
                    System.out.println("\n--- 📄 GERAR LISTAGEM ---");
                    // System.out.println("*** 🚧 Falta implementar ❓ ***");
                    System.out.println("*** ⏳ Faltava implementar 💡 ***");
                    util.DataTransfer.apresentar("iUltimoTrimestre");
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