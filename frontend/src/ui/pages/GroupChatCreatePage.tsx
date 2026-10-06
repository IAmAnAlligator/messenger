import {
    useNavigate
} from "react-router-dom";

import {
    useChatCreate
} from "../../hooks/useChatCreate";

import ChatNameInput
    from "../components/chat/create/ChatNameInput";

import UserSearch
    from "../components/chat/create/UserSearch";

import SelectedUsers
    from "../components/chat/create/SelectedUsers";


export default function GroupChatCreatePage() {

    const navigate =
        useNavigate();


    const chat =
        useChatCreate();


    async function submit() {

        await chat.createChat();

        navigate(
            "/chats"
        );

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
                Create group chat
            </h2>


            <ChatNameInput

                value={
                    chat.name
                }

                onChange={
                    chat.setName
                }

            />


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
                    chat.addUser
                }

            />


            <SelectedUsers

                users={
                    chat.selectedUsers
                }

                onRemove={
                    chat.removeUser
                }

            />


            <button

                type="button"

                disabled={
                    chat.selectedUsers.length === 0 ||
                    !chat.name.trim()
                }

                onClick={() => {
                    void submit();
                }}

            >
                Create group
            </button>

        </div>

    );

}
