package util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 🎓 **SISTEMA DE CALENDÁRIO PORTUGUÊS**
 * 🏗️ **FUNCIONALIDADES:**
 * - 🎨 Modelos Temáticos: Luxo, Gótico, Celta, Culinária, Hotelaria, Veterinária, etc.
 * - 🇵🇹 Feriados nacionais, móveis e municipais (Lisboa, Porto, Braga, Sintra, Évora).
 * - 📅 Cálculos automáticos de Páscoa, Carnaval e Corpo de Deus.
 * - 💼 Listagem de dias úteis e próximos eventos.
 * * @author Prof. Porfírio Filipe
 */
public class Calendario {

	// Define uma constante Locale para evitar new Locale("pt", "PT") repetido
    private static final Locale LOCALE_PT = Locale.forLanguageTag("pt-PT");
    
    // 📅 Formatadores de data para exibição por extenso e leitura de inputs
    private static final DateTimeFormatter FORMATO_EXTENSO = 
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' uuuu", LOCALE_PT);

    private static final DateTimeFormatter FORMATO_INPUT = 
        DateTimeFormatter.ofPattern("d/M/yyyy");
    
    private static final DateTimeFormatter FORMATO_CURTO = 
    	    DateTimeFormatter.ofPattern("dd/MM");
    

    // 📍 Regiões configuradas para feriados municipais
    public enum Regiao { LISBOA, PORTO, BRAGA, SINTRA, EVORA }

    // --- ⚙️ LÓGICA DE DATAS ---

