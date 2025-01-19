export function resolvePath(path, itemName) {
    return path.trim() === ''
        ? `${itemName}`
        : `${path}/${itemName}`
}

export function resolveTypeEmoji(item) {
    const extensionStartIndex = item.name.lastIndexOf('.');

    if (extensionStartIndex < 0) {
        return item.directory ? '📁' : '❓';
    }

    const extension = item.name.slice(extensionStartIndex + 1).toLowerCase();

    const emojiMap = {
        'jpg': '🖼️', 'jpeg': '🖼️', 'png': '🖼️', 'gif': '🖼️',
        'pdf': '📄', 'doc': '📄', 'docx': '📄', 'xls': '📊',
        'xlsx': '📊', 'txt': '📄', 'zip': '🗄️', 'mp3': '🎵',
        'mp4': '🎥', 'html': '🌐', 'css': '🎨', 'js': '💻',
    };

    return emojiMap[extension] || '❓';
}
