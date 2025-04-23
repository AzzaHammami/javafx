import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

function DisponibiliteManager() {
  const [disponibilites, setDisponibilites] = useState([]);
  const [selectedDisponibilite, setSelectedDisponibilite] = useState(null);
  const [formData, setFormData] = useState({
    medecinId: '',
    dateDebut: '',
    dateFin: '',
    statut: 'Disponible'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDisponibilites();
  }, []);

  const fetchDisponibilites = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/disponibilites');
      if (!response.ok) throw new Error('Erreur lors du chargement des disponibilités');
      const data = await response.json();
      setDisponibilites(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const url = selectedDisponibilite
        ? `http://localhost:8080/api/disponibilites/${selectedDisponibilite.id}`
        : 'http://localhost:8080/api/disponibilites';
      
      const response = await fetch(url, {
        method: selectedDisponibilite ? 'PUT' : 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) throw new Error('Erreur lors de l\'enregistrement');
      
      fetchDisponibilites();
      resetForm();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette disponibilité ?')) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/disponibilites/${id}`, {
        method: 'DELETE',
      });
      
      if (!response.ok) throw new Error('Erreur lors de la suppression');
      
      fetchDisponibilites();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleEdit = (disponibilite) => {
    setSelectedDisponibilite(disponibilite);
    setFormData({
      medecinId: disponibilite.medecinId,
      dateDebut: format(new Date(disponibilite.dateDebut), "yyyy-MM-dd'T'HH:mm"),
      dateFin: format(new Date(disponibilite.dateFin), "yyyy-MM-dd'T'HH:mm"),
      statut: disponibilite.statut
    });
  };

  const resetForm = () => {
    setSelectedDisponibilite(null);
    setFormData({
      medecinId: '',
      dateDebut: '',
      dateFin: '',
      statut: 'Disponible'
    });
  };

  if (loading) return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Chargement...</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-purple-600 text-white p-4 mb-6">
        <div className="container mx-auto">
          <h1 className="text-2xl font-bold">Gestion des Disponibilités</h1>
        </div>
      </header>

      <div className="container mx-auto px-4">
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
            <button onClick={() => setError(null)} className="float-right">&times;</button>
          </div>
        )}

        {/* Form */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">
            {selectedDisponibilite ? 'Modifier la disponibilité' : 'Ajouter une disponibilité'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-gray-700 mb-2">ID Médecin</label>
                <input
                  type="number"
                  value={formData.medecinId}
                  onChange={(e) => setFormData({...formData, medecinId: e.target.value})}
                  className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-purple-600"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 mb-2">Statut</label>
                <select
                  value={formData.statut}
                  onChange={(e) => setFormData({...formData, statut: e.target.value})}
                  className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-purple-600"
                >
                  <option value="Disponible">Disponible</option>
                  <option value="Indisponible">Indisponible</option>
                </select>
              </div>
              <div>
                <label className="block text-gray-700 mb-2">Date de début</label>
                <input
                  type="datetime-local"
                  value={formData.dateDebut}
                  onChange={(e) => setFormData({...formData, dateDebut: e.target.value})}
                  className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-purple-600"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-700 mb-2">Date de fin</label>
                <input
                  type="datetime-local"
                  value={formData.dateFin}
                  onChange={(e) => setFormData({...formData, dateFin: e.target.value})}
                  className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-purple-600"
                  required
                />
              </div>
            </div>
            
            <div className="flex gap-2">
              <button
                type="submit"
                className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700 transition"
              >
                {selectedDisponibilite ? 'Modifier' : 'Ajouter'}
              </button>
              {selectedDisponibilite && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="bg-gray-200 text-gray-700 px-6 py-2 rounded hover:bg-gray-300 transition"
                >
                  Annuler
                </button>
              )}
            </div>
          </form>
        </div>

        {/* List */}
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID Médecin</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date de début</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date de fin</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {disponibilites.map((dispo) => (
                <tr key={dispo.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">{dispo.medecinId}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {format(new Date(dispo.dateDebut), 'PPP à HH:mm', { locale: fr })}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {format(new Date(dispo.dateFin), 'PPP à HH:mm', { locale: fr })}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      dispo.statut === 'Disponible' 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {dispo.statut}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => handleEdit(dispo)}
                      className="bg-green-500 hover:bg-green-600 text-white font-semibold px-4 py-1 rounded mr-2 transition"
                    >
                      Modifier
                    </button>
                    <button
                      onClick={() => handleDelete(dispo.id)}
                      className="bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-1 rounded transition"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              ))}
              {disponibilites.length === 0 && (
                <tr>
                  <td colSpan="5" className="px-6 py-4 text-center text-gray-500">
                    Aucune disponibilité trouvée
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default DisponibiliteManager;
