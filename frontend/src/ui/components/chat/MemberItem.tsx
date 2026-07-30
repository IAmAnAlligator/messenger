import type {
    ChatMemberDto
} from "../../../hooks/useChatEdit";



type Props = {


    member:ChatMemberDto;


    canRemove:boolean;


    onRemove(
        userId:number
    ):void;


};



export default function MemberItem({

    member,

    canRemove,

    onRemove

}:Props){



return (

<div className="member-item">


<div>


<b>
{
member.user.username
}
</b>


<div>

Role:
{
member.chatRole
}

</div>


</div>



{
canRemove &&

<button

onClick={() =>
    onRemove(
        member.user.id
    )
}

>

Remove

</button>

}



</div>

);


}