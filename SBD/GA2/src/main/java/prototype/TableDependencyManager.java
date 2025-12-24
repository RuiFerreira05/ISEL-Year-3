package prototype;
import java.sql.*;
import java.util.*;

import util.Configura; 

/**
 * 💡 Classe principal para Gestão de Dependências de Tabelas em Bases de Dados.
 * 🎯 Objetivo: Determinar a ordem segura para operações (INSERT, DELETE, DROP) em tabelas,
 * respeitando as dependências de Chaves Estrangeiras (Foreign Keys - FKs).
 */
public class TableDependencyManager { // ⬅️ Nome da classe alterado

    // -----------------------------------------------------------------------------------
    // 🔗 MÉTODOS DE OBTENÇÃO DE METADADOS E ORDENAÇÃO
    // -----------------------------------------------------------------------------------

    /**
     * 🏗️ Constrói o grafo de dependência e obtém a lista de tabelas ordenada para INSERT.
     * Esta ordem é essencial para garantir que as tabelas de referência (Primary Key)
     * sejam preenchidas antes das tabelas dependentes (Foreign Key).
     * @param schemaName O nome do esquema/base de dados a ser analisado.
     * @return A lista de tabelas ordenada pelo algoritmo Topológico (ordem de INSERT).
     * @throws SQLException Se ocorrer um erro durante a consulta aos metadados.
     */
    public List<String> getDependencyOrder() throws SQLException {
        ResultSet tables = null;
        ResultSet rs = null;

        // 1. 🌐 Estabelecer Conexão
        Configura cfg = new Configura();
        try (Connection conn = cfg.getConnection()) {
            
            // Map: Tabela dependente -> Lista de tabelas primárias.
            Map<String, List<String>> dependencies = new HashMap<>();
            
            // Map: Tabela primária -> Lista de tabelas dependentes.
            Map<String, List<String>> reverseDependencies = new HashMap<>();
            
            DatabaseMetaData meta = conn.getMetaData();
            
            // 2. 📋 Obter todas as tabelas no esquema
            tables = meta.getTables(cfg.getDTB(), null , "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                dependencies.put(tableName, new ArrayList<>());
                reverseDependencies.put(tableName, new ArrayList<>());
            }

            // 3. 🔑 Obter as Chaves Estrangeiras (FKs) e construir o grafo
            for (String dependentTable : dependencies.keySet()) {
                rs = meta.getImportedKeys(cfg.getDTB(), null, dependentTable);
                while (rs.next()) {
                    String primaryTable = rs.getString("PKTABLE_NAME"); 
                    
                    // Ignorar auto-referências
                    if (dependentTable.equals(primaryTable)) continue;
                    
                    // ➡️ Construir o grafo de dependência
                    dependencies.get(dependentTable).add(primaryTable);
                    
                    // ⬅️ Construir o grafo inverso
                    if (reverseDependencies.containsKey(primaryTable)) {
                        reverseDependencies.get(primaryTable).add(dependentTable);
                    }
                }
            }
            
            // 4. 🌳 Aplicar a ordenação Topológica (Algoritmo de Kahn)
            return topologicalSort(dependencies, reverseDependencies);
            
        } finally {
            // 5. 🛑 Fechar os ResultSets no bloco finally
            if (rs != null)
                rs.close();
            if (tables != null)
                tables.close();
        }
    }

