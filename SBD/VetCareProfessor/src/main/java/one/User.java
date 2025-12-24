package one;

import java.util.HashMap;
import java.util.Map;

/**
 * 👨‍⚕️ Classe User: Simula a autenticação e atribuição de perfis.
 */
public class User {
    public static String getGroup(String userName) {
        Map<String, String> db = new HashMap<>();
        db.put("admin_user", "Gerente");
        db.put("vet_silva", "Veterinário");
        db.put("reception_ana", "Rececionista");
        db.put("tutor_joao", "Tutor");
        return db.getOrDefault(userName, "Guest");
    }
}
