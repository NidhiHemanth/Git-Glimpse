import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8081/api/github',
  headers: {
    Accept: 'application/json',
  },
});

export default apiClient;