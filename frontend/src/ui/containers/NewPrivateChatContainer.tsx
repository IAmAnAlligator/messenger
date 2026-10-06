
import {
    useState
} from "react";

import {
    useLocation,
    useNavigate
} from "react-router-dom";

import {
    usePrivateMessage
} from "../../hooks/usePrivateMessage";

import MessageInput
    from "../components/chat/view/MessageInput";


type Recipient = {

    id: string;

    username: string;

    handle: string;

};


type LocationState = {

    recipient?: Recipient;

    backTo?: string;

};


type Props = {

    recipientId: string;

};


export default function NewPrivateChatContainer({
    recipientId
}: Props) {

    const navigate =
        useNavigate();


    const location =
        useLocation();


    const {
        sending,
        error,
        sendMessage,
        sendFile
    } =
        usePrivateMessage();


    const [
        text,
        setText
    ] =
        useState("");


    const state =
        location.state as LocationState | null;


    const recipient =
        state?.recipient;


    async function handleSend() {

        const content =
            text.trim();


        if (
            !content ||
            sending
        ) {
            return;
        }


        try {

            const result =
                await sendMessage(
                    recipientId,
                    content
                );


            setText("");


            navigate(
                `/chats/${result.chatId}`,
                {
                    replace: true
                }
            );

        } catch {
            // Error already stored in hook.
        }

    }


    async function handleSendFile(
        file: File
    ) {

        if (sending) {
            return;
        }


        try {

            const result =
                await sendFile(
                    recipientId,
                    file
                );


            navigate(
                `/chats/${result.chatId}`,
                {
                    replace: true
                }
            );

        } catch {
            // Error already stored in hook.
        }

    }


    function handleBack() {

        if (state?.backTo) {

            navigate(
                state.backTo
            );

            return;

        }


        navigate(
            "/chats"
        );

    }


    return (

        <div className="chat-page">

            <header className="chat-header">

                <button
                    type="button"
                    onClick={
                        handleBack
                    }
                >
                    ← Back
                </button>


                <div className="chat-header-info">

                    <div className="chat-header-name">

                        {
                            recipient?.username
                            ??
                            "New message"
                        }

                    </div>


                    {
                        recipient?.handle && (

                            <div className="chat-header-handle">

                                @{
                                    recipient.handle
                                }

                            </div>

                        )
                    }

                </div>

            </header>


            {
                error && (

                    <div className="ws-error">

                        {
                            error
                        }

                    </div>

                )
            }


            <div className="messages">

                {
                    !sending && (

                        <div className="new-chat-empty">

                            Start a conversation

                        </div>

                    )
                }


                {
                    sending && (

                        <div className="new-chat-empty">

                            Sending...

                        </div>

                    )
                }

            </div>


            <MessageInput
                value={
                    text
                }
                onChange={
                    setText
                }
                onSend={() => {
                    void handleSend();
                }}
                onSendFile={
                    handleSendFile
                }
            />

        </div>

    );

}
