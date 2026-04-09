<script setup lang="ts">
import { Bell, Sun, Moon, Settings, Menu } from 'lucide-vue-next';
import { isSidebarOpen } from '../../composable/useSidebar';
import { useDarkMode } from '../../composable/useDarkMode';

const { isDark, toggleDark } = useDarkMode();
</script>

<template>
  <header class="h-20 bg-white dark:bg-gray-800 mx-4 mt-4 lg:ml-6 lg:mr-6 lg:mt-6 rounded-[20px] flex items-center justify-between px-4 lg:px-8 shadow-[0_2px_10px_rgba(0,0,0,0.02)] shrink-0 transition-all duration-300">
    <button @click="isSidebarOpen = !isSidebarOpen" class="p-2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-50 dark:hover:bg-slate-700 rounded-xl transition-colors shrink-0">
      <Menu class="w-6 h-6" />
    </button>
    <div class="flex items-center justify-end flex-1 gap-2 lg:gap-4 overflow-x-auto pr-2 hide-scrollbar">
      <button class="relative p-2.5 rounded-[14px] bg-red-100 dark:bg-red-500/10 text-red-500 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-500/20 transition-colors shrink-0 hidden sm:block">
        <Bell class="w-5 h-5" />
        <span class="absolute top-2 right-2.5 w-1.5 h-1.5 bg-red-500 rounded-full"></span>
      </button>
      <button @click="toggleDark" class="relative p-2.5 rounded-[14px] bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-slate-600 transition-colors">
        <Sun v-if="!isDark" class="w-5 h-5" />
        <Moon v-else class="w-5 h-5" />
      </button>
      <button class="relative p-2.5 rounded-[14px] bg-red-100 dark:bg-red-500/10 text-red-500 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-500/20 transition-colors">
        <Settings class="w-5 h-5" />
        <span class="absolute top-2 right-2.5 w-1.5 h-1.5 bg-red-500 rounded-full"></span>
      </button>
      <div class="h-8 w-px bg-gray-200 dark:bg-gray-700 mx-1 lg:mx-2 shrink-0"></div>
      <div class="flex items-center gap-2 lg:gap-3 cursor-pointer shrink-0">
        <span class="text-[13px] lg:text-sm text-gray-500 dark:text-gray-400 truncate hidden sm:block">Xin chào, <strong class="text-gray-800 dark:text-gray-100 font-semibold">Admin</strong></span>
        <div class="w-9 h-9 lg:w-10 lg:h-10 rounded-full bg-slate-800 dark:bg-gray-600 flex items-center justify-center text-white overflow-hidden shadow-sm shrink-0">
          <img src="https://i.pravatar.cc/150?u=a042581f4e29026704d" alt="Admin" class="w-full h-full object-cover"/>
        </div>
      </div>
    </div>
  </header>
</template>
