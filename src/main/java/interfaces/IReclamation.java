package interfaces;

import models.Reclamation;
import java.util.List;

public interface IReclamation {
    void ajouter(Reclamation reclamation);
    void modifier(Reclamation reclamation);
    void supprimer(int id);
    List<Reclamation> getAll();
    Reclamation getById(int id);
}
