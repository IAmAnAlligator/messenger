type Props = {

    onLogout():void;

};


export default function ChatsHeader({
    onLogout
}:Props){


return (

<header className="chats-header">


    <h2>
        Chats
    </h2>


    <button
        onClick={onLogout}
    >
        Logout
    </button>


</header>

);

}