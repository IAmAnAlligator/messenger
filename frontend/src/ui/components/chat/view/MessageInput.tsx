import {
    useRef
} from "react";


type Props = {

    value: string;

    onChange(
        value: string
    ): void;

    onSend(): void;

    onSendFile(
        file: File
    ): Promise<void>;

};


export default function MessageInput({
    value,
    onChange,
    onSend,
    onSendFile

}: Props) {


const fileInputRef =
    useRef<HTMLInputElement>(null);

const MAX_FILE_SIZE =
    10 * 1024 * 1024;


async function handleFileChange(
    event: React.ChangeEvent<HTMLInputElement>
) {

    const file =
        event.target.files?.[0];

    if (!file) {
        return;
    }


    if (file.size > MAX_FILE_SIZE) {

        console.warn(
            "[handleFileChange] File too large:",
            file.size
        );

        event.target.value = "";

        return;
    }


    console.log(
        "[handleFileChange]",
        file.name,
        file.size
    );


    try {

        await onSendFile(file);

    } catch (error) {

        console.error(
            "Failed to send file",
            error
        );

    } finally {

        event.target.value = "";

    }
}



    function handleKeyDown(
        event: React.KeyboardEvent<HTMLInputElement>
    ) {

        if (
            event.key === "Enter" &&
            !event.shiftKey
        ) {

            event.preventDefault();

            onSend();

        }

    }



    return (

        <div className="message-input">


            <button
                type="button"
                onClick={() =>
                    fileInputRef.current?.click()
                }
            >
                📎
            </button>



            <input
                ref={fileInputRef}
                type="file"
                hidden
                onChange={handleFileChange}
            />



            <input
                type="text"
                value={value}
                onChange={event =>
                    onChange(
                        event.target.value
                    )
                }
                onKeyDown={handleKeyDown}
            />



<button
    type="submit"
    onClick={onSend}
    className="send-btn"
    aria-label="Send message"
>
    ➤
</button>


        </div>

    );

}