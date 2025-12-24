package prototype;

import gestor.Gestor;
import util.Configura;
import util.IOx;

/**
 * Classe de demonstração para testar as operações de alto nível
 * (Administração da BD) fornecidas pela classe Gestor.
 * Esta classe simula o fluxo de trabalho de configuração inicial e
 * limpeza da base de dados académica.
 */
public final class GestorDemo {

    /**
     * O ponto de entrada da demonstração.
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        
        System.out.println("=================================================");
        System.out.println("🚀 Iniciando Demonstração da Classe Gestor (Admin)");
        System.out.println("=================================================");
        
        // --- 1. CONFIGURAÇÃO INICIAL ---
        Configura cfg = new Configura();
        if (cfg.eliminarBaseDeDados() && cfg.criarBaseDeDados()) {
             System.out.println(">> 🛢️ Base de dados configurada com sucesso.");
         } else {
        	 	System.err.println(">> ❌ Falha na criação da base de dados. Abortando a demonstração.");
        	 	return;
         }
        
        // --- 2. CRIAÇÃO DOS OBJETOS (Tabelas e Comentários) ---
        System.out.println("\n--- 🧱 Passo 1: Criar Tabelas e Comentários ---");
        if (Gestor.criarTabelas()) {
            System.out.println(">> ✅ Tabelas e Comentários criados com sucesso.");
        } else {
            System.err.println(">> ❌ Falha crítica na criação das tabelas. Abortando a demonstração.");
            return; 
        }

        // --- 3. CRIAÇÃO DAS VISTAS ---
        System.out.println("\n--- 👁️ Passo 2: Criar Vistas ---");
        if (Gestor.criarVistas()) {
            System.out.println(">> ✅ Vistas criadas com sucesso.");
        } else {
            System.err.println(">> ❌ Falha na criação das vistas.");
            // Continua, pois a falha nas vistas pode não ser fatal.
        }
        
        // --- 4. CARREGAMENTO DOS DADOS ---
        System.out.println("\n--- 🗄️ Passo 3: Carregar Dados Iniciais ---");
        // O carregarTabelas engloba Aluno, Disciplina, Inscricao e Foto (em transação)
        if (Gestor.carregarTabelas()) {
            System.out.println(">> ✅ Dados iniciais carregados com sucesso.");
        } else {
            System.err.println(">> ❌ Falha no carregamento dos dados iniciais.");
            // Continua para tentar a limpeza.
        }
        
        // --- 5. LISTAGEM DOS OBJETOS ---
        System.out.println("\n--- 📑 Passo 4: Listar Tabelas e Vistas ---");
        Configura.listarObjectos();
        
        // Pausa para visualização
        System.out.println("\n[Pressione ENTER para continuar para a fase de limpeza...]");
        IOx.in(); 
        
        // --- 6. LIMPEZA DOS DADOS (DELETE) ---
        System.out.println("\n--- 🗑️ Passo 5: Apagar Dados (DELETE) ---");
        if (Gestor.apagarTabelas()) {
            System.out.println(">> ✅ Dados apagados com sucesso.");
        } else {
            System.err.println(">> ❌ Falha ao apagar os dados.");
        }
        
        // --- 7. REMOÇÃO DOS OBJETOS (Vistas) ---
        System.out.println("\n--- ➖ Passo 6: Remover Vistas (DROP VIEW) ---");
        if (Gestor.removerVistas()) {
            System.out.println(">> ✅ Vistas removidas com sucesso.");
        } else {
            System.err.println(">> ❌ Falha ao remover as vistas.");
        }

        // --- 8. REMOÇÃO DOS OBJETOS (Tabelas) ---
        System.out.println("\n--- ➖ Passo 7: Remover Tabelas (DROP TABLE) ---");
        if (Gestor.removerTabelas()) {
            System.out.println(">> ✅ Tabelas removidas com sucesso.");
        } else {
            System.err.println(">> ❌ Falha ao remover as tabelas.");
        }
        
        // --- 9. FIM DA DEMONSTRAÇÃO ---
        System.out.println("\n=================================================");
        System.out.println("🏁 Demonstração da Classe Gestor Concluída.");
        System.out.println("=================================================");
    }
}
