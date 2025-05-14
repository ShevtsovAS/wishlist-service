import apiClient from './client';
import { API_ENDPOINTS } from './endpoints';

// Типы для запросов и ответов
export interface Wish {
  id: number;
  title: string;
  description: string;
  completed: boolean;
  priority: number;
  category: string;
  dueDate: string | null;
  completedAt: string | null;
}

export interface WishlistDTO {
  wishes: Wish[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}

export interface CreateWishRequest {
  title: string;
  description: string;
  priority: number;
  category: string;
  dueDate: string | null;
}

export interface UpdateWishRequest {
  title?: string;
  description?: string;
  priority?: number;
  category?: string;
  dueDate?: string | null;
}

// Методы для работы со списком желаний
export const wishlistApi = {
  // Получение всех желаний пользователя
  getWishes: async (): Promise<Wish[]> => {
    try {
      console.log('wishlistApi: Making GET request to:', API_ENDPOINTS.WISHES.BASE);
      const response = await apiClient.get<WishlistDTO | Wish[]>(API_ENDPOINTS.WISHES.BASE);
      console.log('wishlistApi: Response structure:', typeof response.data);
      
      // Проверяем формат данных
      if (Array.isArray(response.data)) {
        console.log('wishlistApi: Response is an array with', response.data.length, 'items');
        return response.data;
      } else if (response.data && typeof response.data === 'object') {
        // Проверяем, есть ли поле wishes
        if ('wishes' in response.data && Array.isArray(response.data.wishes)) {
          console.log(`wishlistApi: Found wishes array with ${response.data.wishes.length} items`);
          return response.data.wishes;
        }
        
        // Проверяем, есть ли поле content (для пагинации)
        if ('content' in response.data && Array.isArray(response.data.content)) {
          console.log(`wishlistApi: Found content array with ${response.data.content.length} items`);
          return response.data.content;
        }
        
        console.warn('wishlistApi: Response is not in expected format:', response.data);
        return [];
      } else {
        console.warn('wishlistApi: Unexpected response format:', response.data);
        return [];
      }
    } catch (error) {
      console.error('wishlistApi: Error fetching wishes:', error);
      throw error;
    }
  },
  
  // Получение завершенных желаний - теперь возвращает пустой массив при ошибке
  getCompletedWishes: async (): Promise<Wish[]> => {
    try {
      console.log('wishlistApi: Making GET request to:', API_ENDPOINTS.WISHES.COMPLETED);
      const response = await apiClient.get<WishlistDTO | Wish[]>(API_ENDPOINTS.WISHES.COMPLETED);
      
      // Проверяем формат данных
      if (Array.isArray(response.data)) {
        console.log('wishlistApi: Completed wishes response is an array with', response.data.length, 'items');
        return response.data;
      } else if (response.data && typeof response.data === 'object') {
        // Проверяем, есть ли поле wishes
        if ('wishes' in response.data && Array.isArray(response.data.wishes)) {
          console.log(`wishlistApi: Found wishes array with ${response.data.wishes.length} completed items`);
          return response.data.wishes;
        }
        
        // Проверяем, есть ли поле content (для пагинации)
        if ('content' in response.data && Array.isArray(response.data.content)) {
          console.log(`wishlistApi: Found content array with ${response.data.content.length} completed items`);
          return response.data.content;
        }
        
        console.warn('wishlistApi: Completed wishes response is not in expected format:', response.data);
        return [];
      } else {
        console.warn('wishlistApi: Unexpected completed wishes response format:', response.data);
        return [];
      }
    } catch (error: any) {
      console.error('wishlistApi: Error fetching completed wishes:', error);
      if (error.response) {
        console.error('- Status:', error.response.status);
        console.error('- Data:', error.response.data);
      }
      // Теперь при ошибке просто возвращаем пустой массив
      return [];
    }
  },
  
  // Получение незавершенных желаний - теперь возвращает пустой массив при ошибке
  getPendingWishes: async (): Promise<Wish[]> => {
    try {
      console.log('wishlistApi: Making GET request to:', API_ENDPOINTS.WISHES.PENDING);
      const response = await apiClient.get<WishlistDTO | Wish[]>(API_ENDPOINTS.WISHES.PENDING);
      
      // Проверяем формат данных
      if (Array.isArray(response.data)) {
        console.log('wishlistApi: Pending wishes response is an array with', response.data.length, 'items');
        return response.data;
      } else if (response.data && typeof response.data === 'object') {
        // Проверяем, есть ли поле wishes
        if ('wishes' in response.data && Array.isArray(response.data.wishes)) {
          console.log(`wishlistApi: Found wishes array with ${response.data.wishes.length} pending items`);
          return response.data.wishes;
        }
        
        // Проверяем, есть ли поле content (для пагинации)
        if ('content' in response.data && Array.isArray(response.data.content)) {
          console.log(`wishlistApi: Found content array with ${response.data.content.length} pending items`);
          return response.data.content;
        }
        
        console.warn('wishlistApi: Pending wishes response is not in expected format:', response.data);
        return [];
      } else {
        console.warn('wishlistApi: Unexpected pending wishes response format:', response.data);
        return [];
      }
    } catch (error: any) {
      console.error('wishlistApi: Error fetching pending wishes:', error);
      if (error.response) {
        console.error('- Status:', error.response.status);
        console.error('- Data:', error.response.data);
      }
      // Теперь при ошибке просто возвращаем пустой массив
      return [];
    }
  },
  
  // Получение желаний по категории
  getWishesByCategory: async (category: string): Promise<Wish[]> => {
    if (!category || category.trim() === '') {
      console.log('wishlistApi: Empty category provided, returning all wishes');
      return wishlistApi.getWishes();
    }
    
    try {
      console.log(`wishlistApi: Making GET request to get wishes for category "${category}"`);
      const response = await apiClient.get<WishlistDTO | Wish[]>(API_ENDPOINTS.WISHES.BY_CATEGORY(category));
      
      // Проверяем формат данных
      if (Array.isArray(response.data)) {
        console.log(`wishlistApi: Category "${category}" response is an array with ${response.data.length} items`);
        return response.data;
      } else if (response.data && typeof response.data === 'object') {
        // Проверяем, есть ли поле wishes
        if ('wishes' in response.data && Array.isArray(response.data.wishes)) {
          console.log(`wishlistApi: Found wishes array with ${response.data.wishes.length} items for category "${category}"`);
          return response.data.wishes;
        }
        
        // Проверяем, есть ли поле content (для пагинации)
        if ('content' in response.data && Array.isArray(response.data.content)) {
          console.log(`wishlistApi: Found content array with ${response.data.content.length} items for category "${category}"`);
          return response.data.content;
        }
        
        console.warn(`wishlistApi: Category "${category}" response is not in expected format:`, response.data);
        return [];
      } else {
        console.warn(`wishlistApi: Unexpected category "${category}" response format:`, response.data);
        return [];
      }
    } catch (error: any) {
      console.error(`wishlistApi: Error fetching wishes for category "${category}":`, error);
      
      if (error.response) {
        console.error('- Status:', error.response.status);
        console.error('- Data:', error.response.data);
        
        // Если сервер вернул 404, это может означать что категория не найдена
        if (error.response.status === 404) {
          console.log(`wishlistApi: Category "${category}" not found, returning empty array`);
          return [];
        }
      }
      
      // При других ошибках возвращаем пустой массив
      return [];
    }
  },
  
  // Поиск желаний
  searchWishes: async (query: string): Promise<Wish[]> => {
    if (!query || query.trim() === '') {
      console.log('wishlistApi: Empty search query provided, returning all wishes');
      return wishlistApi.getWishes();
    }
    
    try {
      console.log(`wishlistApi: Searching wishes with query "${query}"`);
      const response = await apiClient.get<WishlistDTO | Wish[]>(API_ENDPOINTS.WISHES.SEARCH(query));
      
      // Проверяем формат данных
      if (Array.isArray(response.data)) {
        console.log(`wishlistApi: Search query "${query}" returned array with ${response.data.length} items`);
        return response.data;
      } else if (response.data && typeof response.data === 'object') {
        // Проверяем, есть ли поле wishes
        if ('wishes' in response.data && Array.isArray(response.data.wishes)) {
          console.log(`wishlistApi: Found wishes array with ${response.data.wishes.length} items for search "${query}"`);
          return response.data.wishes;
        }
        
        // Проверяем, есть ли поле content (для пагинации)
        if ('content' in response.data && Array.isArray(response.data.content)) {
          console.log(`wishlistApi: Found content array with ${response.data.content.length} items for search "${query}"`);
          return response.data.content;
        }
        
        console.warn(`wishlistApi: Search response for "${query}" is not in expected format:`, response.data);
        return [];
      } else {
        console.warn(`wishlistApi: Unexpected search response format for "${query}":`, response.data);
        return [];
      }
    } catch (error: any) {
      console.error(`wishlistApi: Error searching wishes with query "${query}":`, error);
      
      if (error.response) {
        console.error('- Status:', error.response.status);
        console.error('- Data:', error.response.data);
        
        // Проверяем различные коды ошибок
        if (error.response.status === 400) {
          console.log(`wishlistApi: Bad request for search "${query}", probably invalid query format`);
        } else if (error.response.status === 404) {
          console.log(`wishlistApi: No results found for search "${query}"`);
        }
      }
      
      // В случае ошибки возвращаем пустой массив
      return [];
    }
  },
  
  // Получение конкретного желания по ID
  getWishById: async (id: number): Promise<Wish | null> => {
    try {
      console.log('wishlistApi: Making GET request to:', API_ENDPOINTS.WISHES.DETAILS(id));
      const response = await apiClient.get<Wish>(API_ENDPOINTS.WISHES.DETAILS(id));
      console.log('wishlistApi: Wish details response:', response.data);
      return response.data;
    } catch (error) {
      console.error(`wishlistApi: Error fetching wish with id ${id}:`, error);
      return null;
    }
  },
  
  // Создание нового желания
  createWish: async (wish: CreateWishRequest): Promise<Wish> => {
    try {
      console.log('wishlistApi: Making POST request to:', API_ENDPOINTS.WISHES.BASE);
      console.log('wishlistApi: With data:', wish);
      const response = await apiClient.post<Wish>(API_ENDPOINTS.WISHES.BASE, wish);
      console.log('wishlistApi: Create wish response:', response.data);
      return response.data;
    } catch (error) {
      console.error('wishlistApi: Error creating wish:', error);
      throw error;
    }
  },
  
  // Обновление желания
  updateWish: async (id: number, wish: UpdateWishRequest): Promise<Wish> => {
    try {
      console.log('wishlistApi: Making PUT request to:', API_ENDPOINTS.WISHES.DETAILS(id));
      console.log('wishlistApi: With data:', wish);
      const response = await apiClient.put<Wish>(API_ENDPOINTS.WISHES.DETAILS(id), wish);
      console.log('wishlistApi: Update wish response:', response.data);
      return response.data;
    } catch (error) {
      console.error(`wishlistApi: Error updating wish with id ${id}:`, error);
      throw error;
    }
  },
  
  // Завершение желания
  completeWish: async (id: number): Promise<Wish> => {
    try {
      console.log('wishlistApi: Making PATCH request to:', API_ENDPOINTS.WISHES.COMPLETE(id));
      const response = await apiClient.patch<Wish>(API_ENDPOINTS.WISHES.COMPLETE(id));
      console.log('wishlistApi: Complete wish response:', response.data);
      return response.data;
    } catch (error) {
      console.error(`wishlistApi: Error completing wish with id ${id}:`, error);
      throw error;
    }
  },
  
  // Удаление желания
  deleteWish: async (id: number): Promise<void> => {
    try {
      console.log('wishlistApi: Making DELETE request to:', API_ENDPOINTS.WISHES.DETAILS(id));
      await apiClient.delete(API_ENDPOINTS.WISHES.DETAILS(id));
      console.log('wishlistApi: Wish deleted successfully');
    } catch (error) {
      console.error(`wishlistApi: Error deleting wish with id ${id}:`, error);
      throw error;
    }
  }
};