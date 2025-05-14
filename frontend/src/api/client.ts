import axios, { AxiosError, AxiosInstance, AxiosRequestHeaders } from 'axios';

// Базовая конфигурация клиента
const apiClient: AxiosInstance = axios.create({
  // baseURL: 'http://localhost:8080', // Не устанавливаем baseURL, потому что запросы идут через прокси Vite
  headers: {
    'Content-Type': 'application/json',
  },
  // Добавляем withCredentials для работы с cookies, если они используются
  withCredentials: true,
});

// Функция для получения актуального токена
const getAuthToken = () => {
  const token = localStorage.getItem('token');
  
  if (token) {
    // Проверяем структуру токена (должен быть JWT)
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.warn('Token does not appear to be a valid JWT (does not have 3 parts)');
    }
  }
  
  return token;
};

// Добавляем перехватчик для токена аутентификации
apiClient.interceptors.request.use((config) => {
  const token = getAuthToken();
  
  // Полный URL и информация о запросе
  const method = config.method?.toUpperCase() || 'UNKNOWN';
  const url = config.url || 'unknown';
  console.log(`API ${method} Request: ${url}`);
  
  // Настраиваем заголовки авторизации
  if (token) {
    if (!config.headers) {
      config.headers = {} as AxiosRequestHeaders;
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  return config;
}, (error) => {
  console.error('Request interceptor error:', error);
  return Promise.reject(error);
});

// Обработка ошибок
apiClient.interceptors.response.use(
  (response) => {
    // Логгируем успешный ответ
    const method = response.config.method?.toUpperCase() || 'UNKNOWN';
    const url = response.config.url || 'unknown';
    console.log(`API ${method} Response (${response.status}) from: ${url}`);
    return response;
  },
  (error: AxiosError) => {
    // Детальный лог ошибки
    const method = error.config?.method?.toUpperCase() || 'UNKNOWN';
    const url = error.config?.url || 'unknown';
    const status = error.response?.status || 'unknown';
    console.error(`API ERROR (${status}) in ${method} ${url}`);
    
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Error setting up request:', error.message);
    }
    
    // Обрабатываем только 401 ошибку для аутентификации
    // 403 (Forbidden) не должен приводить к логауту пользователя
    if (error.response?.status === 401) {
      console.log('Unauthorized access detected');
      
      // Проверяем, связана ли ошибка с аутентификацией
      // Только для /auth/ эндпоинтов выполняем перенаправление на логин
      if (url.includes('/auth/me') || url.includes('/auth/user')) {
        console.log('Authentication error, redirecting to login');
        localStorage.removeItem('token');
        window.location.href = '/login';
      } else {
        console.log('API unauthorized error, but not redirecting to login');
      }
    } else if (error.response?.status === 403) {
      // 403 ошибки (Forbidden) обрабатываем отдельно
      console.log('Forbidden access error, but keeping user logged in');
    }
    
    return Promise.reject(error);
  }
);

export default apiClient; 