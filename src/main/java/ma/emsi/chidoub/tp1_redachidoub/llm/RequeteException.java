package ma.emsi.chidoub.tp1_redachidoub.llm;

public class RequeteException extends Exception {
    private int status;
    private String requeteJson;

    public RequeteException(int status) { this.status = status; }
    public RequeteException(String message) { super(message); }
    public RequeteException(String message, String requeteJson) { super(message); this.requeteJson = requeteJson; }
    public RequeteException() {}

    public int getStatus() { return status; }
    public String getRequeteJson() { return requeteJson; }
}