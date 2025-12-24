package disciplina;

import java.util.*;

import util.Configura;

import java.sql.*;

public class DiscDAO {

	public static int save(Disc d) {
		if (d == null || !d.valid())
			return -1;
		String cmd = "INSERT INTO disciplina (codigo, designacao) VALUES (?, ?)";
		int nRows = -1;
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
		try (Connection con = new Configura().getConnection(); PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, d.getCodigo().replaceAll("'", "''"));
			ps.setString(2, d.getDesignacao().replaceAll("'", "''"));
			nRows = ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao inserir a disciplina: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

	public static int update(Disc d, Disc o) {
		if (d == null || !d.valid())
			return -1;
		if (o == null || !o.valid())
			return -1;
		String cmd = "UPDATE disciplina SET codigo = ? , designacao = ? WHERE codigo= ? AND designacao = ?";
		int nRows = -1;
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
		try (Connection con = new Configura().getConnection(); PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, d.getCodigo().replaceAll("'", "''"));
			ps.setString(2, d.getDesignacao().replaceAll("'", "''"));
			ps.setString(3, o.getCodigo().replaceAll("'", "''"));
			ps.setString(4, o.getDesignacao().replaceAll("'", "''"));
			nRows = ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao atualizar a disciplina: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

	public static int delete(Disc d) {
		if (d == null || !d.valid())
			return -1;
		String cmd = "DELETE FROM disciplina WHERE codigo= ? AND designacao = ?";
		int nRows = -1;
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
		try (Connection con = new Configura().getConnection(); PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, d.getCodigo().replaceAll("'", "''"));
			ps.setString(2, d.getDesignacao().replaceAll("'", "''"));
			nRows = ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao apagar a disciplina: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return nRows;
	}

	public static Disc getByCodigo(String codigo) {
		if (codigo == null || codigo.isEmpty()) 
			return null;
		String cmd = "SELECT codigo, designacao FROM disciplina WHERE codigo= ?";
		Disc d = null;
		int nRows = -1;
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
		try (Connection con = new Configura().getConnection();
				PreparedStatement ps = con.prepareStatement(cmd)) {
			ps.setString(1, codigo);
			try (ResultSet rs = ps.executeQuery()) {
				nRows=0;
				if (rs.next()) {
					d = new Disc(rs.getString("codigo"), rs.getString("designacao"));
					nRows=1;
				}
			}
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao consultar a disciplina: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return d;
	}

	public static List<Disc> getAll() {
		List<Disc> list = new ArrayList<Disc>();
		String cmd = "SELECT codigo, designacao FROM disciplina ORDER BY codigo";
		int nRows = -1;
		System.out.println("Executa a instrução SQL: [" + cmd + "]");
		try (Connection con = new Configura().getConnection();
				Statement ps = con.createStatement();
				ResultSet rs = ps.executeQuery(cmd)) {
			nRows=0;
			while (rs.next()) {
				list.add(new Disc(rs.getString("codigo"), rs.getString("designacao")));
				nRows+=1;
			}
		} catch (SQLException e) {
			System.err.println("❌ ERRO ao consultar as disciplinas: " + e.getMessage());
		}
		System.out.println("Linhas afetadas: " + nRows);
		return list;
	}
}
