import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8888/nas'
});

export default axiosClient;