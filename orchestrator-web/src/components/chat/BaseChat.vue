<template>
    <div class="chat-container">
        <div class="msg" v-for="(item, index) in msgObj.currentMessages" :key="index"></div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { SessionManager } from '@/utils/ws/session-manager';
import { renderMessage, autoScroll } from '@/utils/ws/ui';

const msgObj = ref({
    manager: null,
    currentSession: null,
    sessions: [],
    inputText: '',
    currentMessages: []
})

const addMsg = () => { }
const rmMsg = () => { }
const getMsg = () => { }

const updateSessionsList = () => {
    msgObj.value.sessions = msgObj.value.manager.getAllSessions()
}

// 渲染单条消息
const renderMsg = (msg) => {
    return renderMessage(msg)
}

// 更新消息列表
const updateMessages = (messages) => {
    msgObj.value.currentMessages = messages
    // 延迟执行滚动，确保 DOM 已更新
    this.$nextTick(() => {
        autoScroll();
    });
}

// 自动滚动到底部
const autoScroll = () => {
}

onMounted(() => {
    // 初始化会话管理器
    msgObj.value.manager = new SessionManager()

    // 创建初始会话
    msgObj.value.currentSession = this.manager.createSession()

    // 设置更新回调
    msgObj.value.currentSession.setUpdateCallback(this.updateMessages)

    // 初始化 WebSocket 连接
    msgObj.value.currentSession.initWebSocket()

    // 渲染会话列表
    updateSessionsList()
})


</script>

<style lang="scss" scoped></style>