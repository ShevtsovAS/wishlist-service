import React, { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { authApi, LoginRequest, RegisterRequest } from '../api/auth';

// Типы для контекста
interface User {
  id: number;
  username: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  register: (username: string, email: string, password: string) => Promise<void>;
}

// Создаем контекст с начальными значениями
export const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isAuthenticated: false,
  login: async () => {},
  logout: () => {},
  register: async () => {},
});

// Хук для использования контекста
export const useAuth = () => useContext(AuthContext);

// Провайдер контекста
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(!!token);
  const navigate = useNavigate();

  // Проверяем при монтировании компонента
  useEffect(() => {
    console.log('AuthProvider initialized');
    const storedToken = localStorage.getItem('token');
    console.log('Initial token from localStorage:', storedToken ? 'exists' : 'not exists');
    
    if (storedToken && !user) {
      console.log('Token found in localStorage, but no user data - fetching profile');
      fetchUserProfile();
    }
  }, [user]);

  // Настраиваем токен авторизации при его изменении
  useEffect(() => {
    console.log('Auth token changed:', token ? 'exists' : 'not exists');
    
    if (token) {
      console.log('Setting token in localStorage and fetching profile');
      localStorage.setItem('token', token);
      fetchUserProfile();
    } else {
      console.log('Removing token and clearing user data');
      localStorage.removeItem('token');
      setUser(null);
      setIsAuthenticated(false);
    }
  }, [token]);

  // Получение профиля пользователя
  const fetchUserProfile = async () => {
    console.log('AuthContext: Fetching user profile...');
    try {
      const storedToken = localStorage.getItem('token');
      if (!storedToken) {
        console.error('AuthContext: Trying to fetch profile without token');
        setIsAuthenticated(false);
        setToken(null);
        return;
      }
      
      // Обновляем состояние токена, если оно отличается от того, что в localStorage
      if (token !== storedToken) {
        console.log('AuthContext: Token in state does not match localStorage, updating...');
        setToken(storedToken);
      }
      
      // Получаем данные пользователя
      const userData = await authApi.getCurrentUser();
      console.log('AuthContext: Profile data received:', userData);
      setUser(userData);
      setIsAuthenticated(true);
      console.log('AuthContext: User authenticated successfully');
    } catch (error: any) {
      console.error('AuthContext: Failed to fetch user profile', error);
      if (error.response) {
        console.error('AuthContext: Error status:', error.response.status);
        console.error('AuthContext: Error data:', error.response.data);
      }
      
      // Only clear token on 401 (Unauthorized) for auth endpoints
      if (error.response && error.response.status === 401) {
        console.log('AuthContext: Authentication error, clearing token');
        localStorage.removeItem('token');
        setToken(null);
        setIsAuthenticated(false);
      }
    }
  };

  // Аутентификация пользователя
  const login = async (username: string, password: string) => {
    try {
      console.log('AuthContext: Trying to login with username:', username);
      
      // Формируем запрос
      const loginRequest: LoginRequest = { username, password };
      
      // Используем authApi
      console.log('AuthContext: Sending login request to API...');
      const authResponse = await authApi.login(loginRequest);
      console.log('AuthContext: Login response received:', authResponse);
      
      if (!authResponse.accessToken) {
        console.error('AuthContext: No accessToken in response', authResponse);
        throw new Error('Invalid server response: no token');
      }
      
      const { accessToken } = authResponse;
      console.log('AuthContext: Access token received:', accessToken.substring(0, 15) + '...');
      
      // Сохраняем токен в localStorage
      localStorage.setItem('token', accessToken);
      console.log('AuthContext: Token saved to localStorage');
      
      // Устанавливаем токен и явно устанавливаем состояние аутентификации
      setToken(accessToken);
      setIsAuthenticated(true);
      
      console.log('AuthContext: Token set, navigating to /wishlist');
      navigate('/wishlist');
    } catch (error: any) {
      console.error('AuthContext: Login failed', error);
      if (error.response) {
        console.error('AuthContext: Error status:', error.response.status);
        console.error('AuthContext: Error data:', error.response.data);
      }
      throw error;
    }
  };

  // Выход пользователя
  const logout = () => {
    setToken(null);
    navigate('/login');
  };

  // Регистрация нового пользователя
  const register = async (username: string, email: string, password: string) => {
    try {
      const registerRequest: RegisterRequest = { username, email, password };
      await authApi.register(registerRequest);
      navigate('/login');
    } catch (error) {
      console.error('Registration failed', error);
      throw error;
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}; 