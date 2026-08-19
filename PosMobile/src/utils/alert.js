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

import { Alert, Platform } from 'react-native';

export function showConfirm(message) {
  if (Platform.OS === 'web') {
    return Promise.resolve(window.confirm(message));
  }
  return new Promise((resolve) => {
    Alert.alert(
      'Xác nhận',
      message,
      [
        { text: 'Hủy', onPress: () => resolve(false), style: 'cancel' },
        { text: 'Đồng ý', onPress: () => resolve(true) }
      ],
      { cancelable: false }
    );
  });
}

export const toastSwal = {
  fire: ({ title, text, icon }) => {
    if (icon === 'success') showToastSuccess(text || title);
    else if (icon === 'error') showError(text || title);
    else showWarning(text || title);
  }
};
