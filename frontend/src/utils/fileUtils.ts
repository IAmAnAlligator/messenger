export function getFileIcon(contentType: string): string {
  const type = contentType.toLowerCase().split(";")[0].trim();

  if (type.startsWith("image/")) return "🖼️";
  if (type.startsWith("video/")) return "🎬";
  if (type.startsWith("audio/")) return "🎵";

  if (type === "application/pdf") return "📄";

  if (type.includes("zip") || type.includes("rar") || type.includes("7z")) {
    return "📦";
  }

  if (type.startsWith("text/") || type.includes("json")) {
    return "📝";
  }

  return "📎";
}
