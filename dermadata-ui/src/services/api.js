import axios from 'axios';

const API_BASE = '/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const analyzeIngredients = async (request) => {
    const response = await api.post('/analyze', request);
    return response.data;
};

export const extractIngredients = async (request) => {
    const response = await api.post('/extract', request);
    return response.data;
};

export const getAllIngredients = async () => {
    const response = await api.get('/ingredients');
    return response.data;
};

export const searchIngredients = async (query) => {
    const response = await api.get(`/ingredients/search?query=${encodeURIComponent(query)}`);
    return response.data;
};

export const getProhibitedIngredients = async () => {
    const response = await api.get('/ingredients/prohibited');
    return response.data;
};

export const healthCheck = async () => {
    const response = await api.get('/health');
    return response.data;
};

export default api;
