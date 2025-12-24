package util;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Objects; 

/**
 * @author Engº Porfírio Filipe
 * 🚀 Esta classe implementa uma camada de acesso a dados otimizada e segura.
 * 1. Thread-Safety: Não possui variáveis de instância para recursos JDBC (Connection, Statement, ResultSet).
 * 2. Performance: Obtém conexões de um Pool (gerido por Configura).
 * 3. Isolamento: Os métodos DML (xDirectiva) retornam o resultado (linhas afetadas).
 */
public class Manipula {
 
    // Objeto de configuração. Declarado como 'final' para thread-safety/imutabilidade.
    private final Configura cfg; 

    /**
     * Construtor por omissão.
     */
    public Manipula() {
        this(new Configura());
    }
    
    /**
     * Construtor que recebe um objeto Configura.
     * @param cfg Objeto de Configuração (responsável por gerir o Pool de Conexões).
     */
    public Manipula(Configura cfg) {
        this.cfg = Objects.requireNonNull(cfg, "A configuração (Configura) não pode ser nula. ❌");
    }
    
    // --------------------------------------------------------------------------------
    // 🔑 MÉTODOS DE GESTÃO DA CONEXÃO
    // --------------------------------------------------------------------------------

    /**
     * 🏊 Obtém uma conexão **reutilizável** do Pool de Conexões.
     * @return Uma Connection ativa do Pool.
     * @throws SQLException Se o Pool falhar ao disponibilizar uma conexão.
     */
    public Connection getLigacao() throws SQLException {
        // Ligação com autocommit.
        return cfg.getConnection();
    }
    
    public Connection getLigacao(boolean autocommit) {
    		return cfg.getConnection(autocommit);
    }
    // --------------------------------------------------------------------------------
    // ⚙️ MÉTODOS DE EXECUÇÃO DML/DDL (Retornam INT)
    // --------------------------------------------------------------------------------

    /**
     * ⚠️ NÃO USAR COM INPUT DE UTILIZADOR, sujeito a SQL Injection.
     * Executa uma directiva SQL DML/DDL (INSERT, UPDATE, DELETE, CREATE, DROP).
     * **Thread-Safe**: Recursos criados e fechados localmente.
     * @param conexao Ligação à base de dados
     * @param directivaSQL Directiva SQL DML ou SQL DDL.
     * @return O número de linhas afetadas, ou -1 em caso de falha.
     */
    static public int xDirectiva(Connection conexao, String directivaSQL) {
        
        // 🛡️ TRY-WITH-RESOURCES: Devolve a Connection ao Pool e fecha o Statement.
        try (Statement stmt = conexao.createStatement()) {
            
            int linhasAfetadasLocal = stmt.executeUpdate(directivaSQL);
            
            System.out.println(getAfetadas(linhasAfetadasLocal));
            System.out.println("✅ Execução bem sucedida.");

            return linhasAfetadasLocal;
            
        } catch (SQLException e) {
            System.out.println("❌ Erro ao executar a directiva SQL: " + directivaSQL);
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Menssagem: " + e.getMessage());
            System.err.println("Código do Fornecedor: " + e.getErrorCode());
            return -1;
        }
    }
    
    /**
     * ⚠️ NÃO USAR COM INPUT DE UTILIZADOR, sujeito a SQL Injection.
     * Executa uma directiva SQL DML/DDL (INSERT, UPDATE, DELETE, CREATE, DROP).
     * **Thread-Safe**: Recursos criados e fechados localmente.
     * @param directivaSQL Directiva SQL DML ou SQL DDL.
     * @return O número de linhas afetadas, ou -1 em caso de falha.
     */
    public int xDirectiva(String directivaSQL) {
        try (Connection conexao = getLigacao()) {
            return xDirectiva(conexao, directivaSQL); 
        } catch (SQLException e) {
            System.out.println("❌ Erro ao executar a directiva SQL: " + directivaSQL);
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Menssagem: " + e.getMessage());
            System.err.println("Código do Fornecedor: " + e.getErrorCode());
            return -1;
        }
    }
    
