import type {
 UserDto
} from "../../../../hooks/useChatCreate";


type Props={

users:UserDto[];

onRemove(id:number):void;

};


export default function SelectedUsers({

users,

onRemove

}:Props){


return (

<div>


{
users.map(user=>(

<div key={user.id}>


{user.username}


<button

onClick={()=>
onRemove(user.id)
}

>
×
</button>


</div>


))
}


</div>

);

}