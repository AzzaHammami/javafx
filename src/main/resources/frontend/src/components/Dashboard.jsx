import React from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-purple-600 text-white p-4 mb-6">
        <div className="container mx-auto">
          <h1 className="text-2xl font-bold">MaSanté Dashboard</h1>
        </div>
      </header>

      <div className="container mx-auto px-4">
        <div className="grid md:grid-cols-3 gap-6">
          {/* Disponibilités Card */}
          <div 
            onClick={() => navigate('/disponibilites')}
            className="bg-white p-6 rounded-lg shadow-sm cursor-pointer hover:shadow-md transition group"
          >
            <h2 className="text-xl font-semibold mb-2 text-purple-600 group-hover:text-purple-700">
              Disponibilités
            </h2>
            <p className="text-gray-600">
              Gérez vos disponibilités et horaires de consultation
            </p>
          </div>

          {/* Rendez-vous Card */}
          <div 
            onClick={() => navigate('/disponibilites')}
            className="bg-white p-6 rounded-lg shadow-sm cursor-pointer hover:shadow-md transition group"
          >
            <h2 className="text-xl font-semibold mb-2 text-purple-600 group-hover:text-purple-700">
              Prendre rendez-vous
            </h2>
            <p className="text-gray-600">
              Consultez et gérez vos rendez-vous
            </p>
          </div>

          {/* Profile Card */}
          <div className="bg-white p-6 rounded-lg shadow-sm cursor-pointer hover:shadow-md transition group">
            <h2 className="text-xl font-semibold mb-2 text-purple-600 group-hover:text-purple-700">
              Profil
            </h2>
            <p className="text-gray-600">
              Mettez à jour vos informations personnelles
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
