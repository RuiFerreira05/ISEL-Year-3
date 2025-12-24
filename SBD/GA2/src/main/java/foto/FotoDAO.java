package foto; 

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import util.Configura;

public class FotoDAO {

    // -----------------------------------------------------------------------------------------------------------------
	// 💾 Método SAVE (Gravar Nova Fotografia)
    // -----------------------------------------------------------------------------------------------------------------
    // Como 'numero' é PK, este método só insere se o aluno ainda não tiver foto.
    
	public static int save(Foto f, Connection con) {
        // 🚦 1. Validação de Entrada
		if (f == null || !f.valid()) 
			return -1;
            
		// 📜 2. Instrução SQL: Inserção.
		String cmd = "INSERT INTO foto (numero, conteudo) VALUES (?, ?)";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		// 🔒 3. Execução JDBC.
		try (PreparedStatement ps = con.prepareStatement(cmd)) {
                
			ps.setInt(1, f.getNumero());
            // 💡 Uso de setBytes() para o BLOB/MEDIUMBLOB.
			ps.setBytes(2, f.getConteudo()); 

			nRows = ps.executeUpdate(); 
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao salvar a fotografia: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}
    
    // -----------------------------------------------------------------------------------------------------------------
	// 🔍 Método GET (Consultar Fotografia pela Chave Primária: Número do Aluno)
    // -----------------------------------------------------------------------------------------------------------------
    
    public static Foto getByNumero(int numero) {
        // 🚦 Validação da Chave
        if (numero <= 0)
            return null;
            
        // 📜 Seleciona o conteúdo binário com base no número.
        String cmd = "SELECT conteudo FROM foto WHERE numero = ?";
        Foto foto = null;
        int nRows = 0;
        
        // System.out.println("Executa a instrução SQL: [" + cmd.replace("?", String.valueOf(numero)) + "]");
        
        try (Connection con = new Configura().getConnection();
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
            ps.setInt(1, numero); // 🔑 Parâmetro da PK
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Se encontrou um registo.
                    
                    // 💾 Leitura dos dados binários com getBytes().
                    byte[] fotoBytes = rs.getBytes("conteudo"); 
                    
                    // 🏗️ Preenche o objeto Foto (incluindo o número que já conhecemos).
                    foto = new Foto(numero, fotoBytes);
                    nRows = 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ ERRO ao consultar a fotografia: " + e.getMessage());
        }
        
        System.out.println("(foto - "+numero+") Linhas afetadas: " + nRows);
        return foto;
    }

    // -----------------------------------------------------------------------------------------------------------------
	// 📝 Método UPDATE (Atualizar o Conteúdo Binário)
    // -----------------------------------------------------------------------------------------------------------------
    // É o método mais comum para fotos, substituindo o conteúdo existente.

	public static int update(Foto f) {
        // 🚦 Validação
		if (f == null || !f.valid()) 
			return -1;
            
		// 📜 Atualiza o campo 'conteudo'.
		String cmd = "UPDATE foto SET conteudo = ? WHERE numero = ?";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			// SET: Novo Conteúdo (índice 1)
            ps.setBytes(1, f.getConteudo());
            
			// WHERE: Chave Primária (índice 2)
			ps.setInt(2, f.getNumero());

			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao atualizar o conteúdo da foto: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}
    
    // -----------------------------------------------------------------------------------------------------------------
	// 🗑️ Método DELETE (Apagar Fotografia)
    // -----------------------------------------------------------------------------------------------------------------
    // Recebe apenas o número do aluno (PK).

	public static int delete(int numero) {
        // 🚦 Validação
		if (numero <= 0)
			return -1;
            
		// 📜 Elimina usando a Chave Primária.
		String cmd = "DELETE FROM foto WHERE numero = ?";
		int nRows = -1;
        
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
		try (Connection con = new Configura().getConnection(); 
             PreparedStatement ps = con.prepareStatement(cmd)) {
                
			ps.setInt(1, numero); // WHERE: Número do Aluno
            
			nRows = ps.executeUpdate();
            
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao eliminar a fotografia: " + e.getMessage());
		}
        
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}
    
    // -----------------------------------------------------------------------------------------------------------------
	// 🌐 Método GET ALL (Consultar Todas as Fotografias)
    // -----------------------------------------------------------------------------------------------------------------
    // ⚠️ Aviso: Este método pode causar problemas de memória se a tabela for grande.

    public static List<Foto> getAll() {
        List<Foto> list = new ArrayList<>(); 
        
        // 📜 Seleciona a PK e o Conteúdo.
        String cmd = "SELECT numero, conteudo FROM foto ORDER BY numero";
        int nRows = 0; 
        
        System.out.println("Executa a instrução SQL: [" + cmd + "]");
        
        try (Connection con = new Configura().getConnection();
             PreparedStatement ps = con.prepareStatement(cmd);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) { 
                // ⚠️ Carrega o array de bytes para a memória para cada foto.
                Foto f = new Foto(
                    rs.getInt("numero"),
                    rs.getBytes("conteudo") 
                );
                list.add(f);
                nRows += 1;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERRO ao consultar todas as fotografias: " + e.getMessage());
        }
        
        System.out.println("Linhas afetadas: " + nRows);
        return list; 
    }
    /**
	 * Atualiza a foto de um aluno na tabela das fotos
	 * 
	 * @param input		stream para acessso à fotografia
	 * @param numero	do aluno
	 * @return			true se correr bem
	 */
	public static boolean setFoto(InputStream input, BigDecimal numero, Connection conn) {
		boolean status=false;
		// deve usar transação e podia usar REPLACE
		try (Statement stm = conn.createStatement();) {
			status = (stm.executeUpdate("DELETE FROM FOTO WHERE numero = " + numero)<=1);
		} catch (SQLException e) {
			System.out.println("Foto Delete Exception: "+e.getMessage());
		}
		if(status) {
			String SQL = "INSERT INTO FOTO (numero, conteudo) VALUES (?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(SQL);){
				// set parameters
				pstmt.setBigDecimal(1, numero);
				pstmt.setBinaryStream(2, input);
				// store in database
				status=(pstmt.executeUpdate() == 1);
			} catch (SQLException e) {
				System.err.println("Foto Insert Prepared Statement: SQL Exception!");
				System.out.println(e.getMessage());
				status=false;
			}
		}
		else {
			System.out.println("Não conseguiu apagar a fotografia!");
		}
	return status;
	}
}