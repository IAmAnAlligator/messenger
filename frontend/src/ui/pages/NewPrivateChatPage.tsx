import {
    useSearchParams
} from "react-router-dom";

import NewPrivateChatContainer
    from "../containers/NewPrivateChatContainer";


export default function NewPrivateChatPage() {

    const [
        searchParams
    ] =
        useSearchParams();


    const recipientId =
        searchParams.get(
            "recipientId"
        );


    if (!recipientId) {
        return (
            <div>
                User not found
            </div>
        );
    }


    return (
        <NewPrivateChatContainer
            recipientId={
                recipientId
            }
        />
    );

}