    /**
     * 🌳 Implementa o algoritmo de Ordenação Topológica (Kahn's Algorithm).
     * Ordena as tabelas em uma sequência (da menos dependente para a mais dependente).
     */
    public List<String> topologicalSort(
        Map<String, List<String>> dependencies, 
        Map<String, List<String>> reverseDependencies) 
    {
        // 1. Calcular o grau de entrada (in-degree)
        Map<String, Integer> inDegree = new HashMap<>();
        for (String table : dependencies.keySet()) {
            inDegree.put(table, dependencies.get(table).size());
        }

        // 2. Inicializar a fila com tabelas com grau de entrada 0
        Queue<String> queue = new LinkedList<>();
        for (String table : inDegree.keySet()) {
            if (inDegree.get(table) == 0) {
                queue.add(table);
            }
        }

        List<String> sortedList = new ArrayList<>();
        // 3. Processar o Grafo
        while (!queue.isEmpty()) {
            String primaryTable = queue.poll();
            sortedList.add(primaryTable); // ✅ Tabela base adicionada
            
            // Iterar sobre as tabelas que dependem desta
            if (reverseDependencies.containsKey(primaryTable)) {
                for (String dependentTable : reverseDependencies.get(primaryTable)) {
                    
                    // ⬇️ Diminuir o grau de entrada da dependente
                    int newDegree = inDegree.get(dependentTable) - 1;
                    inDegree.put(dependentTable, newDegree);
                    
                    // ➕ Se o grau de entrada atingir zero, a tabela está pronta
                    if (newDegree == 0) {
                        queue.add(dependentTable);
                    }
                }
            }
        }

        // 4. ⚠️ Verificação de Ciclos
        if (sortedList.size() != dependencies.size()) {
            System.err.println("⚠️ ATENÇÃO: Ciclo de dependência detetado! A ordenação é incompleta. (Ex: Tabela A aponta para B, e B aponta para A).");
        }

        // A lista está na ordem segura para INSERT (Tabela Base -> Tabela Dependente)
        return sortedList;
    }

