package one;
import java.util.*;

/**
 * 🐾 Classe Gestor: Sistema de Gestão Veterinária Multi-idioma.
 */
public class Gestor {
    // 📚 Mapas para armazenar traduções e permissões
	public static final Map<String, Map<String, String>> LANGUAGES = new HashMap<>();
	public static final Map<String, List<String>> COMMANDS = new TreeMap<>();
    
    // ⌨️ Scanner único para evitar conflitos de leitura no System.in
    private static final Scanner sc = new Scanner(System.in);

    static {
        setupLanguages(); // 🌐 Inicializa os textos
        setupRBAC();      // 🛡️ Inicializa as permissões
    }

    /**
     * 📋 Menu Principal: Gere a interface e a lógica de escolha.
     */
    public static void menu(String userName) {
        System.out.println("\n1: en-US | 2: pt-PT | 3: fr-FR | 4: de-DE | 5: it-IT | 6: el-GR");
        System.out.print("👉 Selecione o Idioma / Select Language: ");
        
        String lChoice = sc.nextLine().trim();
        String currentLang = switch(lChoice) {
            case "1" -> "en-US";
            case "3" -> "fr-FR";
            case "4" -> "de-DE";
            case "5" -> "it-IT";
            case "6" -> "el-GR";
            default -> "pt-PT";
        };

        Map<String, String> txt = LANGUAGES.get(currentLang);
        String group = User.getGroup(userName);

        while (true) {
            System.out.println("\n" + "=".repeat(30) + " " + txt.get("header") + " " + "=".repeat(30));
            System.out.println("👤 " + txt.get("logged_as") + ": " + userName + " | 🔑 " + txt.get("role") + ": " + group);

            // 🔍 Filtra as opções baseadas no perfil (RBAC)
            Map<String, String> activeOptions = new TreeMap<>();
            for (var entry : COMMANDS.entrySet()) {
                if (entry.getValue().contains(group)) {
                    System.out.println(entry.getKey() + ": " + txt.get(entry.getKey()));
                    activeOptions.put(entry.getKey(), txt.get(entry.getKey()));
                }
            }

            System.out.println("-".repeat(90));
            System.out.println("X: " + txt.get("change_pw") + " | Q: " + txt.get("logout"));
            System.out.print("\n👉 " + txt.get("choice") + ": ");
            
            String choice = sc.nextLine().trim().toUpperCase();

            if (choice.equals("Q")) break; // 🚪 Sair do menu
            if (choice.equals("X")) { changePassword(); continue; }

            if (activeOptions.containsKey(choice)) {
                System.out.println("\n>>> " + txt.get("executing") + ":");
                System.out.println("✅ " + activeOptions.get(choice));
                System.out.println("\n" + txt.get("footer"));
                sc.nextLine(); // ⏸️ Pausa para leitura (equivalente ao input do Python)
            } else {
                System.out.println("\n❌ " + txt.get("invalid"));
            }
        }
    }

    /**
     * 🛡️ Define quais perfis podem aceder a quais tarefas (1.1 até 4.8).
     */
    private static void setupRBAC() {
        String G = "Gerente", V = "Veterinário", R = "Rececionista", T = "Tutor";
        
        COMMANDS.put("A", Arrays.asList(R, G)); // 1.1
        COMMANDS.put("B", Arrays.asList(R, G)); // 1.2
        COMMANDS.put("C", Arrays.asList(V, G)); // 2.1
        COMMANDS.put("D", Arrays.asList(V, G)); // 2.2
        COMMANDS.put("E", Arrays.asList(V, G)); // 2.3
        COMMANDS.put("F", Arrays.asList(V, G)); // 2.4
        COMMANDS.put("G", Arrays.asList(V, G)); // 2.5
        COMMANDS.put("H", Arrays.asList(V, G)); // 2.6
        COMMANDS.put("I", Arrays.asList(T, G)); // 3.1
        COMMANDS.put("J", Arrays.asList(T, G)); // 3.2
        COMMANDS.put("K", List.of(G));          // 4.1
        COMMANDS.put("L", List.of(G));          // 4.2
        COMMANDS.put("M", List.of(G));          // 4.3
        COMMANDS.put("N", List.of(G));          // 4.4
        COMMANDS.put("O", List.of(G));          // 4.5
        COMMANDS.put("P", List.of(G));          // 4.6
        COMMANDS.put("Q", List.of(G));          // 4.7
        COMMANDS.put("R", List.of(G));          // 4.8
    }

