<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

import PinkButton from '@/components/button/PinkButton.vue';
import RetractableInput from '@/components/input/RetractableInput.vue';
import CatalogItem from '@/components/li-item/CatalogItem.vue';
import LineCard from '@/components/card/LineCard.vue';

const pinkButtonContent = ref('insert')
const pinkButtonActive = ref(true)
const inputRef = ref(null)

const changePinkButtonActive = () => {
    pinkButtonActive.value = !pinkButtonActive.value
}


const catalogList = ref([
    {
        content: 'test--test--test--test------test',
        icon: 'O',
        path: ''
    }
])


</script>

<template>
    <div class="chat-container">
        <div class="chat-catalog">
            <div class="pkg-box">
                <pink-button :content="pinkButtonContent" v-show="pinkButtonActive" />
                <retractable-input @click="changePinkButtonActive" ref="inputRef" />
            </div>
            <div class="catalog-content">
                <catalog-item v-for="(item, index) in catalogList" :key="index" :content="item.content"
                    :icon="item.icon" :path="item.path" />
            </div>
            <div class="pkg-box">
                <line-card />
            </div>
        </div>
        <div class="chat-content-container">
            <div class="chat-tool-menu"></div>
            <div class="chat-content">
                <div class="content">
                    
                </div>
            </div>
            <div class="chat-input-container"></div>
        </div>
        <div class="chat-plus-container"></div>
    </div>
</template>

<style lang="scss" scoped>
.chat-container {
    width: 100%;
    height: 100%;
    display: flex;
    background-color: var(--color-bg-tertiary);
    border-radius: 10px;

    .chat-catalog {
        width: 14%;
        min-width: 178px;
        height: 100%;
        background-color: var(--color-bg-secondary);
        border-radius: 10px 0px 0px 10px;
    }

    .pkg-box {
        height: 70px;
        width: 100%;
        display: flex;
        justify-content: space-evenly;
        align-items: center;

        &:last-child {
            padding: 5px;
        }
    }

    .catalog-content {
        height: calc(100% - 140px);
        width: 100%;
        padding: 8px;
    }

    .chat-content-container {
        width: 100%;
        height: 100%;

        .chat-tool-menu {
            width: 100%;
            height: 40px;
            background-color: #c0bbbb;
        }

        .chat-content {
            width: 100%;
            height: 78%;
            display: flex;
            flex-direction: column;
            align-items: center;
            overflow: auto;

            &::-webkit-scrollbar {
                width: 8px;
            }

            &::-webkit-scrollbar-track {
                background: rgba(255, 255, 255, 0.1);
                border-radius: 4px;
            }

            &::-webkit-scrollbar-thumb {
                background: rgba(255, 255, 255, 0.3);
                border-radius: 4px;
            }

            &::-webkit-scrollbar-thumb:hover {
                background: rgba(255, 255, 255, 0.5);
            }

            .content {
                min-height: 100%;
                min-width: 72%;
                background-color: lavenderblush;
            }
        }

        .chat-input-container {
            width: 100%;
            height: calc(22% - 40px);
            background-color: green;
        }
    }
}
</style>