    /**
     * 🎯 Executa a operação de DELETE ou DROP em todas as tabelas na ordem inversa.
     * @param tableList A lista de tabelas na ordem de INSERT (obtida de getDependencyOrder).
     * @param operationType O tipo de operação ("DELETE" ou "DROP").
     * @param disableFkChecks Se as verificações de chaves estrangeiras devem ser desativadas.
     * @return true se a operação foi concluída com sucesso, false caso contrário.
     * @throws SQLException Se ocorrer um erro no SQL.
     */
    public boolean executeTableOperation(List<String> tableList, String operationType, boolean disableFkChecks) throws SQLException {
        Configura cfg = new Configura(); 
        
        // Obter conexão com AutoCommit=false (essencial para o controlo de transação).
        try (Connection conn = cfg.getConnection(false)) { 
            // 1. Validação de Parâmetros e Tipo de Operação
            if (conn == null || tableList == null || tableList.isEmpty()) {
                System.err.println("Erro: A conexão ou a lista de tabelas não pode ser nula/vazia.");
                return false;
            }
            
            operationType = operationType.toUpperCase();
            if (!operationType.equals("DELETE") && !operationType.equals("DROP")) {
                throw new IllegalArgumentException("Tipo de operação inválido. Use 'DELETE' ou 'DROP'.");
            }

            // 2. 🔄 Inverter a lista para obter a ordem segura (DELETE/DROP)
            List<String> operationOrderList = new ArrayList<>(tableList);
            Collections.reverse(operationOrderList);
            
            System.out.println("\nSequência de Operação (" + operationType + " em Ordem Inversa): " + operationOrderList);

            // 3. GESTÃO DE CHAVES ESTRANGEIRAS E TRANSAÇÃO
            boolean fkChecksWereDisabled = false; 
            
            try (Statement stmt = conn.createStatement()) {
                // 3.1. 🔒 Desativar as verificações de FK (Condicional ao parâmetro)
                if (disableFkChecks) {
                    System.out.println("⚠️ Desativando verificações de Chave Estrangeira (FOREIGN_KEY_CHECKS=0)...");
                    stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    fkChecksWereDisabled = true; 
                }
                
                // 3.2. Definir a instrução SQL base
                String sql;
                if (operationType.equals("DROP")) {
                    sql = "DROP TABLE IF EXISTS ";
                } else { 
                    sql = "DELETE FROM "; 
                }
                
                // 3.3. 🎯 Executar a Operação
                for (String tableName : operationOrderList) {
                    System.out.println("⚙️ Executando: " + sql + tableName+". ❗ Não executa 'propositadamente' -> "+sql + tableName);
                    // stmt.executeUpdate(sql + tableName);
                }
                
                conn.commit(); // ✅ Sucesso: confirmar a transação
                System.out.println("✅ Operação '" + operationType + "' concluída com sucesso.");
                return true;
                
            } catch (SQLException e) {
                // ❌ Falha: reverter a transação (rollback)
                if (conn != null) {
                    conn.rollback(); 
                }
                System.err.println("❌ Erro grave ao executar " + operationType + ". Transação revertida: " + e.getMessage());
                throw e;
                
            } finally {
                // 4. 🔑 RESTAURO OBRIGATÓRIO
                if (fkChecksWereDisabled) {
                    try (Statement stmtRestore = conn.createStatement()) {
                        System.out.println("🔑 Reativando verificações de Chave Estrangeira (FOREIGN_KEY_CHECKS=1)...");
                        stmtRestore.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                    } catch (SQLException e) {
                        System.err.println("Erro irrecuperável ao reativar FK_CHECKS: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro no estabeleciento da ligação à base de dados: " + e.getMessage());
            throw e;
        }
    }
    
    // ---------------------------------------------------------------------------------------
    // 🚀 MÉTODO MAIN PARA DEMONSTRAÇÃO 🚀
    // ---------------------------------------------------------------------------------------

    public static void main(String[] args) {
        
        // Instancia a classe com o novo nome
        TableDependencyManager manager = new TableDependencyManager(); 
        List<String> sortedTableList = null;

        System.out.println("=".repeat(60));
        System.out.println("🚀 INÍCIO DA DEMONSTRAÇÃO DO TABLE DEPENDENCY MANAGER");
        System.out.println("=".repeat(60));

        try {
            // 1. 🌳 DEMONSTRAÇÃO DA ORDENAÇÃO TOPOLÓGICA (Simulação/Real)
            System.out.println("\n### 2. Obtendo Ordem Topológica para INSERT/DELETE");
            
            try {
                 // Tenta obter a ordem real. Se falhar, usa lista fixa.
                 sortedTableList = manager.getDependencyOrder();
                 System.out.println("✅ Ordem real obtida da BD.");
            } catch (SQLException e) {
                System.err.println("⚠️ FALHA na conexão/metadados. Usando lista de simulação para a demo de execução.");
                System.err.println("Erro: " + e.getMessage());
                // Ordem simulada (INSERT SAFE): Tabela Base -> ... -> Tabela Mais Dependente
                sortedTableList = Arrays.asList("Cliente", "Produto", "Pedido", "ItemPedido");
            }
            
            System.out.println("📝 Ordem Topológica (INSERT SAFE): " + sortedTableList);
            
            if (sortedTableList.isEmpty()) {
                System.out.println("Lista de tabelas vazia. Encerrando demonstração.");
                return;
            }
            
            // 2. 🗑️ DEMONSTRAÇÃO 1: LIMPEZA DE DADOS (DELETE)
            System.out.println("\n" + "-".repeat(60));
            System.out.println("### 3. Execução de DELETE (Limpeza de Dados)");
            System.out.println("-".repeat(60));
            manager.executeTableOperation(sortedTableList, "DELETE", true);
            
            // 3. 💣 DEMONSTRAÇÃO 2: REMOÇÃO DE ESTRUTURA (DROP)
            System.out.println("\n" + "-".repeat(60));
            System.out.println("### 4. Execução de DROP (Remoção da Estrutura da Tabela)");
            System.out.println("-".repeat(60));
            manager.executeTableOperation(sortedTableList, "DROP", true);

        } catch (IllegalArgumentException e) {
            System.err.println("\n❌ Erro de Argumento (Operação Inválida): " + e.getMessage());
        } catch (SQLException e) { 
            System.err.println("\n❌ Erro de Base de Dados durante a Execução: " + e.getMessage());
        } catch (Exception e) { 
            System.err.println("\n❌ Erro Inesperado: " + e.getMessage());
        } finally {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🏁 FIM DA DEMONSTRAÇÃO.");
            System.out.println("=".repeat(60));
        }
    }
}



