package web;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import util.Manipula;
import util.Configura;
import util.DataFormatter;
import foto.Foto;
import foto.FotoDAO;

@WebServlet("/AlunoUp")
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 10,  		// 10 MB
		maxFileSize = 1024 * 1024 * 50, 				// 50 MB
		maxRequestSize = 1024 * 1024 * 100) 			// 100 MB
public class AlunoUpload extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Se correr bem, devolve o numero senão devolve null
	private String AtualizaAluno(HttpServletRequest request, Connection conexao) throws SQLException {
		String comando=request.getParameter("Comando");
		if(comando==null) 
			return null;
		String numero=request.getParameter("Numero");
		String nome=request.getParameter("Nome");
		String genero=request.getParameter("Genero");
		String nascido=request.getParameter("Data");
		String nm=numero;
		if(comando.compareTo("I")==0){
			if(nm==null || nm.compareTo("")==0)
				nm=Manipula.getString(conexao, "select coalesce(max(numero), 0)+1 from aluno");
			String directiva = 
			"insert into aluno (numero, nome, genero, nascido) values ("+
					nm +",'"+ 
					nome.trim().replaceAll("'", "''") +"','" + 
					genero + "','" + 
					nascido + "')";			
			System.out.println("Insere: "+directiva);
			if (Manipula.xDirectiva(conexao, directiva)==1)
				return nm;
		} else // fim do insert

		if(comando.compareTo("U")==0){ 
			// verifica antes de fazer a actualização se o registo se mantém, reforçando o WHERE
			String numeroOld=request.getParameter("NumeroOld");
			String nomeOld=request.getParameter("NomeOld");
			String generoOld=request.getParameter("GeneroOld");
			String nascidoOld=request.getParameter("DataOld");
 			nome=nome.trim().replaceAll("'", "''");
			nomeOld=nomeOld.replaceAll("'", "''");
			String directiva = "update aluno set "
					+ "numero="+numero+", " 
					+ "nome='"+nome+"', "
				    + "genero='"+genero+"', "
				    + "nascido='"+nascido+"'"
					+ " where "
					+ DataFormatter.igual("numero", numeroOld)+" AND " 
					+ DataFormatter.igual("nome", nomeOld)+" AND "
				    + DataFormatter.igual("genero", generoOld)+" AND "
				    + DataFormatter.igual("nascido", nascidoOld);
			System.out.println("Atualiza: "+directiva);
			if (Manipula.xDirectiva(conexao, directiva)==1)
				return nm;
		}  // fim da alteração
		
		return null;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException	 {
		String num=null;
		String msg=null;
		String comando=request.getParameter("Comando");
		// Procura por foto semelhante
		if(comando!=null && comando.compareTo("K")==0) {
			String FotoBase64 = request.getParameter("FotoBase64");
			String numero=request.getParameter("Numero");
			if(FotoBase64==null || FotoBase64.length()==0) {
				if(numero!=null && numero.compareTo("")!=0) {
					Foto foto = FotoDAO.getByNumero(Integer.parseInt(numero));
					FotoBase64 = foto.getConteudo64();
					System.out.println("Foi buscar a fotografia à base de dados! "+FotoBase64.substring(0,20));
				} 
			} else {
				FotoBase64=FotoBase64.substring(FotoBase64.indexOf(',')+1);
				System.out.println("Utilizou a foto passada em parametro! "+FotoBase64.substring(0,20));
			}
			if(FotoBase64==null || FotoBase64.length()<=0) {
				msg="Nada para fazer!";
			} else {
				if(numero==null || numero.length()==0) {
					numero="0";
				}
				// vai percorrer as fotos e faz a comparação
				// primeiro procura até ao fim e depois volta ao inico
				String sql = "SELECT * FROM ("+
									"SELECT NUMERO, 1 FROM FOTO WHERE NUMERO > "+numero+
										" UNION "+
									"SELECT NUMERO, 2 FROM FOTO WHERE NUMERO < "+numero+") T ORDER BY 2";

				boolean found=false;
				try (Connection con = new Configura().getConnection(); 
			         Statement st = con.createStatement();
					 ResultSet rs = st.executeQuery(sql)){
					while (rs.next()) {
						int nm=rs.getInt("NUMERO");
						Foto foto = FotoDAO.getByNumero(nm);
						String Foto = foto.getConteudo64();
						if(Foto.compareToIgnoreCase(FotoBase64)==0) {
							num=String.valueOf(nm);
							msg="Foi encontrada a fotografia do estudante número "+num+"!";
							found=true;
							break;
						}
					}
					if(!found)
						msg="Não encontrou a fotografia!";
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(comando!=null && (comando.compareTo("I")==0 || comando.compareTo("U")==0) ) {
			num = request.getParameter("Numero");
			if ((num == null || num.compareTo("") == 0) && comando.compareTo("U")==0)
				System.out.println("Número do Aluno Inválido!");
			else {
				boolean status=false;
				try (Connection con = new Configura().getConnection(false)) { // inicia transação 
					num=AtualizaAluno(request, con); // processa os dados do aluno
					status=(num!=null);
					if(status) {  // processa a foto
						String FotoBase64 = request.getParameter("FotoBase64");
						if(FotoBase64!=null && FotoBase64.length()>0) {
							// remove a indicação de tipo base64
							FotoBase64 = FotoBase64.substring(FotoBase64.indexOf(',')+1);
							Foto foto  = new Foto();
							foto.setNumero(Integer.parseInt(num));
							foto.setConteudo64(FotoBase64);
							status=(FotoDAO.save(foto, con)==1);
						} else {	
							Part filePart = request.getPart("fotoInput");
							if (filePart != null && filePart.getSize() != 0) {
								// prints out some information for debugging
								System.out.println("Parametro:" + filePart.getName());
								System.out.println("Ficheiro:" + filePart.getSubmittedFileName());
								System.out.println("Dimensão:" + filePart.getSize());
								System.out.println("Tipo:" + filePart.getContentType());
								// obtains input stream of the upload file
								try (InputStream input = filePart.getInputStream()) {
									status=FotoDAO.setFoto(input, new BigDecimal(num),con);
									}
								}
							}
					}
					if(status) {
						con.commit();
						System.out.println("Sucesso: Aluno & Foto "+(num==null?"":"nº"+num)+"!");
						msg="O registo do aluno foi atualizado/criado com sucesso!";
					}
					else {
						System.out.println("Falhou: Aluno & Foto nº"+(num==null?"":"nº"+num)+"!");
						msg="A atualização/criação do registo do aluno falhou...!";
						con.rollback();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		request.setAttribute("numero",num);
		request.setAttribute("mensagem",msg);
		request.getRequestDispatcher("alunos.jsp").include(request, response);	
	}
}