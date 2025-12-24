package gestor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import util.*;

/**
 * Este projeto, promovido pelo Eng.º Porfírio Filipe, 
 * surge no âmbito da unidade curricular de Sistemas de Bases de Dados, 
 * enquadrada na Licenciatura em Engenharia Informática e Multimédia. 
 * Consiste na implementação parcial (em desenvolvimento) do protótipo de um gestor académico que, 
 * essencialmente, manipula inscrições de alunos em disciplinas de forma minimalista, 
 * com o objetivo de demonstrar possíveis abordagens de concretização, 
 * harmonizando tecnologias em evolução.
 * 
 * A classe Gestor atua como a 🎓 Camada de Lógica de Negócio (Business Logic) da aplicação, 
 * orquestrando as operações de alto nível, como a gestão da estrutura da base de dados 
 * e a importação de dados. 
 * Depende dos objetos Configura e Manipula para gerir a conexão e executar o SQL.
 * NOTA: Esta classe não é thread-safe.
 * 
 */
public class Gestor {
	// Caminho base para fotos
	static final String pathFotos = new Configura().getRealPath()+"pessoas"+File.separator;

	/**
	 * Carrega o conteudo da tabela 'aluno'. 
	 * Não faz commit nem rollback, porque vai ser executada no contexto de uma transação
	 * 
	 * @return true se correr bem
	 */
	public static boolean carregarAluno(Connection con) {
		return -1 != Manipula.xDirectiva(con, """
				INSERT INTO ALUNO (numero, nome, genero, nascido) VALUES
				(1,'ABEL ALVES BOTELHO','M','19801020'), 
				(2,'Abel Alves da Costa Pina','M','19710504'), 
				(3,'ABILIO DOS SANTOS PINTO BRANCO','M','19640114'),
				(4,'Abílio Pires dos Santos','M','19750106'), 
				(5,'ACACIO CARDOSO DA ROCHA','M','19771216'), 
				(6,'Acácio Cardoso do Nascimento','M','19670201'),
				(7,'Adalberto Luís Marques Rabaça','M','19661227'), 
				(8,'ADALBERTO LUIS MARTINHO DE MELO','M','19661215'), 
				(9,'ADAO DE ALMEIDA SILVARES','M','19740228'),
				(10,'Adao de Almeida Vasconcelos','M','19680608'), 
				(11,'ADAO FINO DA COSTA','M','19650930'), 
				(12,'Adélia Oliveira Pereira','F','19650730'),
				(13,'ADELIA MARIA VAZ PESTANA SETE DIAS','F','19731030'), 
				(14,'ADERITO AUGUSTO FERREIRINHA','M','19630318'), 
				(15,'Adérito Augusto Figueira','M','19720707'),
				(16,'ZACARIAS MACHADO FERREIRA','M','19720407'), 
				(17,'ZACARIAS MAGALHAES FERNANDES','M','19720416'), 
				(18,'Zélia Maria Lima da Costa','F','19710702'),
				(19,'Zélia Maria Lopes Dias Moreira','F','19570807'), 
				(20,'Álvaro Silva d''Almeida','M','19580907'), 
				(21,'António Fagundes Fraga','M','19591107'),
				(22,'Abreu Oliveira Antunes','M','19600107'), 
				(23,'ABEL JORGE DE ALPOIM E OSORIO DE VALDOLEIROS','M','20000101'), 
				(24,'ADILSON SALVE-RAINHA VICENTE','M','20000401'),
				(25,'AFONSO MARIA PERESTRELLO CORTE-REAL PERDIGÃO','M','20000301'), 
				(26,'VASCO DE VASCONCELOS DIAS DOS SANTOS CARNEIRO','M','20000501'), 
				(27,'Abreu Poças PETAVY','M','19990107'),
				(28,'ABEL JORGE DE ALPOIM E OSORIO DE VALDOLEIROS','M','20010803'), 
				(29,'ADILSON SALVE-RAINHA VICENTE','M','20021006'), 
				(30,'AFONSO MARIA PERESTRELLO CORTE-REAL PERDIGÃO','M','19980509'),
				(31,'PROFIRIO DIAS AGUIAR MOTA','M','19970108'), 
				(32,'Procópio Tinta Fina Taniças PATRAO CARNEIRO','M','20010701'), 
				(33,'PRIYESKUMAR PRAVINCHANDRA NANDVANA DA SILVA','M','19990401'),
				(34,'PROTÁSIO DE MATOS CARNEIRO LEÃO','M','20000501'), 
				(35,'BERNARDO PANTALEÃO NICO LOJA','M','19980903'),
				(36,'Maria AmÉlia Augusta EugÉnia Josefina LuÍsa Teodolinda HeloÍsa Francisca Xavier de Paula Gabriela Rafaela Gonzaga de BraganÇa','F','18530204')""");
	}

	/**
	 * Carrega o conteúdo binário de fotos (assumindo o formato p[numero].jpg) 
	 * para a base de dados. Este método itera sobre todos os números de aluno 
	 * e, se o ficheiro de foto existir, chama uma rotina externa para atualizar o registo.
	 * * 🛡️ Thread-Safe em relação ao JDBC: Usa try-with-resources para Statement e ResultSet.
	 * 🚀 NIO.2: Utiliza Path.of() e Files.exists() para manipulação de ficheiros mais robusta.
	 *
	 * @param conn A conexão ativa, que deve fazer parte de uma transação externa.
	 * @param path O caminho (String) para o diretório raiz das fotos.
	 * @return true se a execução da query SELECT e todas as chamadas 'Alunos.alterarFoto'
	 * foram bem-sucedidas. Retorna false se ocorrer uma exceção SQL ou se um 
	 * carregamento de foto falhar (interrompendo o processo).
	 */
	public static boolean carregarFoto(String path) {
	    // Definimos o status inicial como sucesso (true).
	    boolean status = true; 
	    
	    // 1. 🚀 Usar Path.of() para criar um objeto Path para o diretório
	    Path dirPath;
	    try {
	        dirPath = Paths.get(path); // Conversão da String para Path (NIO.2)
	    } catch (Exception e) {
	        System.err.println("❌ Erro ao criar o objeto Path para o diretório: " + path);
	        return false;
	    }

	    // 🛡️ TRY-WITH-RESOURCES: Garante que o Statement e o ResultSet são fechados.
	    try (Connection conn=new Configura().getConnection(); Statement stm = conn.createStatement();
	         ResultSet rs = stm.executeQuery("select numero from aluno order by numero")) {

	        while (rs.next()) {
	            Integer numero = rs.getInt("numero");
	            
	            // 2. Construção segura do caminho do ficheiro usando resolve()
	            Path filePath = dirPath.resolve("p" + numero + ".jpg"); 
	            String fich = filePath.toString(); // Obtém o caminho como String para o método legado
	            
	            // 3. Verificação de existência usando Files.exists()
	            if (Files.exists(filePath)) {
	                
	                // 4. Execução da alteração (Chama o método externo)
	                if (Alunos.alterarFoto(fich, numero)) {
	                    System.out.println("Carregou a foto '" + fich + "'! ✅");
	                } else {
	                    // Falha na rotina de UPDATE da base de dados (Alunos.alterarFoto)
	                    System.out.println("Falha no carregamento da foto '" + fich + "... ❌");
	                    status = false; // Marca o status de falha
	                    break; // Interrompe o processo para permitir um rollback coordenado
	                }
	            } else {
	                // Ficheiro não existe no sistema de ficheiros. Continua.
	                System.out.println("A foto '"+fich+"' não existe! ⚠️ Ignorado e a prosseguir...");
	            }
	        }
	    } catch (SQLException e) {
	        // Exceção de Base de Dados (ex: falha na query SELECT ou na conexão)
	        System.out.println("-----SQLException----- 🛑");
	        System.err.println("SQLState:  " + e.getSQLState());
	        System.err.println("Menssagem:  " + e.getMessage());
	        System.err.println("Código do Fornecedor:  " + e.getErrorCode());
	        status = false;
	    } 
	    // O retorno 'status' reflete o sucesso da operação em lote.
	    return status;
	}
	
