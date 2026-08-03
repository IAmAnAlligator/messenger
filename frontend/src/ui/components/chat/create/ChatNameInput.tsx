type Props={

value:string;

onChange(value:string):void;

};


export default function ChatNameInput({
value,
onChange

}:Props){


return (

<input

value={value}

placeholder="Chat name"

onChange={
e=>onChange(
    e.target.value
)
}

/>

);

}