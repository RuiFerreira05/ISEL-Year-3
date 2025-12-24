package gestor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.zxing.WriterException;

import util.Calendario;
import util.Configura;
import util.IOx;
import util.DataFormatter;
import util.Name;
import util.QRCode;

/**
 * Classe responsável pela gestão (CRUD) da entidade Aluno.
 * * ⚠️ AVISO: Esta classe não adere ao Princípio da Responsabilidade Única (SRP).
 * Utiliza métodos estáticos que podem dificultar a manutenção e testes.
 */
public final class Alunos {
    
    // ----------------------------------------------------------------------
    // MÉTODOS DE NEGÓCIO (API Pública)
    // ----------------------------------------------------------------------
    
    /**
     * Pergunta o número, nome, género e data de nascimento do aluno ao utilizador
     * e insere o novo aluno na tabela 'aluno', usando PreparedStatement.
     * @return true se a inserção for bem-sucedida, false caso contrário.
     */
	public static boolean novo() {
		Configura configuradorBD = new Configura();
		
        // 1. Obtenção de dados do utilizador.
		Integer numero 	= Input.getCNmrAluno();
        String  nome 	= Input.getCNmAluno();
        String  genero 	= Input.getCGnrAluno();
        Date 	nascido 	= Input.getCNascAluno();
		
        // SQL SEGURO: Usando marcadores de posição (?)
		String sqlInsert = "INSERT INTO aluno (numero, nome, genero, nascido) VALUES (?, ?, ?, ?)";
		
        // Uso de try-with-resources com PreparedStatement
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sqlInsert)) {
			
            // 2. Vincula os valores aos marcadores de posição
            preparacao.setInt(1, numero);
            preparacao.setString(2, nome);
            preparacao.setString(3, genero);
            preparacao.setDate(4, nascido);
			
            // 3. Execução da inserção
			if (1 == preparacao.executeUpdate()) {
				System.out.println("✅ Aluno registado com sucesso.");
				apresentarFicha(numero);
				return true;
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro ao inserir o aluno.");
            System.err.println("----- SQLException de Inserção -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Vendor:  " + e.getErrorCode());
		}
		return false;
	}
    
    /**
     * Pede ao utilizador um número de aluno e apresenta a respetiva ficha.
     */
    public static void procurarNumero() {
        // Solicita o número de aluno ao utilizador.
        Integer numero = Input.getCNmrAluno();
        
        // Chamada de método seguro.     
		apresentarFicha(numero);
		apresentarFoto(numero);
		try {
			QRCode.displayQR(numero.toString());
		} catch (WriterException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Mostra a foto de um aluno numa imagem.
     * @param numero O número do aluno cuja foto será apresentada.
     * @return true se a foto foi obtida e apresentada com sucesso, false caso contrário.
     */
    public static boolean apresentarFoto(int numero) {
        // 1. Tenta obter a foto do aluno como um array de bytes (buffer)
        byte[] buffer = getFoto(numero);

        // 2. Verifica se o buffer é válido (não é nulo e contém dados)
        if (buffer != null && buffer.length > 0) {
            try {
                // Cria uma nova instância da classe Foto (assumida)
                Foto ft = new Foto(); 
                // Define o array de bytes da foto no objeto Foto
                ft.setFoto(buffer);
                // Mostra a imagem numa janela com o título especificado
                ft.show("Fotografia nº " + numero); 

                // Mensagem de Sucesso com ícone ✨
                System.out.println("🖼️ Fotografia apresentada com sucesso!");
                return true;
            } catch (Exception e) {
                System.out.println("❌ Erro na apresentação da fotografia: "+e.getMessage());
                // Retorna false, indicando falha
                return false;
            }
        } else {
            // 3. Caso o buffer seja nulo ou vazio (foto não encontrada na BD)
            // Mensagem de Aviso/Erro com ícone ⚠️
            System.out.println("⚠️ Não foi encontrada fotografia para o aluno Nº " + numero + ".");
            return false;
        }
    }
	
	/**
	 * Obtem a foto de um aluno em formato binário
	 * 
	 * @param numero 	do aluno
	 * @return			bytes da foto ou null se não existir
	 */
	public static byte[] getFoto(int numero) {
		Configura configuradorBD=new Configura();
		try (Connection con = configuradorBD.getConnection()){
			PreparedStatement pstmt =con.prepareStatement("SELECT conteudo FROM foto WHERE numero=?");
			pstmt.setInt(1, numero);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getBytes(1);
			} else
				System.out.println("Sem fotografia!");
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Obtem a foto de um aluno em formato base64
	 * 
	 * @param numero do aluno
	 * @return string com a foto ou null se não existir
	 */
	public static String getFoto64(Integer numero) {
		byte[] aux = getFoto(numero);
		if (aux == null)
			return null;
		return Base64.getEncoder().encodeToString(aux);
	}
    /**
     * Executa uma consulta parametrizada na tabela ALUNO e gera uma tabela em HTML 
     * formatada com estilos inline (web-friendly).
     *
     * @param atributo O nome da coluna (ex: "nome", "numero") a ser filtrada.
     * @param operador O operador SQL (ex: "=", "LIKE") a ser aplicado na pesquisa.
     * @param valor O valor a procurar no atributo. Se for 'null', verifica a condição IS NULL.
     * @return String contendo o HTML formatado da tabela ou uma mensagem de aviso/erro em HTML.
     */
    public static String obterTabelaHTML(String atributo, String operador, String valor) {    			
        // A classe Configura, DataFormatter e NameUtils devem estar acessíveis.
        Configura configuradorBD = new Configura();
        StringBuilder htmlOutput = new StringBuilder(); 
        
        // SQL SEGURO: Usando marcador de posição (?) para o parâmetro
        String sqlSelect = "SELECT numero, nome, genero, nascido FROM aluno WHERE "
            			+atributo+" "+((valor==null)?"IS NULL":operador)+" "+((valor==null)?"":"?")+" ORDER BY numero";
                           
        List<String[]> dataRows = new ArrayList<>(); 
        int rowCount = 0;

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) { 
        		if(valor!=null)
        			preparacao.setString(1, valor);
            
            try (ResultSet rs = preparacao.executeQuery()) { 

                // 1. PRIMEIRA ITERAÇÃO: Recolher dados
                while (rs.next()) {
                    // Formatação dos dados (necessária antes de construir o HTML)
                    String numero = rs.getString("numero");
                    String nome = Name.normalize(rs.getString("nome"));
                    String genero = DataFormatter.obterGenero(rs.getString("genero"));
                    String nascido = DataFormatter.DateToString(rs.getDate("nascido"));
                    
                    dataRows.add(new String[]{numero, nome, genero, nascido});
                    rowCount++;
                }
            } 

            if (rowCount == 0) {
                String valorPesquisa = (valor == null) ? "NULL" : "'" + valor + "'";
                return "<p style=\"color: orange; font-weight: bold; padding: 10px;\">⚠️ Aviso: Não foram encontrados alunos onde o "+atributo+" é "+valorPesquisa+".</p>";
            }

            // 2. GERAÇÃO DO HTML COM ESTILOS INLINE
            
            // Contêiner principal para o título e margem
            htmlOutput.append("<div style=\"margin: 20px 0; font-family: Arial, sans-serif;\">");
            
            // Título
            htmlOutput.append("<h4 style=\"margin-bottom: 10px; color: #333;\">")
                      .append("Alunos Encontrados (").append(rowCount).append(")")
                      .append("</h4>");

            // Tabela
            htmlOutput.append("<table style=\"border-collapse: collapse; width: 100%; border: 1px solid #ccc;\">");

            // Estilos para Cabeçalhos e Células
            String headerStyle = "style=\"background-color: #4CAF50; color: white; border: 1px solid #4CAF50; padding: 10px; text-align: left;\"";
            String numHeaderStyle = "style=\"background-color: #4CAF50; color: white; border: 1px solid #4CAF50; padding: 10px; text-align: right;\"";
            String cellStyle = "style=\"border: 1px solid #ddd; padding: 8px;\"";
            String numCellStyle = "style=\"border: 1px solid #ddd; padding: 8px; text-align: right;\"";

            // Cabeçalho da Tabela
            htmlOutput.append("<thead><tr>");
            htmlOutput.append("<th ").append(numHeaderStyle).append(">Nº</th>");
            htmlOutput.append("<th ").append(headerStyle).append(">NOME</th>");
            htmlOutput.append("<th ").append(headerStyle).append(">GÉNERO</th>");
            htmlOutput.append("<th ").append(numHeaderStyle).append(">NASCIMENTO</th>");
            htmlOutput.append("</tr></thead>");
            
            // Corpo da Tabela
            htmlOutput.append("<tbody>");

            // Desenhar Linhas de Dados
            for (int i = 0; i < rowCount; i++) {
                String[] row = dataRows.get(i);
                String numero = row[0];
                String nome = row[1];
                String genero = row[2];
                String nascido = row[3];
                
                // Estilo para linhas alternadas (Zebra striping)
                String rowStyle = (i % 2 == 0) ? "style=\"background-color: #f2f2f2;\"" : "";

                htmlOutput.append("<tr ").append(rowStyle).append(">");
                
                // Nº (Direita)
                htmlOutput.append("<td ").append(numCellStyle).append(">").append(numero).append("</td>");
                // NOME (Esquerda)
                htmlOutput.append("<td ").append(cellStyle).append(">").append(nome).append("</td>");
                // GÉNERO (Esquerda)
                htmlOutput.append("<td ").append(cellStyle).append(">").append(genero).append("</td>");
                // NASCIMENTO (Direita)
                htmlOutput.append("<td ").append(numCellStyle).append(">").append(nascido).append("</td>");
                
                htmlOutput.append("</tr>");
            }

            htmlOutput.append("</tbody></table></div>");
            
            // Retorna a String HTML final
            return htmlOutput.toString();

        } catch (SQLException e) {
            // Log do erro completo para o desenvolvedor
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("Message:  " + e.getMessage());
            
            // Retorna uma mensagem de erro em HTML
            return "<p style=\"color: red; font-weight: bold; padding: 10px;\">❌ Erro ao listar os alunos. Detalhes de erro no log do sistema.</p>";
        }
    }
    /**
     * Gera uma tabela formatada em modo texto (ASCII/Box-Drawing Characters) 
     * com os alunos filtrados, ajustando dinamicamente a largura das colunas.
     *
     * NOTA: Este método encapsula a chamada a gerarTabelaTXT, capturando 
     * o output do PrintWriter para retornar uma String.
     *
     * @param atributo O nome da coluna (ex: "nome", "numero") a ser filtrada.
     * @param operador O operador SQL (ex: "=", "LIKE") a ser aplicado na pesquisa.
     * @param valor O valor a procurar no atributo. Se for 'null', verifica a condição IS NULL.
     * @return String contendo a tabela formatada, ou uma mensagem de aviso/erro.
     */
    public static String obterTabelaTXT(String atributo, String operador, String valor) {
        // 1. Cria um buffer de memória para armazenar o output
        StringWriter sw = new StringWriter();
        
        // 2. Cria um PrintWriter que escreve nesse buffer
        PrintWriter pw = new PrintWriter(sw);

        try {
            // 3. Chama o método de geração, direcionando a saída para o PrintWriter de memória
            gerarTabelaTXT(atributo, operador, valor, pw);
            
            // 4. Retorna o conteúdo formatado do buffer como String
            return sw.toString();

        } catch (IOException e) {
            // Esta exceção é improvável com StringWriter, mas é tratada por segurança
            return "❌ Erro interno ao formatar a tabela: " + e.getMessage();
        }
    }
    /**
     * Pede ao utilizador uma expressão de pesquisa e lista todos os alunos
     * que correspondem ao padrão na coluna 'nome', usando PreparedStatement.
     */
    public static void procurarNome() {
        String nomePesquisa = ""; 
        do {
            System.out.println("🔎 Indique a expressão para pesquisar pelo nome (usa % ou _):");
            nomePesquisa = IOx.in();
            nomePesquisa = nomePesquisa.trim();
        } while (nomePesquisa.length() > 60);
        apresentarTabela("nome","LIKE",nomePesquisa);
     }
    
    /**
     * Apresenta no ecrã (consola) uma tabela formatada em modo texto
     * com os alunos filtrados, usando o método gerarTabelaTXT.
     *
     * @param atributo O nome da coluna (ex: "nome", "numero") a ser filtrada.
     * @param operador O operador SQL (ex: "=", "LIKE") a ser aplicado na pesquisa.
     * @param valor O valor a procurar no atributo. Se for 'null', verifica a condição IS NULL.
     */
    public static void apresentarTabela(String atributo, String operador, String valor) {
        
        // Cria um PrintWriter que escreve na consola (System.out) com auto-flush
        PrintWriter consoleWriter = new PrintWriter(System.out, true);

        try {
            // Chama o método gerarTabelaTXT, direcionando a saída para a consola
            gerarTabelaTXT(atributo, operador, valor, consoleWriter);
            
            // Não é necessário consoleWriter.flush() pois o auto-flush está ativo (true)
            // e o método gerarTabelaTXT() já chama out.flush() no final.
            
        } catch (IOException e) {
            // Em caso de erro de I/O na consola (o que é raro)
            System.err.println("❌ Erro de I/O ao apresentar a tabela na consola: " + e.getMessage());
        }
    }
    
    /**
     * Pede ao utilizador o número do aluno e um novo nome, procedendo à atualização,
     * usando PreparedStatement.
     * @return true se a atualização for bem-sucedida, false caso contrário.
     */
    public static boolean alterarNome() {
		Configura configuradorBD = new Configura();
        
        Integer numero = Input.getCNmrAluno();
        apresentarFicha(numero);
        
        String novoNome = Input.getCNmAluno();
        
        // SQL SEGURO: Usando marcadores de posição (?)
        String sqlUpdate = "UPDATE aluno SET nome = ? WHERE numero = ?";
        
        try (Connection conexao = configuradorBD.getConnection();
       		 PreparedStatement preparacao = conexao.prepareStatement(sqlUpdate)) {
        	
            // 1. Vincula os valores
            preparacao.setString(1, novoNome);
            preparacao.setInt(2, numero);
            
            // 2. Execução da atualização
            if (1 == preparacao.executeUpdate()) {
                System.out.println("✅ Nome alterado com sucesso.");
                apresentarFicha(numero);
                return true;
            } 
        } catch (SQLException e) {
        		System.out.println("❌ Erro ao alterar o nome.");
            System.err.println("----- SQLException de Alteração -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Vendor:  " + e.getErrorCode());
		}
        return false;
    }
    
    /**
     * Pergunta ao utilizador uma data de nascimento e lista todos os alunos
     * que nasceram nessa data, utilizando PreparedStatement para segurança.
     */
    public static void procurarNascido() {

        java.sql.Date dataNascimento = Input.getCNascAluno();
        apresentarTabela("nascido","=",dataNascimento.toString());
        
    }
    /**
     * Gera a ficha do aluno usando ASCII/Box-Drawing Characters.
     * @param configuradorBD Objeto de configuração para obter a conexão.
     * @param numero O número de identificação do aluno a procurar.
     * @param out O PrintWriter onde a saída formatada será escrita.
     * @throws IOException Se ocorrer um erro durante a escrita (out.println).
     */
    public static void gerarFichaTXT(Integer numero, PrintWriter out) throws IOException {
        Configura configuradorBD = new Configura();
        String sqlSelect = "SELECT numero, nome, genero, nascido FROM aluno WHERE numero = ?";
        final int W_LABEL = 13;     // Largura fixa para as etiquetas (e.g., "🆔 Número:")
        final int PADDING = 2;      // Padding interno (espaços) para os dados
        final int W_MIN_DATA = 25;  // Largura mínima legível para a coluna de dados

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
            
            preparacao.setInt(1, numero);

            try (ResultSet rs = preparacao.executeQuery()) {
                if (rs != null && rs.next()) {
                    
                    // Obter e formatar dados como Strings finais
                    String num = rs.getString("numero");
                    String nome = Name.normalize(rs.getString("nome")); 
                    String genero = DataFormatter.obterGenero(rs.getString("genero"));
                    Date nasceu = rs.getDate("nascido");
                    String strEstado = "'"+Calendario.getEstadoDia(nasceu.toLocalDate())+"'";
                    String dataNascimento = DataFormatter.DateToString(nasceu); 
                    
                    // Determinar a largura MÁXIMA necessária para os DADOS (W_DATA)
                    int w_data = Math.max(
                        Math.max(num.length(), nome.length()),
                        Math.max(genero.length(), dataNascimento.length())
                    );
                    
                    // Adicionar padding e garantir largura mínima de W_MIN_DATA
                    w_data = Math.max(w_data + PADDING, W_MIN_DATA); 

                    // 3. Calcular largura total da linha e Bordas
                    final int W_TOTAL = W_LABEL + w_data + 3; // +3 para " ║ " e " ║"
                    final int W_INNER = W_TOTAL - 2;          // Largura interna para caracteres '═'
                    
                    final String BORDER_LINE = "═".repeat(W_INNER);
                    final String BORDER_TOP = "╔" + BORDER_LINE + "╗";
                    final String BORDER_SEP = "╠" + BORDER_LINE + "╣";
                    final String BORDER_BOT = "╚" + BORDER_LINE + "╝";

                    // Desenhar Título e Separador
                    out.println(BORDER_TOP);
                    String titleText = "🧑‍ Ficha do Aluno 🎓";
                    String centeredTitle = DataFormatter.padCenter(titleText, W_INNER);
                    out.println("║" + centeredTitle + " ║"); 
                    out.println(BORDER_SEP); 
                 
                    // Campo 1: Número
                    out.println("║ " 
                                    + DataFormatter.padRight("Número", W_LABEL-6)+"  🆔: " 
                                    + DataFormatter.padLeft(num, w_data-1) + " ║"); 

                    // Campo 2: Nome
                    out.println("║ " 
                                    + DataFormatter.padRight("Nome", W_LABEL-6)+"  🏷️: "
                                    + DataFormatter.padLeft(" "+nome, w_data)+ "║");
                    
                    // Campo 3: Género
                    out.println("║ " 
                                    + DataFormatter.padRight("Género", W_LABEL-6)+" ♂/♀: "
                                    + DataFormatter.padLeft(genero, w_data) + "║");
                    
                    // Campo 4: Nascimento
                    out.println("║ " 
                                    + DataFormatter.padRight("Nascido", W_LABEL-6)+"  📅: "
                                    + DataFormatter.padLeft(dataNascimento, w_data) + "║");
                    
                    // Campo 5: Observação sobre a data de nascimento
                    out.println("║ " 
                                    + DataFormatter.padRight("Comenta", W_LABEL-6)+"  📝: "
                                    + DataFormatter.padLeft(strEstado.trim(), w_data) + "║");       
                    out.println(BORDER_BOT + "\n");
                
                } else {
                    out.println(" ❌ Não foi encontrado o aluno com o número: " + numero + ".");
                }
            }
            
            // Garante que tudo foi escrito no destino
            out.flush();

        } catch (SQLException e) {
            // Para erros na base de dados, ainda usamos System.err para a consola de logs
            out.println("❌ Erro ao consultar a ficha do aluno.");
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            System.err.println("Vendor:  " + e.getErrorCode());
        } 
    }
    /**
     * Gera uma tabela formatada em modo texto (ASCII/Box-Drawing Characters) 
     * com os alunos filtrados, ajustando dinamicamente a largura das colunas,
     * e escreve o resultado diretamente no PrintWriter fornecido.
     *
     * @param atributo O nome da coluna (ex: "nome", "numero") a ser filtrada.
     * @param operador O operador SQL (ex: "=", "LIKE") a ser aplicado na pesquisa.
     * @param valor O valor a procurar no atributo. Se for 'null', verifica a condição IS NULL.
     * @param out O PrintWriter onde a saída formatada será escrita.
     * @throws IOException Se ocorrer um erro durante a escrita (out.println).
     */
    public static void gerarTabelaTXT(String atributo, String operador, String valor, PrintWriter out) throws IOException {    			
        Configura configuradorBD = new Configura();
        
        // SQL SEGURO: Usando marcador de posição (?) para o parâmetro (se valor não for null)
        String sqlSelect = "SELECT numero, nome, genero, nascido FROM aluno WHERE "
                +atributo+" "+((valor==null)?"IS NULL":operador)+" "+((valor==null)?"":"?")+" ORDER BY numero";
                           
        // --- Constantes de Largura e Preenchimento ---
        final int PADDING 		= 2; 
        final int W_MIN_NUMERO 	= 8;
        final int W_MIN_NOME 	= 30;
        final int W_MIN_GENERO 	= 10;
        final int W_MIN_NASCIDO 	= 12;
        
        int maxWNumero 	= W_MIN_NUMERO;
        int maxWNome 	= W_MIN_NOME;
        int maxWGenero 	= W_MIN_GENERO;
        int maxWNascido 	= W_MIN_NASCIDO;
        int rowCount = 0;

        List<String[]> dataRows = new ArrayList<>(); 

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) { 
        		if(valor!=null)
        			preparacao.setString(1, valor);
            
            try (ResultSet rs = preparacao.executeQuery()) { 

                // 1. PRIMEIRA ITERAÇÃO: Recolher dados e calcular larguras dinâmicas
                while (rs.next()) {
                    // Formatação dos dados para determinar a largura real
                    String numero = rs.getString("numero");
                    String nome = Name.normalize(rs.getString("nome"));
                    String genero = DataFormatter.obterGenero(rs.getString("genero"));
                    String nascido = DataFormatter.DateToString(rs.getDate("nascido"));
                    
                    dataRows.add(new String[]{numero, nome, genero, nascido});
                    rowCount++;

                    // Ajuste de largura
                    if (numero.length() > maxWNumero) maxWNumero = numero.length();
                    if (nome.length() > maxWNome) maxWNome = nome.length();
                    if (genero.length() > maxWGenero) maxWGenero = genero.length();
                    if (nascido.length() > maxWNascido) maxWNascido = nascido.length();
                }
            } 

            if (rowCount == 0) {
                // Escreve a mensagem de aviso diretamente no PrintWriter
                String valorPesquisa = (valor == null) ? "NULL" : "'" + valor + "'";
                out.println("⚠️ Aviso: Não foram encontrados alunos onde o atributo '"+atributo+"' compara com o valor '"+valorPesquisa+"'.");
                out.flush();
                return; // Termina o método
            }

            // 2. CALCULAR LARGURAS FINAIS E BORDAS
            
            // Larguras Finais (dado + padding)
            final int W_NUMERO = maxWNumero + PADDING;
            final int W_NOME = maxWNome + PADDING;
            final int W_GENERO = maxWGenero + PADDING;
            final int W_NASCIDO = maxWNascido + PADDING;
            
            // Largura Total Interna
            final int W_INNER = W_NUMERO + W_NOME + W_GENERO + W_NASCIDO + 5; 

            // Definições de Linhas
            final String L_NUMERO = "═".repeat(W_NUMERO);
            final String L_NOME = "═".repeat(W_NOME);
            final String L_GENERO = "═".repeat(W_GENERO);
            final String L_NASCIDO = "═".repeat(W_NASCIDO);
            
            final String S_NUMERO = "─".repeat(W_NUMERO);
            final String S_NOME = "─".repeat(W_NOME);
            final String S_GENERO = "─".repeat(W_GENERO);
            final String S_NASCIDO = "─".repeat(W_NASCIDO);
            
            final String TITLE_TEXT = "Alunos Encontrados (" + rowCount + ")‍";
            
            // Bordas da Tabela
            final String BORDER_TOP = "╔" + L_NUMERO + "═" + L_NOME + "═" + L_GENERO + "═" + L_NASCIDO + "╗";
            final String HEADER_SEP = "╠" + L_NUMERO + "╦" + L_NOME + "╦" + L_GENERO + "╦" + L_NASCIDO + "╣";
            final String BORDER_BOT = "╚" + L_NUMERO + "╩" + L_NOME + "╩" + L_GENERO + "╩" + L_NASCIDO + "╝";
            
            // Separador entre Linhas de Dados
            final String ROW_SEP = "╟" + S_NUMERO + "╫" + S_NOME + "╫" + S_GENERO + "╫" + S_NASCIDO + "╢"; 

            // 3. GERAÇÃO DA TABELA (Usando out.println)

            out.println("\n" + BORDER_TOP);
            // -1 porque o emoji "‍" no título original tem largura 0 em alguns ambientes
            out.println("║" + DataFormatter.padCenter(TITLE_TEXT, W_INNER - 1) + "║"); 
            out.println(HEADER_SEP);

            // Cabeçalho da Tabela
            String headerLine = "║" 
                              + DataFormatter.padCenter("NÚMERO", W_NUMERO) 
                              + "║" 
                              + DataFormatter.padCenter("NOME", W_NOME) 
                              + "║"
                              + DataFormatter.padCenter("GÉNERO", W_GENERO)
                              + "║"
                              + DataFormatter.padCenter("NASCIMENTO", W_NASCIDO)
                              + "║";
            out.println(headerLine);
            out.println(HEADER_SEP.replace("╦","╬")); 

            // 4. Desenhar as linhas de dados com separador simples
            for (int i = 0; i < rowCount; i++) {
                String[] row = dataRows.get(i);
                String numero = row[0];
                String nome = row[1];
                String genero = row[2];
                String nascido = row[3];
                
                String numRight 			= DataFormatter.padRight(numero, W_NUMERO-1)+" ";
                String nascidoLeft 		= " "+DataFormatter.padLeft(nascido, W_NASCIDO-1);
                String nomeLeft 			= " "+DataFormatter.padLeft(nome, W_NOME-1);
                String generoCenter 		= DataFormatter.padCenter(genero, W_GENERO);
                
                String rowLine = "║"
                               + numRight
                               + "║"
                               + nomeLeft
                               + "║"
                               + generoCenter
                               + "║"
                               + nascidoLeft
                               + "║";
                
                out.println(rowLine);
                
                // Imprime o separador
                if (i < rowCount - 1) {
                    out.println(ROW_SEP);
                }
            }

            out.println(BORDER_BOT);
            
            // Garante que todos os dados foram enviados para o destino
            out.flush();

        } catch (SQLException e) {
            // Log do erro completo para o desenvolvedor
            System.err.println("----- SQLException de Consulta -----");
            System.err.println("SQLState:  " + e.getSQLState());
            System.err.println("Message:  " + e.getMessage());
            
            // Retorna uma mensagem de erro amigável para a interface do usuário
            out.println("❌ Erro ao listar os alunos. Detalhes de erro no log do sistema.");
            out.flush();
        }
    }
   
    /**
     * Retorna a ficha do aluno formatada como uma String, 
     * obtida a partir da chamada ao método gerarFicha.
     * @param numero O número de identificação do aluno a procurar.
     * @return A ficha do aluno formatada como String, ou uma String de erro.
     */
    public static String obterFichaTXT(Integer numero) {
        // 1. Cria um buffer de memória (StringWriter) para armazenar a saída.
        StringWriter sw = new StringWriter();
        
        // 2. Cria um PrintWriter que escreve nesse buffer.
        // O 'true' ativa o auto-flush (embora seja opcional neste caso, o 'flush'
        // é forçado dentro do método 'apresentarFicha').
        PrintWriter pw = new PrintWriter(sw, true);

        try {
            // 3. Chama o método original, passando o PrintWriter de memória.
            // Assume-se que 'apresentarFicha' está no escopo ou importado.
            gerarFichaTXT(numero, pw);
            
            // 4. Retorna o conteúdo do buffer de memória como String.
            return sw.toString();

        } catch (IOException e) {
            // Trata a exceção de I/O, embora improvável com StringWriter.
            return "❌ Erro interno ao formatar a ficha: " + e.getMessage();
        }
    }
    /**
     * Apresenta a ficha do aluno de forma formatada diretamente no ecrã (System.out),
     * consultando a base de dados através do número fornecido.
     * * @param numero O número de identificação do aluno a procurar.
     */
    public static void apresentarFicha(Integer numero) {
        // 1. Cria um PrintWriter ligado ao System.out.
        // O 'true' garante o auto-flush, para que o output seja exibido imediatamente.
        PrintWriter consoleWriter = new PrintWriter(System.out, true);
        
        try {
            // 2. Chama o método de geração de ficha, direcionando o output para o consoleWriter.
            gerarFichaTXT(numero, consoleWriter);
            
        } catch (IOException e) {
            // Exceção de I/O é muito improvável ao escrever no System.out, 
            // mas é tratada conforme a assinatura do método gerarFichaTXT.
            System.err.println("❌ Erro de I/O ao escrever no ecrã: " + e.getMessage());
        }
    }
    
    /**
     * Gera a ficha do aluno em formato HTML com estilos inline, 
     * consultando a base de dados através do número fornecido.
     *
     * @param numero O número de identificação do aluno a procurar.
     * @return String contendo o HTML formatado da ficha ou uma mensagem de aviso/erro em HTML.
     */
    public static String obterFichaHTML(Integer numero) {
        // A classe Configura, NameUtils e DataFormatter devem estar acessíveis.
        Configura configuradorBD = new Configura();
        StringBuilder htmlOutput = new StringBuilder();
        
        String sqlSelect = "SELECT numero, nome, genero, nascido FROM aluno WHERE numero = ?";
        
        // --- Estilos CSS Inline ---
        final String CONTAINER_STYLE = "font-family: Arial, sans-serif; max-width: 450px; margin: 20px 0; border: 1px solid #4CAF50; border-radius: 8px; background-color: #f9fff9; box-shadow: 2px 2px 5px rgba(0,0,0,0.1);";
        final String HEADER_STYLE = "padding: 10px 15px; background-color: #4CAF50; color: white; border-radius: 8px 8px 0 0; margin: 0;";
        final String BODY_STYLE = "padding: 15px;";
        final String ROW_STYLE = "margin-bottom: 10px; line-height: 1.5;";
        final String LABEL_STYLE = "font-weight: bold; width: 120px; display: inline-block; color: #555;";
        final String DATA_STYLE = "color: #333; margin-left: 10px;";

        try (Connection conexao = configuradorBD.getConnection();
             PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
            
            preparacao.setInt(1, numero);

            try (ResultSet rs = preparacao.executeQuery()) {
                if (rs != null && rs.next()) {
                    
                    // Obter e formatar dados como Strings finais
                    String num = rs.getString("numero");
                    String nome = Name.normalize(rs.getString("nome")); 
                    String genero = DataFormatter.obterGenero(rs.getString("genero"));
                    Date nasceu = rs.getDate("nascido");
                    String strEstado = "'"+Calendario.getEstadoDia(nasceu.toLocalDate())+"'";
                    String dataNascimento = DataFormatter.DateToString(nasceu); 
                    
                    // 1. Iniciar o contêiner principal
                    htmlOutput.append("<div style=\"").append(CONTAINER_STYLE).append("\">");
                    
                    // 2. Título (Header)
                    htmlOutput.append("<h4 style=\"").append(HEADER_STYLE).append("\">");
                    htmlOutput.append("🧑‍ Ficha do Aluno 🎓");
                    htmlOutput.append("</h4>");
                    
                    // 3. Corpo da Ficha
                    htmlOutput.append("<div style=\"").append(BODY_STYLE).append("\">");

                    // --- Linha 1: Número ---
                    htmlOutput.append("<div style=\"").append(ROW_STYLE).append("\">");
                    htmlOutput.append("<span style=\"").append(LABEL_STYLE).append("\">🆔 Número:</span>");
                    htmlOutput.append("<span style=\"").append(DATA_STYLE).append(" font-family: monospace;\">").append(num).append("</span>");
                    htmlOutput.append("</div>");

                    // --- Linha 2: Nome ---
                    htmlOutput.append("<div style=\"").append(ROW_STYLE).append("\">");
                    htmlOutput.append("<span style=\"").append(LABEL_STYLE).append("\">🏷️ Nome:</span>");
                    htmlOutput.append("<span style=\"").append(DATA_STYLE).append("\">").append(nome).append("</span>");
                    htmlOutput.append("</div>");
                    
                    // --- Linha 3: Género ---
                    htmlOutput.append("<div style=\"").append(ROW_STYLE).append("\">");
                    htmlOutput.append("<span style=\"").append(LABEL_STYLE).append("\">♂/♀ Género:</span>");
                    htmlOutput.append("<span style=\"").append(DATA_STYLE).append("\">").append(genero).append("</span>");
                    htmlOutput.append("</div>");
                    
                    // --- Linha 4: Nascimento ---
                    htmlOutput.append("<div style=\"").append(ROW_STYLE).append("\">");
                    htmlOutput.append("<span style=\"").append(LABEL_STYLE).append("\">📅 Nascido:</span>");
                    htmlOutput.append("<span style=\"").append(DATA_STYLE).append("\">").append(dataNascimento+" "+strEstado).append("</span>");
                    htmlOutput.append("</div>");
                                        
                    htmlOutput.append("</div>"); // Fecha BODY_STYLE
                    htmlOutput.append("</div>"); // Fecha CONTAINER_STYLE
                    
                    return htmlOutput.toString();
                
                } else {
                    // Caso o aluno não seja encontrado
                    return "<p style=\"color: orange; font-weight: bold; padding: 10px;\"> ❌ Não foi encontrado o aluno com o número: " + numero + ".</p>";
                }
            }

        } catch (SQLException e) {
            // Retorna a mensagem de erro em HTML
            System.err.println("----- SQLException de Consulta Ficha -----");
            System.err.println("Message:  " + e.getMessage());
            return "<p style=\"color: red; font-weight: bold; padding: 10px;\">❌ Erro ao consultar a ficha do aluno. Detalhes de erro no log do sistema.</p>";
        } 
    }
	/**
	 * Atualiza a foto de um aluno na tabela das fotos, garantindo a atomicidade
	 * das operações DELETE e INSERT através de transações explícitas.
	 */
	public static boolean alterarFoto(InputStream input, Integer numero) {
	    if (input == null) {
	        System.out.println("❌ A fotografia fornecida é inválida!.");
	        return false;
	    }

	    Configura configuradorBD = new Configura();

	    // 1. Desliga o Auto-Commit para iniciar a transação explícita
	    try (Connection conn = configuradorBD.getConnection(false)) {	 
    				// no MySQL, podia ser usado REPLACE 
	            // 2. DELETE
	    			String sqlDelete = "DELETE FROM FOTO WHERE numero = ?";
	            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
	                pstmtDelete.setInt(1, numero);
	                pstmtDelete.executeUpdate();
	            }

	            // 3. INSERT
	    	    		String sqlInsert = "INSERT INTO FOTO (numero, conteudo) VALUES (?, ?)";
	            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
	                pstmtInsert.setInt(1, numero);
	                pstmtInsert.setBinaryStream(2, input); 
	                
	                if (1 == pstmtInsert.executeUpdate()) {
	                    conn.commit(); // Confirma (grava) ambas as operações
	                    System.out.println("✅ Foto do aluno Nº " + numero + " atualizada com sucesso.");
	                    return true;
	                } else {
	                    conn.rollback(); // Reverte se a inserção falhar
	                    System.out.println("❌ A inserção da nova foto falhou inesperadamente.");
	                }
	            }
	    } catch (SQLException e) {
	        // Tratamento final de erros
	        System.out.println("❌ Erro ao atualizar a foto do aluno.");
	        System.err.println("----- SQLException de Alteração de Foto -----");
	        System.err.println("SQLState:  " + e.getSQLState());
	        System.err.println("Message:  " + e.getMessage());
	        System.err.println("Vendor:  " + e.getErrorCode());
	    }
	    return false;
	}
	
	/**
	 * Atualiza a foto de um aluno na tabela das fotos
	 * 
	 * @param input		array com a fotografia
	 * @param numero	do aluno
	 * @return			true se correr bem
	 */
	public static boolean alterarFoto(byte[] input, Integer numero) {
		 ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
		 boolean ret=alterarFoto(byteStream, numero);
		 try {
			byteStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return false;
		}
		 return ret;
	}
	/**
	 * Atualiza a foto de um aluno na tabela das fotos, lendo-a a partir de um ficheiro.
	 * @param pathFoto	Caminho completo do ficheiro com a fotografia.
	 * @param numero		Número do aluno.
	 * @return			true se a foto for lida e atualizada com sucesso, false caso contrário.
	 */
	public static boolean alterarFoto(String pathFoto, Integer numero) {
	    File ft = new File(pathFoto);
	    
	    // 1. Verificação inicial: O ficheiro existe?
	    if (!ft.exists()) {
	        System.out.println("❌ Erro: Ficheiro de fotografia não encontrado no caminho: " + pathFoto);
	        return false;
	    }
	    
	    // 2. Tenta ler o ficheiro usando try-with-resources
	    try (FileInputStream inputfile = new FileInputStream(ft)) {
	        
	        // 3. Delega a operação de base de dados para o método InputStream
	        return alterarFoto(inputfile, numero);
	        
	    } catch (FileNotFoundException e) {
	        // Exceção de ficheiro não encontrado (embora já tenhamos verificado, é boa prática)
	        System.out.println("❌ Erro de Leitura: O ficheiro '" + pathFoto + "' não foi encontrado.");
	        System.err.println("Detalhe: " + e.getMessage());
	    } catch (IOException e) {
	        // Erro durante a leitura ou fecho do stream
	        System.out.println("❌ Erro de I/O: Falha ao ler ou fechar o ficheiro de fotografia.");
	        System.err.println("Detalhe: " + e.getMessage());
	    }
	    
	    return false;
	}

    // ----------------------------------------------------------------------
    // MÉTODO MAIN E MENU DE TESTE
    // ----------------------------------------------------------------------
    
	/**
	 * Método principal (Entry Point) da classe Aluno.
	 * Implementa um menu básico na consola para testar as funcionalidades.
	 * @param args Argumentos de linha de comandos (não utilizados).
	 */
	public static void main(String[] args) {
	    
	    System.out.println("=============================================");
	    System.out.println("        🧪 Menu - Aluno            ");
	    System.out.println("=============================================");
	    char opcao;
	    do {
	        System.out.println("\n‍🎓 > Alunos:");
	        System.out.println("a. 🧑 Inserir Novo Aluno");
	        System.out.println("b. 🔢 Procurar Aluno por Número");
	        System.out.println("c. 🔍 Procurar Alunos por Nome");
	        System.out.println("d. 📅 Procurar Alunos por Nascimento");
	        System.out.println("e. 🔄 Alterar Nome do Aluno");
	        System.out.println("f. 🖼️ Alterar Foto do Aluno");
	        System.out.println("z. 🔙 Sair");
	        System.out.println("---------------------------------------------");
	        
	        System.out.println("Opção: ");
	        opcao = Character.toLowerCase(IOx.inChar());
	        switch (opcao) {
	            case 'a':
	                System.out.println("\n--- 🧑 INSERIR NOVO ALUNO ---");
	                // Assumindo a existência do método 'novo()' na classe Aluno
	                novo();
	                break;
	            case 'b':
	                System.out.println("\n--- 🔢 PROCURAR POR NÚMERO ---");
	                procurarNumero();
	                break;
	            case 'c':
	                System.out.println("\n--- 🔍 PROCURAR POR NOME ---");
	                procurarNome();
	                break;
	            case 'd':
	                System.out.println("\n--- 📅 PROCURAR POR NASCIMENTO ---");
	                procurarNascido();
	                break;
	            case 'e':
	                System.out.println("\n--- 🔄 ALTERAR NOME ---");
	                alterarNome();
	                break;
	            case 'f':
	                System.out.println("\n--- 🖼️ ALTERAR FOTO DO ALUNO ---");
	                // 1. Pede o número do aluno
	                Integer numero = Input.getCNmrAluno();
	                
	                // 2. Pede o caminho do ficheiro
	                System.out.println("Introduza o caminho completo da fotografia (ex: C:/fotos/aluno.jpg): ");
	                String pathFoto = IOx.in();
	                
	                // 3. Chama o método de atualização
	                if (alterarFoto(pathFoto, numero)) {
	                	 	apresentarFoto(numero);
	                    // Mensagem de sucesso já é tratada no alterarFoto(InputStream, Integer)
	                } else {
	                    // A mensagem de erro específica é tratada dentro do método.
	                    System.out.println("Tentativa de alteração de foto falhou. Verifique os logs de erro.");
	                }
	               
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