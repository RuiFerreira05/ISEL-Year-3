package disciplina;

public class Disc {

	private String codigo = null;
	private String designacao = null;
	
	public Disc(String codigo, String designacao) {
		this.codigo=codigo;
		this.designacao=designacao;
	}
	
	public String getCodigo() {
		return codigo;
	}

	public String getDesignacao() {
		return designacao;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public void setDesignacao(String designacao) {
		this.designacao = designacao;
	}

	public void print() {
		System.out.println("Disciplina: ");
		System.out.println("  Código: '" + codigo + "'");
		System.out.println("  Designacao: '" + designacao + "'");
	}

	public boolean valid() {
		return !(getDesignacao() == null || getDesignacao().isEmpty() || getCodigo() == null || getCodigo().isEmpty());
	}
	
}
