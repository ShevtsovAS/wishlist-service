import apiClient from './client';
import { API_ENDPOINTS } from './endpoints';

// Типы для запросов и ответов
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  username: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
}

// Методы для работы с аутентификацией
export const authApi = {
  // Вход в систему
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(API_ENDPOINTS.AUTH.LOGIN, data);
    return response.data;
  },
  
  // Регистрация
  register: async (data: RegisterRequest): Promise<void> => {
    await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, data);
  },
  
  // Получение данных текущего пользователя
  getCurrentUser: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>(API_ENDPOINTS.AUTH.ME);
    return response.data;
  }
}; 