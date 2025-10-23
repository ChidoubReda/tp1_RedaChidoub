package ma.emsi.chidoub.tp1_redachidoub.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// === Imports côté LLM/JSON (mets bien les bons packages) ===
import ma.emsi.chidoub.tp1_redachidoub.llm.JsonUtilPourGemini;
import ma.emsi.chidoub.tp1_redachidoub.llm.LlmInteraction;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 */
@Named
@ViewScoped
public class Bb implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- Mode Debug ---
    private boolean debug = false;
    private String texteRequeteJson;
    private String texteReponseJson;

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public String getTexteRequeteJson() { return texteRequeteJson; }
    public void setTexteRequeteJson(String texteRequeteJson) { this.texteRequeteJson = texteRequeteJson; }

    public String getTexteReponseJson() { return texteReponseJson; }
    public void setTexteReponseJson(String texteReponseJson) { this.texteReponseJson = texteReponseJson; }

    /** Bouton qui bascule le mode debug (utilisé par le <h:commandButton/> de la page). */
    public void toggleDebug() { this.setDebug(!isDebug()); }

    // --- Rôle système / UI ---
    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;

    // --- Question / Réponse / Conversation ---
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // --- Injections ---
    @Inject
    private FacesContext facesContext;

    @Inject
    private JsonUtilPourGemini jsonUtil; // Utilisé comme dans les écrans

    public Bb() {}

    // === Getters/Setters UI ===
    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public String getConversation() { return conversation.toString(); }
    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }

    /**
     * Envoie la question au serveur (via JsonUtil -> LLM).
     * Retourne null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Début de conversation : fige le rôle et l'envoie à JsonUtil (comme dans l'énoncé)
        if (this.conversation.isEmpty()) {
            this.roleSystemeChangeable = false;
            jsonUtil.setSystemRole(roleSysteme);
        }

        try {
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();
        } catch (Exception e) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
        }

        afficherConversation();
        return null;
    }

    /** Pour un nouveau chat : change de vue pour réinitialiser le bean. */
    public String nouveauChat() {
        return "index";
    }

    /** Affiche la conversation dans le textarea de la page JSF. */
    private void afficherConversation() {
        this.conversation
                .append("== User:\n").append(question)
                .append("\n== Serveur:\n").append(reponse)
                .append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }
}
