import axios from 'axios';

// Base Axios instance pointing to Spring Boot REST backend (/api/v1)
export const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to attach JWT token from local storage
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor to unwrap standard backend ApiResponse<T> ({ success, data, error })
// and automatically redirect to /login on 401 Unauthorized errors.
apiClient.interceptors.response.use(
  (response) => {
    if (response.data && response.data.data !== undefined) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Clear expired credentials
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');

      // Redirect to login page if not already there
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
      return Promise.reject(new Error('Session expired or unauthorized. Please log in again.'));
    }

    const message =
      error.response?.data?.error?.message ||
      error.response?.data?.message ||
      'An unexpected backend error occurred';
    return Promise.reject(new Error(message));
  }
);
