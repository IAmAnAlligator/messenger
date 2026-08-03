import type { ChatType } from "../../../../hooks/useChatCreate";


type Props = {

    value:ChatType;

    onChange(
        value:ChatType
    ):void;

};


export default function ChatTypeSelector({
    value,
    onChange

}:Props){


return (

<select

value={value}

onChange={
e=>onChange(
    e.target.value as ChatType
)
}

>

<option value="GROUP">
GROUP
</option>


<option value="PRIVATE">
PRIVATE
</option>


</select>

);

}