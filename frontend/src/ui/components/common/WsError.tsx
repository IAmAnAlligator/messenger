interface Props {

    message:string|null;

}


export default function WsError({
    message
}:Props){


    if(!message)
        return null;



    return (

        <div className="ws-error">

            {message}

        </div>

    );
}