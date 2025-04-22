package interfaces;

import models.Reponse;
import java.util.List;

public interface IReponse {
    void ajouter(Reponse reponse);
    void modifier(Reponse reponse);
    void supprimer(int id);
    List<Reponse> getAll();
    Reponse getById(int id);
}

