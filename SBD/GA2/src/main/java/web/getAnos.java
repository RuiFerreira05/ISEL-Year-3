package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.Configura;

/**
 * Devolve a lista de anos associados a inscrições (sem nota) 
 */
@WebServlet("/getAnos")
public class getAnos extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public getAnos() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String numero=request.getParameter("Numero");
		String designacao=request.getParameter("Designacao");
		if(designacao==null || designacao.isEmpty() || numero==null || numero.isEmpty())
			return;
		String select="SELECT DISTINCT ANO FROM INSCRICAO "+
											"WHERE CODIGO=(SELECT CODIGO FROM DISCIPLINA WHERE DESIGNACAO= ?) AND "+
												  "NUMERO = ? AND "+
											      "NOTA IS NULL "+
											"ORDER BY ANO";
		String data="";
		try (Connection con = new Configura().getConnection(); 
			 PreparedStatement ps = con.prepareStatement(select)) { 
			ps.setString(1, designacao);
			ps.setString(2, numero);
		 
			try (ResultSet rs = ps.executeQuery()) {
				while (rs!=null && rs.next()) 
					data+="<option value='"+rs.getString("ANO")+"'/>";
			}
			
		} catch (SQLException e) {
			System.err.println(e.toString());
		}
		response.setContentType("text/html; charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.println(data);
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
