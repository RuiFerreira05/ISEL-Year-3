package foto;

import java.util.Base64;

/**
 * POJO que representa a tabela 'foto'.
 * Contém a Chave Primária (numero) e o conteúdo binário da foto.
 */
public class Foto {

    // --- 🔑 Chave Primária/Chave Estrangeira ---
    // Mapeia para a coluna 'numero' (int NOT NULL), que referencia a tabela 'aluno'.
    private int numero; 

    // --- 💾 Conteúdo Binário ---
    // Mapeia para a coluna 'conteudo' (MEDIUMBLOB NOT NULL). 
    // Em Java, dados binários de grande porte são representados por um array de bytes.
    private byte[] conteudo; 

    // --- 🏗️ Construtores ---
    
    /**
     * Construtor Completo.
     * Útil para carregar ou criar um registo completo de foto.
     */
    public Foto(int numero, byte[] conteudo) {
        this.numero = numero;
        this.conteudo = conteudo;
    }

    /**
     * Construtor Vazio (Default).
     * Necessário para frameworks.
     */
    public Foto() {
    }

    // --- ⚙️ Getters e Setters ---

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public byte[] getConteudo() {
        return conteudo;
    }
    
	// devolve a imagem em base64
	public String getConteudo64() {
		return Base64.getEncoder().encodeToString(conteudo);
	}
    
	// usa base64 para modificar o byte[] armazenado 
	public void setConteudo64(String encoded64) {
		Base64.getDecoder().decode(encoded64);
	}
	
    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }
    
    // --- 💡 Método de Validação ---
    /**
     * Verifica se os campos obrigatórios (Número e Conteúdo) estão presentes e válidos.
     * @return true se o objeto for válido para operações CRUD. ✅
     */
    public boolean valid() {
        // O número deve ser positivo, e o array de bytes não pode ser nulo e deve ter comprimento > 0.
        return this.numero > 0 && this.conteudo != null && this.conteudo.length > 0;
    }
    
    // --- 🖨️ Método de Impressão (toString) ---
    @Override
    public String toString() {
        // Mostra o tamanho do conteúdo em vez de tentar imprimir o array de bytes.
        String tamanho = (conteudo != null) ? conteudo.length + " bytes" : "null";
        return "Foto [numero=" + numero + ", conteudo (tamanho)=" + tamanho + "]";
    }
}