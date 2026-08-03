type Props={

value:string;

onChange(value:string):void;

onSend():void;

};


export default function MessageInput({

value,

onChange,

onSend

}:Props){


return (

<div className="message-input">


<input

value={value}

onChange={e=>
onChange(e.target.value)
}

onKeyDown={e=>{

if(e.key==="Enter")
onSend();

}}

placeholder="Message..."


/>


<button
onClick={onSend}
>
➤
</button>


</div>

);

}