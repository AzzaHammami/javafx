package com.example.rendez_vous.interfaces;

import java.util.List;

public interface IRendez_Vous<T> {
    Boolean ajouterRendezVous(T t);
    Boolean modifierRendezVous(T t);
    Boolean supprimerRendezVous(int id);
    List<T> listerRendezVous();
    T getRendezVousById(int id);
}