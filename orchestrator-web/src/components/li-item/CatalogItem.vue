<template>
    <div class="catalog-container" @click="goTo(props.path)" @mouseenter="iconActive = true"
        @mouseleave="iconActive = false">
        <span class="content">{{ props.content }}</span>
        <span class="end-icon" :class="{ show: iconActive }">
            {{ props.icon }}
        </span>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
    content: String,
    icon: String,
    path: {
        type: String,
        default: ''
    }
})

const iconActive = ref(false)

const goTo = (path: string) => {
    if (!path) return
    router.push(path)
}
</script>

<style lang="scss" scoped>
.catalog-container {
    width: 100%;
    height: 40px;
    padding: 10px;
    display: flex;
    align-items: center;
    cursor: pointer;
    border-radius: 8px;
    transition: background-color 0.3s;
    position: relative;
    overflow: hidden; // 隐藏滑入前的图标

    &:hover {
        background-color: var(--color-primary-dark);
    }

    .content {
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 14px;
        color: var(--color-text-secondary);
    }

    .end-icon {
        height: 100%;
        width: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 0px 8px 8px 0px;
        background-color: royalblue;
        position: absolute;
        top: 0;
        right: -40px; // 初始：完全移出右侧
        opacity: 0;
        transform: scale(0.8);
        transform-origin: center right;

        transition:
            right 0.3s cubic-bezier(0.4, 0, 0.2, 1),
            opacity 0.3s ease,
            transform 0.3s ease;
    }

    // 显示状态
    .end-icon.show {
        right: 0;
        opacity: 1;
        transform: scale(1);
    }
}
</style>