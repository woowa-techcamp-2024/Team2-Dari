import axios from 'axios';

const setupAxiosInterceptors = () => {
    axios.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response && error.response.status === 502) {
                // 502 에러 발생 시 특정 URL로 리다이렉트
                window.location.href = 'https://www.naver.com';
            }
            // return Promise.reject(error);
        }
    );
};

export default setupAxiosInterceptors;