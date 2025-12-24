package prototype;
import java.io.File;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface Simples para imitar o javax.servlet.ServletContext.
 */
interface ServletContextPrototype {
    String getRealPath(String path);
}

/**
 * Classe principal com a lógica de detecção de ambiente.
 */
public class PathUtil {

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

    // =========================================================
    // getBasePath (permanece a mesma lógica condicional)
    // =========================================================

    public static String getBasePath(Object context, String relativePath) {
        
        // Lógica de Reflection para Web (omitida para brevidade no corpo da explicação, mas presente no código final)
        if (context != null && isWebEnvironment()) {
            System.out.println("[DETECÇÃO]: Modo Web (Container) detectado e Contexto fornecido.");
            try {
                // Tenta chamar getRealPath("/") via Reflection
                Class<?> contextClass = context.getClass();
                Method getRealPath = contextClass.getMethod("getRealPath", String.class);
                String realPath = (String) getRealPath.invoke(context, "/");
                
                if (realPath != null) {
                    return Paths.get(realPath, relativePath).toAbsolutePath().toString();
                }

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.err.println("Aviso Web: Falha na Reflection. Causa: " + e.getMessage());
            }
        }
        
        // Modo Console (Fallback e Padrão)
        System.out.println("[DETECÇÃO]: Modo Console (Standalone ou Fallback) detectado.");
        try {
            // Retorna o diretório de trabalho corrente (user.dir)
            String userDir = System.getProperty("user.dir");
            return Paths.get(userDir, relativePath).toAbsolutePath().toString();
        } catch (SecurityException e) {
            System.err.println("Erro de segurança ao acessar a propriedade user.dir.");
            return ""; 
        }
    }
    
    // ... (MockServletContext e método main para demonstração)
    
    // =========================================================
    // CLASSE DE SIMULAÇÃO PARA DEMONSTRAÇÃO DO MODO WEB
    // =========================================================
    public static class MockServletContext implements ServletContextPrototype {
        @Override
        public String getRealPath(String path) {
            return "C:\\Tomcat\\webapps\\minha_app_web_deploy"; 
        }
    }

    // =========================================================
    // MÉTODO MAIN DE DEMONSTRAÇÃO
    // =========================================================

    public static void main(String[] args) {
        
        System.out.println("=================================================");
        System.out.println("  PathUtil Demo: Detecção Aprimorada  ");
        System.out.println("=================================================");
        System.out.println("Status de Detecção (isWebEnvironment()): " + PathUtil.isWebEnvironment());
        System.out.println("-------------------------------------------------");


        // --- CENÁRIO 1: MODO CONSOLE (Real) ---
        System.out.println("\n--- CENÁRIO 1: CONSOLE/STANDALONE ---");
        // O isWebEnvironment() retornará false no console, forçando o fallback.
        String caminhoConsole = PathUtil.getBasePath(null, "dados" + File.separator + "config.properties");
        System.out.println("-> Resultado Final: " + caminhoConsole);
        
        
        // --- CENÁRIO 2: MODO WEB (Simulação) ---
        System.out.println("\n--- CENÁRIO 2: WEB/TOMCAT (Simulação) ---");
        
        // Para SIMULAR o sucesso da detecção em um ambiente de console,
        // precisaríamos definir uma das propriedades do sistema manualmente.
        if (!PathUtil.isWebEnvironment()) {
            System.out.println("SIMULANDO: Forçando detecção de Web definindo 'catalina.base'.");
            System.setProperty("catalina.base", "/simulacao/tomcat"); 
        }

        if (PathUtil.isWebEnvironment()) {
            System.out.println("Status: Propriedade de Container ENCONTRADA. Simulação Web OK.");
            Object mockContext = new MockServletContext();
            String caminhoWeb = PathUtil.getBasePath(mockContext, "WEB-INF" + File.separator + "recursos");
            System.out.println("-> Resultado da Chamada: " + caminhoWeb);
            
        } else {
             System.out.println("Status: Propriedades de Container NÃO ENCONTRADAS. A simulação falhou.");
        }
        
        System.out.println("\n=================================================");
    }
}