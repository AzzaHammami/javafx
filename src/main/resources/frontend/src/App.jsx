import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import DisponibiliteManager from './components/DisponibiliteManager';
import Dashboard from './components/Dashboard';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/disponibilites" element={<DisponibiliteManager />} />
      </Routes>
    </Router>
  );
}

export default App;
