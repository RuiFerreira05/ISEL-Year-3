package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Define a configuração usada no acesso à base de dados via JDBC.
 * A configuração de acesso é carregada de um ficheiro externo (db.properties) 
 * para garantir segurança e fácil manutenção.
 * Preparado para o MySQL e para o SQLServer com driver JDBC tipo 4.
 * 
 * @author Engº Porfírio Filipe
 */

/*
--> Linguagem simplicada do JDBC
	 System.out.println("✅ Conexão estabelecida com sucesso!");
	 System.out.println("🔌 Conexão fechada.");
	 System.err.println("❌ Falha ao estabelecer a conexão.");
     System.out.println("⚙️ A iniciar transação...");
     System.out.println("👍 Transação confirmada (COMMIT).");
     System.err.println("🚨 Erro no SQL. Transação revertida (ROLLBACK).");
     System.out.println("🛠️ Conexão iniciada por omissão.");

*/

public class Configura {
	
	// Enum para selecionar explicitamente o SGBD na instanciação
	public enum SGBD {
		SQLServer, MySQL
	}
	
    // Define o nome da sua aplicação
    private static final String APP_NAME = "Gestor Académico";
	// Define a versão da aplicação
    private static final String APP_VERSION = "2.0.Beta"; 
	// Define a abrevidatura da aplicação
    private static final String APP_ABR = "GA2"; 
	// Define o caminho relativo para aces☼o ao ficheiro de configuração
	private static final String CONFIG_FILE  = "WEB-INF/config.properties";
    // Define, em tempo de execução, o caminho para acesso a ficheiros
    private static String filePath = null; 
	
	// --- Variáveis que facilitam configurações ---
    
    // Pode ser alterada em modo administração
	private static String database_ 	= "GA";		// É usada se não existir em WEB-INF\config.properties	
	
	// --- Configuração credenciais hardcoded, caso o ficheiro "config.properties" não esteja presente ---
	private String database 			= database_;		// Nome por omissão da Base de Dados
	private String server 	    		= "localhost"; 	// Servidor por omissão
	private String usr 		    		= "root";		// Utilizador por omissão (deve ser carregado do ficheiro)
	private String pwd 		    		= "root";		// Password por omissão (deve ser carregada do ficheiro)
	
	private String drv		    		= null;			// Nome da classe do Driver JDBC específico
	private String url		    		= null;			// URL de conexão JDBC específico
	private SGBD   sgbd				= null;			// O SGBD selecionado para esta instância

	
	// --- Construtor ---

	/**
	 * construtor sem parâmetros que, por omissão, configura a conexão para MySQL.
	 */
	public Configura () {
		// Chama o construtor principal, usando MySQL como o SGBD padrão.
		this(SGBD.MySQL); 
	}
	/**
	 * Cria uma nova instância de Configura para um SGBD específico.
	 * Inicializa o driver e a URL, e carrega as credenciais do ficheiro de propriedades.
	 * @param sgbd O Sistema Gestor de Base de Dados (SGBD.MySQL ou SGBD.SQLServer).
	 */
	public Configura (SGBD sgbd) {
		this.sgbd = sgbd;
		build();
		loadProperties();
		loadDriver();
	}
	
	private synchronized void build() {
			this.database=Configura.database_;
			if(this.isSQLServer()) {
				this.drv = "com.microsoft.sqlserver.jdbc.SQLServerDriver";	
				this.url = "jdbc:sqlserver://"+this.server+":1433;databaseName="
				+this.database+";encrypt=true;trustServerCertificate=true;";
			} else if(this.isMySQL()) {
				this.drv = "com.mysql.cj.jdbc.Driver";
				this.url = "jdbc:mysql://" + this.server + ":3306/" + this.database
						+ "?useLegacyDatetimeCode=false&serverTimezone=Europe/Lisbon";
			}
	}
	