    /**
     * Executa uma directiva SQL DML (INSERT, UPDATE, DELETE) de forma **SEGURA** * (PreparedStatement).
     * @param sqlSegura A directiva SQL com marcadores de posição (?).
     * @param objParametros Uma lista ordenada de objetos a serem ligados ao SQL.
     * @return O número de linhas afetadas, ou -1 em caso de falha.
     */
    public int xDirectiva(String sqlSegura, List<Object> objParametros) {
	    
	    // 🛡️ TRY-WITH-RESOURCES: Devolve a Connection ao Pool e fecha o PreparedStatement.
	    try (Connection conexao = getLigacao();
             PreparedStatement preparedStatement = conexao.prepareStatement(sqlSegura)) {
	        
	        // 1. Ligar os parâmetros (?)
	        if (objParametros != null) {
	            int index = 1;
	            for (Object param : objParametros) {
	                
	                // --- MAPEAMENTO DE TIPOS ---
	                if (param == null) {
	                    preparedStatement.setNull(index, Types.NULL); 
	                } else if (param instanceof String) {
	                    preparedStatement.setString(index, (String) param);
	                } else if (param instanceof Integer) {
	                    preparedStatement.setInt(index, (Integer) param);
	                } else if (param instanceof Long) {
	                    preparedStatement.setLong(index, (Long) param);
	                } else if (param instanceof Boolean) {
	                    preparedStatement.setBoolean(index, (Boolean) param);
	                } else if (param instanceof BigDecimal) {
	                    preparedStatement.setBigDecimal(index, (BigDecimal) param);
	                } else if (param instanceof java.util.Date) {
	                    preparedStatement.setTimestamp(index, new Timestamp(((java.util.Date) param).getTime()));
	                } else if (param instanceof Double) {
	                    preparedStatement.setDouble(index, (Double) param);
	                } else if (param instanceof Float) {
	                    preparedStatement.setFloat(index, (Float) param);
	                } else {
	                    preparedStatement.setObject(index, param); 
	                }
	                // --- FIM DO MAPEAMENTO DE TIPOS ---
	                index++;
	            }
	        }
	        
	        // 2. Executar e obter o número de linhas afetadas
	        int linhasAfetadasLocal = preparedStatement.executeUpdate();
	        

	        System.out.println(getAfetadas(linhasAfetadasLocal));
	        System.out.println("✅ Execução bem sucedida.");
	        
	        return linhasAfetadasLocal;
	        
	    } catch (SQLException e) {
	        System.out.println("❌ Erro em xDirectiva: " + e.getMessage());
	        System.out.println("❌ Falhou a execução: "+sqlSegura);
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Menssagem: " + e.getMessage());
            System.err.println("Código do Fornecedor: " + e.getErrorCode());
	        return -1; // Indica falha
	    }
	}
    
    // --------------------------------------------------------------------------------
    // 📊 MÉTODOS THREAD-SAFE DE CONSULTA (SELECT)
    // --------------------------------------------------------------------------------

