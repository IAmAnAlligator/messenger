import axios from "axios";

export async function refreshToken() {

    const response =
        await axios.post(
            "http://localhost:8080/api/auth/refresh",
            {},
            {
                withCredentials: true
            }
        );

    return response.data;
}