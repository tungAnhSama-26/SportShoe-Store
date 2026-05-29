import { reactive } from 'vue'
import { showSuccess, showError } from '../utils/alert'

export function useToast() {
  const toast = reactive({
    show: false,
    message: '',
    type: 'success',
  })

  function showToast(message, type = 'success') {
    if (type === 'success') {
      showSuccess(message)
    } else {
      showError(message)
    }
  }

  function hideToast() {
    // No longer needed with SweetAlert2
  }

  return {
    toast,
    showToast,
    hideToast,
  }
}