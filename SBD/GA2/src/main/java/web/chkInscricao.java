package web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import inscricao.*;

/**
 * Devolve mensagem relativa à inscrição estar 'duplicada' 
 */
@WebServlet("/chkInscricao")
public class chkInscricao extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public chkInscricao() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String numero=request.getParameter("Numero");
		String codigo=request.getParameter("Codigo");
		String ano=request.getParameter("Ano");
		if(numero==null || numero.compareTo("")==0 || codigo==null || codigo.compareTo("")==0 || ano==null || ano.compareTo("")==0)
			return;
		System.out.println("Inscrição do Nº: "+numero);
		Inscricao inscricao = InscricaoDAO.getByPK(Integer.parseInt(numero), codigo, Short.parseShort(ano));
		String msg="";
		if (inscricao!=null) {
			System.out.println("Inscrição duplicada!");
			msg = "A inscrição já existe!";
		}
		response.setContentType("text/html; charset=UTF-8");
		try(PrintWriter out = response.getWriter()) {
			out.println(msg);
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
