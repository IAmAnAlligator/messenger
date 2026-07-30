import {
    useParams,
    useNavigate
} from "react-router-dom";


import {
    useChatEdit
} from "../../hooks/useChatEdit";


import ChatEditHeader
from "../components/chat/ChatEditHeader";


import MemberList
from "../components/chat/MemberList";


import AddMemberForm
from "../components/chat/AddMemberForm";


import ChatRenameForm
from "../components/chat/ChatRenameForm";


import "../styles/chatEditPage.css";



export default function ChatEditPage(){


const {
    chat,
    members,
    loading,

    chatName,
    setChatName,

    searchUsername,
    setSearchUsername,

    users,

    currentUserId,

    permissions,

    renameChat,
    addMember,
    removeMember,
    leaveChat,
    deleteChat

}=useChatEdit(
    useParams().chatId
);



const navigate =
    useNavigate();



if(loading)
    return <p>Loading...</p>;



return (

<div className="chat-edit-page">


<ChatEditHeader

title={
chat?.type==="PRIVATE"
?"Private Chat"
:"Group Settings"
}

onBack={()=>
navigate(`/chats/${chat?.id}`)
}

onLeave={leaveChat}

onDelete={deleteChat}

canLeave={
permissions.canLeave
}

canDelete={
permissions.canDelete
}

/>



{
permissions.canRename &&

<ChatRenameForm

value={chatName}

onChange={setChatName}

onSave={renameChat}

/>

}



{
permissions.canAdd &&

<AddMemberForm

value={searchUsername}

onChange={setSearchUsername}

users={users}

onAdd={addMember}

/>

}



<MemberList

members={members}

currentUserId={currentUserId}

canRemove={
permissions.canRemove
}

onRemove={removeMember}

/>


</div>

);

}