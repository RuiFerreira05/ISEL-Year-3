package prototype;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

// --- 🖼️ Imports para visualização (Requer AWT/Swing - Substituição do Pillow) ---
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;

/**
 * 🇵🇹✨ Aplicação (DAOFoto) contém os modelos (Aluno, Foto) e os 
 * DAOs (AlunoDAO, FotoDAO) para manipulação de registos e BLOBs (fotos) numa base de dados MySQL.
 * * * ⚠️ Dependências: 
 * 1. Driver JDBC do MySQL no classpath.
 * 2. Um ambiente gráfico (GUI) para a funcionalidade de visualização (Swing/AWT).
 * * Utiliza o padrão try-with-resources para gestão segura das ligações JDBC e NIO.2 para I/O eficiente.
 */
public class DAOFoto {
    
    // ----------------------------------------------------------------------
    // --- 💡 IMPLEMENTAÇÃO DE LOGGING SIMPLES ---
    // ----------------------------------------------------------------------

    private static final class Log {
        /**
         * 💡 Classe interna temporária que simula um logger (INFO/WARN/ERROR) para stdout/stderr.
         */
        public static void info(String message) {
            System.out.println(message);
        }
        public static void warn(String message) {
            System.err.println("⚠️ AVISO: " + message);
        }
        public static void error(String message) {
            System.err.println("❌ ERRO: " + message);
        }
    }

    // --- 0. 🔗 CONFIGURAÇÃO DA BASE DE DADOS (DB Connector) ---

    private static class db {
        /** * ⚙️ Classe estática para gerir a configuração e o estabelecimento da ligação MySQL.
         * ⚠️ AJUSTAR AS CREDENCIAIS CONFORME O AMBIENTE!
         */
        private static final String HOSTNAME = "localhost";
        private static final String DBNAME = "GAS";
        private static final String USERNAME = "root";
        private static final String BDPASSWORD = "root";
        
        // 🌐 URL de conexão: driver MySQL, Timezone e desativação de SSL
        private static final String URL = "jdbc:mysql://" + HOSTNAME + "/" + DBNAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        
        static {
            // Tenta carregar o driver JDBC do MySQL
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Erro: Driver JDBC MySQL não encontrado. Certifique-se que o JAR está no classpath.");
                e.printStackTrace();
            }
        }