    /**
     * 🌐 Configuração integral de todos os textos e regras de negócio por idioma.
     */
    private static void setupLanguages() {
        // --- PORTUGUÊS (pt-PT) ---
        Map<String, String> pt = new HashMap<>();
        pt.put("header", "MENU DO SISTEMA VETERINÁRIO");
        pt.put("choice", "Escolha uma opção");
        pt.put("logout", "Sair");
        pt.put("change_pw", "Alterar password");
        pt.put("invalid", "Opção inválida!");
        pt.put("footer", "Pressione Enter para voltar...");
        pt.put("logged_as", "Utilizador");
        pt.put("role", "Perfil");
        pt.put("executing", "A executar");
        pt.put("A", "1.1 – Criar/Atualizar dados dos tutores e dos respetivos animais incluindo fotografia.");
        pt.put("B", "1.2 – Agendar/Cancelar/Reagendar a prestação de serviços veterinários para um determinado animal.");
        pt.put("C", "2.1 – Implementar um controlo autocomplete que permita encontrar fichas de animais a partir do nome do tutor.");
        pt.put("D", "2.2 – Consultar o registo clínico de um animal incluindo a idade detalhada (dias/semanas/meses/anos) e escalão etário (bebé, jovem, adulto ou idoso).");
        pt.put("E", "2.3 – Visualizar a árvore genealógica de um animal.");
        pt.put("F", "2.4 – Obter a lista de chamada (em data-hora) dos animais com agendamento de serviços sob a sua supervisão.");
        pt.put("G", "2.5 – Atualizar, no contexto da prestação de serviços, o histórico clínico do animal.");
        pt.put("H", "2.6 – Agendar/Cancelar a prestação de serviços veterinários.");
        pt.put("I", "3.1 – Consultar ficha e histórico clínicos dos seus animais incluindo serviços agendados.");
        pt.put("J", "3.2 – Agendar/Reagendar/Rejeitar consultas para os seus animais que já tenham ficha clínica.");
        pt.put("K", "4.1 – Criar/Atualizar dados dos médicos veterinários, tutores e respetivos animais.");
        pt.put("L", "4.2 – Atualizar o horário (supervisão). As clínicas não funcionam aos fins de semana e feriados.");
        pt.put("M", "4.3 – Exportar para um documento XML/JSON a ficha e histórico clínicos de um animal.");
        pt.put("N", "4.4 – Importar de um documento XML/JSON a ficha e histórico clínicos de um animal.");
        pt.put("O", "4.5 – Elaborar lista (ordenada por idade) de animais que ultrapassaram a expetativa de vida.");
        pt.put("P", "4.6 – Produzir lista (ordenada por nome) dos tutores e respetiva quantidade de animais com excesso de peso.");
        pt.put("Q", "4.7 – Listar os tutores com mais agendamentos de serviços cancelados no último trimestre.");
        pt.put("R", "4.8 – Apresentar, por serviço, a quantidade de agendamentos previstos para a próxima semana.");
        LANGUAGES.put("pt-PT", pt);

        // --- INGLÊS (en-US) ---
        Map<String, String> en = new HashMap<>();
        en.put("header", "VETERINARY SYSTEM MENU");
        en.put("choice", "Your choice");
        en.put("logout", "Logout");
        en.put("change_pw", "Password");
        en.put("invalid", "Invalid!");
        en.put("footer", "Press Enter to return...");
        en.put("logged_as", "User");
        en.put("role", "Role");
        en.put("executing", "Executing");
        en.put("A", "1.1 – Create/Update tutor and animal data including photo.");
        en.put("B", "1.2 – Schedule/Cancel/Reschedule veterinary services for a specific animal.");
        en.put("C", "2.1 – Autocomplete search for animal records based on tutor name.");
        en.put("D", "2.2 – Consult clinical record including age (days/weeks/months/years) and group (baby, young, adult, senior).");
        en.put("E", "2.3 – View animal family tree.");
        en.put("F", "2.4 – Obtain call list of animals with scheduled services under your supervision.");
        en.put("G", "2.5 – Update clinical history during veterinary service delivery.");
        en.put("H", "2.6 – Schedule/Cancel veterinary services.");
        en.put("I", "3.1 – View clinical records and history including scheduled services.");
        en.put("J", "3.2 – Schedule/Reschedule/Reject appointments for registered animals.");
        en.put("K", "4.1 – Manage data of veterinarians, tutors, and animals.");
        en.put("L", "4.2 – Update schedule and supervision. Closed on weekends and holidays.");
        en.put("M", "4.3 – Export clinical record and history to XML/JSON.");
        en.put("N", "4.4 – Import clinical record and history from XML/JSON.");
        en.put("O", "4.5 – List animals (sorted by age) exceeding life expectancy.");
        en.put("P", "4.6 – List tutors and their number of overweight animals.");
        en.put("Q", "4.7 – Tutors with most cancellations in the last quarter.");
        en.put("R", "4.8 – Weekly scheduled appointments count by service.");
        LANGUAGES.put("en-US", en);

        // --- FRANCÊS (fr-FR) ---
        Map<String, String> fr = new HashMap<>();
        fr.put("header", "MENU DU SYSTÈME VÉTÉRINAIRE");
        fr.put("choice", "Votre choix");
        fr.put("logout", "Quitter");
        fr.put("change_pw", "Mot de passe");
        fr.put("invalid", "Option invalide!");
        fr.put("footer", "Appuyez sur Entrée pour revenir...");
        fr.put("logged_as", "Utilisateur");
        fr.put("role", "Rôle");
        fr.put("executing", "Exécution");
        fr.put("A", "1.1 – Créer/Modifier les données des tuteurs et animaux avec photo.");
        fr.put("B", "1.2 – Gérer les prestations de services vétérinaires pour un animal.");
        fr.put("C", "2.1 – Autocomplétion pour trouver des fiches d'animaux par nom de tuteur.");
        fr.put("D", "2.2 – Dossier clinique (âge en jours/semaines/mois/ans) et groupe (bébé, jeune, adulte, senior).");
        fr.put("E", "2.3 – Arbre généalogique de l'animal.");
        fr.put("F", "2.4 – Liste d'appel des animaux sous votre supervision.");
        fr.put("G", "2.5 – Mise à jour de l'historique clinique pendant le service.");
        fr.put("H", "2.6 – Planifier/Annuler les services vétérinaires.");
        fr.put("I", "3.1 – Historique clinique et services prévus pour vos animaux.");
        fr.put("J", "3.2 – Gérer les rendez-vous pour les animaux avec dossier.");
        fr.put("K", "4.1 – Gérer les vétérinaires, tuteurs et animaux.");
        fr.put("L", "4.2 – Planning et supervision. Fermé les week-ends et jours fériés.");
        fr.put("M", "4.3 – Exporter le dossier clinique en XML/JSON.");
        fr.put("N", "4.4 – Importer le dossier clinique depuis XML/JSON.");
        fr.put("O", "4.5 – Animaux ayant dépassé l'espérance de vie.");
        fr.put("P", "4.6 – Liste des tuteurs et animaux en surpoids.");
        fr.put("Q", "4.7 – Tuteurs avec le plus d'annulations au dernier trimestre.");
        fr.put("R", "4.8 – Prévisions de rendez-vous par service la semaine prochaine.");
        LANGUAGES.put("fr-FR", fr);

        // --- ALEMÃO (de-DE) ---
        Map<String, String> de = new HashMap<>();
        de.put("header", "TIERARZTSYSTEM MENÜ");
        de.put("choice", "Auswahl");
        de.put("logout", "Abmelden");
        de.put("change_pw", "Passwort ändern");
        de.put("invalid", "Ungültig!");
        de.put("footer", "Eingabetaste zum Zurückkehren...");
        de.put("logged_as", "Benutzer");
        de.put("role", "Rolle");
        de.put("executing", "Ausführung");
        de.put("A", "1.1 – Tutor- und Tierdaten erstellen/aktualisieren.");
        de.put("B", "1.2 – Tierärztliche Dienstleistungen verwalten.");
        de.put("C", "2.1 – Autovervollständigung für Tierakten nach Tutornamen.");
        de.put("D", "2.2 – Klinische Akte (Alter in Tagen/Wochen/Monaten/Jahren).");
        de.put("E", "2.3 – Stammbaum des Tieres anzeigen.");
        de.put("F", "2.4 – Anrufliste der Tiere unter Ihrer Aufsicht.");
        de.put("G", "2.5 – Klinische Historie während der Behandlung aktualisieren.");
        de.put("H", "2.6 – Dienste planen oder stornieren.");
        de.put("I", "3.1 – Klinische Akten und geplante Dienste Ihrer Tiere.");
        de.put("J", "3.2 – Termine für registrierte Tiere verwalten.");
        de.put("K", "4.1 – Tierärzte, Tutoren und Tiere verwalten.");
        de.put("L", "4.2 – Zeitplan und Aufsicht. Am Wochenende geschlossen.");
        de.put("M", "4.3 – Akte nach XML/JSON exportieren.");
        de.put("N", "4.4 – Akte von XML/JSON importieren.");
        de.put("O", "4.5 – Tiere über der Lebenserwartung.");
        de.put("P", "4.6 – Tutoren und Tiere mit Übergewicht.");
        de.put("Q", "4.7 – Meiste Stornierungen im letzten Quartal.");
        de.put("R", "4.8 – Terminvorschau für die nächste Woche.");
        LANGUAGES.put("de-DE", de);

        // --- ITALIANO (it-IT) ---
        Map<String, String> it = new HashMap<>();
        it.put("header", "MENU SISTEMA VETERINARIO");
        it.put("choice", "Scelta");
        it.put("logout", "Esci");
        it.put("change_pw", "Cambia password");
        it.put("invalid", "Opzione non valida!");
        it.put("footer", "Premere Invio per tornare...");
        it.put("logged_as", "Utente");
        it.put("role", "Ruolo");
        it.put("executing", "Esecuzione");
        it.put("A", "1.1 – Crea/Aggiorna dati tutor e animali con foto.");
        it.put("B", "1.2 – Gestione servizi veterinari per un animale.");
        it.put("C", "2.1 – Autocompletamento ricerca record animali per tutor.");
        it.put("D", "2.2 – Cartella clinica (età in giorni/settimane/mesi/anni).");
        it.put("E", "2.3 – Visualizza albero genealogico dell'animale.");
        it.put("F", "2.4 – Elenco chiamate sotto la tua supervisione.");
        it.put("G", "2.5 – Aggiorna storia clinica durante il servizio.");
        it.put("H", "2.6 – Pianifica/Annulla servizi veterinari.");
        it.put("I", "3.1 – Cartella clinica e servizi dei propri animali.");
        it.put("J", "3.2 – Gestione appuntamenti animali registrati.");
        it.put("K", "4.1 – Gestione veterinari, tutor e animali.");
        it.put("L", "4.2 – Orari e supervisione. Chiuso nei festivi.");
        it.put("M", "4.3 – Esporta cartella in XML/JSON.");
        it.put("N", "4.4 – Importa cartella da XML/JSON.");
        it.put("O", "4.5 – Animali oltre l'aspettativa di vita.");
        it.put("P", "4.6 – Tutor e animali in sovrappeso.");
        it.put("Q", "4.7 – Maggiori cancellazioni nell'ultimo trimestre.");
        it.put("R", "4.8 – Appuntamenti previsti per la prossima settimana.");
        LANGUAGES.put("it-IT", it);

        // --- GREGO (el-GR) ---
        Map<String, String> el = new HashMap<>();
        el.put("header", "ΜΕΝΟΥ ΚΤΗΝΙΑΤΡΙΚΟΥ ΣΥΣΤΗΜΑΤΟΣ");
        el.put("choice", "Επιλογή");
        el.put("logout", "Έξοδος");
        el.put("change_pw", "Αλλαγή κωδικού");
        el.put("invalid", "Μη έγκυρη επιλογή!");
        el.put("footer", "Πατήστε Enter για επιστροφή...");
        el.put("logged_as", "Χρήστης");
        el.put("role", "Ρόλος");
        el.put("executing", "Εκτέλεση");
        el.put("A", "1.1 – Δημιουργία/Ενημέρωση δεδομένων κηδεμόνων και ζώων.");
        el.put("B", "1.2 – Προγραμματισμός κτηνιατρικών υπηρεσιών.");
        el.put("C", "2.1 – Αυτόματη συμπλήρωση αναζήτησης κλινικών αρχείων.");
        el.put("D", "2.2 – Κλινικό μητρώο (ηλικία σε ημέρες/εβδομάδες/μήνες/έτη).");
        el.put("E", "2.3 – Προβολή γενεαλογικού δέντρου.");
        el.put("F", "2.4 – Λίστα κλήσεων υπό την επίβλεψή σας.");
        el.put("G", "2.5 – Ενημέρωση κλινικού ιστορικού κατά την υπηρεσία.");
        el.put("H", "2.6 – Προγραμματισμός/Ακύρωση υπηρεσιών.");
        el.put("I", "3.1 – Προβολή ιστορικού των ζώων σας.");
        el.put("J", "3.2 – Διαχείριση ραντεβού για εγγεγραμμένα ζώα.");
        el.put("K", "4.1 – Διαχείριση κτηνιάτρων, κηδεμόνων και ζώων.");
        el.put("L", "4.2 – Ωράριο και επίβλεψη. Κλειστά τα Σαββατοκύριακα.");
        el.put("M", "4.3 – Εξαγωγή αρχείου σε XML/JSON.");
        el.put("N", "4.4 – Εισαγωγή αρχείου από XML/JSON.");
        el.put("O", "4.5 – Λίστα ζώων που υπερέβησαν το προσδόκιμο ζωής.");
        el.put("P", "4.6 – Κηδεμόνες και υπέρβαρα ζώα.");
        el.put("Q", "4.7 – Κηδεμόνες με τις περισσότερες ακυρώσεις.");
        el.put("R", "4.8 – Προγραμματισμένα ραντεβού ανά υπηρεσία.");
        LANGUAGES.put("el-GR", el);
    }

    private static void changePassword() { System.out.println("\n🔒 [PROTOCOL] Password change initialized."); }

    /**
     * 🏁 Método de entrada do programa.
     */
    public static void main(String[] args) {
        while (true) {
            System.out.println("\n" + "-".repeat(15) + " VETERINARY LOGIN " + "-".repeat(15));
            System.out.print("Username (admin_user, vet_silva, reception_ana, tutor_joao) or 'exit': ");
            String user = sc.nextLine().trim();
            
            if (user.equalsIgnoreCase("exit")) {
                System.out.println("👋 Goodbye!");
                break;
            }
            
            menu(user);
        }
    }
}