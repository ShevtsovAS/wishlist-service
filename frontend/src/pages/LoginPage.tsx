import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPages.css';

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const { login, isAuthenticated, token } = useAuth();
  const navigate = useNavigate();
  
  // Проверяем, если пользователь уже авторизован, перенаправляем его
  useEffect(() => {
    console.log('LoginPage: isAuthenticated =', isAuthenticated, 'token =', token ? 'exists' : 'not exists');
    if (isAuthenticated && token) {
      console.log('LoginPage: User is already authenticated, redirecting to /wishlist');
      navigate('/wishlist');
    }
  }, [isAuthenticated, token, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    console.log('LoginPage: Submitting login form with username:', username);

    try {
      await login(username, password);
      console.log('LoginPage: Login successful');
    } catch (err: any) {
      console.error('LoginPage: Login error:', err);
      setError(err.response?.data?.message || 'Failed to login. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <h1>Login to Your Wishlist</h1>
        
        {error && <div className="error-message">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          
          <button type="submit" className="submit-btn" disabled={isLoading}>
            {isLoading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        
        <div className="auth-footer">
          <p>
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage; 