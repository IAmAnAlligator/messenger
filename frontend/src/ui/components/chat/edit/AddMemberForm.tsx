import type {
    UserDto
} from "../../../../hooks/useChatEdit";



type Props = {

    value:string;

    onChange(
        value:string
    ):void;


    users:UserDto[];


    onAdd(
        user:UserDto
    ):void;

};



export default function AddMemberForm({

    value,

    onChange,

    users,

    onAdd

}:Props){



return (

<div className="add-member-form">


<h3>
    Add member
</h3>



<input

    value={value}

    placeholder="Search users"

    onChange={
        e =>
            onChange(
                e.target.value
            )
    }

/>




{
users.map(user => (

<div

key={user.id}


>

<span>
    {user.username}
</span>


<button

onClick={() =>
    onAdd(user)
}

>
    Add
</button>


</div>

))
}



</div>

);


}