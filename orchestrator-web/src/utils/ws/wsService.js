// 支持多会话的 WebSocket 聊天类

const BASE_URL = 'ws://localhost:65504/ai-plus';

export class ChatSession {
    // 每个实例独立的状态
    messages = [];
    aiMessageBuffer = '';
    ws = null;
    onUpdate = null;

    constructor() {
        // 可以传入配置，比如用户ID、会话ID等
    }

    // 设置 UI 更新回调
    setUpdateCallback(callback) {
        this.onUpdate = callback;
    }

    // 触发 UI 更新
    triggerUpdate() {
        if (this.onUpdate) {
            this.onUpdate(this.messages);
        }
    }

    // 初始化 WebSocket 连接
    initWebSocket() {
        if (typeof WebSocket === 'undefined') {
            console.error('浏览器不支持 WebSocket');
            return null;
        }

        const token = localStorage.getItem('token');
        if (!token) {
            console.warn('未找到 token');
            return null;
        }

        const url = new URL(BASE_URL);
        url.searchParams.set('token', token);
        this.ws = new WebSocket(url.toString());

        this.ws.onopen = () => {
            console.log('WebSocket 已连接');
        };

        this.ws.onmessage = (event) => {
            const data = event.data;

            // 第一次收到 AI 响应时，添加占位消息
            if (this.aiMessageBuffer === '' && this.messages.length > 0 && this.messages[this.messages.length - 1].role !== 'ai') {
                this.setMessage({ role: 'ai', text: '' });
                this.triggerUpdate();
            }

            // 拼接 AI 响应
            this.aiMessageBuffer += data;

            // 更新最后一条 AI 消息
            const lastMsg = this.messages[this.messages.length - 1];
            if (lastMsg && lastMsg.role === 'ai') {
                lastMsg.text = this.aiMessageBuffer;
                this.triggerUpdate();
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket 错误:', error);
            const lastMsg = this.messages[this.messages.length - 1];
            if (lastMsg && lastMsg.role === 'ai' && !lastMsg.text) {
                lastMsg.text = 'AI 响应失败，请重试。';
                this.triggerUpdate();
            }
        };

        this.ws.onclose = () => {
            console.log('WebSocket 已关闭');
        };

        return this.ws;
    }

    // 发送消息
    sendMessage(text) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            alert('连接未建立，请稍后重试');
            return;
        }

        this.setMessage({ role: 'user', text });
        this.triggerUpdate();

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

        // 重置 AI 缓冲区
        this.aiMessageBuffer = '';
    }

    // 添加消息（可扩展）
    setMessage(info) {
        this.messages.push(info);
    }

    // 关闭连接
    closeWebSocket() {
        if (this.ws) {
            this.ws.close();
        }
    }

    // 销毁连接（避免重复关闭提示）
    destroyWebSocket() {
        if (this.ws) {
            this.ws.onclose = () => { }; // 防止重复触发
            this.ws.close();
        }
    }

    // 获取当前消息列表
    getMessages() {
        return this.messages;
    }

    // 清除当前会话（开始新聊天）
    clear() {
        this.messages = [];
        this.aiMessageBuffer = '';
        this.destroyWebSocket();
        this.triggerUpdate();
    }
}