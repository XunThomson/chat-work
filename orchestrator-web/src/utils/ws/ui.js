import marked from 'marked';

export function renderMessages(messages) {
    return messages.map(msg => {
        let text = msg.text || '';
        // Markdown 渲染
        text = marked.parse(text);

        const className = msg.role === 'user' ? 'user' : 'ai';
        const avatar = msg.role === 'user' ? '👤' : '🤖';

        return `
            <div class="message ${className}">
                <div class="avatar">${avatar}</div>
                <div class="content">
                    <div class="text">${text}</div>
                    ${msg.status === 'streaming' ? '<div class="loading">AI 正在思考...</div>' : ''}
                </div>
            </div>
        `;
    }).join('');
}

export function renderMessage(message) {
    let text = message.text || '';
    try {
        text = marked.parse(text); // 使用 parse 方法更明确
    } catch (error) {
        console.warn('Markdown rendering failed:', error);
        text = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); // 简单转义
    }

    const avatar = message.role === 'user' ? '👤' : '🤖';
    const loading = message.status === 'streaming' ?
        '<div class="loading">AI 正在思考...</div>' : '';

    return `
        <div class="avatar">${avatar}</div>
        <div class="content">
          <div class="text">${text}</div>
          ${loading}
        </div>
      `;
}

export function autoScroll(container) {
    container.scrollTop = container.scrollHeight;
}