    /**
     * 📊 Executa uma consulta SQL (SELECT) e copia **TODOS** os resultados 
     * do ResultSet para uma estrutura de dados estática em memória.
     * * 🛡️ THREAD-SAFE: A Connection é obtida do Pool e devolvida/fechada 
     * imediatamente após a leitura dos dados, eliminando o estado partilhado.
     * * 🔄 Retorna uma Lista de Mapas, onde cada
     * elemento da Lista representa uma linha, e cada Mapa utiliza o nome
     * da coluna como chave para o valor (Object) correspondente.
     * * @param interroga A instrução SQL de consulta (SELECT).
     * @return Uma {@code List<Map<String, Object>>} contendo todas as linhas. 
     * Retorna {@code null} em caso de exceção SQL.
     */
    public List<Map<String, Object>> getResultado(String interroga) {
        // 1. Usar ArrayList e HashMap para maior performance e flexibilidade
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(interroga)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                // 2. Cada linha é um HashMap (Nome da Coluna -> Valor)
                Map<String, Object> row = new HashMap<>(columnCount); 
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i); // Ou getColumnName(i)
                    row.put(columnName, rs.getObject(i));
                }
                data.add(row);
            }
            
            return data; 

        } catch (SQLException e) {
            System.err.println("❌ Erro em getResultado (SELECT): " + e.getMessage());
            return null; 
        }
    }
    /**
     * 📦 Executa um SELECT e retorna o primeiro objeto presente na primeira linha e coluna.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return O objeto Java, ou null se não houver resultados ou em caso de erro.
     */
    public Object getObject(String directiva) {
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {
            
            if (rs.next())
                return rs.getObject(1);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getVObject: " + e.getMessage());
            return null;
        }
    }

    /**
     * 📜 Executa um SELECT e retorna a primeira String presente na primeira linha e coluna.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return A String, ou null se não houver resultados ou em caso de erro.
     */
    public String getString(String directiva) {
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {
            
            if (rs.next())
                return rs.getString(1);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getVString: " + e.getMessage());
            return null;
        }
    }
    
    public static String getString(Connection conexao, String directiva) {
        try (Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {
            if (rs.next())
                return rs.getString(1);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getVString: " + e.getMessage());
            return null;
        }
    }
    /**
     * 📅 Executa um SELECT e retorna a primeira Data (java.sql.Date) presente na primeira linha e coluna.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return A data SQL, ou null se não houver resultados ou em caso de erro.
     */
    public Date getDate(String directiva) {
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {
            
            if (rs.next())
                return rs.getDate(1);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getVDate: " + e.getMessage());
            return null;
        }
    }

    /**
     * 💰 Executa um SELECT e retorna o primeiro valor numérico (BigDecimal) presente na primeira linha e coluna.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return O valor numérico, ou null se não houver resultados ou em caso de erro.
     */
    public BigDecimal getBigDecimal(String directiva) {
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {
            
            if (rs.next())
                return rs.getBigDecimal(1);
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getVBigDecimal: " + e.getMessage());
            return null;
        }
    }

    /**
     * ➡️ Executa um SELECT e retorna a **primeira linha** completa como um Vector de Objetos.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return O Vector dos elementos da primeira linha, ou um Vector vazio em caso de erro/sem resultados.
     */
    public Vector<Object> getVectorLinha(String directiva) {
        Vector<Object> linha = new Vector<>();
        
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {

            if (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int cols = rsmd.getColumnCount();
                
                // Processa a primeira linha
                for (int i = 1; i <= cols; i++) {
                    linha.add(rs.getObject(i));
                }
            }
            return linha;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getLVector: " + e.getMessage());
            return new Vector<>();
        }
    }

    /**
     * ⬇️ Executa um SELECT e retorna a **primeira coluna** completa como um Vector de Objetos.
     * Recursos fechados/devolvidos após o uso.
     * @param directiva SQL SELECT.
     * @return O Vector dos elementos da primeira coluna, ou um Vector vazio em caso de erro.
     */
    public Vector<Object> getVectorColuna(String directiva) {
        Vector<Object> coluna = new Vector<>();
        
        try (Connection conexao = getLigacao();
             Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(directiva)) {

            // Processa a primeira coluna de todas as linhas
            while (rs.next()) {
                coluna.add(rs.getObject(1));
            }
            return coluna;
        } catch (SQLException e) {
            System.err.println("❌ Erro em getCVector: " + e.getMessage());
            return new Vector<>();
        }
    }

    // --------------------------------------------------------------------------------
    // ℹ️ MÉTODOS AUXILIARES ESTATICOS (Thread-Safe por natureza)
    // --------------------------------------------------------------------------------
    
    /**
     * Obtém os metadados da base de dados.
     * **Thread-Safe**: A conexão é devolvida ao Pool.
     * @return O objeto DatabaseMetaData ou null em caso de falha.
     */
    public DatabaseMetaData getMetaData() {
        try (Connection conexao = getLigacao()) {
            return conexao.getMetaData(); 
        } catch (SQLException e) {
            System.err.println("❌ Erro ao obter metadados da BD: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retorna uma mensagem formatada com base no número de linhas afetadas.
     * @param linhasAfetadas O número de linhas afetadas devolvido pelo método xDirectiva.
     * @return mensagem relativa ao número de linhas afetadas.
     */
    public static String getAfetadas(int linhasAfetadas) {
	    if (linhasAfetadas == -1) {
	        return "❌ Falha na execução. Nenhuma linha afetada.";
	    } else if (linhasAfetadas == 0) {
	        return "⚠️ Nenhuma linha afetada.";
	    } else if (linhasAfetadas == 1) {
	        return "✅ 1 linha afetada com sucesso.";
	    }
	    return "📝 " + linhasAfetadas + " linhas afetadas."; 
    }
    /**
	 * Tenta executar um conjunto de comandos SQL (DML ou DDL) em modo 'Batch' (lote).
	 * Caso o Driver JDBC não suporte 'Batch', as directivas são executadas uma a uma.
	 * O envio em lote melhora significativamente a performance em operações DML repetitivas.
	 * @param directivas Array de strings com comandos SQL (não pode incluir SELECTs).
	 * @return true se todos os comandos forem executados com sucesso, false caso contrário.
	 */
	static public boolean executaBatch(Connection conexao, String directivas[]) {
		DatabaseMetaData dbmd;
		boolean ok=false;
		try {
			dbmd = conexao.getMetaData();
			// Verificar suporte a Batch
			ok=dbmd.supportsBatchUpdates();
		} catch (SQLException e) {
			System.out.println("❌ Erro ao consultar a metadata.");
			System.err.println("   Detalhe: " + e.getMessage());
			return false;
		}

		if(ok) {
			try (Statement stmt=conexao.createStatement()) {
				stmt.clearBatch(); // Limpa qualquer operação batch anterior
				
				System.out.println("⚙️ A preparar a execução de " + directivas.length + " directivas em modo 'Batch'...");
				
				// Adicionar comandos ao lote
				for (int i = 0; i < directivas.length; i++) {
					try {
						stmt.addBatch(directivas[i]);
					} catch (SQLException e) {
						System.out.println("❌ Erro ao adicionar directiva ao lote.");
						System.err.println("   Directiva: " + directivas[i]);
						System.err.println("   Detalhe: " + e.getMessage());
						return false;
					}
				}
				// Execução em MODO BATCH (Lote)
				int[] numUpdates = stmt.executeBatch();
				System.out.println("✅ Execução do lote concluída. A rever os resultados:");
				
				for (int i = 0; i < numUpdates.length; i++) {
					if (numUpdates[i] == -2)
						System.out.println("❓ Directiva "
								+ (i + 1)
								+ ": Número desconhecido de linhas atualizadas.");
					else if (numUpdates[i] == 1)
						System.out.println("✅ Directiva " + (i + 1)
								+ ": 1 linha atualizada com sucesso.");
					else
						System.out.println("📝 Directiva " + (i + 1) + ": "
								+ numUpdates[i] + " linhas atualizadas com sucesso.");
				}
				return true;
			} 
			catch (BatchUpdateException e) {
				e.printStackTrace();
				System.out.println("❌ Erro Grave (BatchUpdateException): Falha na execução de uma das directivas do lote.");
				System.err.println("   Detalhe: " + e.getMessage());
			} 
			catch (SQLException e) {
				System.out.println("❌ Erro (SQL Exception): Falha no acesso à Base de Dados.");
				System.err.println("   Detalhe: " + e.getMessage());
			} 
		} else
		// Execução em MODO SEQUENCIAL (Fallback)
			{
			System.out.print("⚠️ O Driver JDBC NÃO suporta a execução em 'Batch'. ");
			System.out.println("O processamento será feito executando comandos individuais.");
			int i = 0;
			try (Statement stmt=conexao.createStatement()) {
				for (; i < directivas.length; i++) {	
					stmt.executeUpdate(directivas[i]);
					System.out.println("➡️ Directiva " + (i + 1) + " de " + directivas.length + " executada com sucesso.");
				}
				return true;
			} catch (SQLException e) {
				System.out.println("❌ Erro na execução da directiva individual.");
				System.out.println("   Directiva: " + directivas[i]);
				System.err.println("   Detalhe: " + e.getMessage());
			}
			}
		return false;
	}

}