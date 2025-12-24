package gestor;

import java.math.BigDecimal;
import util.IOx;
import util.DataFormatter;

/**
 * Lida com a interação com o utilizador (prompts) e a validação dos dados de entrada
 * (regras de negócio simples: tamanho, intervalo de valores, formato).
 * Utiliza Console para as operações básicas de I/O.
 */
final public class Input {

    // --- Métodos de Validação de Disciplina ---

    /**
     * Pergunta ao utilizador o ano de funcionamento da disciplina (max 4 dígitos).
     * @return O ano de funcionamento.
     */
    public static Integer getCAnoDis() {
        String a = null;
        Integer ano = null;
        do {
            System.out.println("Indique o ano de funcionamento da disciplina:");
            a = IOx.in().trim();
            if(a.isEmpty()) continue;
            
            try {
                ano = Integer.parseInt(a);
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Formato do ano inválido. Use apenas números inteiros.");
                ano = null; 
                continue;
            }
            
            if (a.length() > 4) {
                System.out.println("❌ Erro: O ano deve ter no máximo 4 dígitos.");
                ano = null;
            }
        } while (ano == null);
        return ano;
    }

    /**
     * Pergunta ao utilizador o código da disciplina (max 4 caracteres).
     * @return Código da disciplina.
     */
    public static String getCCodDis() {
        String codigo = null;
        do {
            System.out.println("Indique o código da disciplina (max. 4 caracteres):");
            codigo = IOx.in().trim();
            if (codigo.length() > 4 || codigo.isEmpty()) {
                System.out.println("❌ Erro: O código deve ter entre 1 e 4 caracteres.");
                codigo="";
            }
        } while (codigo.length() > 4);
        return codigo;
    }

    /**
     * Pergunta ao utilizador a designação da disciplina (max 60 caracteres).
     * @return a designação da disciplina.
     */
    public static String getCDsgDis() {
        String designacao = null;
        do {
            System.out.println("Indique a nova designação (max. 60 caracteres):");
            designacao = IOx.in().trim();
            if (designacao.length() > 60 || designacao.isEmpty()) {
                System.out.println("❌ Erro: A designação deve ter entre 1 e 60 caracteres.");
            }
        } while (designacao.length() > 60);
        return designacao;
    }
    
    // --- Métodos de Validação de Aluno e Nota ---

    /**
     * Pergunta ao utilizador o número do aluno (max 5 dígitos).
     * @return Número do aluno.
     */
    public static Integer getCNmrAluno() {
        String num = null;
        Integer numero = null;
        do {
            System.out.println("Indique o número do aluno (max. 5 digitos):");
            num = IOx.in().trim();
            if(num.isEmpty()) continue;

            try {
                numero = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Formato do número de aluno inválido. Use apenas números inteiros.");
                numero = null;
                continue;
            }
            
            if (num.length() > 5) {
                System.out.println("❌ Erro: O número de aluno deve ter no máximo 5 dígitos.");
                numero = null;
            }
        } while (numero == null);
        return numero;
    }

    /**
     * Pergunta ao utilizador o nome do aluno (max 100 caracteres).
     * @return o nome do aluno.
     */
    public static String getCNmAluno() {
        String nome = null;
        do {
            System.out.println("Indique o novo nome (max. 100 caracteres):");
            nome = IOx.in().trim();
            if (nome.length() > 100 || nome.isEmpty()) {
                System.out.println("❌ Erro: O nome deve ter entre 1 e 100 caracteres.");
            }
        } while (nome.length() > 100 || nome.isEmpty());
        return nome;
    }

    /**
     * Pergunta ao utilizador o género do aluno.
     * @return género do aluno ('m', 'f' ou 'x').
     */
    public static String getCGnrAluno() {
        String genero = null;
        do {
            System.out.println("Indique o género do aluno (M/F/X):");
            genero = IOx.in().trim().toLowerCase();

            if (genero.length() == 1) {
                char g = genero.charAt(0);
                if (g == 'm' || g == 'f' || g == 'x') {
                    break;
                }
            }
            System.out.println("❌ Erro: Género inválido. Use 'M', 'F' ou 'X'.");
        } while (true);
        return genero;
    }

    /**
     * Pergunta ao utilizador a data de nascimento do aluno.
     * @return data de nascimento do aluno.
     */
    public static java.sql.Date getCNascAluno() {
        String nascido = null;
        java.sql.Date parsed = null;
        do {
            System.out.println("Indique a data de nascimento do aluno ("
                    + DataFormatter.getInFormato() + "):");
            nascido = IOx.in().trim();
            if(nascido.isEmpty())
            		break;
            parsed = DataFormatter.StringToSqlDate(nascido);
            if(parsed==null)
            		System.out.println("❌ Esperado a data no formato: " + DataFormatter.getInFormato()+"!");
        } while (true);
        return parsed;
    }

    /**
     * Pergunta ao utilizador a nota do aluno [0.0...20.0].
     * Utiliza BigDecimal.compareTo() para comparação segura.
     * @return A nota do aluno.
     */
    public static BigDecimal getCNotaDis() {
        String nt = null;
        BigDecimal nota = null;
        do {
            System.out.println("Indique a nota [0.0...20.0]:");
            nt = IOx.in().trim();
            
            // Validação preliminar do comprimento máximo da string (ex: 18.5)
            if (nt.isEmpty() || nt.length() > 4) {
                 if (!nt.isEmpty()) System.out.println("❌ Erro: Formato ou tamanho da nota inválido.");
                 nota = null;
                 continue; 
            }

            try {
                nota = new BigDecimal(nt);
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Formato da nota inválido. Use formato numérico (ex: 12.5).");
                nota = null; 
                continue;
            }
            
            // Validação do intervalo de valores (0.0 a 20.0)
            if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.valueOf(20.0)) > 0) {
                System.out.println("❌ Erro: A nota deve estar entre 0.0 e 20.0.");
                nota = null;
            }
            
        } while (nota == null);
        return nota;
    }
}