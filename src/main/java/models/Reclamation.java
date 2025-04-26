package models;
import java.time.LocalDate;
public class Reclamation {

        private int id;
        private String sujet;
        private String description;
        private String statut;
        private LocalDate dateReclamation;
        private Integer userId;



        public Reclamation() {
            this.dateReclamation = LocalDate.now();
            this.statut = "En attente";
        }


        public Reclamation(int id, String sujet, String description, String statut, LocalDate dateReclamation, Integer userId) {
            this.id = id;
            this.sujet = sujet;
            this.description = description;
            this.statut = statut;
            this.dateReclamation = dateReclamation;
            this.userId = userId;
        }


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSujet() {
            return sujet;
        }

        public void setSujet(String sujet) {
            this.sujet = sujet;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }

        public LocalDate getDateReclamation() {
            return dateReclamation;
        }

        public void setDateReclamation(LocalDate dateReclamation) {
            this.dateReclamation = dateReclamation;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }


        @Override
        public String toString() {
            return "Reclamation{" +
                    "id=" + id +
                    ", sujet='" + sujet + '\'' +
                    ", description='" + description + '\'' +
                    ", statut='" + statut + '\'' +
                    ", dateReclamation=" + dateReclamation +
                    ", userId=" + userId +
                    '}';
        }
    }