    /** 🏖️ Verifica se o dia é Sábado ou Domingo */
    public static boolean fimDeSemana(LocalDate data) {
        return data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** 🏛️ Valida feriados específicos de cada concelho */
    public static boolean feriadoMunicipal(LocalDate data, Regiao regiao) {
        if (regiao == null) return false;
        if (regiao == Regiao.LISBOA) return data.getMonth() == Month.JUNE && data.getDayOfMonth() == 13;
        if (regiao == Regiao.PORTO || regiao == Regiao.BRAGA) return data.getMonth() == Month.JUNE && data.getDayOfMonth() == 24;
        if (regiao == Regiao.SINTRA || regiao == Regiao.EVORA) return data.getMonth() == Month.JUNE && data.getDayOfMonth() == 29;
        return false;
    }

    /** 📑 Retorna a descrição do feriado (Nacional ou Local) */
    public static String getDescricaoCompleta(LocalDate data) {
        // Feriados Nacionais Fixos
        if (data.getMonth() == Month.JANUARY && data.getDayOfMonth() == 1) return "🥂 Ano Novo";
        if (data.getMonth() == Month.APRIL && data.getDayOfMonth() == 25) return "🌹 Dia da Liberdade";
        if (data.getMonth() == Month.MAY && data.getDayOfMonth() == 1) return "🛠️ Dia do Trabalhador";
        if (data.getMonth() == Month.JUNE && data.getDayOfMonth() == 10) return "🇵🇹 Dia de Portugal";
        if (data.getMonth() == Month.AUGUST && data.getDayOfMonth() == 15) return "☁️ Assunção";
        if (data.getMonth() == Month.OCTOBER && data.getDayOfMonth() == 5) return "🏛️ Implantação República";
        if (data.getMonth() == Month.NOVEMBER && data.getDayOfMonth() == 1) return "🕯️ Todos os Santos";
        if (data.getMonth() == Month.DECEMBER && data.getDayOfMonth() == 1) return "⚔️ Restauração";
        if (data.getMonth() == Month.DECEMBER && data.getDayOfMonth() == 8) return "👑 Imaculada Conceição";
        if (data.getMonth() == Month.DECEMBER && data.getDayOfMonth() == 25) return "🎄 Natal";

        // Feriados Móveis (baseados na Páscoa)
        LocalDate pascoa = getDomingoPascoa(data.getYear());
        if (data.equals(pascoa.minusDays(47))) return "🎭 Carnaval";
        if (data.equals(pascoa.minusDays(2))) return "🙏 Sexta Santa";
        if (data.equals(pascoa)) return "🐣 Páscoa";
        if (data.equals(pascoa.plusDays(60))) return "🍷 Corpo de Deus";

        // Agregação de feriados municipais
        List<String> locais = new ArrayList<>();
        for (Regiao r : Regiao.values()) if (feriadoMunicipal(data, r)) locais.add(r.name());
        return locais.isEmpty() ? "" : "🎉 Feriado Local: " + String.join(", ", locais);
    }

    /** 🐣 Cálculo matemático para determinar o Domingo de Páscoa */
    public static LocalDate getDomingoPascoa(int ano) {
        int a = ano % 19, b = ano / 100, c = ano % 100, d = b / 4, e = b % 4, f = (b + 8) / 25, 
            g = (b - f + 1) / 3, h = (19 * a + b - d - g + 15) % 30, i = c / 4, k = c % 4, 
            l = (32 + 2 * e + 2 * i - h - k) % 7, m = (a + 11 * h + 22 * l) / 451, 
            n = (h + l - 7 * m + 114) / 31, p = (h + l - 7 * m + 114) % 31;
        return LocalDate.of(ano, n, p + 1);
    }

    // --- 💾 EXPORTAÇÃO E DESIGN ---
    
    /**
     * ✍️ **MÉTODO PRIVADO AUXILIAR**
     * Escreve o conteúdo HTML do calendário para um PrintWriter.
     * Esta lógica é partilhada entre a geração para ficheiro e a geração para String.
     */
    private static void appendCalendarContent(PrintWriter w, int ano, int opcaoModelo) {
        String[] nomes = {"", "luxo", "premium", "purpura", "impressao", "terminal", "gotico", "celta", "ascii","culinaria","hotelaria","veterinaria"};
        String[] emojis = {"", "👑", "✨", "🔮", "🖨️", "💻", "🏰", "🍀", "👾","👨‍🍳","🛎️","🐾"};
        
        // Ícones específicos para cada tema
        String[] iconesGotico = {"", "💀", "🐺", "🌑", "🕯️", "⚔️", "🏰", "🍷", "🦇", "📜", "🏹", "🗡️", "⛓️"};
        String[] iconesCelta = {"", "🌑", "⚔️", "🐺", "🏰", "🛡️", "🏹", "📜", "🦅", "🕯️", "🌲", "🏔️", "🔥"};
        String[] iconesCulinaria = {"", "🍊", "🍫", "🥦", "🥗", "🍓", "🍒", "🍦", "🍉", "🍇", "🎃", "🌰", "🍪"};
        String[] iconesHotelaria = {"", "🔑", "🛌", "🧖", "☂️", "🍹", "🏊", "🚿", "🌅", "🛎️", "🍷", "🛋️", "🎁"};
        String[] iconesVeterinaria = {"", "🐱", "🐶", "🐰", "🐥", "🐣", "🦋", "🐠", "🦎", "🦜", "🐕", "🐈", "🦌"};

        String modelo = (opcaoModelo > 0 && opcaoModelo < nomes.length) ? nomes[opcaoModelo] : "impressao";
        String emojiModelo = (opcaoModelo > 0 && opcaoModelo < emojis.length) ? emojis[opcaoModelo] : "🖨️";

        List<LocalDate> feriadosEncontrados = new ArrayList<>();

        // 1. HEAD e STYLES
        w.println("<!DOCTYPE html><html lang='pt-PT'><head><meta charset='UTF-8'/>");
        
        // Inclusão de fontes
        if(modelo.equals("gotico")) {
            w.println("<link href='https://fonts.googleapis.com/css2?family=UnifrakturMaguntia&display=swap' rel='stylesheet'>");
        } else if(modelo.equals("celta") || modelo.equals("purpura") || modelo.equals("luxo")) {
            w.println("<link href='https://fonts.googleapis.com/css2?family=Cinzel+Decorative:wght@700&family=Eagle+Lake&family=Orbitron&display=swap' rel='stylesheet'>");
        }
        
        w.println("<style>");
        // Regras base
        w.println(".grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 20px; padding: 20px; text-align: center; }");
        w.println(".mes { padding: 15px; border-radius: 12px; } table { width: 100%; border-collapse: collapse; } td { padding: 10px; border: 1px solid rgba(128,128,128,0.1); }");
        w.println(".feriado-mun { opacity: 0.6; filter: brightness(0.85); border: 1px dashed rgba(0,0,0,0.3) !important; }");
        w.println("body { margin: 0; padding: 0; background: #fff; color: #000; font-family: sans-serif; }");

        // Regras CSS específicas por modelo (replicadas do original)
        switch(modelo) {
        	case "luxo":
                w.println("body { background: #0f0f0f; color: #fff; font-family: 'Cinzel Decorative', serif; }");
                w.println("h1 { color: #D4AF37; text-shadow: 0 0 10px rgba(212,175,55,0.5); }");
                w.println(".mes { background: #1a1a1a; border: 1px solid #D4AF37; box-shadow: 0 5px 15px rgba(212,175,55,0.2); }");
                w.println("h2 { color: #D4AF37; border-bottom: 1px solid #333; }");
                w.println("td { border: 1px solid #333; }");
                w.println(".feriado-nac { background: linear-gradient(45deg, #BF953F, #FCF6BA, #BF953F); color: #000; font-weight: bold; border: 1px solid #D4AF37 !important; }");
                break;
            case "terminal":
                w.println("body { background: #1a1a1a; color: #33ff33; font-family: 'Courier New', monospace; }");
                w.println(".mes { border: 2px solid #33ff33; background: #000; } .feriado-nac { color: #000; background: #33ff33; }");
                break;
            case "gotico":
                w.println("body { background: #0a0a0a; color: #b0b0b0; font-family: 'UnifrakturMaguntia', serif; }");
                w.println("h1 { color: #8b0000; font-size: 3.5em; text-shadow: 2px 2px 5px #000; margin-bottom: 30px; }");
                w.println(".mes { border: 2px solid #333; background: #1a1a1a; box-shadow: 5px 5px 15px #000; }");
                w.println("h2 { color: #8b0000; border-bottom: 1px solid #444; padding-bottom: 5px; font-size: 2em; }");
                w.println(".feriado-nac { background: #4a0000; color: #ff0000; font-weight: bold; border: 1px solid #8b0000 !important; }");
                break;
            case "celta":
                w.println("body { background: #001a33; color: #D4AF37; font-family: 'Eagle Lake', cursive; }");
                w.println("h1 { font-family: 'Cinzel Decorative', serif; }");
                w.println(".mes { background: #002b55; border: 3px double #D4AF37; }");
                w.println("h2 { font-family: 'Cinzel Decorative', serif; }");
                w.println(".feriado-nac { background: #D4AF37; color: #001a33; font-weight: bold; }");
                break;
            case "purpura":
                w.println("body { background: #1a0033; color: #fff; font-family: 'Orbitron', sans-serif; }");
                w.println("h1 { color: #bc13fe; text-shadow: 0 0 10px #bc13fe; }");
                w.println(".mes { background: #2d0059; border: 1px solid #bc13fe; box-shadow: 0 0 15px rgba(188, 19, 254, 0.3); }");
                w.println("h2 { color: #e0aaff; }");
                w.println("td, th { border: 1px solid #fff; }");
                w.println(".feriado-nac { background: #bc13fe; color: #fff; font-weight: bold; border: 1px solid #fff !important; }");
                break;
            case "culinaria":
                w.println("body { background: #fffcf5; color: #5d4037; font-family: 'Brush Script MT', cursive; } .mes { background: #fff; border: 2px solid #ff7043; box-shadow: 8px 8px 0px #ff7043; } .feriado-nac { background: #ffccbc; color: #bf360c; }"); break;
            case "veterinaria":
                w.println("body { background: #e0f2f1; color: #004d40; font-family: 'Comic Sans MS', cursive; } .mes { background: #fff; border: 3px dashed #4db6ac; border-radius: 40px; } .feriado-nac { background: #b2dfdb; }"); break;
            case "hotelaria":
                w.println("body { background: #f4f4f4; color: #2c3e50; font-family: 'Garamond', serif; } .mes { background: #fff; border-top: 5px solid #c5a059; } .feriado-nac { background: #c5a059; color: white; }"); break;
            default:
                // Estilos para "impressao"
                w.println(".mes { border: 1px solid #000; } .feriado-nac { background: #eee; }");
        }

        w.println(".footer-legend { margin-top: 40px; border-top: 1px solid gray; padding: 20px; display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; }");
        w.println(".tag { border: 1px solid gray; padding: 5px 10px; border-radius: 15px; font-size: 0.8em; }");
        w.println("</style></head><body>");
        
        // 2. TÍTULO PRINCIPAL
        String tituloPrincipal = modelo.equals("gotico") ? "ANNO DOMINI " + ano :  
                                 emojiModelo + " CALENDÁRIO " + modelo.toUpperCase() + " " + ano;
        
        w.println("<h1 style='text-align:center;'>" + tituloPrincipal + "</h1><div class='grid'>");

        // 3. CONTEÚDO MÊS A MÊS
        for (int m = 1; m <= 12; m++) {
            LocalDate dataMes = LocalDate.of(ano, m, 1);
            
            // Lógica de prefixo/ícone (sazonal ou temático)
            String prefixo = "";
            if(modelo.equals("gotico")) prefixo = iconesGotico[m] + " ";
            else if(modelo.equals("celta")) prefixo = iconesCelta[m] + " ";
            else if(modelo.equals("culinaria")) prefixo = iconesCulinaria[m] + " ";
            else if(modelo.equals("hotelaria")) prefixo = iconesHotelaria[m] + " ";
            else if(modelo.equals("veterinaria")) prefixo = iconesVeterinaria[m] + " ";
            else if(modelo.equals("purpura") || modelo.equals("luxo")) {
                if (m == 12 || m <= 2) prefixo = "❄️ "; 
                else if (m <= 5) prefixo = "🌸 ";      
                else if (m <= 8) prefixo = "☀️ ";      
                else prefixo = "🍂 ";                 
            }
            
            w.println("<div class='mes'><h2>" + prefixo + dataMes.getMonth().getDisplayName(TextStyle.FULL, LOCALE_PT).toUpperCase() + "</h2>");
            w.println("<table><tr><th>D</th><th>S</th><th>T</th><th>Q</th><th>Q</th><th>S</th><th>S</th></tr><tr>");
            
            int espacos = dataMes.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < espacos; i++) w.println("<td></td>");
            
            while (dataMes.getMonthValue() == m) {
                String desc = getDescricaoCompleta(dataMes);
                String cl = "";
                
                if (!desc.isEmpty()) {
                    cl = desc.contains("Local") ? "feriado-mun" : "feriado-nac";
                    // Garante que o feriado é adicionado à lista apenas uma vez
                    if (!feriadosEncontrados.contains(dataMes)) {
                        feriadosEncontrados.add(dataMes);
                    }
                }
                
                w.println("<td class='" + cl + "' title='" + desc + "'>" + dataMes.getDayOfMonth() + "</td>");
                if (dataMes.getDayOfWeek() == DayOfWeek.SATURDAY) w.println("</tr><tr>");
                dataMes = dataMes.plusDays(1);
            }
            w.println("</tr></table></div>");
        }
        w.println("</div><div class='footer-legend'>");
        
        // 4. LEGENDA DE FERIADOS
        for (LocalDate f : feriadosEncontrados) w.println("<div class='tag'><b>" + f.format(FORMATO_CURTO) + "</b>: " + getDescricaoCompleta(f) + "</div>");
        w.println("</div></body></html>");
    }

