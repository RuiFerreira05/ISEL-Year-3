package inscricao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List; // 📚 Necessário para o método getAll().

import util.Configura;

public class InscricaoDAO {

    // -----------------------------------------------------------------------------------------------------------------
	// 💾 Método SAVE (Gravar Nova Inscrição)
    // -----------------------------------------------------------------------------------------------------------------

	public static int save(Inscricao i) {
		if (i == null || !i.valid()) 
			return -1;
            
		String cmd = "INSERT INTO inscricao (numero, codigo, ano, nota) VALUES (?, ?, ?, ?)";
		int nRows = -1;
        String insert = cmd.replaceFirst("\\?", String.valueOf(i.getNumero()));
        insert = insert.replaceFirst("\\?", i.getCodigo());
        insert = insert.replaceFirst("\\?", String.valueOf(i.getAno()));
		System.out.println("Executa a instrução SQL: [" + insert + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			ps.setInt(1, i.getNumero());
			ps.setString(2, i.getCodigo());
			ps.setShort(3, i.getAno());
            
            // 💡 Tratamento do campo 'nota' (pode ser NULL).
            if (i.getNota() != null) {
                ps.setBigDecimal(4, i.getNota());
            } else {
                ps.setNull(4, java.sql.Types.DECIMAL); 
            }

			nRows = ps.executeUpdate(); 
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao inserir a inscrição: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}
    
    // -----------------------------------------------------------------------------------------------------------------
	// 🔍 Método GET (Consultar por Chave Primária Composta)
    // -----------------------------------------------------------------------------------------------------------------
    
    public static Inscricao getByPK(int numero, String codigo, short ano) {
        if (numero <= 0 || codigo == null || codigo.isEmpty() || ano <= 0)
            return null;
            
        String cmd = "SELECT numero, codigo, ano, nota, inscrito FROM inscricao WHERE numero = ? AND codigo = ? AND ano = ?";
        Inscricao inscricao = null;
        int nRows = 0;
        
        System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
        try (Connection con = new Configura().getConnection();
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
            ps.setInt(1, numero);
            ps.setString(2, codigo);
            ps.setShort(3, ano);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Se encontrou um registo.
                    inscricao = new Inscricao(rs);
                    nRows = 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ ERRO ao consultar a inscrição: " + e.getMessage());
        }
        
        System.out.println("Linhas afetadas: " + nRows);
        return inscricao;
    }


    // -----------------------------------------------------------------------------------------------------------------
	// 📝 Método UPDATE (Atualizar campos não-PK)
    // -----------------------------------------------------------------------------------------------------------------

	public static int update(Inscricao i) {
		if (i == null || !i.valid()) 
			return -1;
            
		String cmd = "UPDATE inscricao SET nota = ?, inscrito = ? WHERE numero = ? AND codigo = ? AND ano = ?";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			// SET: Novos Valores para Nota e Data (índices 1 e 2)
            if (i.getNota() != null) {
                ps.setBigDecimal(1, i.getNota());
            } else {
                ps.setNull(1, java.sql.Types.DECIMAL);
            }
            ps.setDate(2, i.getInscrito()); 
            
			// WHERE: Chave Primária (índices 3, 4 e 5)
			ps.setInt(3, i.getNumero());
			ps.setString(4, i.getCodigo());
			ps.setShort(5, i.getAno());

			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao atualizar a inscrição: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

    // -----------------------------------------------------------------------------------------------------------------
	// 🗑️ Método DELETE (Apagar Inscrição pela Chave Primária)
    // -----------------------------------------------------------------------------------------------------------------

	public static int delete(Inscricao i) {
		if (i == null || !i.valid())
			return -1;
            
		String cmd = "DELETE FROM inscricao WHERE numero = ? AND codigo = ? AND ano = ?";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			ps.setInt(1, i.getNumero());
			ps.setString(2, i.getCodigo());
			ps.setShort(3, i.getAno());
            
			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao eliminar a inscrição: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}
    
    // -----------------------------------------------------------------------------------------------------------------
	// 🌐 Método GET ALL (Consultar Todos os Registos de Inscrição)
    // -----------------------------------------------------------------------------------------------------------------

    public static List<Inscricao> getAll() {
        List<Inscricao> list = new ArrayList<>(); // 📝 Inicializa a lista de retorno.
        
        // 📜 Query para selecionar todos os campos e ordenar pela Chave Primária Composta.
        String cmd = "SELECT numero, codigo, ano, nota, inscrito FROM inscricao ORDER BY numero, codigo, ano";
        int nRows = 0; 
        
        System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
        // 🔒 Execução JDBC: Garante o fecho da Connection, PreparedStatement e ResultSet.
        try (Connection con = new Configura().getConnection();
             PreparedStatement ps = con.prepareStatement(cmd);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) { // 🔄 Itera sobre todos os registos.
                
                // 🏗️ Cria um novo objeto Inscricao e preenche-o.
                Inscricao i = new Inscricao(rs);
                list.add(i);
                nRows += 1; // Incrementa o contador.
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERRO ao consultar todas as inscrições: " + e.getMessage());
        }
        
        System.out.println("Linhas afetadas: " + nRows);
        return list; // Retorna a lista completa.
    }
	/**
	 * Retorna a quantidade de inscricoes realizadas na disciplina
	 */
	public static Integer contaAtivas(String codigo) {
		if(codigo==null || codigo.isEmpty())
			return 0;
		String cmd = "select count(codigo) conta from inscricao where codigo = ?";
		try (Connection con = new Configura().getConnection();
			 PreparedStatement ps = con.prepareStatement(cmd)) {
				ps.setString(1, codigo);
				try( ResultSet rs = ps.executeQuery()) {
					if(rs.next())
						return rs.getInt("conta");
				}
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao consultar a quantidade de inscrições: " + e.getMessage());
		}
		return 0;
	}
}