import {
useNavigate
} from "react-router-dom";


import {
useChatCreate
} from "../../../../hooks/useChatCreate";


import ChatTypeSelector
from "./ChatTypeSelector";


import ChatNameInput
from "./ChatNameInput";


import UserSearch
from "./UserSearch";


import SelectedUsers
from "./SelectedUsers";



export default function ChatCreateContainer(){


const navigate =
    useNavigate();



const chat =
    useChatCreate();



async function submit(){

    await chat.createChat();

    navigate("/chats");

}



return (

<div>


<button
onClick={()=>
navigate("/chats")
}
>
Back
</button>



<h2>
Create chat
</h2>



<ChatTypeSelector

value={chat.type}

onChange={chat.setType}

/>



{
chat.type==="GROUP" &&

<ChatNameInput

value={chat.name}

onChange={chat.setName}

/>

}



<UserSearch

value={chat.search}

users={chat.users}

loading={chat.searchLoading}

onChange={chat.setSearch}

onSelect={chat.addUser}

/>



<SelectedUsers

users={chat.selectedUsers}

onRemove={chat.removeUser}

/>



<button

disabled={
chat.selectedUsers.length===0 ||
(
chat.type==="GROUP"
&&
!chat.name.trim()
)

}

onClick={submit}

>

Create

</button>



</div>

);

}