import type {
    MessageDto
} from "../../../../types/message";

import { useAuth } from "../../../../contexts/AuthContext";


type Props = {

    message: MessageDto;

    onDelete(id:number): void;

};


export default function MessageItem({

    message,

    onDelete

}: Props) {


    const { user } =
        useAuth();



    const mine =
        user?.id === message.sender.id;


        console.log(
    "MESSAGE ITEM",
    message.id,
    message.status,
    "mine:",
    mine,
    "user:",
    user?.id,
    "sender:",
    message.sender.id
);


console.log(
    "RENDER STATUS",
    message.id,
    message.status === "READ" ? "✓✓" : "✓"
);



    return (

        <div
            className={
                mine
                    ? "message-row mine"
                    : "message-row"
            }
        >


            <div className="message-bubble">


                <div className="message-header">


                    <b>
                        {
                            mine
                                ? "You"
                                : message.sender.username
                        }
                    </b>



                    {
                        mine && (

                            <button

                                className="delete-btn"

                                onClick={() =>
                                    onDelete(message.id)
                                }

                            >
                                ×

                            </button>

                        )
                    }


                </div>




                <div className="message-text">

                    {message.content}

                </div>




                <div className="message-footer">


                    <span className="message-time">

                        {
                            new Date(
                                message.createdAt
                            )
                            .toLocaleTimeString(
                                [],
                                {
                                    hour: "2-digit",
                                    minute: "2-digit"
                                }
                            )
                        }

                    </span>



                    {
                        mine && (

                            <span
                                className="message-status"
                            >

                                {
                                    message.status === "READ"
                                        ? "✓✓"
                                        : "✓"
                                }

                            </span>

                        )
                    }


                </div>


            </div>


        </div>

    );

}