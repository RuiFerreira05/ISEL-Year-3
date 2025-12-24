package prototype;

public class CurrentDirectory {

    public static void main(String[] args) {
        try {
            // 1. Usando a propriedade do sistema "user.dir"
            String currentDir = System.getProperty("user.dir");
            System.out.println("Diretório de Trabalho (user.dir): " + currentDir);

            // 2. Usando a classe Path (mais moderno para operações de arquivo)
            // Note: O método toAbsolutePath() em um Path não-qualificado (como ".") geralmente 
            // retorna o CWD.
            java.nio.file.Path currentPath = java.nio.file.Paths.get(".");
            String absolutePath = currentPath.toAbsolutePath().normalize().toString();
            System.out.println("Diretório Absoluto (.): " + absolutePath);

        } catch (SecurityException e) {
            // Pode ocorrer se houver um SecurityManager que impeça a leitura da propriedade
            System.err.println("Erro de segurança ao acessar a propriedade do sistema: " + e.getMessage());
        }
    }
}