	public String getRealPath() {
		if(filePath==null) {
			filePath = "src/main/webapp/";
			if(!new File(filePath).exists())
				filePath = getWebRootPath(server);
			System.out.println("📂 Caminho dos ficheiros: "+filePath);
		}
		return filePath;
	}
	/**
	 * Carrega o ficheiro de propriedades e inicializa os parametros de conexão (servidor, utilizador, password).
	 * Sugestão: Num ambiente de produção, garantir que este método lança uma exceção se o ficheiro falhar.
	 */
	private void loadProperties() {
		String filePath = getRealPath()+CONFIG_FILE;
			
		// System.out.println("💡 Caminho da configuração de acesso:\n'"+filePath+"'");
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(filePath)) {
			properties.load(fis);
			
			// Atualiza as variáveis de INSTÂNCIA com as credenciais lidas do ficheiro.
			setSRV(properties.getProperty("db.server"));
			setUSR(properties.getProperty("db.user"));
			setPWD(properties.getProperty("db.password"));
			setDTB(properties.getProperty("db.database"));
			
		} catch (IOException e) {
			System.err.println("❌ Falha ao carregar o ficheiro '"+CONFIG_FILE+"'.");
			// e.printStackTrace();
		}
	}

	/**
	 * Carrega a classe do Driver JDBC para esta instância.
	 * @return true se o driver foi carregado com sucesso, false caso contrário.
	 */
	public boolean loadDriver() {
		try {
			// System.out.println("Vai carregar o driver (" + this.drv + ")...");
			Class.forName(this.drv);
			return true;
		} catch (ClassNotFoundException e) {
			System.err.println("❌ Não é possível carregar o Driver JDBC: " + this.drv + ".");
			System.err.println("Verifique se o JAR do Driver está no classpath.");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("🚨 Erro inesperado no carregamento do Driver JDBC: " + this.drv + ".");
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Apresenta as propriedades do driver corrente.
	 * Sugestão: Este método é útil para diagnóstico, mas deve ser removido ou protegido em produção.
	 */
	public void showDriverProperties() {
		try {
			// Carrega o driver explicitamente, embora já o tenha feito no construtor.
			Class.forName(drv); 

			Driver driver = DriverManager.getDriver(url);

			System.out.println("Vai listar as propriedades do driver...");
			DriverPropertyInfo[] info = driver.getPropertyInfo(url, null);
			for (int i = 0; i < info.length; i++) {
				// Os detalhes são impressos para diagnóstico.
				String name = info[i].name;
				boolean isRequired = info[i].required;
				String value = info[i].value;
				String desc = info[i].description;
				String[] choices = info[i].choices;
				System.out.println(name + " (" + ((isRequired) ? "Obrigatório" : "Opcional") + ") " + ": " + value
						+ ", " + desc + ", " + choices);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Driver: "+e.getMessage());
		} catch (SQLException e) {
			System.err.println("SQLException" + e.getMessage());
		}
	}

	// --- Métodos de Conexão ---
	
	/**
	 * Devolve uma nova conexão à base de dados com as configurações transacionais por omissão:
	 * - AutoCommit: TRUE
	 * - Nível de Isolamento: TRANSACTION_READ_UNCOMMITTED
	 * @return Objeto Connection ou null em caso de falha.
	 */
	public Connection getConnection() {
		// Por omissão fica em autocommit
		return getConnection(true);
	}
	
	/* *
	 * Níveis de Isolamento (ANSI/ISO SQL) e Anomalias:
	 * ------------------------------------------------------------------------------------------------------------------------------------------------
	 * Nível                                  | Descrição                                         | Previne
	 * ------------------------------------------------------------------------------------------------------------------------------------------------
	 * TRANSACTION_READ_UNCOMMITTED (Level 0) | O mais baixo. Permite todas as anomalias.         | Nenhuma
	 * TRANSACTION_READ_COMMITTED   (Level 1) | Previne Leitura Suja (Dirty Read).                | Leitura Suja (Dirty Read)
	 * TRANSACTION_REPEATABLE_READ  (Level 2) | Previne Leitura Suja e Leitura Não Repetível.     | Dirty Read e Non-Repeatable Read
	 * TRANSACTION_SERIALIZABLE     (Level 3) | O mais alto. Previne todas as anomalias.          | Todas, incluindo Leitura Fantasma (Phantom Read)
	 * ------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	/**
	 * Devolve uma nova conexão à base de dados, permitindo a configuração 
     * do modo AutoCommit e do Nível de Isolamento da Transação.
	 * @param autocommit Define o estado do AutoCommit (true/false).
	 * @param level Define o nível de isolamento da transação (constantes Connection.TRANSACTION_...).
	 * @return Objeto Connection.
	 */
	public Connection getConnection(boolean autocommit) {
		Connection con = null;	
		try {
			// 1. Estabelecer a Conexão (Usa as variáveis de instância)
			con = DriverManager.getConnection(this.url, this.usr, this.pwd);
			// 2. Configurar 0 nivel de isolamento
			if(!autocommit)  // nivel de isolamento mais rápido
				con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			// 3. Configurar o modo transacional
			con.setAutoCommit(autocommit);
		} catch (SQLException e) {
			System.out.println("🚨 Falha no estabelecimento da conexão SQL.");
			System.err.println("❌ Não foi possivel estabelecer a ligação com a base de dados.");
			System.err.println("Veja a descrição completa do erro:");
			// AVISO: Em aplicações robustas, esta exceção deve ser propagada
			e.printStackTrace();
		} 
		return con;
	}
	
	// --- Métodos de Suporte Estáticos ---
	
    /**
     * Verifica a presença de propriedades do sistema que são 
     * definidas por containers web (Tomcat, Jetty, etc.) e que não seriam definidas 
     * em um ambiente de console normal.
     * @return true se o ambiente for um Container Web.
     */
    public static boolean isWebEnvironment() {
        // 1. Verificação Tomcat: Propriedade que aponta para o diretório base do Tomcat.
        if (System.getProperty("catalina.base") != null) {
            return true;
        }

        // 2. Verificação Jetty: Propriedade que aponta para o diretório base do Jetty.
        if (System.getProperty("jetty.home") != null || System.getProperty("jetty.base") != null) {
            return true;
        }
        
        // 3. Verificação WildFly/JBoss (Exemplo):
        if (System.getProperty("jboss.server.base.dir") != null) {
            return true;
        }
        
        // Se nenhuma propriedade de container for encontrada, assume-se Modo Console.
        return false;
    }
	
	/**
     * Faz um acesso HTTP GET ao servlet especificado para obter o caminho real
     * do sistema de ficheiros para a raiz da aplicação (o diretório do .war).
     * @return O caminho obtido do Servlet, ou null se ocorrer um erro.
     */
    public static String getWebRootPath(String server) {
    		String servletURL	= "http://"+server+"/"+APP_ABR+"/WebRootPath";
        System.out.println("🌐 Acesso ao URL: " + servletURL);      
        try {
            // 1. Configurar e abrir a conexão
        		// 1.1. Converter a String para URI
            URI uri = new URI(servletURL);
            // 1.2. Converter para URL:
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); 
            
            // 2. Verificar o código de resposta
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                
                // 3. Ler o conteúdo da resposta
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    return response.toString(); // Retorna o Context Path
                }
            } else {
                System.err.println("❌ Falha no acesso HTTP. Código de resposta: " + responseCode);
                System.out.println("🌐 + ❌ Verifique se o servidor web está a correr e se o URL está correto.");
                return null; 
            }
        } catch (Exception e) {
            System.err.println("\n❌ Erro de I/O ou URL malformado: " + e.getMessage());
            return null;
        }
    }
    
	/**
	 * Verifica se a conexão está ativa e é válida.
	 * @param con A conexão a verificar.
	 * @return true se a conexão é válida, false caso contrário.
	 */
	public static boolean isConnectionValid(Connection con) {
        final int TIMEOUT_SEGUNDOS=5;
	    if (con == null) 
	        return false;
	    try {
	        return con.isValid(TIMEOUT_SEGUNDOS); 
	    } catch (SQLException e) {
	        // e.printStackTrace(); 
	        return false;
	    }
	}
	
	/**
     * Fecha a conexão de forma segura (ignora se a conexão for null).
     * @param con A conexão a fechar.
     */
    public static void close(Connection con) {
        if (con != null) {
            try {
                con.close();
                // System.out.println("🔌 Conexão fechada.");
            } catch (SQLException e) {
                System.err.println("❌ Erro ao fechar a conexão.");
                e.printStackTrace();
            }
        }
    }

	// --- Getters (acesso às configurações) ---
	
    public SGBD   getSGBD()  		{ return sgbd; }
    public String getDTB() 			{ return database; }
	public String getDRV() 			{ return drv; }
	public String getUSR() 			{ return usr; }
	public String getURL() 			{ return url; }
	public static String getDTB_() 	{ return database_; }
	
	public boolean isMySQL() 		{ return sgbd==SGBD.MySQL; }
	public boolean isSQLServer() 	{ return sgbd==SGBD.SQLServer; }
	
	// --- Setters (alteração dinâmica das configurações) ---
	// usado só na administração!
	public synchronized static void setDTB_(final String str) 
		{ database_=str.trim(); }
	
	/**
	 * Altera o nome da base de dados (o URL de conexão deve ser reconfigurado após esta chamada).
	 */
	public synchronized void setDTB(final String str) { 
		if (str != null) {
			database = str.trim();
			database_= database;
			build();
		}
	}

	/**
	 * Altera o nome da base de dados (o URL de conexão deve ser reconfigurado após esta chamada).
	 */
	public synchronized void setSRV(final String str) { 
		if (str != null) {
			server = str.trim();
			build();
		}
	}
	
	/**
	 * Altera a palavra passe do utilizador da base de dados.
	 */
	public synchronized void setPWD(final String str) { 
		if (str != null) 
			pwd = str.trim(); 
	}
	
	/**
	 * Altera o nome do utilizador da base de dados.
	 */
	public synchronized void setUSR(final String str) { 
		if (str != null) 
			usr = str.trim(); 
	}
	/**
     * Devolve uma lista com todos os nomes de bases de dados (catalogs) existentes no servidor.
     * O método é sincronizado para garantir a segurança da thread, pois altera e restaura
     * temporariamente o campo de instância 'database'.
     *
     * @return Uma lista de Strings com os nomes das bases de dados, ou uma lista vazia em caso de erro.
     */
    public synchronized List<String> getBasesDeDados() {
        List<String> databases = new ArrayList<>();
        String dtb = database; // Guarda o valor original

        // A sincronização (na assinatura do método) protege esta secção:
        setDTB(""); // Altera o estado temporariamente para conexão ao servidor (sem DB específica)
        
        try (Connection con = getConnection()) {
            
            // Verifica se a conexão falhou
            if (con == null) {
                System.err.println("❌ Falha ao obter a conexão para listar bases de dados.");
                return databases;
            }
            
            // Obter os metadados da conexão
            DatabaseMetaData metaData = con.getMetaData();
            
            // Usar getCatalogs() para obter os nomes (Catálogos)
            try (ResultSet catalogs = metaData.getCatalogs()) {
            
                // Iterar sobre o ResultSet para extrair os nomes
                while (catalogs.next()) {
                    String dbName = catalogs.getString(1); // A coluna 1 é sempre o nome do Catalog/Database
                    databases.add(dbName);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("🚨 Erro SQL ao obter a lista de bases de dados para " + sgbd + ".");
            System.err.println("Verifique as credenciais, o servidor e as permissões de acesso aos metadados.");
            e.printStackTrace();
        } 
        	// Restaura o valor original do campo 'database' antes de sair do bloco sincronizado.
        setDTB(dtb); 
        return databases;
    }
    
    /**
     * Lista (imprime na consola) todos os nomes de bases de dados (catalogs) existentes no servidor.
     * Utiliza o método getBasesDeDados() para obter a lista.
     *
     * @return Uma lista de Strings com os nomes das bases de dados, ou uma lista vazia em caso de erro.
     */
    public List<String> listarBasesDeDados() {
        // Chama o método que contém a lógica sincronizada e de conexão
        List<String> databases = getBasesDeDados();

        System.out.println("⚙️ Bases de Dados encontradas no servidor '"+sgbd+"':");
        
        if (databases.isEmpty()) {
            System.out.println("⚠️ Nenhuma base de dados encontrada, ou houve um erro.");
            return databases;
        }

        // Iterar sobre a lista devolvida e imprimir
        for (String dbName : databases) {
            System.out.println("   -> " + dbName);
        }
        
        // Devolve a lista por conveniência, embora o principal seja a impressão.
        return databases;
    }
    
    /**
     * Obtém uma lista de todas as Tabelas e Vistas na base de dados (Catalog) atual 
     * e, opcionalmente, anexa o seu comentário de metadados.
     *
     * @param incluirComentarios Se true, o comentário é anexado ao nome do objeto 
     * 			(ex: "tabela [Comentário]").
     * @return Uma lista de Strings com os nomes dos objetos (com ou sem comentário).
     * @throws SQLException Em caso de falha de conexão ou metadados na Base de Dados.
     */
    public List<String> getObjects(boolean incluirComentarios) {
        // Inicializa a lista de objetos
        List<String> listaObjetos = new ArrayList<>();
        
        // Obtém o nome da base de dados/Catalog a ser usado para filtragem
        String databaseName = this.getDTB(); 
        
        // Usa try-with-resources para garantir que a conexão seja fechada
        try (Connection con = this.getConnection()) {
        		if(con==null)
        			return listaObjetos;
            DatabaseMetaData metaData = con.getMetaData();
        
            // Filtros: Tipos de objetos a listar. Usamos "TABLE" e "VIEW".
            String[] tipos = {"TABLE", "VIEW"};

            // Usa try-with-resources para garantir que o ResultSet seja fechado
            try (ResultSet rs = metaData.getTables(databaseName, null, "%", tipos)) {
                
                while (rs.next()) {
                    String nomeObjeto = rs.getString("TABLE_NAME");
                    String objetoFormatado = nomeObjeto;
                    
                    if (incluirComentarios) {
                        try {
                            // Usa this.getDTB() e this.getSGBD() explicitamente
                            String comentario = Comment.getObjectComment(con, this.getDTB(), this.getSGBD(), nomeObjeto);
                            
                            if (!comentario.isEmpty()) {
                                // Formata a string para incluir o comentário
                                objetoFormatado = nomeObjeto + " [" + comentario + "]";
                            }
                        } catch (Exception e) {
                             // Captura qualquer exceção ao buscar o comentário (IAE, SQLException, etc.)
                             System.out.println("⚠️ Aviso: Não foi possível obter o comentário para '" + nomeObjeto + "'. Detalhe: " + e.getMessage());
                        }
                    }
                    
                    listaObjetos.add(objetoFormatado);
                }
            }
        } catch (SQLException e) {
            // Não ignorar a exceção: Loga o erro e relança para o código chamador tratar.
            System.err.println("❌ Erro grave ao obter objetos da Base de Dados: " + e.getMessage());
            e.printStackTrace();
        }
            
        return listaObjetos;
    }
    
    public boolean criarBaseDeDados() {
    		return criarBaseDeDados(getDTB());
    }
    
    public synchronized boolean eliminarBaseDeDados() {
		return eliminarBaseDeDados(getDTB());
    }
    /**
     * Cria uma nova base de dados (Catálogo) no servidor SGBD.
     * O método é sincronizado para garantir a segurança da thread, pois manipula temporariamente 
     * o campo de instância 'database' para forçar a conexão ao servidor principal.
     *
     * @param nomeBD O nome que a nova base de dados deve ter.
     * @return true se a base de dados foi criada com sucesso, false caso contrário.
     */
    public synchronized boolean criarBaseDeDados(String nomeBD) {
        if (nomeBD == null || nomeBD.trim().isEmpty()) {
            System.err.println("❌ Erro: O nome da base de dados não pode ser vazio.");
            return false;
        }

        String dtb = getDTB(); // Guarda o valor original
        boolean sucesso = false;
        // 1. Altera o estado para forçar a conexão ao servidor principal (sem DB alvo)
        setDTB(""); 
        
        try (Connection con = getConnection()) {
            
            if (con == null) {
                System.err.println("❌ Falha ao obter a conexão para criar a base de dados.");
                return false;
            }

            // 2. Monta a instrução SQL
            String sql = "CREATE DATABASE " + nomeBD;

            // Nota: Para SQLServer, 'CREATE DATABASE' funciona.
            // Para MySQL, se a DB for criada sem 'IF NOT EXISTS', pode lançar exceção.
            // Poderia ser usada uma instrução mais robusta como: CREATE DATABASE IF NOT EXISTS " + nomeBD

            // 3. Executa a instrução DDL (Data Definition Language)
            try (Statement stmt = con.createStatement()) {
                
                System.out.println("\n⚙️ Executando DDL: " + sql + " (SGBD: " + this.sgbd + ")");
                
                // O executeUpdate() é usado para DDLs como CREATE, DROP, ALTER.
                stmt.executeUpdate(sql);
                sucesso = true;
                System.out.println("✅ Base de dados '" + nomeBD + "' criada com sucesso!");
            }

        } catch (SQLException e) {
            // Código de erro 1007 para MySQL e 1801 para SQLServer indicam DB já existente.
            // Para simplificar, tratamos a exceção como um erro geral na criação.
            System.err.println("🚨 Erro SQL ao criar a base de dados '" + nomeBD + "'.");
            System.err.println("Verifique se já existe ou se as permissões estão corretas.");
            // e.printStackTrace();
        } finally {
        	// 4. Restaura o valor original do campo 'database'
            setDTB(dtb); 
        }
        
        return sucesso;
    }
    
    /**
     * Elimina uma base de dados (Catálogo) existente no servidor SGBD.
     * O método é sincronizado para garantir a segurança da thread, pois manipula temporariamente 
     * o campo de instância 'database' para forçar a conexão ao servidor principal.
     *
     * @param nomeBD O nome da base de dados a ser eliminada.
     * @return true se a base de dados foi eliminada com sucesso, false caso contrário.
     */
    public synchronized boolean eliminarBaseDeDados(String nomeBD) {
        if (nomeBD == null || nomeBD.trim().isEmpty()) {
            System.err.println("❌ Erro: O nome da base de dados não pode ser vazio.");
            return false;
        }

        String dtb = getDTB(); // Guarda o valor original
        boolean sucesso = false;
        
        // 1. Altera o estado para forçar a conexão ao servidor principal (sem DB alvo)
        setDTB(""); 
        
        try (Connection con = getConnection()) {
            
            if (con == null) {
                System.err.println("❌ Falha ao obter a conexão para eliminar a base de dados.");
                return false;
            }

            // 2. Monta a instrução SQL
            String sql = "DROP DATABASE " + nomeBD;

            // Para robustez, pode ser usado: DROP DATABASE IF EXISTS " + nomeBD
            
            // 3. Executa a instrução DDL
            try (Statement stmt = con.createStatement()) {
                
                System.out.println("\n⚙️ Executando DDL: " + sql + " (SGBD: " + this.sgbd + ")");
                
                stmt.executeUpdate(sql);
                sucesso = true;
                System.out.println("✅ Base de dados '" + nomeBD + "' eliminada com sucesso!");
            }

        } catch (SQLException e) {
            // Código de erro 1007 para MySQL e 1801 para SQLServer indicam DB já existente.
        		if(e.getErrorCode()==1007 && isMySQL() || e.getErrorCode()==1801 && isSQLServer())
        			System.out.println("✅ A base de dados '" + nomeBD + "' já existe!");
        		else {
        			System.err.println("🚨 Erro SQL ("+e.getErrorCode()+") ao eliminar a base de dados '" + nomeBD + "'.");
        			System.err.println("Verifique se a DB existe ou se as permissões estão corretas.");
        			// e.printStackTrace();
        		}
        } finally {
        	// 4. Restaura o valor original do campo 'database'
            setDTB(dtb); 
        }
        
        return sucesso;
    }
    
    /**
     * Executa testes para uma dada instância de Configura:
     * 1. Listagem Inicial de Bases de Dados.
     * 2. Criação de uma DB de Teste.
     * 3. Listagem para confirmação.
     * 4. Eliminação da DB de Teste (Cleanup).
     * @param cfg A instância de Configura a ser testada (MySQL ou SQLServer).
     */
    private static void executarTeste(Configura cfg) {
        
        String sgbdNome = cfg.getSGBD().name(); 
        
		cfg.infoServer();

        // Cria um nome de DB
        String novaBDTeste = "A_TESTE_JDBC_" + sgbdNome.toUpperCase();
        
        // 1. LISTAGEM INICIAL DE BASE DE DADOS
        System.out.println("\n--- 2. Listagem Inicial de DBs ---");
        cfg.listarBasesDeDados(); 

        // 2. CRIAÇÃO DE BASE DE DADOS
        System.out.println("\n--- 3. CRIAÇÃO (DDL) de DB de Teste: " + novaBDTeste + " ---");
        cfg.criarBaseDeDados(novaBDTeste);

        // 3. CONFIRMAÇÃO E LISTAGEM INTERMÉDIA
        System.out.println("\n--- 3. Listagem Intermédia (Confirmação de Criação) ---");
        cfg.listarBasesDeDados();
        
        // 4. ELIMINAÇÃO (CLEANUP)
        System.out.println("\n--- 5. ELIMINAÇÃO (Cleanup) da DB de Teste: " + novaBDTeste + " ---");
        cfg.eliminarBaseDeDados(novaBDTeste);

        // 5. CONFIRMAÇÃO FINAL
        System.out.println("\n--- 6. Listagem Final (Confirmação de Eliminação) ---");
        cfg.listarBasesDeDados();
    }
    
	/**
	 * Devolve a data de hoje obtida a partir do SGBD configurado
	 */
	public synchronized LocalDate today() {
		LocalDate hoje = null;
        String dtb = getDTB(); // Guarda o valor original
        setDTB(""); 
		String func = "CURDATE()";
		if(sgbd==SGBD.SQLServer)
			func = "GETDATE()";
		try (Connection con=getConnection(); 
			 Statement stm = con.createStatement();
			 ResultSet rs = stm.executeQuery("SELECT "+func+" AS Today")) {
			 if(rs.next())
				 hoje =  rs.getDate(1).toLocalDate();
		} catch (SQLException e) {
			System.err.println("\nOcorreu um erro na obtenção da data de hoje...");
			System.err.println("Ver detalhes abaixo:\n");
			System.err.println("-----SQLException-----");
			System.err.println("Message:  " + e.getMessage());
			System.err.println("SQLState:  " + e.getSQLState());
			System.err.println("Vendor:  " + e.getErrorCode());
		}
		setDTB(dtb);
		return hoje;
	}
	
    /**
     * Lista as tabelas/views da base de dados e imprime cada linha na consola.
     */
    public static void listarObjectos() {
        List<String> tableNames = new Configura().getObjects(true);
        if (tableNames.isEmpty()) {
            System.out.println("⚠️ Nenhuma tabela ou vista encontrada, ou houve algum erro!");
            return;
        }
        System.out.println("🔍 A consultar a base de dados: ");
        for (String name : tableNames)
            System.out.println("-> "+name); 
        System.out.println("⚙️ Tabelas e Vistas encontradas (" + tableNames.size() +")");
    }	
    
    /**
     * Reúne e apresenta informações sobre o servidor.
     */
	public void infoServer() {
        String dtb = getDTB(); // Guarda o valor original
        setDTB(""); 
		try (Connection con=getConnection()) {
	        // Obter Informações do Ambiente do Servidor
			System.out.println("Informação sobre o Servidor:");
			System.out.println("A data de hoje: "
					+DataFormatter.LocalDateToString(today()));
	        String javaVersion = System.getProperty("java.version");
	        String osName = System.getProperty("os.name");
	        String osArch = System.getProperty("os.arch");
			System.out.println("Versão do Java: "+javaVersion);
			System.out.println("Sistema Operativo: "+osName);
			System.out.println("Arquitetura (Arch): "+osArch);
			DatabaseMetaData metaInformacaoBD = con.getMetaData();
			// Obter o nome do SGBD
			System.out.println("Nome do SGBD: "+metaInformacaoBD.getDatabaseProductName());
			// Obter o número máximo de conexões activas permitidas
			System.out.println("Nº Máximo de Ligações: "+metaInformacaoBD.getMaxConnections());
		} catch (SQLException e) {
			System.err.println("\nOcorreu um erro na obtenção de informações do servidor...");
			System.err.println("Ver detalhes abaixo:\n");
			System.err.println("-----SQLException-----");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Menssagem: " + e.getMessage());
            System.err.println("Código do Fornecedor: " + e.getErrorCode());
		} 
        setDTB(dtb); 
	}
    
    /**
     * Reúne e apresenta numa linha de informação sobre a aplicação.
     */
	public static String infoApp(String mensagem) {
		if(mensagem==null || mensagem.isEmpty())
			mensagem = (Configura.isWebEnvironment()?"WebApp: ":"Consola: ");
		return mensagem+APP_NAME + " ("+APP_VERSION+")";
	}

    /* 💻 main
	* ===================================================================
	* 📢 TESTE COMPLETO: Conexão, Listagem, Criação e Eliminação de DBs.
	* ===================================================================
	*/
    
    public static void main(String[] args) {
    		

		System.out.println("===================================================================");
		System.out.println("📢 TESTE: Conexão, Criação e Eliminação de DBs (DDL).");
		System.out.println("===================================================================");
		System.out.println(infoApp("Teste do "));		
		// --------------------------------------------------------------------------------
		// 🚀 TESTE 1: MYSQL
		// --------------------------------------------------------------------------------
		System.out.println("\n\n###################################################################");
		System.out.println("🧪 INÍCIO DO TESTE: MYSQL");
		System.out.println("###################################################################");
		
		// Instanciar a classe Configura para MySQL
		Configura cfgMySQL = new Configura(SGBD.MySQL);
		executarTeste(cfgMySQL);

		System.out.println("\nParametros finais configurados para MySQL:");
		System.out.println("		Base de Dados: " + cfgMySQL.getDTB());
		System.out.println("		URL: " + cfgMySQL.getURL());

        System.out.println("\n\n===================================================================");
		System.out.println("🏁 FIM DO TESTE: MYSQL");
		System.out.println("===================================================================");
		// --------------------------------------------------------------------------------
		// 🚀 TESTE 2: SQLSERVER
		// --------------------------------------------------------------------------------
		System.out.println("\n\n###################################################################");
		System.out.println("🧪 INÍCIO DO TESTE: SQLSERVER");
		System.out.println("###################################################################");
		
		// Criar uma nova instância independente configurada para SQLServer
		Configura cfgSQLServer = new Configura(SGBD.SQLServer);
		executarTeste(cfgSQLServer);

		System.out.println("\nParametros finais configurados para SQLServer:");
		System.out.println("		Base de Dados: " + cfgSQLServer.getDTB());
		System.out.println("		URL: " + cfgSQLServer.getURL());
        
        System.out.println("\n\n===================================================================");
		System.out.println("🏁 FIM DO TESTE: SQLSERVER");
		System.out.println("===================================================================");
	}	// --- 📢 Fim main (Exemplo de Uso) ---
}