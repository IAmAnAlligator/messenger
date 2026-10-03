import type {
    ChatMemberDto
} from "../../../../hooks/useChatEdit";


type Props = {

    member: ChatMemberDto;

    currentUserId: string | null;

    canRemove: boolean;

    onRemove(
        userId: string
    ): void;

    onUserProfile(
        userId: string
    ): void;

};


export default function MemberItem({

    member,

    currentUserId,

    canRemove,

    onRemove,

    onUserProfile

}: Props) {

    const isCurrentUser =
        member.user.id === currentUserId;


    return (

        <div className="member-item">

            <div>

                {
                    isCurrentUser ? (

                        <b>
                            {member.user.username} (you)
                        </b>

                    ) : (

                        <button
                            type="button"
                            className="member-name-button"
                            onClick={() =>
                                onUserProfile(
                                    member.user.id
                                )
                            }
                        >
                            {member.user.username}
                        </button>

                    )
                }


                <div>
                    Role:
                    {member.chatRole}
                </div>

            </div>


            {
                canRemove && (

                    <button
                        type="button"
                        onClick={() =>
                            onRemove(
                                member.user.id
                            )
                        }
                    >
                        Remove
                    </button>

                )
            }

        </div>

    );

}