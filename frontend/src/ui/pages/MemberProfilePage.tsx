import {
    useEffect,
    useState
} from "react";

import {
    useLocation,
    useNavigate,
    useParams,
    useSearchParams
} from "react-router-dom";

import {
    api
} from "../../api/client";

import "../styles/profilePage.css";


type UserProfileResponse = {

    id: string;

    username: string;

    handle: string;

};


type ExistingPrivateChatResponse = {

    id: string;

};


export default function MemberProfilePage() {

    const navigate =
        useNavigate();


    const location =
        useLocation();


    const {
        chatId,
        userId
    } =
        useParams();


    const [
        profile,
        setProfile
    ] =
        useState<UserProfileResponse | null>(
            null
        );


    const [
        loading,
        setLoading
    ] =
        useState(true);


    const [
        openingChat,
        setOpeningChat
    ] =
        useState(false);


    const [
        searchParams
    ] =
        useSearchParams();


    const [
        error,
        setError
    ] =
        useState<string | null>(
            null
        );


    useEffect(() => {

        if (!chatId || !userId) {

            setError(
                "Invalid profile"
            );

            setLoading(false);

            return;

        }


        async function loadProfile() {

            try {

                const response =
                    await api.get<UserProfileResponse>(
                        `/chats/${chatId}/members/${userId}/profile`
                    );


                setProfile(
                    response.data
                );


            } catch (error) {

                console.error(
                    "Failed to load member profile",
                    error
                );


                setError(
                    "Failed to load profile"
                );


            } finally {

                setLoading(false);

            }

        }


        void loadProfile();

    }, [
        chatId,
        userId
    ]);


    function handleBack() {

        if (!chatId) {

            navigate(
                "/chats"
            );

            return;

        }


        const from =
            searchParams.get(
                "from"
            );


        if (from === "edit") {

            navigate(
                `/chats/${chatId}/edit`
            );

            return;

        }


        navigate(
            `/chats/${chatId}`
        );

    }


    async function handleSendMessage() {

        if (
            !profile ||
            openingChat
        ) {
            return;
        }


        try {

            setOpeningChat(true);

            setError(null);


            const response =
                await api.get<ExistingPrivateChatResponse>(
                    `/chats/private-with/${profile.id}`,
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
                    `/chats/new?recipientId=${encodeURIComponent(profile.id)}`,
                    {
                        state: {
                            recipient: profile,

                            backTo:
                                location.pathname +
                                location.search
                        }
                    }
                );

                return;

            }

        } catch (error) {

            console.error(
                "Failed to open private chat",
                error
            );


            setError(
                "Failed to open chat"
            );

        } finally {

            setOpeningChat(false);

        }

    }


    if (loading) {

        return (

            <div className="profile-page">

                <header className="profile-header">

                    <button
                        type="button"
                        onClick={
                            handleBack
                        }
                    >
                        ← Back
                    </button>


                    <h2>
                        Profile
                    </h2>

                </header>


                <main className="profile-content">

                    Loading...

                </main>

            </div>

        );

    }


    if (error || !profile) {

        return (

            <div className="profile-page">

                <header className="profile-header">

                    <button
                        type="button"
                        onClick={
                            handleBack
                        }
                    >
                        ← Back
                    </button>


                    <h2>
                        Profile
                    </h2>

                </header>


                <main className="profile-content">

                    {
                        error
                        ??
                        "Profile not found"
                    }

                </main>

            </div>

        );

    }


    return (

        <div className="profile-page">

            <header className="profile-header">

                <button
                    type="button"
                    onClick={
                        handleBack
                    }
                >
                    ← Back
                </button>


                <h2>
                    Profile
                </h2>

            </header>


            <main className="profile-content">

                <div className="profile-field">

                    <span className="profile-label">
                        Username
                    </span>

                    <span className="profile-value">
                        {
                            profile.username
                        }
                    </span>

                </div>


                <div className="profile-field">

                    <span className="profile-label">
                        Handle
                    </span>

                    <span className="profile-value">
                        @{
                            profile.handle
                        }
                    </span>

                </div>


                <button
                    type="button"
                    className="profile-send-message-button"
                    disabled={
                        openingChat
                    }
                    onClick={() => {
                        void handleSendMessage();
                    }}
                >
                    {
                        openingChat
                            ? "Opening..."
                            : "Send message"
                    }
                </button>

            </main>

        </div>

    );

}

