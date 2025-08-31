<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, RouterView } from 'vue-router'

import TheWelcome from '../components/TheWelcome.vue'


const router = useRouter()

const leftAsideMenuData = ref(
  [
    {
      icon: '',
      content: '对话',
      path: '/chat'
    }
  ]
)

const goTo = (path: string) => {
  router.push(path)
}

</script>

<template>
  <div class="container">
    <div class="head-menu">
      <h2 class="logo">
        <img src="@/assets/image/login_sign_image/logo.png" alt="Logo"> 项目管理系统
      </h2>
      <label class="login-link">登录</label>
    </div>
    <div class="main-content-container">
      <div class="left-aside-menu">
        <ul>
          <li v-for="(item, index) in leftAsideMenuData" :key="index" @click="goTo(item.path)">
            <span>{{ item.icon }}</span>
            <span>{{ item.content }}</span>
          </li>
        </ul>
        <ul>
          <li>list</li>
        </ul>
      </div>
      <div class="content-container">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.head-menu {
  height: 60px;
  padding: 0 50px;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-primary);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  display: flex;

  img {
    vertical-align: middle;
  }

}

.main-content-container {
  width: 100%;
  height: calc(100% - 60px);
  display: flex;

  .left-aside-menu {
    width: 80px;
    height: 100%;
    background-color: var(--color-bg-primary);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    font-family: 'icomoon';

    ul {
      width: 100%;
    }

    ul {
      li {
        height: 80px;
        text-align: center;
        display: flex;
        flex-direction: column;
        justify-content: center;

        span {
          &:first-of-type {
            font-size: 24px;
          }
        }
      }
    }

  }

  .content-container {
    flex: 1;
    height: 100%;
    background-color: var(--color-bg);
  }
}
</style>