    // --- 💾 MÉTODOS PÚBLICOS REFATORADOS ---

    /** 🎨 Gera o ficheiro HTML com o motor de estilos (Refatorado para usar o método privado) */
    public static void gerarCalendarioHTML(int ano, int opcaoModelo) {
        String[] nomes = {"", "luxo", "premium", "purpura", "impressao", "terminal", "gotico", "celta", "ascii","culinaria","hotelaria","veterinaria"};
        String modelo = (opcaoModelo > 0 && opcaoModelo < nomes.length) ? nomes[opcaoModelo] : "impressao";
        String nomeFicheiro = "Calendario_" + modelo + "_" + ano + ".html";

        try (PrintWriter w = new PrintWriter(new FileWriter(nomeFicheiro))) {
            appendCalendarContent(w, ano, opcaoModelo);
            System.out.println("✨ Ficheiro gerado: " + nomeFicheiro);
        } catch (IOException e) { 
            System.out.println("❌ Erro de gravação: " + e.getMessage()); 
        }
    }
    
    /** 🎨 Gera o conteúdo HTML do calendário como uma String (Refatorado para usar o método privado) */
    public static String gerarCalendarioHTMLtoString(int ano, int opcaoModelo) {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        
        try {
            appendCalendarContent(w, ano, opcaoModelo);
            return sw.toString();
        } finally {
            w.close();
        }
    }

