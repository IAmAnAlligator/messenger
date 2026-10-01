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
};

export default function MemberList({
    members,
    currentUserId,
    canRemove,
    onRemove
}: Props) {
    return (
        <div className="member-list">

            {members.map(member => (
                <MemberItem
                    key={member.user.id}
                    member={member}
                    canRemove={
                        canRemove &&
                        member.chatRole !== "ADMIN" &&
                        member.user.id !== currentUserId
                    }
                    onRemove={onRemove}
                />
            ))}

        </div>
    );
}
