const BASE_URL = 'ws://localhost:65504/ai-plus';

export class ChatSession {
    id;
    messages = [];
    aiMessageBuffer = '';
    ws = null;
    onUpdate = null;
    token = null;
    reconnectAttempts = 0;
    maxReconnectAttempts = 3;

    constructor(id, options = {}) {
        this.id = id;
        this.token = options.token || localStorage.getItem('token');
        this.messages = options.messages || [];
    }

    setUpdateCallback(callback) {
        this.onUpdate = callback;
    }

    triggerUpdate() {
        if (this.onUpdate) {
            this.onUpdate(this.messages);
        }
    }

    // 保存会话到 localStorage
    save() {
        const data = {
            id: this.id,
            messages: this.messages,
            updatedAt: Date.now()
        };
        localStorage.setItem(`chat-session-${this.id}`, JSON.stringify(data));
    }

    // 清除会话
    clear() {
        this.messages = [];
        this.aiMessageBuffer = '';
        this.destroyWebSocket();
        this.save();
        this.triggerUpdate();
    }

    // 初始化连接
    initWebSocket() {
        if (typeof WebSocket === 'undefined') {
            console.error('不支持 WebSocket');
            return null;
        }

        if (!this.token) {
            console.warn('未找到 token');
            return null;
        }

        const url = new URL(BASE_URL);
        url.searchParams.set('token', this.token);
        this.ws = new WebSocket(url.toString());

        this.ws.onopen = () => {
            console.log(`会话 ${this.id} WebSocket 已连接`);
            this.reconnectAttempts = 0; // 重置重连计数
        };

        this.ws.onmessage = (event) => {
            const data = event.data;


            const regex = /^>>--.*?--<<$/;
            if (regex.test(data)) return

                if (this.aiMessageBuffer === '' && this.messages.length > 0 && this.messages[this.messages.length - 1].role !== 'ai') {
                    this.setMessage({ role: 'ai', text: '', status: 'streaming' });
                    this.triggerUpdate();
                }

            this.aiMessageBuffer += data;
            const lastMsg = this.messages[this.messages.length - 1];
            if (lastMsg?.role === 'ai') {
                lastMsg.text = this.aiMessageBuffer;
                this.save(); // 实时保存
                this.triggerUpdate();
            }
        };

        this.ws.onerror = (error) => {
            console.error(`会话 ${this.id} 错误:`, error);
            const lastMsg = this.messages[this.messages.length - 1];
            if (lastMsg?.role === 'ai' && !lastMsg.text) {
                lastMsg.text = 'AI 响应失败，请重试。';
                lastMsg.status = 'error';
                this.triggerUpdate();
            }
        };

        this.ws.onclose = () => {
            console.log(`会话 ${this.id} 已关闭`);
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
                this.reconnectAttempts++;
                setTimeout(() => {
                    console.log(`会话 ${this.id} 正在尝试重连 (${this.reconnectAttempts})`);
                    this.initWebSocket();
                }, 2000 * this.reconnectAttempts); // 指数退避
            }
        };

        return this.ws;
    }

    sendMessage(text) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            this.initWebSocket(); // 尝试重连
            return; // 不再递归调用 sendMessage
        }

        this.setMessage({ role: 'user', text });
        this.triggerUpdate();
        this.save();

        const content = {
            msg: text,
            third: [
                {
                    name: "DeepSeek-思考联网",
                    plus: false,
                    apiKey: "",
                    secretKey: "",
                    loginActive: "",
                    qrInfo: "PLAY_GET_DEEPSEEK_QRCODE",
                    newChat: true
                }
            ]
        };

        this.ws.send(JSON.stringify(content));
        this.aiMessageBuffer = '';
    }

    // 在 chat-session.js 的 setMessage 方法中
    setMessage(info) {
        info.timestamp = Date.now(); // 添加时间戳
        this.messages.push(info);
    }

    destroyWebSocket() {
        if (this.ws) {
            this.ws.onclose = () => { };
            this.ws.close();
        }
    }

    toJSON() {
        return {
            id: this.id,
            messages: this.messages
        };
    }
}