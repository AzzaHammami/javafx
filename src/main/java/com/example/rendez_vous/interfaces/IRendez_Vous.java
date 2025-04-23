package com.example.rendez_vous.interfaces;

import java.util.List;

public interface IRendez_Vous<T> {
    void ajouterRendezVous(T rendezVous);
    void modifierRendezVous(T rendezVous);
    void supprimerRendezVous(int id);
    List<T> listerRendezVous();
    T getRendezVousById(int id);
}
