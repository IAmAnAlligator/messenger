import type {
    UserDto
} from "../../../../types/user";

type Props = {
    users: UserDto[];

    onRemove(
        id: string
    ): void;
};

export default function SelectedUsers({
    users,
    onRemove
}: Props) {
    return (
        <div>
            {users.map(user => (
                <div key={user.id}>
                    {user.username}

                    <button
                        onClick={() =>
                            onRemove(user.id)
                        }
                    >
                        ×
                    </button>
                </div>
            ))}
        </div>
    );
}
