import {
    useEffect,
    useState
} from "react";


import type {
    MessageDto
} from "../../../../types/message";


import {
    useAuth
} from "../../../../contexts/AuthContext";


import {
    getMessageFile
} from "../../../../services/messageService";


type Props = {

    message: MessageDto;

    onDelete(
        id: number
    ): void;

};


export default function MessageItem({

    message,

    onDelete

}: Props) {


    const { user } =
        useAuth();


    const [fileUrl, setFileUrl] =
        useState<string | null>(null);


    const [fileLoading, setFileLoading] =
        useState(false);


    const [fileError, setFileError] =
        useState(false);


    const mine =
        user?.id === message.sender.id;



    /*
        Загружаем файл через Axios,
        чтобы Authorization: Bearer ...
        был добавлен interceptor'ом.
    */
    useEffect(() => {

        if (!message.attachment) {

            setFileUrl(null);

            return;

        }


        let objectUrl: string | null = null;

        let cancelled = false;



        async function loadFile() {

            try {

                setFileLoading(true);

                setFileError(false);


                const blob =
                    await getMessageFile(
                        message.chatId,
                        message.id
                    );


                if (cancelled) {
                    return;
                }


                objectUrl =
                    URL.createObjectURL(blob);


                setFileUrl(
                    objectUrl
                );


            } catch (error) {

                if (cancelled) {
                    return;
                }


                console.error(
                    "Failed to load message file",
                    error
                );


                setFileError(true);

                setFileUrl(null);


            } finally {

                if (!cancelled) {

                    setFileLoading(false);

                }

            }

        }


        loadFile();


        return () => {

            cancelled = true;


            if (objectUrl) {

                URL.revokeObjectURL(
                    objectUrl
                );

            }

        };

    }, [
        message.chatId,
        message.id,
        message.attachment
    ]);



    const messageDate =
        new Date(
            message.createdAt
        );



    const formattedDate =
        new Intl.DateTimeFormat(
            "en-US",
            {
                weekday: "short",
                month: "short",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                hour12: false
            }
        ).format(messageDate);



    const isImage =
        message.attachment?.contentType
            .startsWith("image/") ?? false;



    return (

        <div
            className={
                mine
                    ? "message-row mine"
                    : "message-row"
            }
        >

            <div className="message-bubble">


                <div className="message-header">

                    <b>
                        {
                            mine
                                ? "You"
                                : message.sender.username
                        }
                    </b>


                    {
                        mine && (

                            <button
                                type="button"
                                className="delete-btn"
                                onClick={() =>
                                    onDelete(
                                        message.id
                                    )
                                }
                            >
                                ×
                            </button>

                        )
                    }

                </div>



                {
                    message.content && (

                        <div className="message-text">

                            {message.content}

                        </div>

                    )
                }



                {
                    message.attachment && (

                        <div className="message-attachment">


                            {
                                fileLoading && (

                                    <div className="message-file-loading">

                                        Loading file...

                                    </div>

                                )
                            }



                            {
                                fileError && (

                                    <div className="message-file-error">

                                        Failed to load file

                                    </div>

                                )
                            }



                            {
                                !fileLoading &&
                                !fileError &&
                                fileUrl && (

                                    <a
                                        href={fileUrl}
                                        target={
                                            isImage
                                                ? "_blank"
                                                : undefined
                                        }
                                        rel={
                                            isImage
                                                ? "noopener noreferrer"
                                                : undefined
                                        }
                                        download={
                                            isImage
                                                ? undefined
                                                : message
                                                    .attachment
                                                    .originalFileName
                                        }
                                        className="message-file"
                                    >

                                        <span className="message-file-icon">

                                            {
                                                getFileIcon(
                                                    message
                                                        .attachment
                                                        .contentType
                                                )
                                            }

                                        </span>


                                        <span className="message-file-info">

                                            <span className="message-file-name">

                                                {
                                                    message
                                                        .attachment
                                                        .originalFileName
                                                }

                                            </span>


                                            <span className="message-file-size">

                                                {
                                                    formatFileSize(
                                                        message
                                                            .attachment
                                                            .size
                                                    )
                                                }

                                            </span>

                                        </span>

                                    </a>

                                )
                            }


                        </div>

                    )
                }



                <div className="message-footer">


                    <span className="message-time">

                        {formattedDate}

                    </span>



                    {
                        mine && (

                            <span
                                className="message-status"
                            >

                                {
                                    message.status === "READ"
                                        ? "✓✓"
                                        : "✓"
                                }

                            </span>

                        )
                    }


                </div>


            </div>

        </div>

    );

}



function getFileIcon(
    contentType: string
): string {

    if (
        contentType.startsWith(
            "image/"
        )
    ) {

        return "🖼️";

    }


    if (
        contentType.startsWith(
            "video/"
        )
    ) {

        return "🎬";

    }


    if (
        contentType.startsWith(
            "audio/"
        )
    ) {

        return "🎵";

    }


    if (
        contentType ===
        "application/pdf"
    ) {

        return "📄";

    }


    if (
        contentType.includes("zip") ||
        contentType.includes("rar") ||
        contentType.includes("7z")
    ) {

        return "📦";

    }


    if (
        contentType.includes("text") ||
        contentType.includes("json")
    ) {

        return "📝";

    }


    return "📎";

}



function formatFileSize(
    size: number
): string {

    if (size < 1024) {

        return `${size} B`;

    }


    if (
        size <
        1024 * 1024
    ) {

        return `${(
            size / 1024
        ).toFixed(1)} KB`;

    }


    return `${(
        size /
        1024 /
        1024
    ).toFixed(1)} MB`;

}