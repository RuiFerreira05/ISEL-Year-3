package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Configura;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Devolve lista (a usar num select) das disciplinas com inscrições (sem nota)  
 */
@WebServlet("/getDisciplinas")
public class getDisciplinas extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public getDisciplinas() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String numero=request.getParameter("Numero");
		if(numero==null || numero.isEmpty())
			return;
		System.out.println("numero: "+numero);
		String select = 	"SELECT DISTINCT DESIGNACAO FROM INSCRICAO I, DISCIPLINA D "+
						    "WHERE I.CODIGO=D.CODIGO AND NUMERO=? AND NOTA IS NULL ORDER BY 1";
		String data="";
		try (Connection conexao = new Configura().getConnection();
		     PreparedStatement preparacao = conexao.prepareStatement(select);
			 PrintWriter out = response.getWriter()){
		    	 preparacao.setInt(1, Integer.parseInt(numero));
		    	 try (ResultSet rs = preparacao.executeQuery()) {
				while (rs!=null && rs.next()) 
					data+="<option value='"+rs.getString(1).replaceAll("'","&#39;")+"'/>";
		    	 }
		    	 response.setContentType("text/html; charset=UTF-8");
		    	 out.println(data);
		} catch (Exception e) {
            // ⛔ Tratamento de qualquer outra exceção genérica (ex: IO Exception no stream)
            System.out.println("❌ Erro: Falha ao obter a lista das disciplinas com inscrições sem nota!");
			System.err.println("Message:  " + e.getMessage());
		} 
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
