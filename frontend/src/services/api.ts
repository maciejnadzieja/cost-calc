import axios from 'axios';
import { getToken } from './keycloak';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const searchServices = (query: string) => {
  return apiClient.get(`/services?query=${query}`);
};

export const searchMaterials = (query: string) => {
  return apiClient.get(`/materials?query=${query}`);
};

export const createCalculation = (calculation: any) => {
  return apiClient.post('/calculations', calculation);
};

export const updateCalculation = (id: string, calculation: any) => {
  return apiClient.put(`/calculations/${id}`, calculation);
};

export const getAllCalculations = () => {
  return apiClient.get('/calculations');
};

export const deleteCalculation = (id: string) => {

  return apiClient.delete(`/calculations/${id}`);

};



export const getCalculationById = (id: string) => {

  return apiClient.get(`/calculations/${id}`);

};



export default apiClient;


