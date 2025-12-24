package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

/**
 * 🇵🇹 Classe de Utilidades (`Name`) para Normalização, Redução e Determinação de Género de Nomes Próprios.
 * Logging implementado com wrapper temporário 'Log'.
 */
public class Name {

    // ----------------------------------------------------------------------
    // --- 💡 IMPLEMENTAÇÃO TEMPORÁRIA DE LOGGING (NOME CLARO: Log) ---
    // ----------------------------------------------------------------------

    /**
     * 💡 Classe interna temporária que simula um logger (INFO/WARN/ERROR) para stdout/stderr.
     * Deve ser removida e substituída por SLF4J no ambiente de produção.
     */
    private static final class Log {
        public static void info(String message) {
        		if (Configura.isWebEnvironment())
        			System.out.println(message);
        }
        public static void warn(String message) {
        		if (Configura.isWebEnvironment())
        			System.err.println("⚠️ WARN: " + message);
        }
        public static void error(String message) {
            System.err.println("❌ ERROR: " + message);
        }
    }
    
    // --- CONFIGURAÇÃO DE CAMINHOS E VARIÁVEIS ---
    
    // Caminho de FALLBACK para Consola/IDE (Assume estrutura Maven/Eclipse)
    private static final String CORR_FILE 			= "corrector.txt";
    private static final String GENERO_FILE 			= "gender.txt";
    private static final String FALLBACK_PATH   		= "src/main/webapp/WEB-INF/resources/";
    
    private static boolean resourcesLoaded = false;
    private static volatile boolean isInitialized = false; 
    
    private static ConcurrentHashMap<String, String> MAPA_CORR;
    private static ConcurrentHashMap<String, String> MAPA_GENERO;

     // Lista de Conectores/Artigos que não devem ser capitalizados (em minúsculas)
	// 🌐 Inclui partículas de ligação em Português, Espanhol, Francês e Inglês.
	private static final List<String> CONNECTORS = Arrays.asList(
		 // PT (da, de, do, e, as, os, etc. + contrações)
		 "da", "de", "do", "das", "dos", "e", "os", "a", "o", "as", "por", "que", "para", "com", "sem", "ou", 
		 "em", "no", "na", "nos", "nas", // Contrações de 'em' + artigos
		 
		 // ES (del, la, las, los, el, y)
		 "del", "la", "las", "los", "el", "y",
		 
		 // FR (du, le, les)
		 "du", "le", "les", 
		 
		 // EN / Outras (of, and, the, von)
		 "of", "and", "the", "von", "van", "zu"
	 );

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    // --- MÉTODOS AUXILIARES DE FALLBACK ---

    /** 💾 Retorna o conjunto mínimo de correções hardcoded como um mapa imutável (Java 9+). */
    private static Map<String, String> getHardcodedCorrecoes() {
        return Map.of(
            "profirio", "Porfírio",
            "acacia", "Acácia",
            "goncalves", "Gonçalves",
            "luis", "Luís"
        );
    }

    // --- BLOCO STATIC: Executado primeiro. Tenta carregar o caminho de Consola/IDE. ---
    static {
	    	if (!resourcesLoaded) {
	        // 1. Inicializa os mapas (mutáveis)
	        MAPA_CORR = new ConcurrentHashMap<>(); 
	        MAPA_CORR.putAll(getHardcodedCorrecoes()); // Adiciona o fallback
	        MAPA_GENERO = new ConcurrentHashMap<>();
	
	        // 2. Tenta carregar os recursos usando o caminho de Consola (se não for Web)
	        resourcesLoaded= loadResources(FALLBACK_PATH+CORR_FILE, MAPA_CORR) &&
	        					 loadResources(FALLBACK_PATH+GENERO_FILE, MAPA_GENERO);
	        // Agora, chamada clara: Log.info
	        Log.info("🤖 Bloco Static Name.java executado."); 
	    }
    }
    
    // ---------------------------------------------------------------------------------
    // --- MÉTODO PARA INICIAR NA WEB (USO, SERVLET: StartupInitializerServlet.java) ---
    // ---------------------------------------------------------------------------------

    /** * 🌐 Deve ser chamado uma vez no arranque da aplicação Web.    */
    /** * Pode ser chamadado automaticamente pelo servlet no arranque. */
    public static synchronized void initialize(String path) {
        if (isInitialized) {
            Log.warn("Name.java já foi inicializado. Ignorando chamada duplicada."); 
            return;
        }
        
        if (path != null && !path.isEmpty()) {
        		path = path.endsWith("/") || path.endsWith("\\") ? path : path + "/";
            // Log.info com concatenação
            Log.info("📂 Name.java inicializado com o caminho Web: " + path); 

            // RECARRREGAMENTO: Limpa e recarrega os mapas com o caminho web correto.
            MAPA_CORR.clear();
            MAPA_CORR.putAll(getHardcodedCorrecoes()); 
            MAPA_GENERO.clear();
            isInitialized = loadResources(path+CORR_FILE, MAPA_CORR) &&
            					loadResources(path+GENERO_FILE, MAPA_GENERO);
            MAPA_CORR = (ConcurrentHashMap<String, String>) Collections.unmodifiableMap(MAPA_CORR);
            MAPA_GENERO = (ConcurrentHashMap<String, String>) Collections.unmodifiableMap(MAPA_GENERO);
        }
    }

