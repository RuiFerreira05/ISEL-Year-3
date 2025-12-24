package prototype;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCDemo {

    private static final String DATABASE = "coffees";
    private static final String SERVER = "localhost";
    private static final String URL = "jdbc:mysql://" + SERVER +":3306/" + DATABASE;
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        // Usa Try-with-Resources para fechar automaticamente a conexão (Java 7+)
        // Não é necessário Class.forName() devido ao registo automático (JDBC 4.0+)
        try (@SuppressWarnings("unused")
		Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

         // Estabelecer a ligação
         // O DriverManager encontra automaticamente o driver registado no classpath
            System.out.println("✅ Conexão ao MySQL estabelecida!");

         // ... Lógica de execução de instruções SQL (Statement, PreparedStatement)
            System.out.println("🔌 Vai fechar a conexão à base de dados... ");
        } catch (SQLException e) {
            // O ClassNotFoundException já não é necessário capturar aqui
            System.err.println("❌ Erro de conexão à BD: " + e.getMessage());
        }
    // O conn.close() é invocado automaticamente ao sair do bloco try-with-resources
        System.out.println("👍 Finalizou a execução. ");
    }
}
