import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api/github',
  headers: {
    Accept: 'application/json',
  },
});

export default apiClient;