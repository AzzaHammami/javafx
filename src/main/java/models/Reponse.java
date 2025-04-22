package models;



import java.time.LocalDate;

public class Reponse {
    private int id;
    private String contenu;
    private LocalDate dateReponse;
    private Reclamation reclamation;

    public Reponse() {
    }

    public Reponse(int id, String contenu, LocalDate dateReponse, Reclamation reclamation) {
        this.id = id;
        this.contenu = contenu;
        this.dateReponse = dateReponse;
        this.reclamation = reclamation;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDate dateReponse) {
        this.dateReponse = dateReponse;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateReponse=" + dateReponse +
                ", reclamation=" + reclamation +
                '}';
    }
}