	/**
	 * Carrega o conteudo da tabela 'disciplina'. Não faz commit nem rollback,
	 * porque vai ser executada no contexto de uma transação.
	 * 
	 * @return true se correr bem
	 */
	public static boolean carregarDisciplina(Connection conn) {
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('SBD', 'Sistemas de Bases de Dados')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into disciplina values ('SdI','Sistemas de Informação')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into disciplina values ('SCD','Sistemas Computacionais Distribuídos')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('ICD', 'Infraestruturas\" Computacionais Distribuídas')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('CPS', 'Comunicações e Processamento de Sinais')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('FSO', 'Fundamentos, de Sistemas Operativos')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('IPM', 'Interfaces'' Pessoa-Máquina')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('MAE', 'Matemática Aplicada à Engenharia')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('MDP', 'Matemática Discreta e Programação')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('MSSN', 'Modelação e Simulação de Sistemas Naturais')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('PCM', 'Produção de Conteúdos Multimédia')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('PIV', 'Processamento de Imagem e Visão')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('RI', 'Redes de Internet')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('RSCM', 'Redes e Serviços de Comunicação Multimédia')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('SA', 'Sensores e Atuadores')"))
			return false;
		if (-1==Manipula.xDirectiva(conn, "insert into Disciplina values ('TI', 'Tecnologias de Informação')"))
			return false;
		return true;
	}

