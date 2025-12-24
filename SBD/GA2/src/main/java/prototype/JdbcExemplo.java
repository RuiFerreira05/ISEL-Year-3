package prototype;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ☕ Exemplo simplificado de ligação JDBC (Java Database Connectivity).
 * Demonstra como abrir e fechar automaticamente uma ligação ao MySQL.
 */
public class JdbcExemplo {

	// 🛠️ Configurações de acesso à base de dados
	private static final String DATABASE = "coffees";
	private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE;
	private static final String USER = "root";
	private static final String PASSWORD = "root";

	public static void main(String[] args) {
		/* * 🔄 Try-with-Resources (Java 7+):
		 * Garante que a ligação 'conn' será fechada automaticamente no final do bloco,
		 * mesmo que ocorra uma exceção. Evita fugas de memória (memory leaks).
		 * * ℹ️ Nota: Desde o JDBC 4.0, não é obrigatório usar Class.forName() se o 
		 * driver estiver no Classpath, pois o registo é automático.
		 */
		try (@SuppressWarnings("unused")
		Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

			// ⚡ Se o fluxo chegar aqui, a ligação foi bem-sucedida
			System.out.println("✅ Conexão ao MySQL estabelecida com sucesso!");

			// 📝 Espaço para a lógica de negócio:
			// Aqui poderias usar Statement ou PreparedStatement para executar SELECT, INSERT, etc.
			System.out.println("⚙️ A processar lógica de base de dados...");
			
			System.out.println("🔌 Vai finalizar a conexão à base de dados... ");
			
		} catch (SQLException e) {
			/* * ⚠️ Captura de erros de SQL:
			 * Trata problemas como credenciais erradas, servidor offline ou BD inexistente.
			 */
			System.err.println("❌ Erro de conexão ao BD: " + e.getMessage());
		}
		
		/* * 🏁 O método conn.close() é invocado silenciosamente ao sair do bloco 'try'.
		 * O programa continua a execução normal após o fecho.
		 */
		System.out.println("🏁 Finalizou a execução do programa. ");
	}
}