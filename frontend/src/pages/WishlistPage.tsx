import React, { useState, useMemo, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { wishlistApi } from '../api/wishlist';
import { API_ENDPOINTS } from '../api/endpoints';
import './WishlistPage.css';

interface Wish {
  id: number;
  title: string;
  description: string;
  completed: boolean;
  priority: number;
  category: string;
  dueDate: string | null;
  completedAt: string | null;
}

interface NewWish {
  id?: number;
  title: string;
  description: string;
  priority: number;
  category: string;
  dueDate: string | null;
}

// Компонент для подсветки текста, содержащего поисковый запрос
const HighlightText: React.FC<{ text: string; searchTerm: string }> = ({ text, searchTerm }) => {
  if (!searchTerm || !text) return <>{text}</>;
  
  const trimmedTerm = searchTerm.trim().toLowerCase();
  if (trimmedTerm === '') return <>{text}</>;
  
  const parts = text.split(new RegExp(`(${trimmedTerm})`, 'i'));
  
  return (
    <>
      {parts.map((part, i) => 
        part.toLowerCase() === trimmedTerm
          ? <span key={i} className="highlight">{part}</span>
          : part
      )}
    </>
  );
};

const WishlistPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [filter, setFilter] = useState<'all' | 'completed' | 'pending'>('all');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [useServerFiltering, setUseServerFiltering] = useState(false);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [isSearching, setIsSearching] = useState(false);
  const [newWish, setNewWish] = useState<NewWish>({
    title: '',
    description: '',
    priority: 1,
    category: '',
    dueDate: null,
  });

  // Функция для изменения фильтра
  const handleFilterChange = (newFilter: 'all' | 'completed' | 'pending') => {
    console.log(`WishlistPage: Changing filter from ${filter} to ${newFilter}`);
    setFilter(newFilter);
  };

  // Функция для сброса категории
  const resetCategory = () => {
    setSelectedCategory('');
    // Если используется серверная фильтрация, сбрасываем запрос
    if (useServerFiltering) {
      queryClient.invalidateQueries(['wishes']);
    }
  };

  // Функция для выбора категории
  const handleCategorySelect = useCallback((category: string) => {
    console.log(`WishlistPage: Selecting category: ${category}`);
    setSelectedCategory(category);
    
    // Если используется серверная фильтрация, делаем запрос к API
    if (useServerFiltering && category) {
      queryClient.invalidateQueries(['wishes', 'category', category]);
    }
  }, [useServerFiltering, queryClient]);

  // Функция для выполнения поиска
  const handleSearch = useCallback((term: string) => {
    setSearchTerm(term);
    
    // Если строка пустая, сбрасываем поиск
    if (!term.trim()) {
      setIsSearching(false);
      queryClient.invalidateQueries(['wishes']);
      return;
    }
    
    setIsSearching(true);
    queryClient.invalidateQueries(['wishes', 'search', term]);
  }, [queryClient]);

  // Функция для очистки поиска
  const clearSearch = useCallback(() => {
    setSearchTerm('');
    setIsSearching(false);
    queryClient.invalidateQueries(['wishes']);
  }, [queryClient]);

  // Запрос для получения всех желаний или отфильтрованных по категории/поисковому запросу
  const { data: allWishes, isLoading, error } = useQuery<Wish[]>(
    ['wishes', 
      ...(useServerFiltering && selectedCategory ? ['category', selectedCategory] : []),
      ...(isSearching ? ['search', searchTerm] : [])
    ],
    async () => {
      console.log(`WishlistPage: Fetching wishes with filters...`);
      
      try {
        // Если выполняется поиск, используем API поиска
        if (isSearching && searchTerm.trim()) {
          console.log(`WishlistPage: Searching wishes with term "${searchTerm}"`);
          return await wishlistApi.searchWishes(searchTerm);
        }
        
        // Если выбрана категория и включена серверная фильтрация, делаем запрос по категории
        if (useServerFiltering && selectedCategory) {
          console.log(`WishlistPage: Using server-side filtering for category "${selectedCategory}"`);
          return await wishlistApi.getWishesByCategory(selectedCategory);
        } 
        
        // Иначе получаем все желания
        const url = API_ENDPOINTS.WISHES.BASE;
        console.log('WishlistPage: API URL for all wishes:', url);
        
        const response = await wishlistApi.getWishes();
        console.log('WishlistPage: API response for wishes:', response);
        
        return response || [];
      } catch (err) {
        console.error('WishlistPage: Error fetching wishes:', err);
        throw err;
      }
    },
    {
      onError: (err) => {
        console.error('WishlistPage: Query error:', err);
      },
      staleTime: 30000,
      retry: 2,
    }
  );

  // Получаем уникальные категории для селектора
  const uniqueCategories = useMemo(() => {
    if (!allWishes) return [];
    
    const categories = allWishes
      .map(wish => wish.category)
      .filter((category): category is string => 
        Boolean(category) && category.trim() !== ''
      );
    
    return [...new Set(categories)].sort();
  }, [allWishes]);

  // Add wish mutation
  const addWishMutation = useMutation(
    (wish: NewWish) => wishlistApi.createWish(wish),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['wishes']);
        setShowForm(false);
        resetForm();
      },
      onError: (error) => {
        console.error('WishlistPage: Error adding wish:', error);
      }
    }
  );

  // Toggle wish completion mutation
  const completeMutation = useMutation(
    (id: number) => wishlistApi.completeWish(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['wishes']);
      },
      onError: (error) => {
        console.error('WishlistPage: Error completing wish:', error);
      }
    }
  );

  // Delete wish mutation
  const deleteWishMutation = useMutation(
    (id: number) => wishlistApi.deleteWish(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['wishes']);
      },
      onError: (error) => {
        console.error('WishlistPage: Error deleting wish:', error);
      }
    }
  );

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setNewWish({ ...newWish, [name]: value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    addWishMutation.mutate(newWish);
  };

  const resetForm = () => {
    setNewWish({
      title: '',
      description: '',
      priority: 1,
      category: '',
      dueDate: null,
    });
  };

  // Функция для клиентской фильтрации желаний по поисковому запросу
  const searchFilteredWishes = useMemo(() => {
    if (!allWishes || allWishes.length === 0 || isSearching) return allWishes || [];
    
    if (!searchTerm.trim()) return allWishes;
    
    const term = searchTerm.toLowerCase().trim();
    return allWishes.filter(wish => 
      wish.title.toLowerCase().includes(term) || 
      (wish.description && wish.description.toLowerCase().includes(term)) ||
      (wish.category && wish.category.toLowerCase().includes(term))
    );
  }, [allWishes, searchTerm, isSearching]);

  // Компонент для отображения ошибок
  const ErrorDisplay: React.FC<{ error: any }> = ({ error }) => {
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred';
    const statusCode = error.response?.status || 'N/A';
    
    return (
      <div className="error">
        <h3>Error loading wishes</h3>
        <p>Status: {statusCode}</p>
        <p>Message: {errorMessage}</p>
        <button onClick={() => queryClient.invalidateQueries(['wishes'])}>
          Retry
        </button>
      </div>
    );
  };

  // Функция для клиентской фильтрации желаний
  const filteredWishes = useMemo(() => {
    if (!searchFilteredWishes || searchFilteredWishes.length === 0) return [];
    
    let filtered = [...searchFilteredWishes];
    
    // Фильтруем по статусу завершения
    switch (filter) {
      case 'completed':
        filtered = filtered.filter(wish => wish.completed);
        break;
      case 'pending':
        filtered = filtered.filter(wish => !wish.completed);
        break;
    }
    
    // Фильтруем по категории только если используем клиентскую фильтрацию
    if (!useServerFiltering && selectedCategory) {
      filtered = filtered.filter(wish => wish.category === selectedCategory);
    }
    
    return filtered;
  }, [searchFilteredWishes, filter, selectedCategory, useServerFiltering]);

  // Вычисляем список статусов желаний для вывода сводной информации
  const wishStats = useMemo(() => {
    if (!filteredWishes) return { total: 0, completed: 0, pending: 0 };
    
    const total = filteredWishes.length;
    const completed = filteredWishes.filter(wish => wish.completed).length;
    const pending = total - completed;
    
    return { total, completed, pending };
  }, [filteredWishes]);

  // Переключение режима фильтрации (клиент/сервер)
  const toggleFilteringMode = () => {
    setUseServerFiltering(prev => !prev);
    
    // При изменении режима сбрасываем выбранную категорию и поиск
    if (selectedCategory || searchTerm) {
      setSelectedCategory('');
      clearSearch();
      queryClient.invalidateQueries(['wishes']);
    }
  };

  // Обработчик отправки формы поиска
  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(searchTerm);
  };

  // Состояние для отслеживания ID желания, с которым выполняются действия
  const [activeWishId, setActiveWishId] = useState<number | null>(null);

  if (isLoading) return <div className="loading">Loading wishes...</div>;
  if (error) return <ErrorDisplay error={error} />;

  return (
    <div className="wishlist-page">
      <div className="wishlist-header">
        <h1>My Wishlist</h1>
        
        <div className="search-container">
          <form onSubmit={handleSearchSubmit} className="search-form">
            <input
              type="text"
              placeholder="Search wishes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
            <button type="submit" className="search-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              <span>Search</span>
            </button>
            {searchTerm && (
              <button 
                type="button"
                className="clear-search-button"
                onClick={clearSearch}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                <span>Clear</span>
              </button>
            )}
          </form>
        </div>
        
        <div className="wishlist-stats">
          <span>Total: {wishStats.total}</span>
          <span>Completed: {wishStats.completed}</span>
          <span>Pending: {wishStats.pending}</span>
        </div>
        
        <div className="filter-mode-toggle">
          <label className="switch">
            <input 
              type="checkbox" 
              checked={useServerFiltering}
              onChange={toggleFilteringMode}
            />
            <span className="slider round"></span>
          </label>
          <span className="filter-mode-label">
            {useServerFiltering ? 'Server-side filtering' : 'Client-side filtering'}
          </span>
        </div>
        
        <div className="wishlist-controls">
          <div className="filter-buttons">
            <button 
              className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
              onClick={() => handleFilterChange('all')}
            >
              All
            </button>
            <button 
              className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
              onClick={() => handleFilterChange('pending')}
            >
              Pending
            </button>
            <button 
              className={`filter-btn ${filter === 'completed' ? 'active' : ''}`}
              onClick={() => handleFilterChange('completed')}
            >
              Completed
            </button>
          </div>

          {uniqueCategories.length > 0 && (
            <div className="category-filter">
              <select
                value={selectedCategory}
                onChange={(e) => handleCategorySelect(e.target.value)}
                className="category-select"
              >
                <option value="">All Categories</option>
                {uniqueCategories.map(category => (
                  <option key={category} value={category}>{category}</option>
                ))}
              </select>
              {selectedCategory && (
                <button 
                  className="reset-category-btn"
                  onClick={resetCategory}
                >
                  ×
                </button>
              )}
            </div>
          )}
          
          <button 
            className="add-wish-button"
            onClick={() => setShowForm(!showForm)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            {showForm ? 'Cancel' : 'Add New Wish'}
          </button>
        </div>
      </div>

      {showForm && (
        <div className="wish-form-container">
          <form onSubmit={handleSubmit} className="wish-form">
            <h2 className="form-title">{newWish.id ? 'Edit Wish' : 'Create New Wish'}</h2>
            
            <div className="form-group">
              <label htmlFor="title">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="4 7 4 4 20 4 20 7"></polyline>
                  <line x1="9" y1="20" x2="15" y2="20"></line>
                  <line x1="12" y1="4" x2="12" y2="20"></line>
                </svg>
                Title
              </label>
              <input
                type="text"
                id="title"
                name="title"
                value={newWish.title}
                onChange={handleInputChange}
                required
                placeholder="What do you wish for?"
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="21" y1="6" x2="3" y2="6"></line>
                  <line x1="21" y1="12" x2="3" y2="12"></line>
                  <line x1="21" y1="18" x2="3" y2="18"></line>
                </svg>
                Description
              </label>
              <textarea
                id="description"
                name="description"
                value={newWish.description}
                onChange={handleInputChange}
                rows={3}
                placeholder="Add more details about your wish (optional)"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="priority">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                    <line x1="12" y1="9" x2="12" y2="13"></line>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                  </svg>
                  Priority
                </label>
                <select
                  id="priority"
                  name="priority"
                  value={newWish.priority}
                  onChange={handleInputChange}
                >
                  <option value={1}>Low</option>
                  <option value={2}>Medium</option>
                  <option value={3}>High</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="category">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                    <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                    <line x1="12" y1="22.08" x2="12" y2="12"></line>
                  </svg>
                  Category
                </label>
                <input
                  type="text"
                  id="category"
                  name="category"
                  value={newWish.category}
                  onChange={handleInputChange}
                  list="category-suggestions"
                  placeholder="E.g. Home, Travel, Books, etc."
                />
                {uniqueCategories.length > 0 && (
                  <datalist id="category-suggestions">
                    {uniqueCategories.map(category => (
                      <option key={category} value={category} />
                    ))}
                  </datalist>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="dueDate">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                  </svg>
                  Due Date
                </label>
                <input
                  type="datetime-local"
                  id="dueDate"
                  name="dueDate"
                  value={newWish.dueDate || ''}
                  onChange={handleInputChange}
                />
              </div>
            </div>

            <div className="form-actions">
              <button 
                type="button" 
                className="cancel-btn"
                onClick={() => {
                  setShowForm(false);
                  resetForm();
                }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                Cancel
              </button>
              <button type="submit" className="submit-btn" disabled={addWishMutation.isLoading}>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17 21 17 13 7 13 7 21"></polyline>
                  <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
                {addWishMutation.isLoading ? 'Adding...' : 'Add Wish'}
              </button>
            </div>
          </form>
        </div>
      )}

      {filteredWishes.length > 0 ? (
        <div className="wishes-container">
          {filteredWishes.map((wish) => (
            <div 
              key={wish.id} 
              className={`wish-card ${wish.completed ? 'completed' : ''}`}
            >
              <div className="wish-header">
                <h3 className="wish-title">
                  <span><HighlightText text={wish.title} searchTerm={searchTerm} /></span>
                  {wish.completed && (
                    <span className="completed-badge">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                      </svg>
                      Completed
                    </span>
                  )}
                </h3>
                <div className="wish-actions">
                  {!wish.completed && (
                    <button
                      className="toggle-btn"
                      onClick={() => {
                        setActiveWishId(wish.id);
                        completeMutation.mutate(wish.id);
                      }}
                      disabled={completeMutation.isLoading && activeWishId === wish.id}
                      title="Mark as completed"
                    >
                      {completeMutation.isLoading && activeWishId === wish.id ? (
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="loading-icon">
                          <circle cx="12" cy="12" r="10"></circle>
                          <path d="M12 6v6l4 2"></path>
                        </svg>
                      ) : (
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                      )}
                    </button>
                  )}
                  <button
                    className="delete-btn"
                    onClick={() => {
                      if (window.confirm('Are you sure you want to delete this wish?')) {
                        setActiveWishId(wish.id);
                        deleteWishMutation.mutate(wish.id);
                      }
                    }}
                    disabled={deleteWishMutation.isLoading && activeWishId === wish.id}
                    title="Delete wish"
                  >
                    {deleteWishMutation.isLoading && activeWishId === wish.id ? (
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="loading-icon">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M12 6v6l4 2"></path>
                      </svg>
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                      </svg>
                    )}
                  </button>
                </div>
              </div>
              
              {wish.description && (
                <p className="wish-description">
                  <HighlightText text={wish.description} searchTerm={searchTerm} />
                </p>
              )}
              
              <div className="wish-details">
                {wish.category && (
                  <span 
                    className="wish-category"
                    onClick={() => handleCategorySelect(wish.category)}
                    title="Click to filter by this category"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                    </svg>
                    <HighlightText text={wish.category} searchTerm={searchTerm} />
                  </span>
                )}
                {wish.priority && (
                  <span className={`wish-priority priority-${wish.priority}`}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                    </svg>
                    {wish.priority === 1 ? 'Low' : wish.priority === 2 ? 'Medium' : 'High'}
                  </span>
                )}
                {wish.dueDate && (
                  <span className={`wish-due-date ${new Date(wish.dueDate) < new Date() && !wish.completed ? 'overdue' : ''}`}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="16" y1="2" x2="16" y2="6"></line>
                      <line x1="8" y1="2" x2="8" y2="6"></line>
                      <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    {new Date(wish.dueDate).toLocaleDateString(undefined, { 
                      year: 'numeric', 
                      month: 'short', 
                      day: 'numeric' 
                    })}
                  </span>
                )}
                {wish.completed && wish.completedAt && (
                  <span className="wish-completed-at">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    {new Date(wish.completedAt).toLocaleDateString(undefined, { 
                      year: 'numeric', 
                      month: 'short', 
                      day: 'numeric' 
                    })}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          {(() => {
            // Определяем сообщение в зависимости от фильтров
            const isFiltered = filter !== 'all' || selectedCategory !== '' || isSearching;
            
            if (!isFiltered && (!allWishes || allWishes.length === 0)) {
              return <p>Your wishlist is empty. Add your first wish!</p>;
            }
            
            // Конструируем сообщение о фильтрах
            let message = '';
            
            if (isSearching) {
              message += `matching "${searchTerm}" `;
            }
            
            if (selectedCategory) {
              message += `in category "${selectedCategory}" `;
            }
            
            if (filter === 'completed') {
              message += 'that are completed ';
            } else if (filter === 'pending') {
              message += 'that are pending ';
            }
            
            return <p>No wishes found {message}.</p>;
          })()}
          
          {(filter !== 'all' || selectedCategory || isSearching) && (
            <button 
              className="reset-filters-btn"
              onClick={() => {
                setFilter('all');
                setSelectedCategory('');
                clearSearch();
                if (useServerFiltering) {
                  queryClient.invalidateQueries(['wishes']);
                }
              }}
            >
              Reset All Filters
            </button>
          )}
          
          <button 
            className="add-new-wish-btn"
            onClick={() => setShowForm(true)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            Add a new wish
          </button>
        </div>
      )}
    </div>
  );
};

export default WishlistPage; 