    /**
     * Retorna o estado de um dia com emojis e grafia correta (AO90).
     * @param data A data a analisar
     * @return String formatada com emoji e descrição
     */
    public static String getEstadoDia(LocalDate data) {
        // 1. Se for feriado, o método já devolve a descrição com emoji (ex: 🎄 Natal)
        String descricaoFeriado = getDescricaoCompleta(data);
        if (!descricaoFeriado.isEmpty()) {
            return descricaoFeriado;
        }

        // 2. Se for fim de semana, adiciona o emoji de lazer
        if (fimDeSemana(data)) {
            return "🏖️ Fim de semana";
        }

        // 3. Caso contrário, utiliza o emoji de trabalho
        return "💼 Dia útil";
    }
    
    /** 🎮 Interface de consola */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LocalDate hoje = LocalDate.now();

        System.out.println("🇵🇹 **SISTEMA DE CALENDÁRIOS PORTUGAL**");
        System.out.println("📅 Hoje: " + hoje.format(FORMATO_EXTENSO));

        String descHoje = getDescricaoCompleta(hoje);
        if (!descHoje.isEmpty()) System.out.println("🌟 Estado: FERIADO (" + descHoje + ")");
        else if (fimDeSemana(hoje)) System.out.println("🏖️ Estado: Fim de Semana.");
        else System.out.println("💼 Estado: Dia Útil.");

