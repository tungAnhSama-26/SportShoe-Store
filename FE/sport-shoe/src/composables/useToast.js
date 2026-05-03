import { reactive } from 'vue'

export function useToast() {
  const toast = reactive({
    show: false,
    message: '',
    type: 'success',
  })

  let toastTimer = null

  function showToast(message, type = 'success') {
    toast.message = message
    toast.type = type
    toast.show = true

    if (toastTimer) {
      clearTimeout(toastTimer)
    }

    toastTimer = setTimeout(() => {
      toast.show = false
    }, 3000)
  }

  function hideToast() {
    toast.show = false
    if (toastTimer) {
      clearTimeout(toastTimer)
      toastTimer = null
    }
  }

  return {
    toast,
    showToast,
    hideToast,
  }
}