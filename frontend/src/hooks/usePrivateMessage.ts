import {
    useState
} from "react";

import {
    api
} from "../api/client";


export type PrivateMessageResult = {

    id: string;

    chatId: string;

};


export function usePrivateMessage() {

    const [
        sending,
        setSending
    ] =
        useState(false);


    const [
        error,
        setError
    ] =
        useState<string | null>(null);


    async function sendMessage(
        recipientId: string,
        content: string
    ): Promise<PrivateMessageResult> {

        try {

            setSending(true);

            setError(null);


            const response =
                await api.post<PrivateMessageResult>(
                    `/private-messages/${recipientId}`,
                    {
                        content
                    }
                );


            return response.data;

        } catch (error) {

            setError(
                "Failed to send message"
            );

            throw error;

        } finally {

            setSending(false);

        }

    }


    async function sendFile(
        recipientId: string,
        file: File
    ): Promise<PrivateMessageResult> {

        try {

            setSending(true);

            setError(null);


            const formData =
                new FormData();


            formData.append(
                "file",
                file
            );


            const response =
                await api.post<PrivateMessageResult>(
                    `/private-messages/${recipientId}/file`,
                    formData
                );


            return response.data;

        } catch (error) {

            setError(
                "Failed to send file"
            );

            throw error;

        } finally {

            setSending(false);

        }

    }


    return {

        sending,

        error,

        sendMessage,

        sendFile

    };

}
