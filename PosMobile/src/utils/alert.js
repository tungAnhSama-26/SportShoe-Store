import Toast from 'react-native-toast-message';

export function showError(message) {
  Toast.show({
    type: 'error',
    text1: 'Lỗi',
    text2: message,
  });
}

export function showToastSuccess(message) {
  Toast.show({
    type: 'success',
    text1: 'Thành công',
    text2: message,
  });
}

export function showWarning(message) {
  Toast.show({
    type: 'info',
    text1: 'Cảnh báo',
    text2: message,
  });
}
