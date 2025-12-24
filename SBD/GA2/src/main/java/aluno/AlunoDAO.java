package aluno; // 📦 Pacote para o DAO do Aluno

import java.util.*;

import util.Configura;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
				
public class AlunoDAO {// 🧑‍🎓 Utiliza a classe POJO Aluno


    // -----------------------------------------------------------------------------------------------------------------
	// 💾 Método SAVE (Gravar Novo Aluno)
    // -----------------------------------------------------------------------------------------------------------------

	public static int save(Aluno a) {
        // Verifica se o objeto Aluno é nulo ou se não é válido (usando o método valid() do POJO)
		if (a == null || !a.valid()) 
			return -1;
            
		// SQL para inserção: a tabela 'aluno' tem (numero, nome, genero, nascido)
		String cmd = "INSERT INTO aluno (numero, nome, genero, nascido) VALUES (?, ?, ?, ?)";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		// Usa try-with-resources para garantir o fecho automático da Connection e do PreparedStatement
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			// Os valores são definidos usando o PreparedStatement. 
            // Não é necessário o replaceAll("'", "''") em strings, pois o PreparedStatement trata o escaping.
			ps.setInt(1, a.getNumero());
			ps.setString(2, a.getNome());
            // Conversão de Character para String para o CHAR(1) no SQL
			ps.setString(3, String.valueOf(a.getGenero())); 
			ps.setDate(4, a.getNascido());

			nRows = ps.executeUpdate(); // Executa a inserção
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao inserir o aluno: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

	public static int save_(Aluno a) {
		if (a == null) 
			return -1;
            
		// SQL para inserção: a tabela 'aluno' tem (?, nome, genero, nascido)
		String cmd = "INSERT INTO aluno (numero, nome, genero, nascido) "+
					 " SELECT (select coalesce(max(numero), 0)+1 from aluno)"+",'"
					 			+a.getNome().replaceAll("'", "''")+"','"
					 			+a.getGenero()+"','"+
					 			 a.getNascido()+"'";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		// Usa try-with-resources para garantir o fecho automático
		try (Connection con = new Configura().getConnection(); 
             Statement st = con.createStatement()) {
			
			nRows = st.executeUpdate(cmd); // Executa a inserção
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao inserir o aluno: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

    // -----------------------------------------------------------------------------------------------------------------
	// 📝 Método UPDATE (Atualizar Aluno)
    // -----------------------------------------------------------------------------------------------------------------

	public static int update(Aluno a, Aluno o) {
        // Validação dos novos dados (a) e dos dados originais (o)
		if (a == null || !a.valid())
			return -1;
		if (o == null || !o.valid())
			return -1;
            
		// SQL para atualização: WHERE usa a chave primária (numero) e pode incluir outros campos (nome) para segurança, 
        // mas a chave primária 'numero' é suficiente para identificar o registo a ser alterado.
        // O AlunoDAO original usa o numero como identificador único.
		String cmd = "UPDATE aluno SET numero = ?, nome = ?, genero = ?, nascido = ? WHERE numero = ?";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			// SET: Novos Valores (a)
			ps.setInt(1, a.getNumero());
			ps.setString(2, a.getNome());
			ps.setString(3, String.valueOf(a.getGenero()));
			ps.setDate(4, a.getNascido());
            
			// WHERE: Chave de identificação Original (o)
            // Assumimos que o 'numero' é a chave para encontrar o registo original.
			ps.setInt(5, o.getNumero()); 

			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao atualizar o aluno: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

    // -----------------------------------------------------------------------------------------------------------------
	// 🗑️ Método DELETE (Apagar Aluno)
    // -----------------------------------------------------------------------------------------------------------------
	public static int delete(Aluno a) {
		return delete(a.getNumero());
	}
	public static int delete(Integer numero) {
        // A eliminação baseia-se na chave primária (numero)
		if (numero == null) 
			return -1;
            
		// SQL para eliminação, usando o número como identificador único.
		String cmd = "DELETE FROM aluno WHERE numero = ?"; 
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			// Condição WHERE: Número do Aluno a eliminar
			ps.setInt(1, numero); 
			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao apagar o aluno: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

	public static int getNInscricoes(String numero) {
		return getNInscricoes(numero, true);  // ultimas
	}
	
	public static int getNInscricoes(String numero, boolean ativas) {
		if (numero == null)
			return 0;
		String objecto = "inscricao";  	// acede a todas
		if(ativas)
			objecto="inscricoes";	// acede às ultimas onde pode lançar notas
		String cmd = "SELECT count(numero) FROM "+objecto+" WHERE numero = ?";
		
		try (Connection con = new Configura().getConnection();
			 PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, numero);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao contar as inscrições: " + e.getMessage());
		}
		return 0;
	}
	
	public static int getNAvaliacoes(String numero) {
		if (numero == null)
			return 0;
		
		String cmd = "SELECT count(numero) FROM avaliacoes WHERE numero = ?";
		try (Connection con = new Configura().getConnection();
			 PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, numero);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao contar as avaliacoes: " + e.getMessage());
		}
		return 0;
	}
	
    // -----------------------------------------------------------------------------------------------------------------
	// 🔍 Método GET BY NUMERO (Consultar por Chave Primária)
    // -----------------------------------------------------------------------------------------------------------------
	public static Aluno getByNumero(String numero) {
		if (numero == null || numero.isEmpty()) 
			return null;
		return getByNumero(Integer.parseInt(numero));
	}
	
	public static Aluno getByNumero(Integer numero) {
        // Validação da chave primária
		if (numero == null || numero <= 0) // Usamos numero <= 0 para garantir que é um número válido (CHECK constraint)
			return null;
            
		String cmd = "SELECT nome, genero, nascido FROM aluno WHERE numero = ?";
		Aluno a = null;
		int nRows = 0; // Inicializado a 0, ou -1, dependendo da sua preferência de log
        
		System.out.println("Executa a instrução SQL: [" + cmd.replace("?", numero.toString()) + "]");
        
		// Atenção: O ResultSet é criado dentro do bloco try-with-resources.
		try (Connection con = new Configura().getConnection();
			 PreparedStatement ps = con.prepareStatement(cmd)) {
                
			ps.setInt(1, numero); // Define o parâmetro da condição WHERE
            
            // É necessário executar o ps.executeQuery() DENTRO do try ou fora, e fechar o ResultSet.
            // O padrão do seu DAO original é mais simples, mas JDBC recomenda fechar o ResultSet.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Preenche o POJO com os dados encontrados
                    // O número já é conhecido, então não precisamos do 'SELECT numero'
                    a = new Aluno();
                    a.setNumero(numero);
                    a.setNome(rs.getString("nome"));
                    // Converte a string char(1) para Character
                    String generoStr = rs.getString("genero");
                    a.setGenero(generoStr != null && !generoStr.isEmpty() ? generoStr.charAt(0) : 'x');
                    a.setNascido(rs.getDate("nascido"));
                    nRows = 1; // Um registo encontrado
                }
            } // rs.close() chamado automaticamente
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao consultar o aluno: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return a;
	}

    // -----------------------------------------------------------------------------------------------------------------
	// 🌐 Método GET ALL (Consultar Todos)
    // -----------------------------------------------------------------------------------------------------------------

	public static List<Aluno> getAll() {
		List<Aluno> list = new ArrayList<>();
		String cmd = "SELECT numero, nome, genero, nascido FROM aluno ORDER BY numero";
		int nRows = 0;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection();
			 PreparedStatement ps = con.prepareStatement(cmd);
			 ResultSet rs = ps.executeQuery()) { // ExecuteQuery pode ser colocado no try-with-resources se não precisar de parâmetros
            
            // Como não há parâmetros, o seu código original executaria a query aqui: ps.executeQuery()
			// Para manter o padrão, criamos o ResultSet no bloco try-with-resources.
			
            // Itera sobre todos os resultados
			while (rs.next()) {
                Aluno a = new Aluno();
                a.setNumero(rs.getInt("numero"));
                a.setNome(rs.getString("nome"));
                String generoStr = rs.getString("genero");
                a.setGenero(generoStr != null && !generoStr.isEmpty() ? generoStr.charAt(0) : 'x');
                a.setNascido(rs.getDate("nascido"));
				
                list.add(a);
				nRows += 1;
			}
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao consultar os alunos: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return list;
	}
	
	/**
	 * Retorna uma lista formatada de todas as disciplinas que um aluno (identificado por 'numero')
	 * AINDA NÃO TEM avaliações registadas.
	 * @param numero O número do aluno (ID).
	 * @return String com as designações das disciplinas separadas por vírgula, com o 
	 * último separador substituído por " e ainda ". Ex: "Matemática, Inglês e ainda Química".
	 * @throws SQLException Se ocorrer um erro de acesso à base de dados.
	 */
	public static String emFalta(Integer numero) throws SQLException {
	    
	    // SQL Otimizado: Usamos NOT IN ou NOT EXISTS, que são mais semânticos e, 
	    // frequentemente, mais performantes do que LEFT JOIN IS NULL, dependendo do SGBD.
	    // Usamos o padrão NOT IN, que é mais fácil de ler.
	    String directiva =
	            "SELECT designacao FROM disciplina " + 
	            "WHERE designacao NOT IN (" +
	                "SELECT designacao FROM avaliacoes WHERE numero = ?) " +
	            "ORDER BY designacao";
	    

	    try (Connection con = new Configura().getConnection(); 
	         PreparedStatement ps = con.prepareStatement(directiva)) {
	             
	        ps.setInt(1, numero); // Define o parâmetro '?' com o numero do aluno.
	         
	        try (ResultSet rs = ps.executeQuery()) {
	            
	            // 1. Usa StringJoiner para construir a lista com o separador ", "
	            StringJoiner sj = new StringJoiner(", ");
	            while (rs.next()) 
	                sj.add(rs.getString("designacao"));
	            
	            String lista = sj.toString();
	            
	            // 2. Verifica se existe alguma disciplina
	            if (lista.isEmpty()) 
	                return "";
	
	            // 3. Aplica a formatação final: 
	            //    Substituir a última ocorrência de ", " por " e ainda ".
	            //    Chamamos o método 'replaceLast' (assumindo que já o implementou).
	            return lista;
	        }
	    }
	}
	public static String resumo(String numero) throws SQLException {
		return resumo(Integer.parseInt(numero));
	}
	
	/**
	 * Apresenta resumo textual do registo do aluno.
	 * 
	 * @param numero do aluno
	 * @return resumo textual
	 */
	public static String resumo(Integer numero) throws SQLException {
		String directiva = "select numero, nome, genero, nascido, "+
				"(select coalesce(avg(nota),-1) from inscricao i where i.numero=a.numero) media "+
				"from aluno a where numero=?";
		
	    try (Connection con = new Configura().getConnection(); 
		     PreparedStatement ps = con.prepareStatement(directiva)) {
		
	        ps.setInt(1, numero); // Define o parâmetro '?' com o numero do aluno.
	        String Disciplinas = emFalta(numero);
	        int nDisciplinas = (Disciplinas.split(", ")).length;
	        Disciplinas = util.DataFormatter.replaceLast(Disciplinas, ", ", " e ainda ");
	        String texto = "";
	        try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {	// só pode encontrar 1 linha
					if (rs.getString("genero").compareTo("F") == 0)
						texto = "A";
					else
						texto = "O";
					texto = texto + "  estudante " + util.Name.normalize(rs.getString("nome")) + " com o número "
					+ rs.getString("numero") + "," + " " + util.Data.saber(rs.getDate("nascido").toLocalDate());
					
					BigDecimal med = rs.getBigDecimal("media");
				
					if (med.compareTo(new BigDecimal(-1)) != 0) {
						if (nDisciplinas > 0) {
							texto = texto + ". A média do curso é atualmente " + util.DataFormatter.NotaToString(med) + " valores.";
							texto = texto + " Para concluir o curso, falta obter aproveitamento ";
							if (nDisciplinas == 1)
								texto = texto + "numa disciplina, designadamente " + Disciplinas + ".";
							else
								texto = texto + " em " + nDisciplinas + " disciplinas, nomeadamente: " + Disciplinas + ".";
						} else
							texto = texto + ". Concluiu o curso com média final de " 
							+ med.setScale(0, RoundingMode.HALF_UP) + " valores.";
					} else
						texto = texto + ". Ainda não tem registadas avaliações.";
				} else
					System.out.println("Não foram encontradas inscrições do estudante como o número: " + numero + ".");
	        }  // try
		return texto;
	    }  // try
	}
	
	/**
	 * 🧱 Classe Auxiliar para armazenar o limite inferior e superior
	 * de cada segmento de filtro a ser gerado.
	 */
	
	static class LimiteFiltro {
	    String inferior; // O valor que define o início do segmento (ex: 'A')
	    String superior; // O valor que define o fim do segmento (ex: 'M')

	    /**
	     * Construtor da estrutura de dados LimiteFiltro.
	     * @param inferior Valor inicial do filtro.
	     * @param superior Valor final do filtro.
	     */
	    public LimiteFiltro(String inferior, String superior) {
	        this.inferior = inferior;
	        this.superior = superior;
	    }
	}
	  
    /**
	 * 📊 Prepara a lista de limites de filtro (inferior e superior) 
     * baseada na coluna e no número de divisões (páginas) desejadas.
	 * * @param coluna A coluna SQL a ser filtrada (ex: "Nome", "Numero").
	 * @param numPaginas O número de divisões ou links de filtro que se quer gerar.
	 * @return Uma lista de objetos LimiteFiltro, ou uma lista vazia se não houver registos.
	 * @throws SQLException Caso haja um erro de acesso à base de dados.
	 */
	private static List<LimiteFiltro> preparaFiltro(String coluna, int numPaginas) throws SQLException {
		// ➡️ SQL para obter todos os valores da coluna ordenados, que servirão de base para a divisão.
        String sql = "select " + coluna + " from aluno order by 1";
        List<LimiteFiltro> limites = new ArrayList<>();
        
        // 🔗 Utilização do bloco try-with-resources para garantir o fecho automático dos recursos JDBC.
	    try (Connection con = new Configura().getConnection(); 
			 Statement st = con.createStatement(
					 ResultSet.TYPE_SCROLL_INSENSITIVE,  // Permite mover o cursor livremente (rs.absolute)
  					 ResultSet.CONCUR_UPDATABLE);
	    		 ResultSet rs = st.executeQuery(sql)) {
			
            // 🔢 Contar o número total de registos (linhas).
			int rowCount = (rs.last()) ? rs.getRow() : 0;
			if(rowCount<numPaginas*2) // não vale a pena gerar o filtro 
				return limites;
			if (rowCount > 0) {
				rs.beforeFirst(); // Volta ao início para começar a leitura.
				int dimPagina = rowCount / numPaginas; // Tamanho base de cada segmento (arredondado para baixo).
				int resto = rowCount % numPaginas;      // O que sobra da divisão, para ser adicionado ao último segmento.
				
                // 🔄 Iterar pelo número de segmentos (páginas) que queremos criar.
				for (int i = 0; i < numPaginas; i++) {
					
                    // 🚩 Ponto de partida (Inferior): Move o cursor para o índice inicial do segmento atual.
					rs.absolute(i * dimPagina + 1);
					String inferior = rs.getString(coluna);
					
                    // 🏁 Ponto de chegada (Superior): Calcula o índice final.
					int fim = (i + 1) * dimPagina;
					if((i + 1) == numPaginas) {
						fim += resto; // Adiciona o resto dos registos à última divisão.
					}
                    
					rs.absolute(fim);
					String superior = rs.getString(coluna);
					
					// 📦 Armazena os limites na lista de retorno.
                    limites.add(new LimiteFiltro(inferior, superior));
				}
			}
            return limites; // Retorna a lista de dados (Limites)
		}
	}
		
	/**
	 * 🎨 Gera a string HTML contendo os links de filtro a partir dos limites fornecidos.
	 * @param coluna A coluna que está a ser filtrada (necessário para o JS).
	 * @param numPaginas O número de divisões ou links de filtro que se quer gerar.
	 * @return A string HTML formatada dos links de filtro.
	 */
	public static String geraFiltro(String coluna, int numPaginas) {
		List<LimiteFiltro> limites;
		try {
			limites = preparaFiltro(coluna, numPaginas);
		} catch (SQLException e) {
			return "?";
		}
		int dim = 7; // ✨ Número máximo de caracteres a mostrar no corpo do link (pré-visualização).
        StringBuilder links = new StringBuilder(); // Usar StringBuilder é mais eficiente para concatenar muitas strings.
        if (limites.isEmpty()) {
            return "?"; // Retorna um placeholder se não houver dados.
        }
        
        // 🏗️ Itera sobre a lista de limites para construir um link para cada intervalo.
        for (int i = 0; i < limites.size(); i++) {
            LimiteFiltro limite = limites.get(i);
            
            // 🛡️ Tratamento de segurança: Substituir aspas simples (single quotes) por &#39;
            // Isto é crucial para que os valores sejam passados corretamente nos atributos HTML 
            // e dentro da chamada JavaScript (href='...').
			String inferior_ = limite.inferior.replaceAll("'", "&#39;");
			String superior_ = limite.superior.replaceAll("'", "&#39;");

            // ✂️ Texto de pré-visualização (ex: "Maria..Pedro")
            String textoLinkInferior = limite.inferior.substring(0, Math.min(limite.inferior.length(), dim));
            String textoLinkSuperior = limite.superior.substring(0, Math.min(limite.superior.length(), dim));
            String textoLink = textoLinkInferior + ".." + textoLinkSuperior;
            
            // 📝 Constrói o elemento <a>
            links.append("<a name='filtro'" 
                    + " title='Filtro: "+coluna+" de " + inferior_ + " até " + superior_ + "'"
                    + " class='button'"
                    
                    // ⚡ Chamada JavaScript: Define os campos ocultos do formulário (MyFrm) e faz o submit.
					+ " href='javascript:ativar(\""+coluna+"\",\""+inferior_+"\",\""+superior_+"\");'>" 
					
					+ textoLink // Texto visível do link
					+ "</a>&nbsp;&nbsp;"); // Espaçamento entre links
        }
        return links.toString();
	}
}