        public static Connection getConnection() throws SQLException {
            /**
             * 🛡️ Retorna um objeto de ligação MySQL seguro.
             */
            try {
                return DriverManager.getConnection(URL, USERNAME, BDPASSWORD);
            } catch (SQLException err) {
                Log.error("Falha ao estabelecer Ligação à Base de Dados: " + err.getMessage());
                throw err;
            }
        }
    }
    
    // --- 1. 🧩 MODELOS DE DADOS (Entity Classes) ---

    static class Aluno {
        /** 👤 Modelo que representa um registo da tabela 'aluno'. */
        private final int numero;
        private final String nome;
        private final String genero;
        private final LocalDate nascido; // Java type para o tipo MySQL DATE (data sem tempo)

        public Aluno(int numero, String nome, String genero, LocalDate nascido) {
            this.numero = numero;
            this.nome = nome;
            this.genero = genero;
            this.nascido = nascido;
        }
        
        // Getters
        public int getNumero() { return numero; }
        public String getNome() { return nome; }
        public String getGenero() { return genero; }
        public LocalDate getNascido() { return nascido; }

        @Override
        public String toString() {
            return String.format("Aluno(numero=%d, nome='%s', genero='%s', nascido='%s')",
                                 numero, nome, genero, nascido.toString());
        }
    }

    static class Foto {
        /** 📸 Modelo que representa um registo da tabela 'foto' e o seu conteúdo BLOB. */
        private final int numero;
        private byte[] conteudo; // O BLOB (conteúdo binário da foto)

        public Foto(int numero, byte[] conteudo) {
            this.numero = numero;
            this.conteudo = conteudo;
        }
        
        // Getters e Setters
        public int getNumero() { return numero; }
        public byte[] getConteudo() { return conteudo; }
        public void setConteudo(byte[] conteudo) { this.conteudo = conteudo; }

        @Override
        public String toString() {
            return String.format("Foto(numero=%d, tamanho_conteudo=%d bytes)",
                                 numero, conteudo != null ? conteudo.length : 0);
        }

        // ----------------------------------------------------------------------
        // --- ⚙️ MÉTODOS DE I/O DE FICHEIROS (NIO.2 - Leitura e Escrita de BLOBs) ---
        // ----------------------------------------------------------------------
        
        public static Optional<byte[]> lerConteudoDeFicheiro(String caminhoFicheiro) {
            /**
             * 📥 Lê o conteúdo binário (byte[]) de um ficheiro usando o NIO.2 `Files.readAllBytes()`.
             * É a forma mais concisa e eficiente de ler um ficheiro inteiro como um único BLOB,
             * tratando o stream internamente.
             */
            Path path = Paths.get(caminhoFicheiro);
            if (!Files.exists(path)) {
                Log.error("❌ Erro de I/O: O ficheiro '" + caminhoFicheiro + "' não foi encontrado.");
                return Optional.empty();
            }
            
            try {
                // 🚀 Abordagem Stream-based NIO.2: lê tudo de uma vez.
                byte[] conteudoBinario = Files.readAllBytes(path);
                
                if (conteudoBinario.length == 0) {
                    Log.warn("⚠️ Aviso: O ficheiro '" + caminhoFicheiro + "' está vazio.");
                    return Optional.empty();
                }

                return Optional.of(conteudoBinario);
                
            } catch (IOException e) {
                Log.error("❌ Erro ao ler o ficheiro '" + caminhoFicheiro + "': " + e.getMessage());
                return Optional.empty();
            }
        }
        
        public static boolean gravarConteudoParaFicheiro(byte[] conteudo, String caminhoFicheiro) {
            /**
             * 📤 Grava o conteúdo binário (byte[]) no caminho especificado usando o NIO.2 `Files.write()`.
             * Útil para descarregar um BLOB lido da BD para o disco.
             */
            if (conteudo == null || conteudo.length == 0) {
                Log.warn("⚠️ Aviso: Conteúdo nulo ou vazio. Não é possível gravar o ficheiro.");
                return false;
            }
            
            Path path = Paths.get(caminhoFicheiro);
            try {
                // 🚀 Abordagem Stream-based NIO.2: escreve todos os bytes de uma vez.
                Files.write(path, conteudo);
                Log.info("✔️ Conteúdo gravado com sucesso no ficheiro: " + caminhoFicheiro);
                return true;
            } catch (IOException e) {
                Log.error("❌ Erro ao gravar o ficheiro '" + caminhoFicheiro + "': " + e.getMessage());
                return false;
            }
        }
        
        public static Optional<Foto> fromFilePath(int numeroAluno, String caminhoFicheiro) {
            /**
             * 🏭 Constrói um objeto Foto lendo o conteúdo binário de um ficheiro (usa lerConteudoDeFicheiro).
             */
            Optional<byte[]> conteudoOpt = lerConteudoDeFicheiro(caminhoFicheiro);
            
            if (conteudoOpt.isPresent()) {
                return Optional.of(new Foto(numeroAluno, conteudoOpt.get()));
            } else {
                return Optional.empty();
            }
        }

        public boolean visualizar() {
            /**
             * 👁️ Carrega o BLOB (byte[]) e tenta exibi-lo numa janela Swing/AWT.
             */
            if (this.conteudo == null || this.conteudo.length == 0) {
                Log.warn(String.format("⚠️ Aviso: A Foto %d não contém conteúdo binário (BLOB).", this.numero));
                return false;
            }
            
            try {
                ImageIcon icon = new ImageIcon(this.conteudo);
                if (icon.getIconWidth() == -1) {
                    Log.error("❌ ERRO: O conteúdo BLOB não é um formato de imagem válido (ex: PNG/JPG).");
                    return false;
                }

                JFrame frame = new JFrame();
                frame.setTitle("Visualização Foto Aluno " + (this.numero == 0 ? "(MEMÓRIA)" : this.numero));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
                frame.getContentPane().add(new JLabel(icon));
                frame.pack();
                frame.setLocationRelativeTo(null); 
                frame.setVisible(true);

                Log.info("✔️ Imagem carregada e exibida com sucesso (feche a janela).");
                return true;
                
            } catch (Exception e) {
                Log.error("❌ Erro inesperado ao visualizar: " + e.getMessage());
                return false;
            }
        }
    }

    // --- 2. 🗄️ CLASSES DAO (Data Access Object) ---

    static class AlunoDAO {
        /** 🧑‍🎓 DAO para operações CRUD na tabela 'aluno'. */

        public boolean inserir(Aluno aluno) {
            /** ➕ Insere um novo registo de aluno. */
            String sql = "INSERT INTO aluno (numero, nome, genero, nascido) VALUES (?, ?, ?, ?)";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, aluno.getNumero());
                stmt.setString(2, aluno.getNome());
                stmt.setString(3, aluno.getGenero());
                // Conversão de java.time.LocalDate para java.sql.Date
                stmt.setDate(4, Date.valueOf(aluno.getNascido())); 
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException err) {
                // 1062: ER_DUP_ENTRY (PK violada)
                if (err.getErrorCode() != 1062) { 
                    Log.error("❌ Erro ao inserir aluno: " + err.getMessage());
                }
                return false;
            }
        }

        public Optional<Aluno> obterPorNumero(int numero) {
            /** 🔍 Obtém um aluno pelo seu número único (PK). */
            String sql = "SELECT numero, nome, genero, nascido FROM aluno WHERE numero = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, numero);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        LocalDate nascido = rs.getDate("nascido").toLocalDate();
                        return Optional.of(new Aluno(
                            rs.getInt("numero"), 
                            rs.getString("nome"), 
                            rs.getString("genero"), 
                            nascido
                        ));
                    }
                    return Optional.empty();
                }
            } catch (SQLException err) {
                Log.error("❌ Erro ao obter aluno: " + err.getMessage());
                return Optional.empty();
            }
        }

        public boolean eliminar(int numero) {
            /** 🗑️ Elimina um aluno pelo seu número. */
            String sql = "DELETE FROM aluno WHERE numero = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, numero);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            } catch (SQLException err) {
                // 1451: ER_ROW_IS_REFERENCED_2 (Restrição de FK)
                if (err.getErrorCode() == 1451) { 
                    Log.warn(String.format("⚠️ Restrição de FK: Não é possível eliminar Aluno %d, existem fotos associadas.", numero));
                }
                return false;
            }
        }
    }

    static class FotoDAO {
        /** 🖼️ DAO para operações CRUD na tabela 'foto', especializada em BLOBs. */

        public boolean inserir(Foto foto) {
            /** 📸 Insere um novo registo de fotografia (BLOB). */
            String sql = "INSERT INTO foto (numero, conteudo) VALUES (?, ?)";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, foto.getNumero());
                stmt.setBytes(2, foto.getConteudo()); // setBytes() é o método para BLOB/LONGBLOB
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException err) {
                Log.error("❌ Erro ao inserir foto: " + err.getMessage());
                return false;
            }
        }

        public boolean carregarEInserirDoFicheiro(int numeroAluno, String caminhoFicheiro) {
            /**
             * ➕ Carrega um ficheiro do disco, cria um objeto Foto e insere o seu conteúdo na BD.
             */
            Optional<Foto> fotoNovaOpt = Foto.fromFilePath(numeroAluno, caminhoFicheiro);
            
            if (fotoNovaOpt.isEmpty()) {
                Log.error("❌ Falha ao criar o objeto Foto a partir do ficheiro.");
                return false;
            }
            
            Foto fotoNova = fotoNovaOpt.get();
            
            Log.info(String.format("   Carregando %d bytes para o Aluno %d...", 
                fotoNova.getConteudo().length, numeroAluno));
            return this.inserir(fotoNova);
        }

        public Optional<Foto> obterPorNumero(int numero) {
            /** 🔎 Obtém a fotografia de um aluno pelo seu número, incluindo o BLOB. */
            String sql = "SELECT numero, conteudo FROM foto WHERE numero = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, numero);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Lê o BLOB como um array de bytes (byte[])
                        byte[] conteudo = rs.getBytes("conteudo"); 
                        return Optional.of(new Foto(rs.getInt("numero"), conteudo));
                    }
                    return Optional.empty();
                }
            } catch (SQLException err) {
                Log.error("❌ Erro ao obter foto: " + err.getMessage());
                return Optional.empty();
            }
        }

        public boolean atualizar(Foto foto) {
            /** 🔄 Atualiza o conteúdo (BLOB) de uma fotografia existente. */
            String sql = "UPDATE foto SET conteudo = ? WHERE numero = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setBytes(1, foto.getConteudo());
                stmt.setInt(2, foto.getNumero());
                
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException err) {
                Log.error("❌ Erro ao atualizar foto: " + err.getMessage());
                return false;
            }
        }

        public boolean eliminar(int numero) {
            /** 🗑️ Elimina a fotografia de um aluno. */
            String sql = "DELETE FROM foto WHERE numero = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, numero);
                return stmt.executeUpdate() > 0;
                
            } catch (SQLException err) {
                Log.error("❌ Erro ao eliminar foto: " + err.getMessage());
                return false;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // --- 🧪 SUITE DE TESTES E DEMONSTRAÇÃO ---
    // ----------------------------------------------------------------------

    private static boolean criarImagemTeste(String caminho) {
        /** 🎨 Cria um pequeno ficheiro PNG no disco para testes de BLOB/Visualização (BLUE/YELLOW). */
        try {
            int width = 120;
            int height = 60;
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            
            g2d.setColor(Color.BLUE);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("TEST PHOTO", 10, 35);
            g2d.dispose();

            ImageIO.write(img, "PNG", new File(caminho));
            return true;
        } catch (IOException e) {
            Log.error("❌ Erro ao criar imagem de teste: " + e.getMessage() + ". Os testes de ficheiro serão ignorados.");
            return false;
        }
    }
    
    private static byte[] criarBytesImagemVermelha() {
        /** 🎨 Cria os bytes de uma imagem RED/YELLOW para simular uma atualização (UPDATE). */
        try {
            int width = 120;
            int height = 60;
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("UPDATED PHOTO", 10, 35);
            g2d.dispose();

            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            ImageIO.write(img, "PNG", os);
            return os.toByteArray();
        } catch (IOException e) {
            Log.error("❌ Erro ao criar bytes de imagem de teste para atualização: " + e.getMessage());
            return new byte[0];
        }
    }

    private static void testDaoSuite(AlunoDAO alunoDao, FotoDAO fotoDao) throws SQLException {
        /** 🧪 Executa testes de ponta a ponta para todas as funcionalidades de BLOBs. */
        
        final int TEST_NUM_A = 9001;
        final int TEST_NUM_B = 9002;
        final String FICHEIRO_TESTE_BLOB = "teste_foto_upload.png";
        
        Log.info("\n========================================================");
        Log.info("🧪 INÍCIO DOS TESTES CRUD DE FOTOS (BLOB)");
        Log.info("========================================================");

        // 🧹 0. LIMPEZA INICIAL
        Log.info("\n--- 0. Limpeza Inicial ---");
        fotoDao.eliminar(TEST_NUM_A); 
        fotoDao.eliminar(TEST_NUM_B);
        alunoDao.eliminar(TEST_NUM_A); 
        alunoDao.eliminar(TEST_NUM_B);

        // 🛠️ Inserir registos de aluno necessários (Pré-requisito de FK)
        alunoDao.inserir(new Aluno(TEST_NUM_A, "Teste Foto A", "f", LocalDate.of(2000, 1, 1)));
        alunoDao.inserir(new Aluno(TEST_NUM_B, "Teste Foto B", "m", LocalDate.of(2000, 1, 1)));

        // 🛠️ Criação do ficheiro de teste no disco
        boolean imagemTesteCriada = criarImagemTeste(FICHEIRO_TESTE_BLOB);
        
        // ----------------------------------------------------
        // 🖼️ 1. TESTES DE CARREGAMENTO E UPDATE
        // ----------------------------------------------------
        Log.info(String.format("\n--- 1. Carregar/Atualizar Foto (%d) ---", TEST_NUM_A));
        
        if (imagemTesteCriada) {
            // 1. Carregar e Inserir a Foto do Aluno A 
            boolean sucessoUploadA = fotoDao.carregarEInserirDoFicheiro(
                TEST_NUM_A, FICHEIRO_TESTE_BLOB
            );
            Log.info("   1. Upload Aluno " + TEST_NUM_A + ": " + (sucessoUploadA ? "✔️ SUCESSO" : "❌ FALHA"));

            // 2. Obter a foto da BD
            Optional<Foto> fotoLidaAOpt = fotoDao.obterPorNumero(TEST_NUM_A);
            
            // 3. Testar Visualização (Primeira versão - BLUE)
            Log.info("\n   3. Teste Visualizar() Aluno A (Versão BLUE):");
            if (fotoLidaAOpt.isPresent()) {
                fotoLidaAOpt.get().visualizar();
            } else {
                Log.error("   ❌ Falha ao obter foto da BD para visualização.");
            }

            // 4. Atualizar a foto do Aluno A (Simulação de bytes de imagem RED)
            byte[] bytesImagemVermelha = criarBytesImagemVermelha();
            Foto fotoV2Memoria = new Foto(TEST_NUM_A, bytesImagemVermelha);
            
            Log.info(String.format("\n   4. Atualizar Foto A (Mudança para Versão RED): %s", 
                (fotoDao.atualizar(fotoV2Memoria) ? "✔️ SUCESSO" : "❌ FALHA")));
            
            // 5. Visualizar a foto atualizada (Segunda versão - RED)
            Optional<Foto> fotoLidaAtualizadaOpt = fotoDao.obterPorNumero(TEST_NUM_A);
            Log.info("   5. Teste Visualizar() Aluno A (Atualizada - Versão RED):");
            if (fotoLidaAtualizadaOpt.isPresent()) {
                fotoLidaAtualizadaOpt.get().visualizar();
            }
            
        } else {
            Log.warn("\n   ⚠️ Testes de Foto avançados ignorados devido à falha na criação da imagem de teste.");
        }

        // ----------------------------------------------------
        // 🗑️ 2. LIMPEZA FINAL
        // ----------------------------------------------------
        Log.info("\n--- 2. Limpeza Final ---");
        Log.info(String.format("   Eliminar Foto %d: %s", TEST_NUM_A, fotoDao.eliminar(TEST_NUM_A) ? "✔️" : "❌"));
        Log.info(String.format("   Eliminar Aluno %d: %s", TEST_NUM_A, alunoDao.eliminar(TEST_NUM_A) ? "✔️" : "❌"));
        Log.info(String.format("   Eliminar Aluno %d: %s", TEST_NUM_B, alunoDao.eliminar(TEST_NUM_B) ? "✔️" : "❌"));
        
        // 🧹 Limpar o ficheiro de teste do disco
        File fileToDelete = new File(FICHEIRO_TESTE_BLOB);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
            Log.info(String.format("   Ficheiro de teste '%s' removido do disco.", FICHEIRO_TESTE_BLOB));
        }

        Log.info("\n========================================================");
        Log.info("✅ TESTES CONCLUÍDOS! (Feche janelas de visualização Swing)");
        Log.info("========================================================");
    }

    private static void demoCarregarFoto(AlunoDAO alunoDao, FotoDAO fotoDao) throws SQLException {
        /**
         * 💡 Função de demonstração interativa para carregar e persistir uma foto.
         */
        
        final int TEST_NUM = 9999;
        
        Log.info("\n========================================================");
        Log.info("DEMO INTERATIVA: Carregar/Visualizar/Persistir Foto");
        Log.info("========================================================");
        
        Scanner scanner = new Scanner(System.in);
        
        // 0. Preparação (Garantir que um Aluno existe para testar a FK)
        Log.info(String.format("⚙️ A garantir a existência do registo Aluno %d para testes de FK...", TEST_NUM));
        fotoDao.eliminar(TEST_NUM); 
        alunoDao.eliminar(TEST_NUM);
        alunoDao.inserir(new Aluno(TEST_NUM, "Demo User", "x", LocalDate.of(1990, 1, 1)));
        
        // 1. Obter o caminho do ficheiro
        System.out.print("\n➡️ Introduza o caminho completo do ficheiro de imagem a carregar: ");
        String caminhoFicheiro = scanner.nextLine().trim();

        // 2. Construir o objeto Foto e pré-visualizar 
        Optional<Foto> fotoDemoOpt = Foto.fromFilePath(0, caminhoFicheiro);
        
        if (fotoDemoOpt.isEmpty()) {
            Log.error("❌ Operação cancelada. Não foi possível ler o ficheiro.");
            alunoDao.eliminar(TEST_NUM);
            scanner.close();
            return;
        }
        
        Foto fotoDemo = fotoDemoOpt.get();
        
        Log.info("\n👁️ A visualizar a foto carregada em memória (feche a janela para continuar)...");
        fotoDemo.visualizar();
        
        // 3. Obter o número do registo
        int numero=0;
        while (true) {
            try {
                System.out.print(String.format("\n➡️ Introduza o NÚMERO do registo a associar (ex: %d demo): ", TEST_NUM));
                String numeroStr = scanner.nextLine().trim();
                numero = Integer.parseInt(numeroStr);
                // Validação: Garante que o número de aluno existe na BD (FK)
                alunoDao.obterPorNumero(numero).orElseThrow(() -> new SQLException("O aluno não existe."));
                break;
            } catch (NumberFormatException e) {
                Log.error("❌ Por favor, introduza um número inteiro válido.");
            } catch (SQLException e) {
                 Log.error(String.format("❌ O Aluno com o número %d não existe. Tente novamente.", numero));
            }
        }
        
        // 4. Inserir/Atualizar na Base de Dados (Lógica de UPSERT)
        Log.info(String.format("\n🔄 A tentar persistir a foto para o registo %d...", numero));
        
        // Cria a foto final com o número correto do aluno
        Foto fotoParaBd = new Foto(numero, fotoDemo.getConteudo());
        
        boolean sucesso;
        String operacao;
        
        if (fotoDao.obterPorNumero(numero).isPresent()) {
            sucesso = fotoDao.atualizar(fotoParaBd);
            operacao = "UPDATE";
            Log.info(String.format("   A foto do registo %d já existe. A tentar **atualizar**...", numero));
        } else {
            sucesso = fotoDao.inserir(fotoParaBd);
            operacao = "INSERT";
            Log.info(String.format("   A foto do registo %d não existe. A tentar **inserir**...", numero));
        }

        // 5. Resultado
        if (sucesso) {
            Log.info(String.format("\n✅ SUCESSO: Operação '%s' concluída para o registo %d.", operacao, numero));
            
            // 6. Verificação e visualização final da BD
            Optional<Foto> fotoVerificadaOpt = fotoDao.obterPorNumero(numero);
            if (fotoVerificadaOpt.isPresent()) {
                Log.info("👁️ A verificar a foto lida da base de dados (última confirmação)...");
                fotoVerificadaOpt.get().visualizar();
            }
            
        } else {
            Log.error(String.format("\n❌ FALHA: A operação '%s' falhou. Verifique a base de dados.", operacao));
        }
        
        // 7. Limpeza da demo
        if (numero == TEST_NUM) {
            fotoDao.eliminar(TEST_NUM);
        }
        alunoDao.eliminar(TEST_NUM); // Limpa o aluno temporário
        
        Log.info("\nDemonstração concluída. Feche a(s) janela(s) de imagem para terminar.");
        scanner.close(); 
    }
    
    public static void main(String[] args) {
        
        AlunoDAO alunoDao = new AlunoDAO();
        FotoDAO fotoDao = new FotoDAO();
        
        try {
            // Executa primeiro a suite de testes automatizada
            testDaoSuite(alunoDao, fotoDao);
            
            // Executa a demonstração interativa
            demoCarregarFoto(alunoDao, fotoDao);
            
        } catch (SQLException e) {
            Log.error("\n🛑 ERRO CRÍTICO NA CONEXÃO OU ESTRUTURA DB.");
            Log.error("   Verifique as credenciais na classe 'db' e se as tabelas 'aluno' e 'foto' existem.");
            Log.error("   Detalhes do Erro: " + e.getMessage());
        } catch (Exception e) {
            Log.error("\n🛑 ERRO CRÍTICO INESPERADO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}