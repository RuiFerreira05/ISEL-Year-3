package prototype;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Manipula;

public class ManipluaDemo {
    
    /**
     * Executa comandos DDL e DML iniciais para configurar o ambiente de teste.
     * @param db A instância da classe Manipula.
     */
    private static void setupDatabase(Manipula db) {
        System.out.println("\n--- ⚙️ Configuração da Base de Dados ---");
        
        // 1. Comando DDL: Eliminar a tabela se já existir (para garantir um teste limpo)
        System.out.println("A tentar eliminar a tabela FANTASMA (se existir)...");
        // Nota: O método xDirectiva sem parâmetros é usado para DDL/DML não seguros.
        db.xDirectiva("DROP TABLE FANTASMA");
        
        // 2. Comando DDL: Criar a tabela FANTASMA
        String createSQL = "CREATE TABLE FANTASMA ("
                         + "ID INT PRIMARY KEY, "
                         + "NOME VARCHAR(100) NOT NULL, "
                         + "IDADE INT, "
                         + "ALTURA DECIMAL(3,2))";
        
        System.out.println("A criar a tabela FANTASMA...");
        db.xDirectiva(createSQL);
        
        // 3. Inserir dados iniciais (DML não seguro, apenas para setup)
        System.out.println("A inserir dados iniciais...");
        db.xDirectiva("INSERT INTO FANTASMA VALUES (1, 'Ana Silva', 22, 1.65)");
        db.xDirectiva("INSERT INTO FANTASMA VALUES (2, 'Rui Costa', 25, 1.78)");
        db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (4, 'Dionísio Computação', 20, 1.80)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (5, 'Érica Programadora', 21, 1.63)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (6, 'Zero Divisão', 18, 1.95)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (7, 'Gilberto Debugger', 24, 1.70)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (8, 'António Código', 22, 1.75)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (9, 'Lia SQL Injection', 19, 1.55)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (10, 'Manual Ajuda', 28, 1.79)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (11, 'Octávio Recursivo', 20, 1.82)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (12, 'P. P. Parênteses', 23, 1.68)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (13, 'Rita Ctrl C', 21, 1.60)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (14, 'Salomé Stack Overflow', 25, 1.88)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (15, 'Telmo Testes', 20, 1.72)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (16, 'Úrsula Undefined', 19, 1.74)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (17, 'Vítor Vazio', 27, 1.85)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (18, 'Xavier XSS', 20, 1.76)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (19, 'Yara Y2K', 30, 1.69)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (20, 'Zack Overflow', 18, 1.81)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (21, 'Bento Bug', 26, 1.77)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (22, 'Custódia Cache', 22, 1.67)");
        	db.xDirectiva("INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (23, 'Élio Erro 404', 24, 1.84)");
        
        // 4. Configurar para modo transacional (AutoCommit = false)
        try {
            if (db.getLigacao() != null) {
                // A Configura.getConnection() usa autocommit=true por omissão.
                // Aqui forçamos a false para demonstrar o COMMIT no desligar().
                db.getLigacao().setAutoCommit(false);
                System.out.println("🔄 AutoCommit definido para false (Modo Transacional Ativo).");
            }
        } catch (SQLException e) {
            System.err.println("Aviso: Não foi possível definir AutoCommit: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        
        // 1. Inicialização da classe Manipula (que inicializa Configura por omissão)
        Manipula db = new Manipula(); // Utiliza a configuração por omissão (MySQL)
        
        // Configura o ambiente de teste
        setupDatabase(db);
        
        // -----------------------------------------------------------------
        // DEMONSTRAÇÃO 1: Execução Segura de DML (INSERT)
        // Usa o método xDirectiva com List<Object>
        // -----------------------------------------------------------------
        System.out.println("\n--- 📝 Demonstração 1: DML Seguro (INSERT) ---");
        
        String insertSQL = "INSERT INTO FANTASMA (ID, NOME, IDADE, ALTURA) VALUES (?, ?, ?, ?)";
        // Demonstra a passagem de um valor NULL para a altura (mapeado para Types.NULL)
        List<Object> params = Arrays.asList(3, "Carlos Dias", 30, null);
        
        System.out.println("A executar INSERT seguro: " + insertSQL);
        int nLinhas = db.xDirectiva(insertSQL, params);
        
        if (nLinhas!=-1) {
            System.out.println("✅ Inserção segura concluída. Linhas afetadas: " + nLinhas);
        }
        
        // -----------------------------------------------------------------
        // DEMONSTRAÇÃO 2: Execução de Queries Simples (getVString)
        // -----------------------------------------------------------------
        System.out.println("\n--- 📚 Demonstração 2: Leitura Simples (getVString) ---");
        

        String nomeFANTASMA = db.getString("SELECT NOME FROM FANTASMA WHERE ID = 2");
        System.out.println("➡️ Resultado (ID 2): " + (nomeFANTASMA != null ? nomeFANTASMA : "Não encontrado"));
        
        // Tentativa de obter um valor inexistente (deve retornar NULL)
        String nomeInexistente = db.getString("SELECT NOME FROM FANTASMA WHERE ID = 999");
        System.out.println("➡️ Resultado (ID 999): " + (nomeInexistente == null ? "NULL (Correto)" : nomeInexistente));

        
        // -----------------------------------------------------------------
        // DEMONSTRAÇÃO 3: Leitura Completa de ResultSet (getResultado)
        // -----------------------------------------------------------------
        System.out.println("\n--- 📄 Demonstração 3: Leitura Completa (getResultado) ---");
        List<Map<String, Object>> dados = db.getResultado("SELECT ID, NOME, IDADE, ALTURA FROM FANTASMA ORDER BY ID");
        
        System.out.println("==================================================");
        System.out.println("📊 RESULTADOS DA CONSULTA");
        System.out.println("==================================================");

        // 2. Iterar sobre a Lista (cada elemento é uma LINHA)
        int numLinha = 1;
        for (Map<String, Object> linha : dados) {
            
            System.out.println("\n➡️ REGISTO #" + numLinha++);
            
            // 3. Iterar sobre o Mapa (cada entrada é uma COLUNA: Nome -> Valor)
            // A iteração sobre o Set de Chaves (Nomes das Colunas) é a forma mais comum.
            
            Set<String> nomesDasColunas = linha.keySet();
            
            for (String nomeColuna : nomesDasColunas) {
                Object valorColuna = linha.get(nomeColuna);
                
                System.out.printf("   - %-15s: %s\n", nomeColuna, valorColuna);
            }
        }
        
        System.out.println("\n==================================================");
    }
}
