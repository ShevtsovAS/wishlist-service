// Константы эндпоинтов API
export const API_ENDPOINTS = {
  // Аутентификация
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/signup',
    ME: '/api/auth/me',
  },
  
  // Список желаний
  WISHES: {
    BASE: '/api/wishes',
    DETAILS: (id: number) => `/api/wishes/${id}`,
    COMPLETE: (id: number) => `/api/wishes/${id}/complete`,
    COMPLETED: '/api/wishes/completed',
    PENDING: '/api/wishes/pending',
    BY_CATEGORY: (category: string) => `/api/wishes/category/${category}`,
    SEARCH: (query: string) => `/api/wishes/search?term=${encodeURIComponent(query)}`,
  }
}; 