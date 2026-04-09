import { ref, watch } from 'vue';

const isDark = ref(false);

if (typeof window !== 'undefined') {
  if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    isDark.value = true;
  }
}

watch(isDark, (val) => {
  if (typeof window === 'undefined') return;
  if (val) {
    document.documentElement.classList.add('dark');
    localStorage.theme = 'dark';
  } else {
    document.documentElement.classList.remove('dark');
    localStorage.theme = 'light';
  }
}, { immediate: true });

export function useDarkMode() {
  const toggleDark = () => {
    isDark.value = !isDark.value;
  };

  return {
    isDark,
    toggleDark
  };
}
