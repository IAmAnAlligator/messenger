type Props = {

    onProfile(): void;

    onLogout(): void;

};


export default function ChatsHeader({
    onProfile,
    onLogout
}: Props) {


    return (

        <header className="chats-header">

            <h2>
                Chats
            </h2>


            <div className="chats-header-actions">

                <button
                    onClick={onProfile}
                >
                    Profile
                </button>


                <button
                    onClick={onLogout}
                >
                    Logout
                </button>

            </div>

        </header>

    );

}