package gestor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement; 
import java.sql.ResultSet;
import java.sql.SQLException;

import util.Configura;
import util.IOx;
import util.DataFormatter;

/**
 * Classe responsável pela gestão (CRUD e Contagem) da entidade Inscrição/Avaliações.
 */
public final class Inscricoes {
	
	// ----------------------------------------------------------------------
    // MÉTODOS DE NEGÓCIO (API Pública)
    // ----------------------------------------------------------------------

    /**
	 * Versão de conveniência. Instancia o Configura e chama o método original.
	 * @return true se a inserção for bem-sucedida, false caso contrário.
	 */
    public static boolean inscrever() {
        return inscrever(new Configura());
    }

	/**
	 * Pergunta o número do aluno e o código da disciplina, 
	 * realizando a inscrição no ano atual.
	 * @param configuradorBD Objeto de configuração para obter a conexão.
	 * @return true se a inserção for bem-sucedida, false caso contrário.
	 */
	public static boolean inscrever(Configura configuradorBD) {
		Integer numero = Input.getCNmrAluno();
		String codigo = Input.getCCodDis();
		int ano = configuradorBD.today().getYear(); 
		// SQL SEGURO: Query de inserção
		String sqlInsert = "INSERT INTO inscricao (numero, ano, codigo) VALUES (?, ?, ?)";
		
		// Try-with-resources para garantir que Connection e PreparedStatement fecham.
		try (Connection conexao = configuradorBD.getConnection();
	         PreparedStatement preparacao = conexao.prepareStatement(sqlInsert)){
			
			// 1. Vincula os valores aos marcadores de posição
			preparacao.setInt(1, numero);
			preparacao.setInt(2, ano);
			preparacao.setString(3, codigo);
			
			// 2. Executa a inserção
			if (1 == preparacao.executeUpdate()) {
				System.out.println(
						"✅ Foi realizada com sucesso a inscrição do aluno Nº " + numero + " na disciplina '" + codigo
						+ "' no ano " + ano + ".");
				return true;
			}
			
			System.out.println(
					"Não foi possível realizar a inscrição do aluno Nº " + numero + " na disciplina '" + codigo + "'.");
		
		} catch (SQLException e) {
			System.out.println("❌ Erro ao fazer a inscrição do aluno. (Possível inscrição duplicada ou erro de BD).");
			System.err.println("----- SQLException de Inscrição -----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		} 
		return false;
	}

    /**
	 * Versão de conveniência. Instancia o Configura e chama o método original.
	 * @return true se a atualização foi bem-sucedida, false caso contrário.
	 */
    public static boolean lancar() {
        return lancar(new Configura());
    }
	
	/**
	 * Pede o código da disciplina, o ano, o número do aluno e a nota. 
	 * Atualiza a nota na tabela 'inscricao' para a inscrição correspondente,
	 * APENAS se a nota ainda não tiver sido atribuída (nota IS NULL).
	 * @param configuradorBD Objeto de configuração para obter a conexão.
	 * @return true se a atualização foi bem-sucedida (ou não houve erro técnico), false caso contrário.
	 */
	public static boolean lancar(Configura configuradorBD) {
		String codigo = Input.getCCodDis();
		Integer ano = Input.getCAnoDis();
		Integer numero = Input.getCNmrAluno();
		BigDecimal nota = Input.getCNotaDis();
		
		// SQL SEGURO: Atualiza a nota, apenas se for NULL (garantindo que só é lançada uma vez)
		String sqlUpdate = "UPDATE inscricao SET nota = ? WHERE numero = ? AND ano = ? AND codigo = ? AND nota IS NULL";
		
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sqlUpdate)) {
			
			// 1. Vincula os valores
			preparacao.setBigDecimal(1, nota);
			preparacao.setInt(2, numero);
			preparacao.setInt(3, ano);
			preparacao.setString(4, codigo);
			
			// 2. Execução da atualização
			int linhasAtualizadas = preparacao.executeUpdate();
			
			if (linhasAtualizadas == 1) {
				System.out.println("✅ A nota " + DataFormatter.NotaToString(nota) + " do aluno nº " + numero + " na disciplina '"
						+ codigo + "' do ano de " + ano + " foi lançada com sucesso.");
				return true;
			} else {
				// Se linhasAtualizadas for 0, a inscrição não existe ou a nota já foi lançada.
				System.out.println("⚠️ Confirme os dados fornecidos. Não foi possível lançar a nota do aluno Nº " + numero
		+ " na disciplina '" + codigo + "' relativa ao ano de " + ano + " (Inscrição inexistente ou nota já lançada).");
				return false;
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro ao lançar a nota do aluno.");
			System.err.println("----- SQLException de Lançamento de Nota -----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		return false;
	}

    /**
	 * Versão de conveniência. Instancia o Configura e chama o método original.
	 * @param numero Número do aluno.
	 * @return O número de avaliações, ou 0 em caso de erro.
	 */
    public static int temCertificado(Integer numero) {
        return temCertificado(new Configura(), numero);
    }
	
	/**
	 * Conta o número de notas já lançadas na vista com avaliações 
	 * para verificar o progresso do aluno para o certificado.
	 * @param configuradorBD Objeto de configuração para obter a conexão.
	 * @param numero Número do aluno.
	 * @return O número de avaliações, ou 0 em caso de erro.
	 */
	public static int temCertificado(Configura configuradorBD, Integer numero) {
		// Se o número for nulo, retorna 0 imediatamente
		if(numero == null) 
			return 0;
		
		String sqlSelect = 	"SELECT COUNT(numero) conta FROM avaliacoes WHERE numero = ?";
		
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
			
			preparacao.setInt(1, numero);
			
			try (ResultSet rs = preparacao.executeQuery()) {
				if(rs.next())
					return rs.getInt(1); // Retorna a primeira coluna (COUNT)
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro de Consulta de Certificado do Aluno.");
            System.err.println("----- SQLException na Consulta de Certificado do Aluno -----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		return 0;
	}

    /**
	 * Versão de conveniência. Instancia o Configura e chama o método original.
	 * @param numero Número do aluno.
	 * @return A contagem de inscrições, ou 0 em caso de erro.
	 */
    public static int temInscricoes(Integer numero) {
        return temInscricoes(new Configura(), numero);
    }
	
	/**
	 * Retorna a quantidade de inscrições ativas (ou registadas) do aluno.
	 * @param configuradorBD Objeto de configuração para obter a conexão.
	 * @param numero Número do aluno.
	 * @return A contagem de inscrições, ou 0 em caso de erro.
	 */
	public static int temInscricoes(Configura configuradorBD, Integer numero) {
		// Se o número for nulo, retorna 0 imediatamente
		if(numero == null) return 0;
		
		String sqlSelect = "SELECT COUNT(numero) conta FROM inscricoes WHERE numero = ?";
		
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
			
			preparacao.setInt(1, numero);
			
			try (ResultSet rs = preparacao.executeQuery()) {
				if(rs.next())
					return rs.getInt(1); // Retorna a contagem
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro na contagem de Inscrições de Aluno.");
			System.err.println("----- SQLException de Inscrições de Aluno -----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		return 0;
	}

    // --- Versão sobrecarregada para Qtd Inscrições (Sem Configura) ---
    /**
	 * Versão de conveniência. Instancia o Configura e chama o método original.
	 * @param disc Código da disciplina.
	 * @return A contagem de inscrições, ou 0 em caso de erro.
	 */
    public static int qInscricoes(String disc) {
        return qInscricoes(new Configura(), disc);
    }
	
	/**
	 * Retorna a quantidade de inscrições ativas (ou registadas) para uma determinada disciplina.
	 * @param configuradorBD Objeto de configuração para obter a conexão.
	 * @param disc Código da disciplina.
	 * @return A contagem de inscrições, ou 0 em caso de erro.
	 */
	public static int qInscricoes(Configura configuradorBD, String disc) {
		if(disc==null || disc.trim().isEmpty())
			return 0;
		
		String sqlSelect = "SELECT COUNT(codigo) conta FROM inscricoes WHERE codigo = ?";
		
		try (Connection conexao = configuradorBD.getConnection();
			 PreparedStatement preparacao = conexao.prepareStatement(sqlSelect)) {
			
			preparacao.setString(1, disc);
			
			try (ResultSet rs = preparacao.executeQuery()) {
				if(rs.next())
					return rs.getInt(1); // Retorna a contagem
			}
		} catch (SQLException e) {
			System.out.println("❌ Erro na contagem de Inscrições na Disciplina.");
			System.err.println("----- SQLException de Inscrições na Disciplina -----");
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Message:  " + e.getMessage());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		return 0;
	}

    // ----------------------------------------------------------------------
    // MÉTODO MAIN E MENU DE TESTE
    // ----------------------------------------------------------------------
    
    /**
     * Método principal (Entry Point) da classe, implementando um menu de teste
     * para interagir com a gestão de Inscrições/Avaliações.
     * @param args Argumentos de linha de comandos (não utilizados).
     */
    public static void main(String[] args) {
        
        Configura configuradorBD = new Configura(); 
        
        System.out.println("=============================================");
        System.out.println("        🧪 Menu - Avaliações       ");
        System.out.println("=============================================");
        char opcao;
        do {
            System.out.println("\n📜 > Avaliações:");
            System.out.println("a. 📝 Inscrever Aluno");
            System.out.println("b. 💯 Lançar Nota");
            System.out.println("c. 📊 Contar Inscrições de um Aluno");
            System.out.println("d. 🎓 Contar Avaliações de um Aluno");
            System.out.println("e. 📚 Contar Inscrições numa Disciplina");
            System.out.println("z. 🔙 Sair");
            System.out.println("---------------------------------------------");
            
            System.out.println("Opção: ");
            opcao = Character.toLowerCase(IOx.inChar());

            switch (opcao) {
                case 'a':
                    System.out.println("\n--- 📝 INSCREVER ALUNO ---");
                    inscrever(configuradorBD); 
                    break;
                case 'b':
                    System.out.println("\n--- 💯 LANÇAR NOTA  ---");
                    lancar(configuradorBD);
                    break;
                case 'c':
                    System.out.println("\n--- 📊 CONTAR INSCRIÇÕES DO ALUNO ---");
                    Integer numAlunoIns = Input.getCNmrAluno();
                    int countIns = temInscricoes(configuradorBD, numAlunoIns); 
                    System.out.println("O Aluno Nº " + numAlunoIns + " tem " + countIns + " inscrição(ões) registada(s).");
                    break;
                case 'd':
                    System.out.println("\n--- 🎓 CONTAR AVALIAÇÕES (CERTIFICADO) ---");
                    Integer numAlunoCert = Input.getCNmrAluno();
                    int countCert = temCertificado(configuradorBD, numAlunoCert);
                    System.out.println("O Aluno Nº " + numAlunoCert + " tem " + countCert + " avaliação(ões) lançada(s).");
                    break;
                case 'e':
                    System.out.println("\n--- 📚 CONTAR INSCRIÇÕES POR DISCIPLINA ---");
                    String codDis = Input.getCCodDis();
                    int countDis = qInscricoes(configuradorBD, codDis);
                    System.out.println("A Disciplina '" + codDis + "' tem " + countDis + " aluno(s) inscrito(s).");
                    break;
                case 'z':
                case 'Z':
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