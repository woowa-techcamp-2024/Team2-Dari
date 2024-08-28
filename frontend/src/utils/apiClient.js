import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    // baseURL: 'http://localhost:8080/api/v1',
    withCredentials: true,
});

apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('isAuthenticated');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default apiClient;