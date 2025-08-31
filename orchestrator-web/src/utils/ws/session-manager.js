import { ChatSession } from './chat-session.js';

export class SessionManager {
    sessions = new Map();
    currentSession = null;

    constructor() {
        this.loadAllSessions();
    }

    // 创建新会话
    createSession(id = null) {
        const sessionId = id || 'session-' + Date.now();
        const session = new ChatSession(sessionId);
        this.sessions.set(sessionId, session);
        this.currentSession = session;
        session.save(); // 保存空会话
        return session;
    }

    // 获取会话（自动恢复）
    getSession(id) {
        if (this.sessions.has(id)) {
            return this.sessions.get(id);
        }

        const saved = this.loadSession(id);
        if (saved) {
            const session = new ChatSession(saved.id, { messages: saved.messages });
            this.sessions.set(id, session);
            return session;
        }

        return null;
    }

    // 设置当前会话
    setCurrentSession(id) {
        const session = this.getSession(id);
        if (session) {
            this.currentSession = session;
        }
        return this.currentSession;
    }

    // 获取所有会话列表（用于侧边栏）
    getAllSessions() {
        return Array.from(this.sessions.values()).map(s => ({
            id: s.id,
            title: s.messages[0]?.text?.substring(0, 30) || '新对话',
            updatedAt: s.messages.length > 0 ?
                new Date(Math.max(...s.messages.map(m => new Date(m.timestamp || Date.now())))).toLocaleString() :
                '刚刚'
        })).sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
    }

    // 删除会话
    deleteSession(id) {
        const session = this.sessions.get(id);
        if (session) {
            session.destroyWebSocket();
            this.sessions.delete(id);
            localStorage.removeItem(`chat-session-${id}`);
            if (this.currentSession?.id === id) {
                this.currentSession = null;
            }
        }
    }

    // 从 localStorage 加载单个会话
    loadSession(id) {
        const saved = localStorage.getItem(`chat-session-${id}`);
        if (saved) {
            try {
                return JSON.parse(saved);
            } catch (e) {
                console.error('解析会话失败:', e);
            }
        }
        return null;
    }

    // 加载所有会话元数据（不加载完整消息）
    loadAllSessions() {
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key.startsWith('chat-session-')) {
                const id = key.replace('chat-session-', '');
                const saved = this.loadSession(id);
                if (saved) {
                    // 只创建空会话，不加载完整消息（节省内存）
                    const session = new ChatSession(saved.id, { messages: [] });
                    this.sessions.set(id, session);
                }
            }
        }
    }

    // 手动触发所有会话保存（可选）
    saveAll() {
        this.sessions.forEach(s => s.save());
    }
}