        System.out.println("\n📍 Regiões: " + Arrays.toString(Regiao.values()));

        System.out.println("\n⏭️ Próximos 5 Feriados (Geral):");
        int cF = 0; LocalDate tF = hoje;
        while(cF < 5) {
            tF = tF.plusDays(1);
            String dNac = getDescricaoCompleta(tF);
            StringBuilder dMun = new StringBuilder();
            for(Regiao r : Regiao.values()) { if(feriadoMunicipal(tF, r)) dMun.append("📍 [").append(r).append("] "); }
            if(!dNac.isEmpty() || dMun.length() > 0) {
                System.out.println("   " + (dNac.isEmpty() ? "" : dNac + " ") + dMun + "(" + tF.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
                cF++;
            }
        }

        System.out.println("\n💼 Próximos 10 Dias Úteis:");
        int cU = 0; LocalDate tU = hoje;
        while(cU < 10) {
            tU = tU.plusDays(1);
            if(!fimDeSemana(tU) && getDescricaoCompleta(tU).isEmpty()) {
                System.out.println("   ✅ " + tU.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cU++;
            }
        }
        // ⌨️ Menu interativo para geração de ficheiros
        System.out.print("\n📅 Introduza o Ano do Calendário (ou Enter para " + hoje.getYear() + "): ");
        String anoInput = scanner.nextLine();
        int anoAlvo = anoInput.isBlank() ? hoje.getYear() : Integer.parseInt(anoInput);

        String opt = "";
        do {
            System.out.println("\n🎨 **Escolha o Modelo do Calendário:**");
            System.out.println("A. 👑 Luxo");
            System.out.println("B. ✨ Premium");
            System.out.println("C. 🔮 Púrpura");
            System.out.println("D. 🖨️ Impressão");
            System.out.println("E. 📟 Terminal");
            System.out.println("F. 🏰 Gótico");
            System.out.println("G. 🍀 Celta");
            System.out.println("H. 🧱 ASCII Art");
            System.out.println("I. 👨‍🍳 Culinária");
            System.out.println("J. 🛎️ Hotelaria");
            System.out.println("K. 🐾 Veterinária");
            System.out.println("Z. 🚪 Sair do Menu");
            System.out.print("\nOpção: ");
            opt = scanner.nextLine().toUpperCase();
            if(opt.isBlank()||opt.equals("Z"))
            		break;
            gerarCalendarioHTML(anoAlvo, opt.charAt(0) - 'A'+1);
        } while(true);

        System.out.println("\n--- 🔍 ANÁLISE DE DATA ESPECÍFICA (FERIADOS) ---");
        System.out.print("Data (d/m/aaaa) ou [Enter] para ignorar: ");
        String inData = scanner.nextLine();
        
        if (!inData.isBlank()) {
            try {
                LocalDate d = LocalDate.parse(inData, FORMATO_INPUT);
                
                String resNac = getDescricaoCompleta(d);
                List<Regiao> regioesFeriado = new ArrayList<>();
                for (Regiao r : Regiao.values()) {
                    if (feriadoMunicipal(d, r)) {
                        regioesFeriado.add(r);
                    }
                }
                
                System.out.println("\n📊 RESULTADO PARA " + d.format(FORMATO_EXTENSO).toUpperCase() + ":");

                // 1. Feriado Nacional
                if (!resNac.isEmpty()) {
                    // Se a descrição contiver "Feriado Local", já é tratada abaixo.
                    if (!resNac.contains("Feriado Local")) {
                        System.out.println("   🌟 FERIADO NACIONAL: " + resNac);
                    }
                }

                // 2. Feriado Regional/Municipal
                if (!regioesFeriado.isEmpty()) {
                    System.out.print("   📍 FERIADO MUNICIPAL (Regiões): ");
                    List<String> nomesRegioes = new ArrayList<>();
                    for (Regiao r : regioesFeriado) {
                        nomesRegioes.add(r.name());
                    }
                    System.out.println(String.join(", ", nomesRegioes));
                }

                // 3. Fim de Semana
                if (fimDeSemana(d)) {
                    System.out.println("   🏖️ FIM DE SEMANA");
                }
                
                // 4. Dia Útil
                if (resNac.isEmpty() && regioesFeriado.isEmpty() && !fimDeSemana(d)) {
                    System.out.println("   💼 DIA ÚTIL");
                }
                
            } catch (Exception e) { 
                System.out.println("⚠️ Erro nos dados. Use o formato d/m/aaaa (ex: 1/12/2025)."); 
            }
        }
        System.out.println("\n👋 Programa terminado.");
        scanner.close();
    }
}