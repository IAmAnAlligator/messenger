type Props = {

    value:string;

    onChange(
        value:string
    ):void;


    onSave():void;

};



export default function ChatRenameForm({

    value,

    onChange,

    onSave

}:Props){


return (

<div className="chat-rename-form">

<input

    value={value}

    placeholder="Group name"

    onChange={
        e =>
            onChange(
                e.target.value
            )
    }

/>


<button
    onClick={onSave}
>
    Rename
</button>


</div>

);


}