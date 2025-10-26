package ma.emsi.chidoub.tp1_redachidoub.llm;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

/**
 * Gère l'interface avec l'API de Gemini.
 * Son rôle est essentiellement de lancer une requête à chaque nouvelle
 * question qu'on veut envoyer à l'API.
 */
@Dependent
public class LlmClientPourGemini implements Serializable {

    // Clé pour l'API du LLM
    private final String key;
    // Client REST. Facilite les échanges avec une API REST.
    private Client clientRest; // Pour pouvoir le fermer
    // Représente un endpoint de serveur REST
    private final WebTarget target;

    public LlmClientPourGemini() {
        // ✅ 1. Récupère la clé secrète dans les variables d'environnement
        this.key = System.getenv("GEMINI_KEY");
        if (this.key == null || this.key.isBlank()) {
            throw new IllegalStateException("La clé API GEMINI_KEY est introuvable dans les variables d'environnement.");
        }

        // ✅ 2. Client REST
        this.clientRest = ClientBuilder.newClient();

        // ✅ 3. URL REST (endpoint Google Gemini)
        this.target = clientRest.target(
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + key
        );
    }

    /**
     * Envoie une requête à l'API de Gemini.
     *
     * @param requestEntity le corps de la requête (en JSON).
     * @return réponse REST de l'API (corps en JSON).
     */
    public Response envoyerRequete(Entity requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        return request.post(requestEntity);
    }

    public void closeClient() {
        this.clientRest.close();
    }
}
