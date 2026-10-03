import MemberItem from "./MemberItem";

import type {
    ChatMemberDto
} from "../../../../hooks/useChatEdit";


type Props = {

    members: ChatMemberDto[];

    currentUserId: string | null;

    canRemove: boolean;

    onRemove(
        userId: string
    ): void;

    onUserProfile(
        userId: string
    ): void;

};


export default function MemberList({

    members,

    currentUserId,

    canRemove,

    onRemove,

    onUserProfile

}: Props) {


    const sortedMembers =
        [...members].sort((a, b) => {

            const aIsCurrentUser =
                a.user.id === currentUserId;

            const bIsCurrentUser =
                b.user.id === currentUserId;


            if (
                aIsCurrentUser &&
                !bIsCurrentUser
            ) {

                return -1;

            }


            if (
                !aIsCurrentUser &&
                bIsCurrentUser
            ) {

                return 1;

            }


            return 0;

        });


    return (

        <div className="member-list">

            {
                sortedMembers.map(
                    member => (

                        <MemberItem

                            key={
                                member.user.id
                            }

                            member={
                                member
                            }

                            currentUserId={
                                currentUserId
                            }

                            canRemove={
                                canRemove &&
                                member.chatRole !== "ADMIN" &&
                                member.user.id !== currentUserId
                            }

                            onRemove={
                                onRemove
                            }

                            onUserProfile={
                                onUserProfile
                            }

                        />

                    )
                )
            }

        </div>

    );

}