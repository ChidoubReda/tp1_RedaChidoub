package ma.emsi.chidoub.tp1_redachidoub.llm;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

@Dependent
public class LlmClientPourGemini implements Serializable {
    // Clé API Gemini (récupérée depuis une variable d'environnement)
    private final String key;
    // Client REST
    private final Client clientRest;
    // Endpoint REST de l’API Gemini
    private final WebTarget target;

    public LlmClientPourGemini() {
        // 1) Récupère la clé API depuis la variable d'environnement GEMINI_KEY
        String envKey = System.getenv("GEMINI_KEY");
        if (envKey == null || envKey.isBlank()) {
            throw new IllegalStateException(
                    "Variable d'environnement GEMINI_KEY absente ou vide. " +
                            "Définis GEMINI_KEY avec ta clé API Gemini."
            );
        }
        this.key = envKey;

        // 2) Initialise le client JAX-RS
        this.clientRest = ClientBuilder.newClient();

        // 3) Prépare l’endpoint generateContent (Gemini v1beta).
        //    Le modèle peut être ajusté selon votre consigne (ex: gemini-1.5-pro, gemini-1.5-flash, etc.)
        this.target = clientRest
                .target("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent")
                .queryParam("key", this.key);
    }

    /**
     * Envoie une requête POST JSON à l'API Gemini.
     * @param requestEntity corps JSON de la requête (MediaType.APPLICATION_JSON_TYPE)
     * @return la réponse HTTP (corps JSON)
     */
    public Response envoyerRequete(Entity<String> requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        return request.post(requestEntity);
    }

    public void closeClient() {
        this.clientRest.close();
    }
}
