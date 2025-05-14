import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, token } = useAuth();
  const [isChecking, setIsChecking] = useState(true);
  
  useEffect(() => {
    console.log('ProtectedRoute: isAuthenticated =', isAuthenticated);
    console.log('ProtectedRoute: token =', token ? 'exists' : 'not exists');
    
    // Короткая задержка для стабилизации состояния аутентификации
    const timer = setTimeout(() => {
      setIsChecking(false);
    }, 100);
    
    return () => clearTimeout(timer);
  }, [isAuthenticated, token]);

  // Пока проверяем аутентификацию, ничего не рендерим
  if (isChecking) {
    return <div>Проверка аутентификации...</div>;
  }
  
  if (!isAuthenticated || !token) {
    console.log('ProtectedRoute: Redirecting to login because not authenticated');
    return <Navigate to="/login" replace />;
  }

  console.log('ProtectedRoute: Rendering children');
  return <>{children}</>;
};

export default ProtectedRoute; 