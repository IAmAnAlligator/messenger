
import {
    useEffect,
    useState
} from "react";

import { api } from "../api/client";

import type {
    UserDto
} from "../types/user";


export type ChatType =
    | "PRIVATE"
    | "GROUP";


export function useChatCreate() {

    const [type, setType] =
        useState<ChatType>("GROUP");


    const [name, setName] =
        useState("");


    const [search, setSearch] =
        useState("");


    const [users, setUsers] =
        useState<UserDto[]>([]);


    const [selectedUsers, setSelectedUsers] =
        useState<UserDto[]>([]);


    const [searchLoading, setSearchLoading] =
        useState(false);


    useEffect(() => {

        const query = search.trim();

        if (!query) {

            setUsers([]);
            setSearchLoading(false);

            return;

        }


        let cancelled = false;


        const timer = setTimeout(async () => {

            try {

                setSearchLoading(true);

                const res = await api.get(
                    "/users/search",
                    {
                        params: {
                            query
                        }
                    }
                );


                if (!cancelled) {

                    setUsers(
                        Array.isArray(res.data)
                            ? res.data
                            : []
                    );

                }

            } catch {

                if (!cancelled) {

                    setUsers([]);

                }

            } finally {

                if (!cancelled) {

                    setSearchLoading(false);

                }

            }

        }, 300);


        return () => {

            cancelled = true;

            clearTimeout(timer);

        };

    }, [search]);


    // Исключаем пользователей,
    // которых уже добавили в группу.
    const availableUsers = users.filter(
        user =>
            !selectedUsers.some(
                selectedUser =>
                    selectedUser.id === user.id
            )
    );


    function addUser(user: UserDto) {

        setSelectedUsers(prev => {

            if (type === "PRIVATE") {

                return [user];

            }


            if (
                prev.some(
                    selectedUser =>
                        selectedUser.id === user.id
                )
            ) {

                return prev;

            }


            return [
                ...prev,
                user
            ];

        });


        setSearch("");

        setUsers([]);

    }


    function removeUser(id: string) {

        setSelectedUsers(prev =>
            prev.filter(
                user => user.id !== id
            )
        );

    }


    async function createChat() {

        if (type === "GROUP") {

            if (!name.trim()) {
                throw new Error(
                    "Group name is required"
                );
            }

            if (selectedUsers.length === 0) {
                throw new Error(
                    "Select at least one member"
                );
            }

        }


        await api.post(
            "/chats",
            {

                type,

                name:
                    type === "GROUP"
                        ? name.trim()
                        : null,

                memberIds:
                    selectedUsers.map(
                        user => user.id
                    )

            }
        );

    }


    return {

        type,
        setType,

        name,
        setName,

        search,
        setSearch,

        users: availableUsers,

        selectedUsers,

        searchLoading,

        addUser,

        removeUser,

        createChat

    };

}