package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import aluno.*;

/**
 * Devolve os dados do aluno em forma de texto 
 */
@WebServlet("/getAluno")
public class getAluno extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public getAluno() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String numero=request.getParameter("Numero");
		if(numero==null || numero.compareTo("")==0)
			return;
		Aluno aluno = AlunoDAO.getByNumero(numero);
		response.setContentType("text/html; charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			if(aluno!=null && aluno.valid()) {
				String[] result = util.Data.saber(aluno.getNascido()).split(",");
				out.println(aluno.getNomeS()+". "+result[0]+","+result[1]+","+result[2]+result[4]);
			}
			else
				out.println("O estudante Nº "+numero+" não foi encontrado!");
			}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
