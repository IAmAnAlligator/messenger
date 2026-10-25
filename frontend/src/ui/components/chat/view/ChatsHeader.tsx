type Props = {

    onProfile(): void;

    onLogout(): void;

    onNotificationSettings: () => void;
};


export default function ChatsHeader({
    onProfile,
    onLogout,
    onNotificationSettings
}: Props) {


    return (

        <header className="chats-header">

            <h2>
                Chats
            </h2>


            <div className="chats-header-actions">


                    <button
        type="button"
        className="chats-header-settings"
        onClick={onNotificationSettings}
        aria-label="settings"
        title="settings"
    >
        ⚙
    </button>

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