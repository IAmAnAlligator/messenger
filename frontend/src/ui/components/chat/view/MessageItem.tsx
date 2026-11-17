import { useEffect, useRef, useState } from "react";

import type { MessageDto } from "../../../../types/message";

import { useAuth } from "../../../../contexts/AuthContext";

import {
  getMessageFile,
  getMessageThumbnail,
} from "../../../../services/messageService";

import { getFileIcon } from "../../../../utils/fileUtils";

type Props = {
  message: MessageDto;
  onDelete(id: string): void;
  onUserProfile(userId: string): void;
  isRead: boolean;
};

export default function MessageItem({
  message,
  onDelete,
  onUserProfile,
  isRead,
}: Props) {
  const { user } = useAuth();

  const [fileUrl, setFileUrl] = useState<string | null>(null);

  const [fileLoading, setFileLoading] = useState(false);

  const [fileError, setFileError] = useState(false);

  const [originalImageUrl, setOriginalImageUrl] = useState<string | null>(null);

  const [originalLoading, setOriginalLoading] = useState(false);

  const [originalError, setOriginalError] = useState(false);

  const originalUrlRef = useRef<string | null>(null);

  const mine = user?.id === message.sender.id;

  const attachment = message.attachment;

  const isImage = attachment?.contentType.startsWith("image/") ?? false;

  // --------------------------------------------------
  // Load thumbnail or original file
  // --------------------------------------------------

  useEffect(() => {
    if (!attachment) {
      setFileUrl(null);
      return;
    }

    let cancelled = false;
    let objectUrl: string | null = null;

    async function loadFile() {
      try {
        setFileLoading(true);
        setFileError(false);
        setFileUrl(null);

        const blob = isImage
          ? await getMessageThumbnail(message.chatId, message.id)
          : await getMessageFile(message.chatId, message.id);

        if (cancelled) return;

        objectUrl = URL.createObjectURL(blob);

        setFileUrl(objectUrl);
      } catch (error) {
        if (cancelled) return;

        console.error(
          isImage ? "Failed to load thumbnail" : "Failed to load message file",
          error,
        );

        setFileError(true);
      } finally {
        if (!cancelled) {
          setFileLoading(false);
        }
      }
    }

    void loadFile();

    return () => {
      cancelled = true;

      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [message.chatId, message.id, attachment?.contentType, isImage]);

  // --------------------------------------------------
  // Release original image URL
  // --------------------------------------------------

  useEffect(() => {
    return () => {
      if (originalUrlRef.current) {
        URL.revokeObjectURL(originalUrlRef.current);

        originalUrlRef.current = null;
      }
    };
  }, []);

  // --------------------------------------------------
  // Load original image on demand
  // --------------------------------------------------

  async function handleOpenOriginalImage() {
    if (originalImageUrl || originalLoading || !attachment) {
      return;
    }

    try {
      setOriginalLoading(true);
      setOriginalError(false);

      const blob = await getMessageFile(message.chatId, message.id);

      const objectUrl = URL.createObjectURL(blob);

      originalUrlRef.current = objectUrl;

      setOriginalImageUrl(objectUrl);
    } catch (error) {
      console.error("Failed to load original image", error);

      setOriginalError(true);
    } finally {
      setOriginalLoading(false);
    }
  }

  function handleCloseOriginalImage() {
    if (originalUrlRef.current) {
      URL.revokeObjectURL(originalUrlRef.current);

      originalUrlRef.current = null;
    }

    setOriginalImageUrl(null);
    setOriginalError(false);
  }

  const messageDate = new Date(message.createdAt);

  const formattedDate = new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(messageDate);

  return (
    <div className={mine ? "message-row mine" : "message-row"}>
      <div className="message-bubble">
        <div className="message-header">
          {!mine && (
            <button
              type="button"
              className="message-sender"
              onClick={() => onUserProfile(message.sender.id)}
            >
              {message.sender.username}
            </button>
          )}

          {mine && (
            <button
              type="button"
              className="delete-btn"
              onClick={() => onDelete(message.id)}
            >
              ×
            </button>
          )}
        </div>

        {message.content && (
          <div className="message-text">{message.content}</div>
        )}

        {attachment && (
          <div className="message-attachment">
            {fileLoading && (
              <div className="message-file-loading">
                {isImage ? "Loading image..." : "Loading file..."}
              </div>
            )}

            {fileError && (
              <div className="message-file-error">Failed to load file</div>
            )}

            {!fileLoading &&
              !fileError &&
              fileUrl &&
              (isImage ? (
                <button
                  type="button"
                  className="message-image-button"
                  onClick={handleOpenOriginalImage}
                  title="Open original image"
                >
                  <img
                    src={fileUrl}
                    alt={attachment.originalFileName}
                    className="message-image-preview"
                  />
                </button>
              ) : (
                <a
                  href={fileUrl}
                  download={attachment.originalFileName}
                  className="message-file"
                >
                  <span className="message-file-icon">
                    {getFileIcon(attachment.contentType)}
                  </span>

                  <span className="message-file-info">
                    <span className="message-file-name">
                      {attachment.originalFileName}
                    </span>

                    <span className="message-file-size">
                      {formatFileSize(attachment.size)}
                    </span>
                  </span>
                </a>
              ))}
          </div>
        )}

        <div className="message-footer">
          <span className="message-time">{formattedDate}</span>

          {mine && (
            <span
              className={isRead ? "message-read read" : "message-read"}
              title={isRead ? "Read" : "Sent"}
            >
              {isRead ? "✓✓" : "✓"}
            </span>
          )}
        </div>
      </div>

      {/* Original image viewer */}

      {originalImageUrl && (
        <div
          className="image-viewer-overlay"
          onClick={handleCloseOriginalImage}
        >
          <button
            type="button"
            className="image-viewer-close"
            onClick={handleCloseOriginalImage}
            aria-label="Close image"
          >
            ×
          </button>

          <img
            src={originalImageUrl}
            alt={attachment?.originalFileName ?? "Original image"}
            className="image-viewer-image"
            onClick={(event) => event.stopPropagation()}
          />
        </div>
      )}

      {originalLoading && (
        <div className="image-viewer-loading">Loading original image...</div>
      )}

      {originalError && (
        <div className="message-file-error">Failed to load original image</div>
      )}
    </div>
  );
}

// --------------------------------------------------
// Helpers
// --------------------------------------------------

function formatFileSize(size: number): string {
  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}
