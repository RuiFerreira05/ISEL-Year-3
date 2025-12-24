package util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Configura.SGBD;

/**
 * 
 * 📚 Classe utilitária para atualização de comentários de Tabelas, Vistas ou Colunas.
 * Implementa uma abordagem segura, combinando Metadados JDBC com análise de DDL
 * para reconstruir definições de colunas sem perder tipos, tamanhos, escalas ou valores DEFAULT.
 *
 * Armazena e gere comentários de vistas num ficheiro.
 */
public final class Comment { 

    // 🔒 O mapa de trabalho: PRIVATE e estático. É o ponteiro que será substituído a cada atualização.
    private static Map<String, String> COMENTARIOS;
    
    // 💾 Nome do ficheiro de persistência
    private static final String COMMENT_FILE = new Configura().getRealPath()+"WEB-INF/comments.properties";
    
    // --- BLOCO DE INICIALIZAÇÃO ESTÁTICA ---
    static {
        // Tenta carregar os comentários do ficheiro.
        COMENTARIOS = loadCommentsFromFile();
        
        // Se o ficheiro estava vazio ou não existia, inicializa com valores base.
        if (COMENTARIOS.isEmpty()) {
             System.out.println("⚠️ Ficheiro de comentários não encontrado ou vazio. Inicializando com valores padrão.");
             Map<String, String> mapaBase = Map.ofEntries(
                Map.entry("VISTA", "Comentário da vista"),
                Map.entry("ALUNOS", "VIEW: Agrega dados básicos de ALUNO com IDADE e FOTO"),
                Map.entry("AVALIACOES", "VIEW: Retorna a MELHOR NOTA (entre 10 e 20) obtida por cada ALUNO numa DISCIPLINA, juntamente com o ano em que essa nota foi registada pela primeira vez (MIN(ANO))")
             );
             COMENTARIOS = new HashMap<>(mapaBase);
             // Salva os valores padrão para criar o ficheiro na primeira vez e garantir persistência.
             saveCommentsToFile(COMENTARIOS);
        } else {
             System.out.println("✅ Comentários carregados com sucesso do ficheiro.");
        }
    }
    
