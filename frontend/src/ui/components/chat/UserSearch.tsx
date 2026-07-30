import type {
 UserDto
} from "../../../hooks/useChatCreate";


type Props={

value:string;

users:UserDto[];

loading:boolean;

onChange(value:string):void;

onSelect(user:UserDto):void;

};


export default function UserSearch({

value,

users,

loading,

onChange,

onSelect

}:Props){


return (

<div>


<input

value={value}

placeholder="Search users"

onChange={
e=>onChange(
    e.target.value
)
}

/>


{
loading &&
<p>
Searching...
</p>
}



{
users.map(user=>(


<div

key={user.id}

onClick={()=>
onSelect(user)
}

style={{
cursor:"pointer",
padding:6
}}

>

{user.username}


</div>


))
}



</div>

);

}