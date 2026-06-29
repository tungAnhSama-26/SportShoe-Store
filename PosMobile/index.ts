import { registerRootComponent } from 'expo';
import { TextEncoder, TextDecoder } from 'text-encoding';

// Polyfill TextEncoder/Decoder cho React Native (cần thiết cho @stomp/stompjs v7+)
global.TextEncoder = TextEncoder as any;
global.TextDecoder = TextDecoder as any;

import App from './App';

// registerRootComponent calls AppRegistry.registerComponent('main', () => App);
// It also ensures that whether you load the app in Expo Go or in a native build,
// the environment is set up appropriately
registerRootComponent(App);
