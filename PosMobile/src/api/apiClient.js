import axios from 'axios';
import { Platform } from 'react-native';

const API_BASE_URL = Platform.OS === 'android' ? 'http://10.0.2.2:8080/api/v1' : 'http://localhost:8080/api/v1';

const mayKhachApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

mayKhachApi.interceptors.response.use(
  (response) => {
    if (response.data && response.data.data !== undefined) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'Lỗi kết nối';
    return Promise.reject(new Error(message));
  }
);

export default mayKhachApi;