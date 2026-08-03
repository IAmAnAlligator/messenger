type Props = {

    title:string;

    onBack():void;

    onLeave():void;

    onDelete():void;

    canLeave:boolean;

    canDelete:boolean;

};


export default function ChatEditHeader({
    title,
    onBack,
    onLeave,
    onDelete,
    canLeave,
    canDelete
}:Props){


return (

<div className="edit-header">


<button onClick={onBack}>
    ← Back
</button>


<h2>
    {title}
</h2>


<div>

{
canLeave &&
<button onClick={onLeave}>
    Leave
</button>
}


{
canDelete &&
<button onClick={onDelete}>
    Delete
</button>
}

</div>


</div>

);

}