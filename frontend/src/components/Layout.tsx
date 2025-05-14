import React from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

const Layout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <header className="header">
        <div className="logo">
          <h1>Wishlist App</h1>
        </div>
        <nav className="nav">
          <ul>
            <li>
              <Link to="/wishlist">My Wishlist</Link>
            </li>
            <li>
              <Link to="/profile">Profile</Link>
            </li>
          </ul>
        </nav>
        <div className="user-info">
          {user && (
            <>
              <span>Welcome, {user.username}!</span>
              <button onClick={handleLogout} className="logout-btn">
                Logout
              </button>
            </>
          )}
        </div>
      </header>
      <main className="main-content">
        <Outlet />
      </main>
      <footer className="footer">
        <p>&copy; {new Date().getFullYear()} Wishlist App. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Layout; 