    // ----------------------------------------------------------------------
    // --- MÉTODOS AUXILIARES DE CARREGAMENTO PRIVADOS ---
    // ----------------------------------------------------------------------

    /** 💾 Carrega os dados para o mapa, usando o caminho indicado. */
    private static boolean loadResources(String filePath, Map<String, String> targetMap) {
        
        if (filePath == null) {
            return false;
        }

        Path path = Paths.get(filePath);
        
        if (Files.exists(path) && Files.isReadable(path)) {
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                readFromReader(br, targetMap);
                Log.info("... ✅ SUCESSO. Foram carregados '"+targetMap.size()+"' items.");
            } catch (IOException e) {
                // Log.error
                Log.error("ERRO NIO ao ler ficheiro '" + filePath + "': " + e.getMessage()); 
                return false;
            }
        } else {
            // Log.warn
            Log.warn("... FALHOU (Não Encontrado/Legível)."); 
            return false;
        }
        return true;
    }
    
    /** 🔄 Lógica central de leitura de linhas a partir de um BufferedReader. */
    private static void readFromReader(BufferedReader br, Map<String, String> map) throws IOException {
        String linha;
        while ((linha = br.readLine()) != null) {
            if (linha.trim().isEmpty() || linha.startsWith("#")) continue; 
            String[] partes = linha.split("=", 2);
            if (partes.length == 2) {
                String chave = partes[0].trim().toLowerCase();
                String valor = partes[1].trim();
                map.put(chave, valor);
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // --- MÉTODOS DE MANIPULAÇÃO DE NOMES (RESTANTE LÓGICA) ---
    // ----------------------------------------------------------------------

    /** 📝 Aplica as correções ortográficas definidas no MAPA_CORR. */
    private static String correct(String name) {
        String[] words = SPACE_PATTERN.split(name);
        StringBuilder correctedName = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            correctedName.append(MAPA_CORR.getOrDefault(word.toLowerCase(), word));
            if (i < words.length - 1) correctedName.append(" ");
        }
        return correctedName.toString();
    }

    /** 📛 Lógica base: Realiza a abreviatura de uma palavra específica num dado índice. */
    private static String doAbbreviate(String name, int index) {
        String[] words = SPACE_PATTERN.split(name);
        int n = words.length;

        if (index < 0 || index >= n || words[index].endsWith(".")) {
            return name;
        }

        List<String> newWords = new ArrayList<>(Arrays.asList(words));
        String wordToAbbreviate = newWords.get(index);
        String abbreviatedWord;

        if (wordToAbbreviate.contains("'")) {
            int apostropheIndex = wordToAbbreviate.indexOf("'");
            String prefix = wordToAbbreviate.substring(0, apostropheIndex + 1);
            String initial = wordToAbbreviate.substring(apostropheIndex + 1, apostropheIndex + 2);
            abbreviatedWord = prefix + initial + ".";
        } else {
            abbreviatedWord = wordToAbbreviate.charAt(0) + ".";
        }
        
        newWords.set(index, abbreviatedWord);
        
        return String.join(" ", newWords);
    }
    
    /** 👤 Determina o género a partir do primeiro nome. Retorna 'M', 'F' ou 'X'. */
    public static String getGender(String full_name) {
        if (full_name == null || full_name.trim().isEmpty()) { return "X"; }
        String[] words = SPACE_PATTERN.split(full_name.trim());
        if (words.length == 0) 
        		{ return "X"; }
        String gender = MAPA_GENERO.getOrDefault(words[0].toLowerCase(), "X");
        if (gender.equals("X")) 
        		gender = MAPA_GENERO.getOrDefault(correct(words[0]).toLowerCase(), "X");
        return gender;
    }

    /** * ✨ **Fase 0: Normalização**. Aplica correção e capitalização. */
    public static String normalize(String name) {
        if (name == null || name.trim().isEmpty()) 
        		{ return name; }

        String normalizedName = name.trim().toLowerCase().replaceAll("\\s+", " ");
        normalizedName = normalizedName.replaceAll("’", "'"); // Padroniza o apóstrofo
        String correctedName = correct(normalizedName); 		// Aplica correções

        String[] words = SPACE_PATTERN.split(correctedName);
        StringBuilder finalName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) { continue; }

            boolean isPureConnector = (i > 0) && CONNECTORS.contains(word.toLowerCase()); 
            boolean hasApostrophe = word.contains("'");
            boolean hasHifen = word.contains("-");

            String processedWord;

            if (isPureConnector) {
                processedWord = word.toLowerCase();
            } else 
            		if (hasApostrophe) {
            			// Lógica de capitalização para nomes como "d'Almeida"
            			int apostropheIndex = word.indexOf("'");
            			String prefix = word.substring(0, apostropheIndex + 1).toLowerCase();
            			String suffix = word.substring(apostropheIndex + 1);
            			String capitalizedSuffix = (suffix.length() > 0) ? Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1).toLowerCase() : "";
            			processedWord = prefix + capitalizedSuffix;
            		} 
            		else {
            			// Capitalização Inicial (ex: "maria-do-céu" -> "Maria-do-Céu")
            			processedWord = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            			// Capitalização com '-' (ex: "maria-do-céu" -> "Maria-do-Céu")
            			if(hasHifen) {
                			processedWord = processedWord.replaceAll("-", " ");
                			processedWord = normalize(processedWord);
                			processedWord = processedWord.replaceAll(" ", "-");
                		}
            		}

            finalName.append(processedWord);

            if (i < words.length - 1)
                finalName.append(" ");
        }
        return finalName.toString();
    }
    
    /** * ⏭️ **Fase 1: Abrevia o Meio**. Abreviar a próxima palavra do meio (não destrutiva). */
    public static String abbreviateMiddle(String name) {
        if (name == null || name.trim().isEmpty()) { return name; }
        String[] words = SPACE_PATTERN.split(name);
        int n = words.length;

        if (n <= 2) { return name; }

        int wordToAbbreviateIndex = -1;

        for (int i = 1; i <= n - 2; i++) {
            String word = words[i];
            if (CONNECTORS.contains(word.toLowerCase())) continue;
            if (word.endsWith(".")) continue;
            wordToAbbreviateIndex = i;
            break;
        }

        if (wordToAbbreviateIndex == -1) { return name; }

        return doAbbreviate(name, wordToAbbreviateIndex);
    }

    /** 💣 **Fase 2: Remoção do Segmento**. Remove o próximo segmento abreviado e o conector anterior (destrutiva). */
    private static String stripSegment(String name) {
        String[] words = SPACE_PATTERN.split(name);
        List<String> newWords = new ArrayList<>(Arrays.asList(words));
        int n = newWords.size();
        if (n <= 2) { return name; }
        int indexToRemove = -1;

        for (int i = 1; i <= n - 2; i++) {
            if (newWords.get(i).endsWith(".")) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) { return name; }

        newWords.remove(indexToRemove); 

        if (indexToRemove > 0) {
            String previousWord = newWords.get(indexToRemove - 1);
            if (CONNECTORS.contains(previousWord.toLowerCase())) {
                newWords.remove(indexToRemove - 1);
            }
        }
        return String.join(" ", newWords);
    }

    /** 💥 **Fase 3: Remoção Imutável**. Remove prioritariamente Conectores e Palavras Intermédias inteiras. */
    private static String stripImmutable(String name) {
        String[] words = SPACE_PATTERN.split(name);
        List<String> newWords = new ArrayList<>(Arrays.asList(words));
        int n = newWords.size();

        if (n <= 2) {
             return name;
        }

        // 1. Prioridade: Conectores no meio
        for (int i = newWords.size() - 1; i > 0; i--) {
            String word = newWords.get(i);
            if (CONNECTORS.contains(word.toLowerCase())) {
                newWords.remove(i);
                return String.join(" ", newWords);
            }
        }

        // 2. Prioridade: Palavra inteira do meio
        for (int i = 1; i < newWords.size() - 1; i++) {
            String word = newWords.get(i);
            if (!CONNECTORS.contains(word.toLowerCase()) && !word.endsWith(".")) {
                newWords.remove(i);
                return String.join(" ", newWords);
            }
        }

        // 3. Últimos recursos: Remover palavras nas extremidades
        int lastIndex = newWords.size() - 1;
        String lastWord = newWords.get(lastIndex);

        if (newWords.size() > 1 && !CONNECTORS.contains(lastWord.toLowerCase()) && !lastWord.endsWith(".")) {
            newWords.remove(lastIndex);
            return String.join(" ", newWords);
        }

         if (newWords.size() > 1) {
            String firstWord = newWords.get(0);
            if (!firstWord.endsWith(".") && !CONNECTORS.contains(firstWord.toLowerCase())) {
                newWords.remove(0);
                return String.join(" ", newWords);
            }
        }

        return name;
    }

    /** 📛 **Fase 4: Abrevia o Primeiro Nome**. Abrevia o primeiro nome como último recurso. */
    private static String abbreviateFirst(String name) {
        return doAbbreviate(name, 0);
    }

    /** ✂️ **Fase 5: Abrevia o Último Nome**, (apelido) de um nome completo.*/
    public static String abbreviateLast(String name) {
        return doAbbreviate(name, name.split(" ").length - 1);
    }
    /**
     * ✂️ **Reduz o tamanho de um nome** (`shorten`) em quatro fases progressivas.
     */
    public static String shorten(String name, int maxSize) {
        String currentName = normalize(name);
        if (currentName == null || currentName.isEmpty() || currentName.length() <= maxSize) 
        		{ return currentName; }

        // Fase 1: Abrevia Progressivamente as Palavras Intermédias
        String previousName = "";
        while (currentName.length() > maxSize && !currentName.equals(previousName)) {
        	 	// Log.info("  ▶️ Fase 1️: Abrevia Progressivamente as Palavras Intermédias");
            previousName = currentName;
            currentName = abbreviateMiddle(currentName);
        }

        // Fase 2: Remoção Progressiva de Segmentos Abreviados (ex: 'F. da')
        if (currentName.length() > maxSize) {
            // Log.info("  ▶️ Fase 2: Remoção Progressiva de Segmentos Abreviados");
            previousName = "";
            while (currentName.length() > maxSize && !currentName.equals(previousName)) {
                previousName = currentName;
                currentName = stripSegment(currentName);
            }
        }
        // Fase 3: Remoção Progressiva de Conectores e Palavras Inteiras Imutáveis
        if (currentName.length() > maxSize) {
             // Log.info("  ▶️ Fase 3: Remoção Agressiva de Conectores e Palavras Inteiras Imutáveis");
             previousName = "";
             while (currentName.length() > maxSize && !currentName.equals(previousName)) {
                previousName = currentName;
                String nextName = stripImmutable(currentName);

                if (currentName.equals(nextName)) {
                    break; 
                }
                currentName = nextName;
            }
        }
        
        // Fase 4: Abrevia o Primeiro Nome
        if (currentName.length() > maxSize) {
        		// Log.info("  ▶️ Fase 4: Abrevia o Primeiro Nome");
            currentName = abbreviateFirst(currentName);
        }
        
        // Fase 5: Abrevia o Ultimo Nome
        if (currentName.length() > maxSize) {
        		// Log.info("  ▶️ Fase 5: Abrevia o Ultimo Nome");
            currentName = abbreviateLast(currentName);
        }
        
        
        // Fase 6: Reduz a duas letras
        if (currentName.length() > maxSize) {
        		// Log.info("  ▶️ Fase 6: Reduz a duas letras!");
            currentName = currentName.replaceAll("[ .]", "");;
        }
        
        return currentName;
    }

    // ----------------------------------------------------------------------
    // 💻 MÉTODO MAIN DE TESTE (Para uso em Consola/IDE)
    // ----------------------------------------------------------------------

    public static void main(String[] args) {

        Log.info("--- 🚀 Teste Name.java em modo CONSOLA/IDE ---"); 
        
        // Nomes de teste
        String nomeOriginal = "Maria-Do-ceu Benedita Frôscolo Jovino D'Almeida MILITÃO De Sousa Baruel Dos Itaparica Boré SALVE-rainha Das abelhas";
        String nomeF = "Capitulina andrioleta da Conceicao do Corte-geral";
        String nomeM = "Joao-de-Deus acacio Techeremunga texugeiro";
        String nomeX = "Manarimba Bupatcha Medronheira";
        
        System.out.println("\n--- 🔎 Normalização ---"); 
        String nomeNormalizado = normalize(nomeOriginal);
        System.out.println("Original: " + nomeOriginal);
        System.out.println("Normalizado: " + nomeNormalizado);
        System.out.println("Tamanho Inicial: " + nomeNormalizado.length() + " caracteres.");

        System.out.println("\n" + "--- ✂️ Testes de Redução do Comprimento do Nome ---");
        for(int i=99; i>1; i=i-5) {
        		String resultado = shorten(nomeOriginal, i);
        		System.out.println("Limie Máximo "+i+": '" + resultado +"' (Comprimento Atual: " + resultado.length() + ")"); 
        }

        System.out.println("\n" + "--- 👤 Teste de Determinação de Género ---");
        System.out.println("Nome: " + normalize(nomeOriginal).substring(0,20) 	+ "... -> Género: " + getGender(nomeOriginal));
        System.out.println("Nome: " + normalize(nomeF).substring(0,20) 	+ "... -> Género: " + getGender(nomeF));
        System.out.println("Nome: " + normalize(nomeM).substring(0,20) 	+ "... -> Género: " + getGender(nomeM));
        System.out.println("Nome: " + normalize(nomeX).substring(0,20) 	+ "... -> Género: " + getGender(nomeX));
    }
}