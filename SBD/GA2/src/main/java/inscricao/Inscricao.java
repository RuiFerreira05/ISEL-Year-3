package inscricao; 			// 📦 Declaração do pacote. Ajuste conforme a sua estrutura.

import java.math.BigDecimal; // 🧮 Importa BigDecimal para lidar com a precisão da Nota (DECIMAL 4,2).
import java.sql.Date;       	// 📅 Importa Date para lidar com o campo 'inscrito'.
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * POJO que representa a tabela 'inscricao'.
 * Chave Primária Composta: (numero, codigo, ano).
 */
public class Inscricao {

    // --- 🔑 Chave Primária Composta e Chaves Estrangeiras ---
    // Usamos primitivos para as chaves NOT NULL (int, short) para otimização de memória.
    private int numero;         // Número do aluno (FK para ALUNO).
    private String codigo;      // Código da disciplina (FK para DISCIPLINA - char(4)).
    private short ano;          // Ano letivo da inscrição (smallint, parte da PK).

    // --- 🏷️ Outros Atributos ---
    // Nota pode ser NULL na BD, mas usamos BigDecimal para precisão.
    private BigDecimal nota = null;    
    // Data de inscrição.
    private Date inscrito;      

    // --- 🏗️ Construtores ---
    
    /**
     * Construtor Completo.
     * Útil para carregar todos os dados de um registo da base de dados.
     */
    public Inscricao(int numero, String codigo, short ano, BigDecimal nota) {
        this.numero = numero;
        this.codigo = codigo;
        this.ano = ano;
        this.nota = nota;
    }
    
    public Inscricao(int numero, String codigo, short ano, BigDecimal nota, Date inscrito) {
        this.numero = numero;
        this.codigo = codigo;
        this.ano = ano;
        this.nota = nota;
        this.inscrito = inscrito;
    }
    
   public Inscricao(final ResultSet rs) {
        try {
			this.numero = rs.getInt("numero");
	        this.codigo = rs.getString("codigo");
	        this.ano = rs.getShort("ano");
	        this.nota = rs.getBigDecimal("nota");
	        this.inscrito =  rs.getDate("inscrito");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
   
    public Inscricao(final HttpServletRequest request) {
        this.numero = Integer.parseInt(request.getParameter("Numero"));
        this.codigo = request.getParameter("Codigo");
        this.ano = Short.parseShort(request.getParameter("Ano"));
    }
    
    /**
     * Construtor para Chave Primária.
     * Útil para operações como DELETE ou GET, onde só a chave é necessária.
     */
    public Inscricao(int numero, String codigo, short ano) {
        this.numero = numero;
        this.codigo = codigo;
        this.ano = ano;
    }

    /**
     * Construtor Vazio (Default).
     * Necessário para frameworks.
     */
    public Inscricao() {
    }

    // --- ⚙️ Getters e Setters (Acessores e Modificadores) ---
    // Os métodos seguem o padrão JavaBeans.

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public short getAno() {
        return ano;
    }

    public void setAno(short ano) {
        this.ano = ano;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public Date getInscrito() {
        return inscrito;
    }

    public void setInscrito(Date inscrito) {
        this.inscrito = inscrito;
    }
    
    // --- 💡 Método de Validação ---
    /**
     * Verifica se os campos que compõem a Chave Primária estão preenchidos com valores válidos (> 0 e não vazios).
     * @return true se o objeto for minimamente válido para operações CRUD. ✅
     */
    public boolean valid() {
        return this.numero > 0 && 
               this.codigo != null && !this.codigo.trim().isEmpty() && 
               this.ano > 0;
    }

    // --- 🖨️ Método de Impressão ---
    @Override
    public String toString() {
        return "Inscricao [numero=" + numero + ", codigo=" + codigo + ", ano=" + ano + 
               ", nota=" + nota + ", inscrito=" + inscrito + "]";
    }
}
