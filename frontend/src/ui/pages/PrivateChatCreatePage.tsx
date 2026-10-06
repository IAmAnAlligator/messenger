import {
    useNavigate
} from "react-router-dom";

import {
    api
} from "../../api/client";

import {
    useChatCreate
} from "../../hooks/useChatCreate";

import UserSearch
    from "../components/chat/create/UserSearch";


type ExistingPrivateChatResponse = {

    id: string;

};


export default function PrivateChatCreatePage() {

    const navigate =
        useNavigate();


    const chat =
        useChatCreate();


    async function handleSelectUser(
        user: typeof chat.users[number]
    ) {

        try {

            const response =
                await api.get<ExistingPrivateChatResponse>(
                    `/chats/private-with/${user.id}`,
                    {
                        validateStatus: (
                            status
                        ) =>
                            status === 200 ||
                            status === 404
                    }
                );


            if (response.status === 200) {

                navigate(
                    `/chats/${response.data.id}`
                );

                return;

            }


            if (response.status === 404) {

                navigate(
                    `/chats/new?recipientId=${encodeURIComponent(user.id)}`,
                    {
                        state: {
                            recipient: user,

                            backTo:
                                "/chats/create/private"
                        }
                    }
                );

            }

        } catch (error) {

            console.error(
                "Failed to open private chat",
                error
            );

        }

    }


    return (

        <div>

            <button
                type="button"
                onClick={() =>
                    navigate("/chats")
                }
            >
                Back
            </button>


            <h2>
                New private message
            </h2>


            <UserSearch

                value={
                    chat.search
                }

                users={
                    chat.users
                }

                loading={
                    chat.searchLoading
                }

                onChange={
                    chat.setSearch
                }

                onSelect={
                    user => {
                        void handleSelectUser(user);
                    }
                }

            />

        </div>

    );

}