    // 🔒 Construtor privado para impedir instâncias (Classe utilitária).
    private Comment() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não pode ser instanciada.");
    }
    
    // --- MÉTODOS DE PERSISTÊNCIA (FICHEIRO) ---

    /**
     * 💾 Tenta carregar o mapa de comentários de um ficheiro de propriedades.
     * @return Um Map carregado do ficheiro, ou um Map vazio se o ficheiro não existir ou houver erro.
     */
    private static Map<String, String> loadCommentsFromFile() {
        Properties properties = new Properties();
        File file = new File(COMMENT_FILE);
        
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (InputStream is = new FileInputStream(file)) {
            properties.load(is);
            // Converte Properties (Object, Object) para Map<String, String>
            Map<String, String> loadedMap = new HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                loadedMap.put(key, properties.getProperty(key));
            }
            return loadedMap;
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar comentários do ficheiro: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 💾 Salva o mapa de comentários atual para o ficheiro de propriedades.
     * @param comments O mapa a ser guardado.
     */
    private static void saveCommentsToFile(Map<String, String> comments) {
        Properties properties = new Properties();
        properties.putAll(comments);

        try (OutputStream os = new FileOutputStream(COMMENT_FILE)) {
            properties.store(os, "Comentários de metadados da aplicação. NÃO EDITAR MANUALMENTE.");
        } catch (IOException e) {
            System.err.println("❌ Erro ao guardar comentários no ficheiro: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE ESCRITA/GESTÃO ---
    
    /**
     * ➕ Adiciona ou atualiza um único comentário no registo, PERSISTE a alteração,
     * e garante que a memória está sincronizada com o disco antes de terminar.
     */
    public static synchronized void set(String chave, String comentario) {
        String upperCaseKey = chave.toUpperCase();
        
        // 1. CÓPIA e MODIFICAÇÃO (Garante que a nossa alteração está em memória)
        Map<String, String> novoMapa = new HashMap<>(COMENTARIOS);
        novoMapa.put(upperCaseKey, comentario);
        COMENTARIOS = novoMapa;

        // 2. GRAVAÇÃO (Persiste a nossa alteração no disco)
        saveCommentsToFile(COMENTARIOS); // Esta gravação já atualiza o lastModifiedTime.

        // 3. 🚨 NOVO: LEITURA FINAL PARA GARANTIR CONSISTÊNCIA
        // Se alguma outra aplicação modificou o ficheiro EXACTAMENTE entre o passo 1 e 2,
        // esta leitura final irá carregar a versão mais recente do disco para a memória.
        // O método loadCommentsFromFile() também atualiza o lastModifiedTime.
        COMENTARIOS = loadCommentsFromFile();
        System.out.println("➡️ Comentário: '" + comentario+"'.");
        System.out.println("✅ Atualizado/persistido e sincronizado com a chave: '" + chave+"'.");
    }
    
    // --- MÉTODOS DE LEITURA ---
    
    /**
     * 🔎 Obtém o comentário associado a uma chave específica.
     *
     * @param chave O nome da tabela ou coluna.
     * @return O comentário, ou '?' se não encontrado.
     */
    public static String get(String chave) {
        // Acesso em maiúsculas para corresponder às chaves guardadas.
        return COMENTARIOS.getOrDefault(chave.toUpperCase(), "?");
    }
    
    // --- MÉTODOS DE INTERAÇÃO COM BASE DE DADOS (REFLECTION) ---

	/**
	 * 🤖 Execução Dinâmica de Método (Reflection) para Comentários de Vistas.
	 * Este método automatiza a seleção e execução da lógica de base de dados (BD) correta,
	 * eliminando a necessidade de verificações {@code if/else} explícitas de driver.
	 *
	 * @param nomeDaVista O nome da vista ou objeto de BD a receber o novo comentário.
	 * @param novoComentario O comentário de texto que será registado no BD.
	 * @return O resultado booleano da chamada de método de ação específica do BD.
	 */
    static public boolean view(String nomeDaVista, String novoComentario) {
    	// 🗂️ Lista de todos os drivers que a aplicação suporta e que serão usados via Reflection.
        final String[] DRIVERS_SUPORTADOS = {"MySQL", "SQLServer"}; 
        String dbDriver = null; 
        // A instância de Configura (que contém os métodos is... e a ligação) é essencial.
        Configura cfg = new Configura();
        Class<?> classeCfg = cfg.getClass(); // Classe Configura
        
        // 1. 🔍 DESCOBERTA DO DRIVER ATIVO (Reflection nos métodos is... de Configura)
        for (String driver : DRIVERS_SUPORTADOS) {
            String nomeDoMetodoIs = "is" + driver; 
            
            try {
                Method metodoIs = classeCfg.getMethod(nomeDoMetodoIs); 
                Object resultado = metodoIs.invoke(cfg); 

                if (resultado instanceof Boolean && (Boolean) resultado) {
                    dbDriver = driver; 
                    System.out.println("➡️ Driver ativo detetado via Reflexão: " + dbDriver);
                    break; 
                }
            } catch (Exception e) {
                System.err.println("❌ Erro na verificação do driver " + nomeDoMetodoIs);
                return false; 
            }
        }
        
        // 🛑 Se o ciclo terminou e nenhum driver foi encontrado.
        if (dbDriver == null) {
            System.out.println("🚫 Nenhum driver de BD suportado ou ativo foi encontrado.");
            return false;
        }
        
        // 2. 🔨 CONSTRUIR E INVOCAR O MÉTODO DE AÇÃO (view... em Comment)
        
        // O método de ação é viewMySQL ou viewSQLServer (estático e privado nesta classe)
        String nomeDoMetodoAcao = "view" + dbDriver; 
        Class<?> classeComment = Comment.class; // A própria classe Comment
        
        try {
            // Os métodos de ação são estáticos e privados.
            Class<?>[] tiposParametros = new Class<?>[] { String.class, String.class };
            
            // É preciso usar getDeclaredMethod e passar a assinatura
            Method metodoAcao = classeComment.getDeclaredMethod(nomeDoMetodoAcao, tiposParametros);
            
            // Torna o método acessível (porque é privado)
            metodoAcao.setAccessible(true); 

            // Invoca o método de AÇÃO (estático, por isso o primeiro argumento é 'null')
            Object[] argumentos = new Object[] { nomeDaVista, novoComentario };
            Object resultado = metodoAcao.invoke(null, argumentos); 
            
            // Devolve o resultado (boolean) da operação de BD.
            return (boolean) resultado;

        } catch (InvocationTargetException e) {
            System.err.println("💥 ERRO INTERNO no método de ação. Causa: " + e.getTargetException().getMessage());
            e.getTargetException().printStackTrace();
        } catch (Exception e) {
            System.err.println("⚠️ ERRO de Reflexão na invocação do método de ação.");
            e.printStackTrace();
        }

        return false;
    }
	
	/**
     * 💬 Adiciona ou modifica o comentário de uma VIEW (VISTA) específica no MySQL.
     * **** Devido a falta de suporte no MySQL para ALTER VIEW COMMENT: É feito um bypass. **********
     * @param nomeDaVista O nome da VIEW a ser comentada.
     * @param novoComentario O texto descritivo para a VIEW.
     * @return true se a operação DDL for bem-sucedida ou se o erro for ignorado.
     */
    @SuppressWarnings("unused") // usado no view
	private static boolean viewMySQL(String nomeDaVista, String novoComentario) {
        Configura cfg = new Configura();
    	if(!cfg.isMySQL()) 
    		return false;
        
        // A sintaxe ALTER VIEW... COMMENT é teoricamente incorreta no MySQL, mas usamos para forçar o erro.
        String sql = "ALTER VIEW " + nomeDaVista + " COMMENT = '" + novoComentario.replace("'", "''") + "'";

        System.out.println("⚙️ A tentar executar: " + sql);

        try (Connection con = cfg.getConnection();
             Statement stmt = con.createStatement()) {

            boolean sucesso = stmt.execute(sql);
            
            if (!sucesso) {
                System.out.println("✅ Comentário da VIEW '" + nomeDaVista + "' atualizado com sucesso!");
                return true;
            }           
        } catch (SQLException e) {
        	// 1064 = Erro de sintaxe. No MySQL, isso é comum para tentar comentar vistas.
        	if(e.getErrorCode() == 1064) {
        		// Ignora o erro no MySQL e salva apenas no mapa/ficheiro estático
        		Comment.set(nomeDaVista, novoComentario); 
        		return true; // Considera a operação como 'sucesso' na aplicação.
        	}
            System.err.println("❌ Erro ao modificar o comentário da VIEW.");
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println("Código do Erro: " + e.getErrorCode());
        }
        return false;
    }
    
	/**
     * Adiciona ou atualiza a descrição (comentário) de uma VIEW (Vista) no SQL Server.
     * @param viewName O nome da vista a ser comentada.
     * @param comment O texto do comentário.
     * @return true se a operação for executada com sucesso.
     */
	@SuppressWarnings("unused")  // usado no view
	private static boolean viewSQLServer(String viewName, String comment) {
        Configura cfg = new Configura();
        if(!cfg.isSQLServer()) 
        		return false;
        
        // O SQL Server usa o procedimento armazenado sp_addextendedproperty
        String sql = "{CALL sys.sp_addextendedproperty (?, ?, ?, ?, ?, ?)}";
        String schema = cfg.getDTB(); // Obtém o esquema (geralmente dbo)
        
        try (Connection con=cfg.getConnection(); CallableStatement cstmt = con.prepareCall(sql)) {
            
            System.out.println("⚙️ A preparar a chamada ao procedimento sp_addextendedproperty...");

            // 1. @name (Nome da Propriedade - 'MS_Description')
            cstmt.setNString(1, "MS_Description"); 

            // 2. @value (O seu comentário)
            cstmt.setNString(2, comment);

            // 3. @level0type (SCHEMA)
            cstmt.setNString(3, "SCHEMA");

            // 4. @level0name (Nome do Esquema)
            cstmt.setNString(4, schema);
            
            // 5. @level1type (VIEW)
            cstmt.setNString(5, "VIEW");

            // 6. @level1name (Nome da Vista)
            cstmt.setNString(6, viewName);

            cstmt.execute();
            
            System.out.println("✅ Comentário adicionado/atualizado para a View '" + viewName + "' com sucesso!");
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao executar sp_addextendedproperty via JDBC: " + e.getMessage());
        }
        return false;
    }

	/**
	 * 🛠️ Método de conveniência para atualizar o comentário de uma Tabela ou Vista.
	 *
	 * @param con A ligação ativa à base de dados MySQL.
	 * @param dbType Sistema de Gestão de Bases de Dados MySQL/SQLServer.
	 * @param objectType O tipo de objeto, deve ser "TABLE" ou "VIEW".
	 * @param objectName O nome da Tabela ou Vista.
	 * @param newComment O novo texto do comentário.
	 * @throws SQLException Se ocorrer um erro SQL.
	 * @throws IllegalArgumentException Se objectType não for "TABLE" ou "VIEW".
	 */
	public static void updateTableOrViewComment(Connection con, Configura.SGBD dbType, String objectType, String objectName, String newComment) throws SQLException {
	    
	    String type = objectType.toUpperCase();
	    
	    if (!type.equals("TABLE") && !type.equals("VIEW")) {
	        throw new IllegalArgumentException("Tipo de objeto inválido. Este método é apenas para 'TABLE' ou 'VIEW'.");
	    }
	    if(dbType==Configura.SGBD.MySQL)
	    		updateObjectCommentMySQL(con, type, objectName, null, newComment);
	    else if(dbType==Configura.SGBD.SQLServer)
	    			updateObjectCommentSQLServer(con, type, objectName, null, newComment);
	    		else
	    			System.err.println("Falha de configuração, Sistema de Gestão de Bases de Dados desconhecido!");
	}
	/**
 * 📝 Atualiza o comentário (Extended Property 'MS_Description') de uma Tabela, 
 * Vista ou Coluna no SQL Server.
 * O método executa um bloco T-SQL que trata da lógica de ADD ou UPDATE.
 *
 * @param con A ligação ativa à base de dados SQL Server.
 * @param objectType O tipo de objeto a comentar (ex: "TABLE", "VIEW", "COLUMN").
 * @param objectName O nome da Tabela ou Vista.
 * @param subObjectName O nome da Coluna (apenas necessário se objectType for "COLUMN", caso contrário null).
 * @param newComment O novo texto do comentário.
 * @throws SQLException Se ocorrer um erro SQL.
 */
public static void updateObjectCommentSQLServer(Connection con, String objectType, String objectName, String subObjectName, String newComment) throws SQLException {
    
    // 1. Preparação
    String safeComment = newComment.replace("'", "''"); // Escapar aspas simples
    String schemaName = "dbo"; // O schema padrão no SQL Server

    String level1Type; // TABLE ou VIEW
    String level2Type; // COLUMN ou NULL
    String level2Name; // Nome da coluna ou NULL
    
    // 2. Determinar os Níveis de Propriedade
    switch (objectType.toUpperCase()) {
        case "VIEW":
            level1Type = "VIEW";
            level2Type = "NULL";
            level2Name = "NULL";
            break;
        case "COLUMN":
            level1Type = "TABLE"; // A coluna é um objeto de 2º nível de uma TABLE
            level2Type = "COLUMN";
            if (subObjectName == null || subObjectName.isEmpty()) {
                 throw new IllegalArgumentException("O nome da coluna (subObjectName) é obrigatório para objectType 'COLUMN'.");
            }
            level2Name = subObjectName;
            break;
        case "TABLE":
        default:
            level1Type = "TABLE";
            level2Type = "NULL";
            level2Name = "NULL";
            break;
    }

    // 3. Construção do Bloco T-SQL (Baseado no tipo de objeto)
    // As variáveis @level1name (Tabela/Vista) e @level2name (Coluna) são injetadas.
    String tsql = String.format(
        // Bloco IF EXISTS (para verificar se o comentário já existe)
        "IF EXISTS ( " +
            "SELECT 1 FROM sys.extended_properties " +
            "WHERE major_id = OBJECT_ID(N'%1$s') " +
            "AND minor_id = %s " + // Lógica de identificação do objeto de nível 2 (coluna ou 0 para tabela/vista)
            "AND name = N'MS_Description' " +
        ") " +
        "BEGIN " +
            // UPDATE: Se o comentário existe
            "EXEC sys.sp_updateextendedproperty " +
                "@name=N'MS_Description', @value=N'%4$s', " +
                "@level0type=N'SCHEMA', @level0name=N'%5$s', " +
                "@level1type=N'%6$s', @level1name=N'%1$s', " +
                "@level2type=%7$s, @level2name=%8$s; " +
        "END " +
        "ELSE " +
        "BEGIN " +
            // ADD: Se o comentário não existe
            "EXEC sys.sp_addextendedproperty " +
                "@name=N'MS_Description', @value=N'%4$s', " +
                "@level0type=N'SCHEMA', @level0name=N'%5$s', " +
                "@level1type=N'%6$s', @level1name=N'%1$s', " +
                "@level2type=%7$s, @level2name=%8$s; " +
        "END",
        
        objectName, // %1$s: Nome da Tabela/Vista
        level2Name.equals("NULL") ? "0" : "COLUMNPROPERTY(OBJECT_ID(N'" + objectName + "'), N'" + level2Name + "', 'ColumnID')", // Lógica para minor_id (0 para Tabela/Vista, ColumnID para Coluna)
        "", // Posição 3 não usada
        safeComment, // %4$s: O valor do novo comentário
        schemaName, // %5$s: dbo
        level1Type, // %6$s: TABLE ou VIEW
        level2Type.equals("NULL") ? "NULL" : "N'" + level2Type + "'", // %7$s: COLUMN ou NULL
        level2Name.equals("NULL") ? "NULL" : "N'" + level2Name + "'"  // %8$s: Nome da Coluna ou NULL
    );
    
    // 4. Executar o Bloco T-SQL
    System.out.println("T-SQL a executar (bloco completo omitido por brevidade)...");
    try (Statement stm = con.createStatement()) {
        // Execute o SQL de forma segura
        stm.executeUpdate(tsql);
        
        String logMessage = "Comentário de " + objectType + " '" + objectName;
        if (!level2Name.equals("NULL")) {
            logMessage += "." + level2Name;
        }
        System.out.println("✅ " + logMessage + "' atualizado no SQL Server.");
    }
}

	
	/**
	 * 🛠️ Método de conveniência para atualizar o comentário de uma coluna,
	 * delegando a chamada para o método específico 
	 * da base de dados (MySQL ou SQL Server) com base no parâmetro dbType.
	 *
	 * @param con A ligação ativa à base de dados.
	 * @param dbType O tipo de SGBD (ex: "MySQL" ou "SQLServer").
	 * @param tableName O nome da tabela.
	 * @param columnName O nome da coluna a modificar.
	 * @param newComment O novo texto do comentário.
	 * @throws SQLException Se ocorrer um erro SQL durante a execução.
	 * @throws IllegalArgumentException Se o tipo de base de dados não for reconhecido.
	 */
	public static void updateColumnComment(Connection con, Configura.SGBD dbType, String tableName, String columnName, String newComment) throws SQLException {
				    
	    // O tipo de objeto a ser atualizado é sempre "COLUMN" neste método de conveniência
	    final String objectType = "COLUMN"; 
	    if (dbType==SGBD.MySQL) {
	        updateObjectCommentMySQL(con, objectType, tableName, columnName, newComment);		        
	    } else if (dbType==SGBD.SQLServer) {
	        updateObjectCommentSQLServer(con, objectType, tableName, columnName, newComment);		        
	    } else {
	    		System.err.println("Falha de configuração, Sistema de Gestão de Bases de Dados desconhecido!");
	    }
	}	
    /**
     * 💡 Método Router: Obtém o comentário de uma Tabela ou Vista com base no SGBD.
     *
     * @param con A ligação ativa à base de dados.
     * @param dbType O tipo de SGBD (ex: "MySQL" ou "SQLServer").
     * @param objectName O nome da Tabela ou Vista.
     * @return O comentário do objeto.
     * @throws SQLException Se ocorrer um erro SQL.
     * @throws IllegalArgumentException Se o tipo de base de dados não for reconhecido.
     */
    public static String getObjectComment(Connection con, String database, Configura.SGBD dbType, String objectName) throws SQLException {
        if (dbType==SGBD.MySQL) {
            return getObjectCommentMySQL(con, database, objectName); 
            
        } else if (dbType==SGBD.SQLServer) {
            return getObjectCommentSQLServer(con, database, objectName);
        } else {
            throw new IllegalArgumentException(
                "Tipo de base de dados não suportado para obter comentários: " + dbType + ". Use 'MySQL' ou 'SQLServer'."
            );
        }
    }
    
    /**
     * 🔍 Obtém o comentário (MS_Description) de uma Tabela ou Vista no SQL Server, 
     * com filtro explícito pelo nome do esquema (schema).
     *
     * @param con A ligação ativa à base de dados SQL Server.
     * @param schemaName O nome do esquema onde a tabela/vista reside (ex: 'dbo').
     * @param objectName O nome da Tabela ou Vista.
     * @return O comentário do objeto, ou uma string vazia se não existir ou for nulo.
     * @throws SQLException Se ocorrer um erro SQL.
     */
    public static String getObjectCommentSQLServer(Connection con, String schemaName, String objectName) throws SQLException {
        String comment = "";
        
        // Concatena o schemaName e objectName para uso em OBJECT_ID
        // 💡 Alterado: OBJECT_ID agora recebe 'schema.object' como um parâmetro.
        String objectFullName = schemaName + "." + objectName;

        // T-SQL para procurar a propriedade estendida 'MS_Description'
        String tsql = "SELECT CAST(p.value AS NVARCHAR(MAX)) AS comment " +
                      "FROM sys.extended_properties AS p " +
                      "WHERE p.major_id = OBJECT_ID(?) " + // O parâmetro inclui agora o schema
                      "AND p.minor_id = 0 " +              
                      "AND p.name = N'MS_Description'";    

        try (PreparedStatement pstmt = con.prepareStatement(tsql)) {
            // Define o nome completo do objeto (schema.tabela)
            pstmt.setString(1, objectFullName); 

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    comment = rs.getString("comment");
                }
            }
        }
        return (comment != null) ? comment : "";
    }
    
	/**
    	 * 🔍 Obtém o comentário (texto) de uma Tabela ou Vista no MySQL, 
    	 * consultando a tabela de metadados information_schema.TABLES.
    	 *
    	 * @param con A ligação ativa à base de dados MySQL.
    	 * @param databaseName O nome da base de dados (schema) a consultar.
    	 * @param objectName O nome da Tabela ou Vista.
    	 * @return O comentário da tabela, ou uma string vazia se não existir ou for nulo.
    	 * @throws SQLException Se ocorrer um erro SQL.
    	 */
    	public static String getObjectCommentMySQL(Connection con, String databaseName, String objectName) throws SQLException {
    	    String comment = "";
    	    
    	    // SQL para extrair o COMMENT da tabela information_schema.TABLES
    	    String sql = "SELECT table_comment, TABLE_TYPE " +
	                 "FROM information_schema.TABLES " +
	                 "WHERE table_schema = ? " + 
	                 "AND table_name = ?";
    	    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
    	        
    	        // 1. O primeiro parâmetro '?' é definido como o nome da base de dados
    	        pstmt.setString(1, databaseName); 
    	        
    	        // 2. O segundo parâmetro '?' é definido como o nome da tabela/vista
    	        pstmt.setString(2, objectName); 
    	        try (ResultSet rs = pstmt.executeQuery()) {
    	            if (rs.next()) {
    	                comment = rs.getString("table_comment");
    	                if(rs.getString("TABLE_TYPE").equals("VIEW"))
    	                		comment = "VIEW: "+ Comment.get(objectName);
    	            }
    	        }
    	    }
    	    // Retorna a string vazia se o comentário for nulo ou não encontrado.
    	    return (comment != null) ? comment : "";
    	}

    /**
     * Atualiza o comentário de uma Tabela, Vista ou Coluna.
     *
     * @param con            Conexão activa ao MySQL
     * @param objectType     "TABLE", "VIEW" ou "COLUMN"
     * @param objectName     Nome da tabela/vista
     * @param subObjectName  Nome da coluna (apenas para COLUMN)
     * @param newComment     Novo comentário desejado
     * @throws SQLException  Caso ocorra erro SQL
     */
    	public static void updateObjectCommentMySQL(Connection con, String objectType, String objectName, String subObjectName, String newComment) throws SQLException {

		    String safeComment = newComment.replace("'", "''"); 
		    String sqlAlter;
		    
		    // 1. Lógica para Tabela ou Vista (Sintaxe Simples)
		    if (objectType.equalsIgnoreCase("TABLE") || objectType.equalsIgnoreCase("VIEW")) {
		        sqlAlter = String.format("ALTER TABLE `%s` COMMENT = '%s'", objectName, safeComment);
		        
		    } 
		    
		    // 2. Lógica para Coluna (Híbrida: Metadados + Regex)
		    else if (objectType.equalsIgnoreCase("COLUMN")) {
		        if (subObjectName == null || subObjectName.isEmpty()) {
		             throw new IllegalArgumentException("O nome da coluna (subObjectName) é obrigatório para objectType 'COLUMN'.");
		        }
		        
		        // A. Obter DDL Completo da Tabela (Necessário para extrair cláusulas como DEFAULT)
		        String sqlShow = "SHOW CREATE TABLE `" + objectName + "`";
		        String createTableSQL = null;

		        try (Statement stm = con.createStatement();
		             ResultSet rs = stm.executeQuery(sqlShow)) {
		            if (rs.next()) 
		            		createTableSQL = rs.getString(2);
		            else throw new SQLException("Objeto não encontrado: " + objectName);
		        }

		        // B. EXTRAÇÃO HÍBRIDA: Obter tipo, precisão e nulidade do JDBC.
		        DatabaseMetaData metaData = con.getMetaData();
		        String tipoBase = null;
		        int tamanho = 0;
		        int escala = 0; 
		        String nulidade = "";
		        try (ResultSet rs = metaData.getColumns(new Configura().getDTB(), null, objectName, subObjectName)) {
		            if (rs.next()) {
		                tipoBase = rs.getString("TYPE_NAME");
		                tamanho = rs.getInt("COLUMN_SIZE");
		                escala = rs.getInt("DECIMAL_DIGITS");
		                String isNullable = rs.getString("IS_NULLABLE");
		                
		                // Mapeamento de nulidade
		                nulidade = "NO".equalsIgnoreCase(isNullable) ? " NOT NULL" : ""; 
		            }
		        }
		        
		        if (tipoBase == null) {
		            throw new SQLException("Coluna '" + subObjectName + "' não encontrada nos metadados.");
		        }
		        
		        // C. RECONSTRUÇÃO SEGURA DO DDL (Garante (P,S) e evita (N) em tipos fixos)
		        StringBuilder ddlBuilder = new StringBuilder();
		        ddlBuilder.append(tipoBase);

		        // Tipos de dados que NÃO podem ter tamanho ou precisão (evita erros em DATE, BLOB, INT, etc.)
		        boolean isFixedSizeOrNoLengthType = tipoBase.matches("(?i)DATE|TIME|TIMESTAMP|YEAR|INT|BIGINT|TINYINT|MEDIUMBLOB|LONGBLOB|TINYBLOB|BLOB|MEDIUMTEXT|LONGTEXT|TINYTEXT|TEXT");

		        if (escala > 0) { // Tipo como DECIMAL(P, S)
		             ddlBuilder.append(String.format("(%d, %d)", tamanho, escala));
		        } else if (tamanho > 0 && !isFixedSizeOrNoLengthType) {
		             // Tipos como VARCHAR(L), CHAR(L), etc., que aceitam tamanho.
		             ddlBuilder.append(String.format("(%d)", tamanho));
		        }
		        
		        // D. EXTRAÇÃO DA CLÁUSULA DEFAULT (usando Regex no DDL completo)
		        String defaultRegex = "(?i)DEFAULT\\s+('.*?'|\\S+)";
		        Matcher defaultMatcher = Pattern.compile(defaultRegex).matcher(createTableSQL);
		        String defaultClause = "";
		        
		        if (defaultMatcher.find()) {
		            defaultClause = " " + defaultMatcher.group(0).trim();
		            
		            // Limpa o 'DEFAULT NULL' redundante se o campo já não for NOT NULL
		            if (nulidade.isEmpty() && defaultClause.toUpperCase().contains("NULL")) {
		                defaultClause = ""; 
		            }
		        }

		        // E. MONTAGEM INICIAL DA DEFINIÇÃO
		        String cleanDefinition = ddlBuilder.toString() + defaultClause + nulidade;
		        
		        // F. LIMPEZA FINAL: Remove cláusulas inválidas e redundâncias de sintaxe
		        
		        // Remove cláusulas de CHARSET e COLLATE (inválidas no CHANGE COLUMN)
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+DEFAULT\\s+CHARSET=\\S+", " ").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+CHARACTER\\s+SET\\s+\\S+", " ").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+COLLATE\\s+\\S+", " ").trim();

		        // Substitui a sequência inválida pelo DDL correto: ' NOT NULL'
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+DEFAULT\\s+NULL\\s*[,]\\s*NOT\\s+NULL", " NOT NULL").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+DEFAULT\\s+NULL\\s+NOT\\s+NULL", " NOT NULL").trim();
		        
		        // Remove cláusulas de chave (por segurança)
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+PRIMARY\\s+KEY", " ").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+UNIQUE\\s+KEY", " ").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+AUTO_INCREMENT", " ").trim();
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+KEY", " ").trim(); 
		        cleanDefinition = cleanDefinition.replaceAll("(?i)\\s+REFERENCES.*", " ").trim(); 
		        cleanDefinition = cleanDefinition.replaceAll(",\\s*$", " ").trim(); // Remove vírgulas soltas no final
		        
		        cleanDefinition = cleanDefinition.trim() + " "; 

		        // G. Construir o Novo Comando ALTER TABLE CHANGE COLUMN
		        sqlAlter = String.format(
		            "ALTER TABLE `%s` CHANGE COLUMN `%s` `%s` %s COMMENT '%s'",
		            objectName,
		            subObjectName,
		            subObjectName,
		            cleanDefinition, 
		            safeComment
		        );
		        
		    } else {
		         throw new IllegalArgumentException("Tipo de objeto inválido. Use 'TABLE', 'VIEW' ou 'COLUMN'.");
		    }

		    // 3. Executar o Comando
		    System.out.println("SQL a executar: " + sqlAlter);
		    try (Statement stm = con.createStatement()) {
		        stm.executeUpdate(sqlAlter);
		        
		        String logMessage = "Comentário de " + objectType + " '" + objectName;
		        if (subObjectName != null && !subObjectName.isEmpty()) {
		            logMessage += "." + subObjectName;
		        }
		        System.out.println("✅ " + logMessage + "' atualizado no MySQL.");
		    }
		}
}