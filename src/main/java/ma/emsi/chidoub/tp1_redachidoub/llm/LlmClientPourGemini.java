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

        this.key = System.getenv("GEMINI_API_KEY");

        if (this.key == null || this.key.isBlank()) {
            throw new IllegalStateException("The GEMINI_API_KEY is not detected in the environment variables");
        }

        this.clientRest = ClientBuilder.newClient();
        this.target = clientRest.target("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
                .queryParam("key", this.key);
    }
    public Response envoyerRequete(Entity requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        return request.post(requestEntity);
    }

    public void closeClient() {
        this.clientRest.close();
    }
}
