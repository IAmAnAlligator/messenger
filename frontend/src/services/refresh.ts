import axios from "axios";
import { API_BASE_URL } from "../config/apiConfig";

export async function refreshToken() {
    const response = await axios.post(
        `${API_BASE_URL}/auth/refresh`,
        {},
        {
            withCredentials: true
        }
    );

    return response.data;
}