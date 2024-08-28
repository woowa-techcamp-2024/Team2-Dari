import axios from 'axios';

const waitClient = axios.create({
    baseURL: 'http://localhost:8081/api/v1',
    withCredentials: true,
});

waitClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('isAuthenticated');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default waitClient;