	/**
	 * Carrega o conteudo da tabela 'inscricao', recorrendo à execução de 'Batch'.
	 * Não faz commit nem rollback, porque vai ser executada no contexto de uma transação
	 * 
	 * @return true se correr bem
	 */
	public static boolean carregarInscricao(Connection conn) {
		String[] inserts = { 
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (1, 2021, NULL, 'SBD','20210103')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (2, 2020, 10.0, 'SBD','20200302')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (3, 2020, 15.0, 'SBD','20200203')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (4, 2020, 10.0, 'SBD','20200205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (6, 2021, NULL, 'SBD','20210205')",
				"INSERT INTO INSCRICAO (numero, codigo, ano, nota, inscrito) VALUES "+
																		 "(7, 'SBD', 2018, NULL,'20180205'),"+ 
																		 "(7, 'SBD', 2019, 18.0,'20190205'),"+
																		 "(7, 'SBD', 2020, 19.0,'20200205'),"+
																		 "(7, 'SBD', 2021, 17.0,'20210205'),"+
																		 "(7, 'SBD', 2023, NULL,'20230205'),"+
																		 "(7, 'SBD', 2022, 13.0,'20220205'),"+
																		 "(7, 'SdI', 2019, NULL,'20190205'),"+
																		 "(7, 'SdI', 2020, 12.0,'20200205'),"+
																		 "(7, 'SdI', 2021,  9.0,'20210205'),"+
																		 "(7, 'SdI', 2022,  7.0,'20220205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (8, 2025, 15.0, 'SBD','20250905')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (9, 2025, 13.0, 'SBD','20250905')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (10,2025, NULL, 'SBD','20250805')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (11,2025, 14.2, 'SBD','20250705')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (12,2025, 18.0, 'SBD','20250704')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (13,2025, 16.0, 'SBD','20250803')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (14,2025, 14.0, 'SdI','20250815')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (15,2025, 15.0, 'SdI','20250712')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (16,2025, 11.0, 'SdI','20250820')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (18,2025, 11.0, 'SdI','20250905')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (19,2021, 13.1, 'SdI','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (20,2021, 16.0, 'SdI','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (21,2021, 17.0, 'SdI','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (22,2021, 12.0, 'SdI','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (14,2020, 13.0, 'ICD','20200205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (15,2020,  8.0, 'ICD','20200205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (15,2021, NULL, 'ICD','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (16,2021, 13.0, 'ICD','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (16,2022, NULL, 'SdI','20220205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (18,2020, 10.0, 'ICD','20200205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (19,2021, 18.1, 'ICD','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (20,2020,  7.0, 'ICD','20200205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (21,2021, 15.0, 'ICD','20210205')",
				"insert into inscricao (numero, ano, nota, codigo, inscrito) values (22,2021, 14.0, 'ICD','20210205')"};
		return Manipula.executaBatch(conn, inserts);
	}

	/**
	 * 📝 Cria/Atualiza comentários de metadados para tabelas e colunas na base de dados 
	 * dentro do contexto de uma Transação.
	 * ⚠️ Nota: O MySQL executa um COMMIT implícito em comandos ALTER TABLE,
	 * o que limita a eficácia do rollback para a operação de comentar.
	 *
	 * @return true se todos os comentários foram aplicados e commitados com sucesso, 
	 * false caso contrário (rollback ou falha na conexão).
	 */
	public static boolean criarComentarios() {
	    // Mapa de comentários permanece inalterado
	    Map<String, String> comentarios = Map.ofEntries(
	        Map.entry("aluno", "Armazena dados sobre os alunos"),              
	        Map.entry("aluno.numero", "Número único do aluno (Chave Primária)"),
	        Map.entry("aluno.nome", "Nome completo do aluno"),
	        Map.entry("aluno.genero","Género do aluno: \"f\" (feminino), \"m\" (masculino) ou \"x\" (desconhecido/default)"),
	        Map.entry("aluno.nascido","Data de nascimento do aluno. Deve ser no passado"),
	        Map.entry("disciplina","Armazena dados sobre as disciplinas"),
	        Map.entry("disciplina.codigo","Código único da disciplina (Chave Primária)"),
	        Map.entry("disciplina.designacao","Nome único da disciplina. (Chave Candidata)"),
	        Map.entry("inscricao","Regista as inscrições/avaliações dos alunos nas disciplinas"),
	        Map.entry("inscricao.numero","Número do aluno inscrito (Chave Estrangeira para ALUNO)"),
	        Map.entry("inscricao.codigo","Código da disciplina (Chave Estrangeira para DISCIPLINA)"),
	        Map.entry("inscricao.ano","Ano letivo da inscrição."),
	        Map.entry("inscricao.nota","Nota final na disciplina (NULL se não avaliado). Deve ser entre 00.00 e 20.00"),
	        //Map.entry("inscricao.inscrito","Data de inscrição. O ano tem de ser <= que o desta data"),
	        Map.entry("foto","Fotografia do aluno"),
	        Map.entry("foto.numero","Número do aluno ao qual pertence a fotografia (Chave Estrangeira para ALUNO)"),
	        Map.entry("foto.conteudo", "Conteudo binário da fotografia em '.jpg'") );
        
	    Configura cfg = new Configura();
	    // 1. INICIAR TRANSAÇÃO: Desliga o auto-commit para controlo manual        
	    try (Connection con=cfg.getConnection(false)) {
	       
	        System.out.println("🔄 A carregar comentários para " + cfg.getSGBD() + "... Transação iniciada.");
	        
	        for (Map.Entry<String, String> comentario : comentarios.entrySet()) {
	            String key = comentario.getKey();
	            String texto = comentario.getValue();
	            
	            // Verifica se é um comentário de coluna (contém o ponto '.')
	            if (key.contains(".")) {
	                int index = key.lastIndexOf('.');
	                String objeto = key.substring(0, index);        // Nome da Tabela/Vista
	                
	                // 🛠️ Extrai o nome da Coluna (à direita do ponto)
	                String coluna = key.substring(index + 1);       
	                
	                // Atualiza o comentário da Coluna
	                Comment.updateColumnComment(con, cfg.getSGBD(), objeto, coluna, texto);
	            } else {
	                // Atualiza o comentário da Tabela ou Vista
	               Comment.updateTableOrViewComment(con, cfg.getSGBD(), "TABLE", key, texto);
	            }
	        }	        
	        // 2. COMMIT: Confirma todas as alterações se o loop for bem-sucedido
	        con.commit(); 
	        System.out.println("✅ Os comentários foram criados com sucesso.");
	        return true;	        
	    } catch (SQLException e) {
	        System.out.println("❌ Falha de SQL no carregamento dos comentários. Tentando Rollback...");
	        System.out.println("Detalhes: " + e.getMessage());
	    } 
	    return false;
	}
	/**
	 * 🚀 Executa o carregamento inicial de dados para as tabelas principais 
	 * da base de dados ('aluno', 'disciplina', 'inscricao', 'foto') dentro de uma 
	 * **Transação Atómica** única. 🛡️
	 * O método utiliza {@code try-with-resources} para garantir que a Connection é 
	 * automaticamente fechada (devolvida ao Pool) no final do bloco.
	 * O fluxo de controlo é simplificado para uma 
	 * sequência lógica que interrompe a execução no primeiro erro e executa o {@code rollback} 
	 * explicitamente no bloco {@code try}.
	 *
	 * @return {@code true} se e só se o carregamento de todas as tabelas for concluído 
	 * e a transação for submetida (COMMIT); {@code false} caso contrário, após o {@code rollback}.
	 * @throws SQLException se houver falha ao obter a conexão ou se ocorrer uma falha 
	 * durante a configuração do auto-commit, commit, ou rollback.
	 */
	public static boolean carregarTabelas() {
	    boolean ok = false;
	    
	    // 1. TWR: Garante que con.close() é chamado automaticamente.
	    try (Connection con = new Configura().getConnection(false)) {// desliga o autocommit 
		        
	        System.out.println("-------------------------------------------------------------------");
	        System.out.println("▶️ INÍCIO: Carregamento Transacional de Tabelas... 🛡️");

	        // 2. FLUXO LINEAR: Se qualquer método retornar 'false', 'ok' será 'false' 
	        // e as expressões seguintes podem ser avaliadas (ou não, dependendo da short-circuit evaluation).
	        
	        System.out.println("Carregar Tabela 'aluno'...");
	        ok = carregarAluno(con);
	        
	        if (ok) {
	            System.out.println("Tabela 'aluno' carregada com sucesso! ✅");
	            System.out.println("Carregar Tabela 'disciplina'...");
	            ok = carregarDisciplina(con);
	        }
	        
	        if (ok) {
	            System.out.println("Tabela 'disciplina' carregada com sucesso! ✅");
	            System.out.println("Carregar Tabela 'inscricao'...");
	            ok = carregarInscricao(con);
	        }        
	        // 3. DECISÃO FINAL: COMMIT ou ROLLBACK (Feito dentro do bloco try)
	        if (ok) {
	            System.out.println("Tabela 'inscricao' carregada com sucesso! ✅");
	            con.commit(); // 🔑 COMMIT Final
	            System.out.println("Carregar Tabela 'foto'...");
	            ok = carregarFoto(pathFotos);  // independente da transação em curso
	            System.out.println("Tabela 'foto' carregada com sucesso! ✅");
	            System.out.println("O carregamento concluído. 🏁");
	        } else {
	            con.rollback(); // 🔙 ROLLBACK
	            System.out.println("O carregamento das Tabelas não teve sucesso. ❌ Transação revertida (ROLLBACK). 🔙");
	        }
	        
	        // No final do bloco try, con.close() é chamado automaticamente pelo TWR.
	        
	    } catch (SQLException e) { 
	        // 4. TRATAMENTO DE EXCEÇÕES JDBC:
	        // Captura exceções que impediram o fluxo principal (ex: falha ao obter ligação, SQL na query, etc.)
	        System.out.println("O carregamento das Tabelas não teve sucesso. ❌");
	        System.err.println("-----DETALHES DO ERRO ORIGINAL----- 🛑");
	        System.err.println("SQLState:  " + e.getSQLState());
	        System.err.println("Menssagem:  " + e.getMessage());
	        System.err.println("Código do Fornecedor:  " + e.getErrorCode());       
	        // Não é necessário um rollback explícito aqui, pois a transação foi interrompida 
	        // e a conexão será fechada pelo TWR, ou o erro ocorreu antes do autocommit ser desativado.
	    }
	    
	    return ok;
	}
	/**
	 * Apresenta lista de alunos com fotografia. 
	 * Não faz commit nem rollback, porque vai ser executada
	 * no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarVistaAlunos(Connection con) {
		return -1!=Manipula.xDirectiva(con, """
				CREATE VIEW alunos AS
				SELECT 		a.numero,
							nome,
							genero,
							nascido,
						    YEAR(curdate())-YEAR(nascido) -
								((MONTH(nascido) > MONTH(curdate())) OR
								(MONTH(nascido) = MONTH(curdate())) AND
								(DAY(nascido) > DAY(curdate()))) idade,
							conteudo foto
				FROM aluno a, foto f 
				WHERE a.numero=f.numero""") 
				&& Comment.view("alunos","""
						Agrega dados básicos de ALUNO com IDADE e FOTO""");
	}
	/**
	 * Apresenta as inscrições mais recentes (ano) de cada aluno em cada disciplina, 
	 * desde que o aluno ainda não tenha obtido uma nota (ou seja, a inscrição está ativa/pendente). 
	 * 
	 * Não faz commit nem rollback, porque vai ser executada
	 * no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarVistaInscricoes(Connection con) {
		return -1!=Manipula.xDirectiva(con, 
				"CREATE VIEW Inscricoes AS"+
						" SELECT  I.NUMERO,"+
						" I.CODIGO,"+
						" (SELECT DESIGNACAO FROM DISCIPLINA S WHERE S.CODIGO=I.CODIGO) DESIGNACAO,"+
						" MAX(I.ANO) ANO"+
						" FROM INSCRICAO I, DISCIPLINA D"+
						" WHERE I.CODIGO=D.CODIGO AND NOTA IS NULL"+ 
						" GROUP BY 1, 2"+
						" HAVING ANO=(SELECT MAX(ANO) FROM INSCRICAO N WHERE N.NUMERO=I.NUMERO AND N.CODIGO=I.CODIGO)"+ 
				" ORDER BY 1, 2") 
				&& Comment.view("Inscricoes","""
						Lista a última inscrição, em cada disciplina, com NOTA NULL (inscrições ativas/pendentes)""");
	}
	
	/**
	 * Apresenta a primeira (mais antiga) inscrição de cada aluno em cada disciplina, 
	 * onde ele obteve a nota máxima de aprovação (nota entre 10 e 20) nessa disciplina. 
	 * Não faz commit nem rollback, porque vai ser executada no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarVistaAvaliacoes(Connection con) {
		return -1!=Manipula.xDirectiva(con, 
				"CREATE VIEW Avaliacoes AS"+
						" SELECT"+
							" x.numero AS NUMERO,"+
							" MIN(x.ano) AS ANO,"+
							" z.DESIGNACAO,"+
							" x.nota AS NOTA"+
						" FROM"+
							" (inscricao x,"+ 
							" (SELECT i.numero AS numero, i.codigo AS codigo, MAX(i.nota) AS MaxNota"+
							" FROM inscricao i"+
							" WHERE i.nota >= 10 AND i.nota <= 20"+
							" GROUP BY 1 , 2) y),"+ 
							" disciplina z"+
						" WHERE"+
							" x.codigo 	= 	z.codigo"+
							" AND x.numero 	= 	y.numero"+
							" AND x.codigo 	= 	y.codigo"+
							" AND x.nota 	= 	y.MaxNota"+
						" GROUP BY 1 , 3 , 4"+
						" ORDER BY 1 , 2 , 3") 
				&& Comment.view("Avaliacoes","""
						Retorna a MELHOR NOTA obtida, por cada ALUNO, DISCIPLINA no MIN(ANO)""");		
	}
	
	/**
	 * Apresenta lista com todos os alunos e, para cada um, 
	 * lista as suas inscrições (se existirem), 
	 * incluindo a nota, o ano, a idade atual do aluno 
	 * e o número de disciplinas onde ainda não teve aproveitamento. 
	 * Não faz commit nem rollback, porque vai ser executada
	 * no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarVistaLista(Connection con) {
		String diretiva= """
				CREATE VIEW lista AS
				    SELECT 
				        a.numero,
				        nome,
				        IF(codigo IS NULL, '-', codigo) codigo,
				        IF(nota IS NULL,
				            IF(ano IS NULL, '-', '--?--'),
				            nota) AS nota,
				        IF(ano IS NULL, '-', ano) ano,
				        YEAR(CURDATE()) - YEAR(nascido) - ((MONTH(nascido) > MONTH(CURDATE()))
				            OR (MONTH(nascido) = MONTH(CURDATE()))
				            AND (DAY(nascido) > DAY(CURDATE()))) AS idade,
				        nascido,
				        (SELECT 
				                COUNT(*)
				            FROM
				                disciplina
				            WHERE
				                codigo NOT IN (SELECT 
				                        codigo
				                    FROM
				                        inscricao n
				                    WHERE
				                        nota IS NOT NULL AND n.numero = a.numero)) AS faltam
				    FROM
				        aluno a
				            LEFT JOIN
				        inscricao i ON a.numero = i.numero
				    ORDER BY faltam , numero , codigo , nota DESC , ano DESC""";

			if(new Configura().isSQLServer())
				diretiva = diretiva.replaceAll("if(", "iif(");
		return -1!=Manipula.xDirectiva(con, diretiva) 
				&& Comment.view("lista","""
				Lista principal de ALUNOS, incluindo idade e histórico de inscrições""");
	}
	
	/**
	 * Cria a tabela 'aluno'. Não faz commit nem rollback, porque vai ser executada
	 * no contexto de uma transação
	 * 
	 * @return true se correr bem
	 */
	public static boolean criarTabAluno(Connection con) {
		return -1!=Manipula.xDirectiva(con, "create table aluno (" 
				    + "numero               decimal(5) not null,"
				    + "nome                 varchar(150) not null," 
				    + "genero               char(1) not null,"
				    + "nascido              date not null," 
				    + "constraint pk_aluno primary key (numero),"
				    + "constraint ck_genero check (genero IN ('f','m','x','F','M','X'))" 
				    + ")".replace("\n", ""));
	}	
	/**
	 * Cria a tabela 'disciplina'. Não faz commit nem rollback, porque vai ser
	 * executada no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarTabDisciplina(Connection con) {
		return -1!=Manipula.xDirectiva(con, "create table disciplina (" 
				+ "codigo               char(4) not null,"
				+ "designacao           char(60) not null," 
				+ "constraint pk_disciplina primary key (codigo),"
				+ "constraint ak_designacao UNIQUE (designacao)" 
				+ ")".replace("\n", ""));
	}

	/**
	 * Cria a tabela 'foto'. Não faz commit nem rollback, porque vai ser executada
	 * no contexto de uma transacção.
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarTabFoto(Connection con) {
		String blobType="mediumblob";
		if(new Configura().isSQLServer())
			blobType="varbinary(max)";
		return -1!=Manipula.xDirectiva(con, "CREATE TABLE foto (" 
				+ "  numero decimal(5) NOT NULL,"
				+ "  conteudo "+blobType	+ " NOT NULL,"
				+ "  CONSTRAINT pk_foto PRIMARY KEY (numero),"
				+ "  CONSTRAINT fk_aluno FOREIGN KEY (numero)"
				+ " REFERENCES aluno (numero) ON DELETE CASCADE ON UPDATE CASCADE)");
	}
	
	/**
	 * Cria a tabela 'inscricao'. Não faz commit nem rollback, porque vai ser
	 * executada no contexto de uma transação
	 * 
	 * @return true se correu bem
	 */
	public static boolean criarTabInscricao(Connection con) {
		String hoje="CURRENT_DATE";
		if (new Configura().isSQLServer())
			hoje="GETDATE()";
		return -1!=Manipula.xDirectiva(con, "create table inscricao (" 
				+ "numero               decimal(5) not null,"
				+ "codigo               char(4) not null," 
				+ "ano                  int not null,"
				+ "nota                 decimal(4,2) null,"
				+ "inscrito			    date not null default ("+hoje+"),"
				+ "constraint pk_inscricao primary key (numero, codigo, ano),"
				+ "constraint fk1_inscricao FOREIGN KEY (codigo) REFERENCES disciplina (codigo) ON UPDATE CASCADE ON DELETE CASCADE,"
				+ "constraint fk2_inscricao FOREIGN KEY (numero) REFERENCES aluno (numero) ON UPDATE CASCADE,"
				+ "constraint ck_nota CHECK (nota IS NULL OR nota >= 0 AND nota<=20),"
				+ "constraint ck_inscrito CHECK (ano >= YEAR(inscrito))"
				+ ")".replace("\n", ""));
	}

	/**
	 * 🛠️ Cria todas as tabelas e respetivos comentários na base de dados 
	 * dentro de uma **Transação Atómica** única. 🛡️
	 * * O método utiliza {@code try-with-resources} para garantir que a Connection é 
	 * automaticamente fechada (devolvida ao Pool) no final.
	 * * O fluxo de controlo é sequencial e utiliza uma variável de estado (ok) 
	 * para decidir se deve ser feito o {@code commit} ou o {@code rollback} no final do bloco {@code try}.
	 *
	 * @return {@code true} se e só se a criação de todas as tabelas e comentários 
	 * for concluída e a transação for submetida (COMMIT); {@code false} caso contrário, 
	 * após o {@code rollback} ou se ocorrer uma exceção.
	 */
	public static boolean criarTabelas() {
	    boolean ok = true; // Assume-se sucesso inicialmente
		System.out.println("⚙️ A transação de criação de Tabelas está a começar...");
	    // TWR: Garante que a conexão é devolvida ao Pool no final.
	    try (Connection con = new Configura().getConnection(false)) {// 🔓 INÍCIO DA TRANSAÇÃO
	        System.out.println("-------------------------------------------------------------------");
	        System.out.println("▶️ INÍCIO: Criação Transacional de Tabelas... 🛡️");

	        // FLUXO SEQUENCIAL
	        // Se a primeira chamada falhar, 'ok' é definido para false e todas as 
	        // chamadas subsequentes dentro dos 'if's são ignoradas.
	        
	        System.out.println("✅ Criação Tabela 'aluno'...");
	        ok = criarTabAluno(con);
	        
	        if (ok) {
	        		System.out.println("Tabela 'aluno' criada com sucesso! ✅");
	            System.out.println("Criação Tabela 'disciplina'...");
	            ok = criarTabDisciplina(con);
	        }
	        
	        if (ok) {
	        		System.out.println("Tabela 'disciplina' criada com sucesso! ✅");
	            System.out.println("Criação Tabela 'foto'...");
	            ok = criarTabFoto(con);
	        }
	        
	        if (ok) {
	        		System.out.println("Tabela 'foto' criada com sucesso! ✅");
	            System.out.println("Criação Tabela 'inscricao'...");
	            ok = criarTabInscricao(con);
	        }
	        
	        if (ok) {
	        		System.out.println("Tabela 'inscricao' criada com sucesso! ✅");
	        		System.out.println("Criação Tabela 'inscricao'...");
	        		ok = criarComentarios();
	        }
	        
	        // DECISÃO FINAL: COMMIT ou ROLLBACK
	        if (ok) {
	            con.commit();
	            System.out.println("👍 Sucesso! Todas as tabelas e comentários foram criados e submetidos (COMMIT). 🎉");
	        } else {
	            con.rollback();
	            System.out.println("❌ Falha! As alterações foram revertidas (ROLLBACK). 🔙");
	        }
	        
	        // 4. Cleanup no TWR: O con.close() é chamado automaticamente aqui.
	        
	    } catch (Exception e) {
	        // Captura quaisquer exceções de tempo de execução (SQL ou outras)
	        System.out.println("❌ Erro inesperado durante a criação das tabelas: " + e.getMessage());
	        
	        // Log detalhado da exceção (opcionalmente)
	        System.err.println("-----DETALHES DO ERRO----- 🛑");
	        e.printStackTrace(); 
	        
	        ok = false; // Garante que o método retorna false
	    }
	    
	    // A Connection é fechada automaticamente pelo TWR.
	    return ok;
	}
	
	/**
	 * 👁️ Cria as vistas 'inscricoes', 'avaliações' e 'lista' no contexto de uma 
	 * **Transação Atómica** única. 🛡️
	 * * O método utiliza {@code try-with-resources} para garantir o fecho automático da Connection.
	 * * Em caso de erro na criação de qualquer vista, a transação é desfeita 
	 * (chamado {@code con.rollback()}), repondo o estado anterior da base de dados.
	 *
	 * @return {@code true} se todas as vistas forem criadas e a transação for submetida (COMMIT), 
	 * {@code false} caso contrário (após ROLLBACK ou exceção SQL).
	 */
	public static boolean criarVistas() {
	    boolean ok = false;
	    System.out.println("⚙️ A transação de criação de Vistas está a começar...");
	    // 1. TWR: Garante que con.close() é chamado automaticamente.
	    try (Connection con = new Configura().getConnection(false)) { // 🔓 INÍCIO DA TRANSAÇÃO
	        System.out.println("-------------------------------------------------------------------");
	        System.out.println("▶️ INÍCIO: Criação Transacional de Vistas... 🚀");

	        // 2. FLUXO SEQUENCIAL E CONTROLO       
	        System.out.println("Criação da Vista 'inscricoes'...");
	        ok = criarVistaInscricoes(con);

	        if (ok) {
	            System.out.println("Vista 'inscricoes' criada com sucesso! ✅");
	            System.out.println("Criação da Vista 'avaliações'...");
	            ok = criarVistaAvaliacoes(con);
	        }
	        
	        if (ok) {
	            System.out.println("Vista 'avaliações' criada com sucesso! ✅");
	            System.out.println("Criação da Vista 'lista'...");
	            ok = criarVistaLista(con);
	        }
	        if (ok) {
	            System.out.println("Vista 'lista' criada com sucesso! ✅");
	            System.out.println("Criação da Vista 'alunos'...");
	            ok = criarVistaAlunos(con);
	        }
	        if(ok) 
	        		System.out.println("Vista 'alunos' criada com sucesso! ✅");
	        	
	        // 3. DECISÃO FINAL: COMMIT ou ROLLBACK
	        if (ok) {
	            con.commit(); // 🔑 COMMIT Final
	            System.out.println("👍 Sucesso! Todas as vistas foram criadas e submetidas (COMMIT). 🎉");
	        } else {
	            con.rollback(); // 🔙 ROLLBACK
	            System.out.println("❌ Falha na criação de uma vista. Transação revertida (ROLLBACK). 🔙");
	        }        
	    } catch (SQLException e) { 
	        // 4. TRATAMENTO DE EXCEÇÕES JDBC:
	        System.out.println("❌ Ocorreu uma SQLException durante a criação das vistas. ");        
	        System.err.println("-----DETALHES DO ERRO ORIGINAL----- 🛑");
	        System.err.println("SQLState:  " + e.getSQLState());
	        System.err.println("Menssagem:  " + e.getMessage());
	        System.err.println("Código do Fornecedor:  " + e.getErrorCode());
	        
	        ok = false; // Garante que o método retorna false em caso de exceção SQL
	    }
	    
	    return ok;
	}

	/**
	 * 💣 Apaga os dados das tabelas 'inscricao', 'foto', 'aluno' e 'disciplina' 
	 * (em ordem inversa de dependência) dentro de uma transação JDBC.
	 * Garante que, em caso de falha em qualquer DELETE, a transação é desfeita (ROLLBACK),
	 * mantendo a integridade dos dados.
	 * @return true se TODAS as tabelas forem limpas com sucesso e o COMMIT for efetuado.
	 */
	public static boolean apagarTabelas() {
	    Connection con = null;
	    boolean sucesso = false;

	    // 1. Não usa 'try-with-resources' para exemplificar o ROLLBACK explicito.
	    try {
		        // Obter a ligação (assumindo que Configura().getConnection() é robusto)
		        con = new Configura().getConnection();
		        
		        System.out.println("⚙️ A transação de limpeza de tabelas está a começar...");
		        
		        // 2. Desativa o 'auto-commit' para iniciar a transação.
		        con.setAutoCommit(false);
		        System.out.println("🔒 AutoCommit desativado. Tudo em transação.");
	
		        // Definimos a ordem de eliminação: dependentes -> independentes.
		        final String[] TABELAS = {"INSCRICAO", "FOTO", "ALUNO", "DISCIPLINA"};
		        
		        // 3. Execução sequencial dos comandos DELETE.
		        for (String tabela : TABELAS) {
		            System.out.println("🗑️ A apagar dados da Tabela '" + tabela + "'...");
		            
		            // Assume-se que Manipula.xDirectiva executa o update/delete e devolve -1 em caso de erro.
		            if (Manipula.xDirectiva(con, "DELETE FROM " + tabela) == -1) 
		                // Se um DELETE falhar, lançamos uma exceção para cair no bloco catch.
		                throw new SQLException("Falha na eliminação da tabela: " + tabela);
		            System.out.println("✅ Tabela '" + tabela + "' limpa com sucesso!");
		        }
	
		        // 4. Se todos os DELETEs correram bem, a transação é confirmada (COMMIT).
		        con.commit();
		        System.out.println("👍 COMMIT realizado! Tabelas apagados (DELETE) com sucesso!");
		        sucesso = true;
	        } catch (SQLException e) {
	        // 5. TRATAMENTO DE EXCEÇÕES: Ocorreu um erro (SQL ou na lógica acima).
	        System.out.println("\n❌ A transação falhou! A realizar ROLLBACK...");
	        
	        try {
	            if (con != null) {
	                // Garante que o ROLLBACK é executado.
	                con.rollback(); 
	                System.out.println("↩️ ROLLBACK concluído. Nenhum dado foi alterado.");
	            }
	        } catch (SQLException rbkEx) {
	            // Em caso de erro ao tentar o ROLLBACK (muito raro).
	            System.err.println("🛑 ERRO CRÍTICO ao tentar fazer ROLLBACK: " + rbkEx.getMessage());
	        }

	        // Informações detalhadas do erro original
	        System.err.println("\n----- DETALHES DO ERRO -----");
	        System.err.println("SQLState: " + e.getSQLState());
	        System.err.println("Mensagem: " + e.getMessage());
	        System.err.println("Código do Fornecedor: " + e.getErrorCode());
	        
	    } finally {
	         // 6. Fechar a ligação para libertar recursos (redundante se usar try-with-resources, mas mais explícito)
	        if (con != null) {
	            try {
	                con.close();
	            } catch (SQLException closeEx) {
	                System.err.println("⚠️ Aviso: Erro ao fechar a ligação: " + closeEx.getMessage());
	            }
	        }
	    }
	    
	    return sucesso;
	}
		
	/**
	 * 💥 Remove (DROP), no contexto de uma transação JDBC, as estruturas das tabelas
	 * 'inscricao', 'foto', 'aluno' e 'disciplina'. 
	 * * ⚠️ Caso qualquer DROP falhe, o Try-With-Resources (TWR) fechará a ligação,
	 * acionando o ROLLBACK implícito das DDLs (Data Definition Language) pendentes, 
	 * preservando a situação anterior.
	 * * @return true se TODAS as tabelas forem removidas com sucesso e a transação confirmada.
	 */
	public static boolean removerTabelas() {
	    
	    // As instruções DDL (DROP TABLE) são frequentemente consideradas operações 
	    // de COMMIT implícito em muitas bases de dados (ex: MySQL/MariaDB com InnoDB).
	    // Contudo, em SGBDs como PostgreSQL ou SQL Server, o DROP pode ser revertido 
	    // se estiver dentro de uma transação e ocorrer um ROLLBACK.
	    
	    // A ordem é importante, embora menos crítica para DROP do que para DELETE.
	    final String[] TABELAS = {"INSCRICAO", "FOTO", "ALUNO", "DISCIPLINA"};
	    
	    System.out.println("⚙️ A transação de REMOÇÃO de estruturas está a começar...");
	    
	    // 1. TWR: Garante que a ligação é fechada automaticamente no final.
	    // Assumimos que 'getConnection(false)' retorna a ligação com AutoCommit=false.
	    try (Connection con = new Configura().getConnection(false)) { 
	        
	        System.out.println("🔒 Ligação em modo transacional (AutoCommit=false).");
	        
	        // 2. Execução sequencial dos comandos DROP TABLE.
	        for (String tabela : TABELAS) {
	            System.out.println("💣 A remover a Tabela '" + tabela + "'...");
	            
	            // Assume-se que Manipula.xDirectiva executa o DDL e devolve -1 em caso de erro.
	            if (Manipula.xDirectiva(con, "DROP TABLE " + tabela) == -1) {
	                // Se um DROP falhar, forçamos uma exceção. O TWR vai para o catch.
	                throw new SQLException("Falha na remoção da tabela: " + tabela);
	            }
	            System.out.println("✅ Tabela '" + tabela + "' removida com sucesso!");
	        }

	        // 3. Se todos os DROP correram bem, confirmamos (COMMIT).
	        // Nota: O método original usava setAutoCommit(true) que, no fim da transação, 
	        // atua como COMMIT + volta ao modo AutoCommit. O con.commit() é mais explícito.
	        con.commit();
	        
	        System.out.println("👍 COMMIT realizado! Tabelas removidas (DROP) com sucesso.");
	        return true;
	        
	    } catch (SQLException e) {
	        // 4. TRATAMENTO DE EXCEÇÕES
	        // Não fazemos ROLLBACK explícito, confiando no fecho da ligação pelo TWR.
	        // O TWR fechará 'con', o que levará o SGBD a fazer ROLLBACK implícito.
	    		System.out.println("\n❌ A remoção das tabelas não teve sucesso.");
	        System.out.println("↩️ A transação falhou! O SGBD fará ROLLBACK implícito.");
	        
	        // Informações detalhadas do erro
	        System.err.println("----- DETALHES DO ERRO -----");
	        System.err.println("SQLState: " + e.getSQLState());
	        System.err.println("Mensagem: " + e.getMessage());
	        System.err.println("Código do Fornecedor: " + e.getErrorCode());
	    } 
	    // ⚠️ O TWR fecha 'con' aqui, garantindo o fecho (e ROLLBACK implícito em muitos SGBDs).
	    
	    return false;
	}
	/**
	 * Remove, no contexto de uma transacção as vistas 'inscricoes', 'avaliacoes' e 'resumo'
	 * Caso ocorra algum erro a transacção é desfeita, repondo a
	 * situação anterior à execução.
	 * 
	 * @return 		true se correu bem
	 */
	public static boolean removerVistas() {
		System.out.println("⚙️ A transação de remoção de vistas está a começar...");
		try (Connection con=new Configura().getConnection(false)){
			System.out.println("Removoção das Vistas ...");
			System.out.println("Remover Vista 'inscricoes'...");
			if (-1!=Manipula.xDirectiva(con, "DROP VIEW inscricoes")) {
				System.out.println("✅ Vista 'inscricoes' removida com sucesso!");
				System.out.println("Remover Vista 'avaliacoes'...");
				if (-1!=Manipula.xDirectiva(con, "DROP VIEW avaliacoes")) {
					System.out.println("✅ Vista 'avaliacoes' removida com sucesso!");
					System.out.println("Remover Vista 'lista'...");
					if (-1!=Manipula.xDirectiva(con, "DROP VIEW lista")) {
						System.out.println("✅ Vista 'lista' removida com sucesso!");
						System.out.println("Remover Vista 'alunos'...");
						if (-1!=Manipula.xDirectiva(con, "DROP VIEW alunos")) {
							System.out.println("✅ Vista 'alunos' removida com sucesso!");
							con.setAutoCommit(true);
							System.out.println("👍 COMMIT realizado! Vistas removidas (DROP) com sucesso.");
							return true;
						}
					}
				}
			}
		}
		catch (SQLException e) {
			// 4. TRATAMENTO DE EXCEÇÕES
	        // Não fazemos ROLLBACK explícito, confiando no fecho da ligação pelo TWR.
	        // O TWR fechará 'con', o que levará o SGBD a fazer ROLLBACK implícito.
	        System.out.println("\n❌ A remoção das vistas não teve sucesso.");
	        System.out.println("↩️ A transação falhou! O SGBD fará ROLLBACK implícito.");
	        
	        // Informações detalhadas do erro
	        System.err.println("----- DETALHES DO ERRO -----");
	        System.err.println("SQLState: " + e.getSQLState());
	        System.err.println("Mensagem: " + e.getMessage());
	        System.err.println("Código do Fornecedor: " + e.getErrorCode());

		}
		return false;
	}
	   
	/**
	 * Executa a aplicação de Gestor Académico, apresentando o menu principal
	 * e submenus para as operações de administração, gestão de dados e relatórios.
	 * @param args nenhum
	 */
	public static void main(String[] args) {
		System.out.println(Configura.infoApp(null));
	    char opcao; // Variável para armazenar a opção selecionada pelo utilizador.
	    do {
	        // 🏫 Menu Principal
	        System.out.println("\n🏫 *** Menu do Gestor Académico ***");
	        System.out.println("🛑 0. Terminar");
	        System.out.println("⚙️ 1. Administração");
	        System.out.println("🎓 2. Alunos");
	        System.out.println("📚 3. Disciplinas");
	        System.out.println("💯 4. Avaliações");
	        System.out.println("📊 5. Relatórios");
	        System.out.println("📝 6. Mudar BD (Atual: " + Configura.getDTB_() + ")");
	        System.out.println("Opção: ");
	        opcao = IOx.inChar(); // Lê a opção
	        
	        switch (opcao) {
	            case '1':
	                // --- SUBMENU ADMINISTRAÇÃO ---
	                do {
	                    System.out.println("\n⚙️ > Administração");
	                    System.out.println("🧱  a. Criar base de dados");
	                    System.out.println("➕  b. Criar tabelas/vistas");
	                    System.out.println("📑  c. Listar tabelas/vistas");
	                    System.out.println("🗄️  d. Carregar tabelas");
	                    System.out.println("🗑️  e. Apagar tabelas");
	                    System.out.println("➖  f. Remover tabelas/vistas");
	                    System.out.println("🧹  g. Eliminar base de dados");
	                    System.out.println("🔄  h. Exportar/Importar tabelas");
	                    System.out.println("🔙  z. Voltar ao Menu Principal");
	                    
	                    System.out.println("Opção: ");
	                    opcao = IOx.inChar();
	                    
	                    // Instância temporária para operações DDL/DML, usando a configuração por omissão
	                    Configura cfg = new Configura(); 
	                    
	                    switch (Character.toLowerCase(opcao)) {
	                        case 'a':
	                            // ⚠️ Criação de Base de Dados
	                            System.out.println("⚠️ Tipicamente, a criação de bases de dados não acontece em produção!");
	                            System.out.println("🛢️ A criar a base de dados...");
	                            if (cfg.criarBaseDeDados()) {
	                                System.out.println("✅ Operação de Criação de DB concluída.");
	                            }
	                            break;
	                        case 'b':
	                            // ⚠️ Criação de Tabelas/Vistas (DDL)
	                            System.out.println("⚠️ Tipicamente, a criação de tabelas/vistas não acontece em produção!");
	                            if(criarTabelas()) {
	                                System.out.println("➕ Tabelas criadas. A criar vistas...");
	                                criarVistas();
	                                System.out.println("✅ Tabelas e Vistas criadas.");
	                            } else 
	                                System.out.println("❌ Falha na criação de tabelas.");
	                            break;
	                        case 'c':
	                            // Consultar Catalogo
	                            System.out.println("📑 A consultar a lista de tabelas/vistas...");
	                            Configura.listarObjectos();
	                            System.out.println("✅ Lista de tabelas/vistas completa.");
	                            break;
	                        case 'd':
	                            // Carregamento de Dados (DML)
	                            System.out.println("🗄️ A carregar dados iniciais nas tabelas...");
	                            if(carregarTabelas())
	                            		System.out.println("✅ Dados carregados.");
	                            else
	                            		System.out.println("❌ Falha no carregamento das tabelas.");
	                            break;
	                        case 'e':
	                            // Apagar (Limpar) Dados (DML - DELETE)
	                            System.out.println("🗑️ A apagar todos os registos das tabelas...");
	                            if(apagarTabelas())
	                            		System.out.println("Dados apagados (Tabelas vazias). 🏁 ");
	                            else
                            			System.out.println("❌ Falha no apagamento das tabelas.");
	                            break;
	                        case 'f':
								// ⚠️ Remoção de Tabelas/Vistas (DDL - DROP)
								System.out.println(
										"⚠️ Tipicamente, a remoção de tabelas/vistas não acontece em produção!");
								if (removerTabelas()) {
									System.out.println("➖ Tabelas removidas. A remover vistas...");
									removerVistas();
									System.out.println("✅ Tabelas e Vistas removidas.");
								} else {
									System.out.println("❌ Falha na remoção de tabelas.");
								}
	                            break;
	                        case 'g':
								// ⚠️ Eliminação de Base de Dados
								System.out.println(
										"⚠️ Tipicamente, a eliminação de bases de dados não acontece em produção!");
								System.out.println("🧹 A eliminar a base de dados...");
								// System.out.println("*** 🚧 Falta implementar ❓ ***");
								System.out.println("*** ⏳ Faltava implementar 💡 ***");
								if (cfg.eliminarBaseDeDados()) {
									System.out.println("✅ Operação de Eliminação de DB concluída.");
								}
	                            else
                            			System.out.println("❌ Falha na Eliminação da BD.");
	                            break;
	                        case 'h':
								// Exportação/Importação de Dados
								System.out.println(
										"⚠️ Tipicamente, a exportação/importação de dados não acontece em produção!");
								System.out.println("🔄 A iniciar exportação/importação de tabelas...");
								// System.out.println("*** 🚧 Falta implementar ❓ ***");
								System.out.println("*** ⏳ Faltava implementar 💡 ***");
								util.DataTransfer.main(null);
								System.out.println("✅ Exportação/importação concluída.");
								break;
	                        case 'z':
	                            System.out.println("↩️ Voltando ao menu principal...");
	                            break;
	                        default:
	                            System.out.println("❌ Opção inválida. Tente outra vez.");
	                    }
		                if (opcao != 'z') {
		                    System.out.println("\n[Pressione ENTER para continuar...]");
		                    IOx.in();
		               }
	                } while (opcao != 'z');
	                break;
	            case '2':
	            		Alunos.main(null);
	                break;
	            case '3':
                    // System.out.println("*** 🚧 Falta implementar ❓ ***");
	            		System.out.println("*** ⏳ Faltava implementar 💡 ***");
	            		Disciplinas.main(null);
	                break;
	            case '4':
	                Inscricoes.main(null);
	                break;
	            case '5':
	            		// System.out.println("*** 🚧 Falta implementar ❓ ***");
	            		System.out.println("*** ⏳ Faltava implementar 💡 ***");
	            		Relatorios.main(null);
	                break;
	            case '6':
	            		new Configura().listarBasesDeDados();
	            		String databaseName = util.IOx.input("Novo nome da Base de Dados (e.g., aula):").toUpperCase();
                    Configura.setDTB_(databaseName);
                    new Configura().setDTB(databaseName);
                    System.out.println("A Base de Dados mudou para: " + databaseName + ".");
                    break;
	            case '0':
	                System.out.println("🛑 A preparar o término da aplicação...");
	                break;
	            default:
	                System.out.println("❌ Opção inválida. Tente outra vez.");
	        }
	    } while (opcao != '0');
	    
	    // Mensagem de término final
	    System.out.println("\n🏁 Terminou a execução do Gestor Académico.");
	}
}