package util;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 🚀 Classe Utilitária IO
 * Lida exclusivamente com a leitura e escrita para a Consola (Standard I/O).
 * Esta classe foi desenhada para ser robusta, tratando erros de formato 
 * e garantindo que o programa não termina inesperadamente devido a inputs inválidos.
 */
final public class IOx {

    // 📖 Leitor de buffer para entrada de dados eficiente
    private static final BufferedReader br;
    
    // 🌐 Stream de saída opcional (útil para integração com ambientes Web/Servlets)
    private static PrintWriter streamOut = null; 

    // 📅 Formatadores de data padrão seguindo o padrão Europeu (dia/mês/ano)
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ⚙️ Bloco estático para inicializar o leitor de sistema uma única vez
    static {
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception exp) {
            System.err.println("❌ Erro fatal: Não foi possível aceder ao Standard Input.");
            throw new RuntimeException(exp); 
        }
    }

    /**
     * 🔒 Fecha o BufferedReader para libertar os recursos de sistema.
     */
    public static void close() {
        try {
            if (br != null)
                br.close();
        } catch (IOException e) {
            System.err.println("❌ Erro ao fechar o fluxo de entrada: " + e.getMessage());
        }
    }

    /**
     * 🔗 Define um stream de saída alternativo (ex: para capturar logs num ficheiro ou browser).
     */
    public static void setOutStream(PrintWriter p) {
        streamOut = p;
    }

    /**
     * 📥 Lê uma linha completa de texto do teclado.
     * @return A string lida ou uma string vazia em caso de erro de I/O.
     */
    public static String in() {
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException exp) {
            System.err.println("❌ Erro na leitura do input: " + exp.getMessage());
            return ""; 
        }
        return (line != null) ? line : "";
    }

    /**
     * 🔢 Lê um número inteiro (int) da consola.
     * @return O número inteiro ou 0 se o formato for inválido.
     */
    public static int inInt() {
        String str = IOx.in().trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Aviso: '" + str + "' não é um número inteiro válido. Retornando 0.");
            return 0;
        }
    }

    /**
     * 📏 Lê um número decimal (float) da consola.
     * 💡 Suporta tanto o ponto (.) como a vírgula (,) como separador.
     * @return O valor float convertido ou 0.0f se houver erro.
     */
    public static float inFloat() {
        String str = IOx.in().trim().replace(',', '.');
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Aviso: '" + str + "' não é um número decimal válido. Retornando 0.0.");
            return 0.0f;
        }
    }

    /**
     * 💰 Lê um número de dupla precisão (double) da consola.
     * 💡 Suporta tanto o ponto (.) como a vírgula (,) como separador.
     * @return O valor double convertido ou 0.0 se houver erro.
     */
    public static double inDouble() {
        String str = IOx.in().trim().replace(',', '.');
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Aviso: '" + str + "' não é um double válido. Retornando 0.0.");
            return 0.0;
        }
    }

    /**
     * 📅 Lê uma data (LocalDate) no formato dd/mm/aaaa.
     * @return O objeto LocalDate lido ou a data atual (hoje) se o formato estiver errado.
     */
    public static LocalDate inDate() {
        String str = IOx.in().trim();
        try {
            return LocalDate.parse(str, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Erro: Data inválida. Use dd/mm/aaaa. Retornando hoje.");
            return LocalDate.now();
        }
    }

    /**
     * 🕒 Lê data e hora (LocalDateTime) no formato dd/mm/aaaa hh:mm.
     * @return O objeto LocalDateTime lido ou o momento atual se o formato estiver errado.
     */
    public static LocalDateTime inDateTime() {
        String str = IOx.in().trim();
        try {
            return LocalDateTime.parse(str, DATETIME_FMT);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Erro: Data/Hora inválida. Use dd/mm/aaaa hh:mm. Retornando agora.");
            return LocalDateTime.now();
        }
    }

    /**
     * 🔤 Lê o primeiro caracter de uma linha (útil para menus S/N).
     * @return O caracter lido em minúscula ou um espaço se vazio.
     */
    public static char inChar() {
        String str = IOx.in().trim().toLowerCase();
        if (str.length() > 0) {
            return str.charAt(0);
        }
        return ' ';
    }

    /**
     * 📤 Escreve uma linha de texto no Standard Output (e no stream opcional, se existir).
     * @param line Texto a exibir.
     */
    public static void out(String line) {
        if (line != null) {
            System.out.println(line);
            if(streamOut != null) {
                streamOut.println("<pre>" + line + "</pre>"); 
            }
        }
    }
    
    /**
     * ❓ Solicita uma entrada de texto, repetindo o pedido se a entrada estiver vazia.
     * @param prompt Mensagem a exibir ao utilizador.
     * @return String de input validada (não vazia).
     */
    public static String input(String prompt) {
        String input;
        do {
            System.out.println(prompt);
            input = IOx.in().trim();
            if (input.isEmpty()) {
                System.out.println("⚠️ Atenção: Este campo é obrigatório. Tente novamente.");
            }
        } while (input.isEmpty());
        return input;
    }
}