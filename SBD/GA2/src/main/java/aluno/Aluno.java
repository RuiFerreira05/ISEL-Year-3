package aluno; // 📦 Declaração do pacote onde a classe está localizada.

import java.sql.Date; // 📅 Importa a classe Date específica para interagir com o JDBC (base de dados).
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;

public class Aluno {

	// --- 🏷️ Campos/Atributos da Classe (Membros de Instância) ---
	
	// Usamos a classe 'Integer' em vez do primitivo 'int' para permitir que o campo seja nulo (NULL),
	// o que é útil ao receber dados incompletos ou ao modelar a chave primária antes da inserção.
	private Integer numero = null;
	
	// Nome completo do aluno. String é nativamente nullable.
	private String nome = null; 
	
	// Usamos 'Character' em vez do primitivo 'char' pelo mesmo motivo (permitir NULL).
	private Character genero = null;
	
	// Data de nascimento, mapeada para o tipo DATE do SQL.
	private Date nascido = null; 
	
	// --- 🏗️ Construtores ---
	
	/**
	 * Construtor para inicializar todos os campos.
     * É útil para criar um objeto Aluno com todos os dados retirados da base de dados (SELECT).
	 */
	public Aluno(final Integer numero, final String nome, final Character genero, final Date nascido) {
		this.numero 		= numero;
		this.nome 		= nome;
		this.genero 		= genero;
		this.nascido 	= nascido;
	}
    
	public Aluno(final ResultSet aluno) {
		try {
			this.numero 		= aluno.getInt("numero");
			this.nome 		= aluno.getString("nome");
			this.genero 		= aluno.getString("genero").charAt(0);
			this.nascido 	= aluno.getDate("nascido");
		} catch (SQLException e) {
			System.out.println("Erro na leitura do aluno: "+e.getMessage());
		}
	}
	
	public Aluno(final HttpServletRequest request) {
		setNumeroP(request.getParameter("Numero"));
		setNomeP(request.getParameter("Nome"));
		setGeneroP(request.getParameter("Genero"));
		setNascidoP(request.getParameter("Data"));
	}
	
    /**
	 * Construtor vazio (default).
	 * Essencial para criar uma instância antes de injetar os dados (setters).
	 */
    public Aluno() {
    }

	// --- ⚙️ Getters (Acessores) ---

	// Permitem aceder aos valores dos atributos de forma controlada.
    
	public Integer getNumero() {
		return numero;
	}
	
	public String getNumeroS() {
		if(numero!=null)
			return String.valueOf(numero);
		return "";
	}

	public String getNome() {
		return nome;
	}


	public String getNomeS() {
		if(nome==null || nome.isEmpty())
			return "";
		return util.Name.normalize(nome);
	}
	
	public Character getGenero() {
		return genero;
	}
	
	public String getGeneroS() {
		if(genero==null)
			return "X";
		return String.valueOf(genero);
	}
	
	public Date getNascido() {
		return nascido;
	}
	
	public String getNascidoS() {
		if(nascido==null)
			return "";
		return nascido.toString();
	}

	// --- 📝 Setters (Modificadores) ---

	// Permitem alterar os valores dos atributos de forma controlada.
	
	public void setNumero(final Integer numero) {
		this.numero = numero;
	}
	
	public void setNumeroP(final String numero) {
		if(numero == null || numero.isEmpty())
			this.numero = null;
		else
			this.numero = Integer.parseInt(numero);
	}

	public void setNome(final String nome) {
		this.nome = nome;
	}
	
	public void setNomeP(final String nome) {// igual!
		this.nome = nome;
	}

	public void setGenero(final Character genero) {
		this.genero = genero;
	} 
	
	public void setGeneroP(final String  genero) {
		if(genero == null || genero.isEmpty())
			this.genero='X';
		else
			this.genero = genero.charAt(0);
	}

	public void setNascido(final Date nascido) {
		this.nascido = nascido;
	}
	
	public void setNascidoP(final String nascido) {
		if(nascido==null || nascido=="")
			this.nascido = null;
		else
			this.nascido = util.DataFormatter.StringToSqlDate(nascido);
	}

	// --- 💡 Métodos Utilitários ---

	/**
	 * Imprime o conteúdo do objeto para a consola.
	 * Útil para debug ou logging rápido. 🖨️
	 */
	public void print() {
		System.out.println("Aluno: ");
		System.out.println("  Número: '" + getNumeroS() + "'");
		System.out.println("  Nome: '" + getNomeS() + "'");
		System.out.println("  Género: '" + getGeneroS() + "'");
		System.out.println("  Nascido: '" + getNascidoS() + "'");
	}

	/**
	 * Valida se os campos estão preenchidos.
	 * @return true se o objeto for considerado válido (tem número e nome preenchidos). ✅
	 */
	public boolean valid() {
		// Verifica se o 'nome' é nulo OU vazio E se o 'numero' é nulo.
        // Estes são os campos mínimos que devem estar presentes para identificar um registo.
		return !(getNome() == null || getNome().isEmpty() || getNumero() == null || getNascido() == null || getGenero() == null); 
	}
}