import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { ref, onUnmounted } from 'vue';

const client = new Client({
    // We use SockJS for fallback
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
        console.log('Connected to WebSocket');
        isConnected.value = true;
        
        // Resubscribe to all topics upon reconnect
        subscriptions.forEach(sub => {
            if (!sub.stompSubscription) {
                sub.stompSubscription = client.subscribe(sub.topic, (message) => {
                    if (message.body) {
                        sub.callback(JSON.parse(message.body));
                    }
                });
            }
        });
    },
    onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
    },
    onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        isConnected.value = false;
        subscriptions.forEach(sub => sub.stompSubscription = null);
    }
});

const isConnected = ref(false);
const subscriptions = []; // Global list of all active subscriptions

export function useRealtime() {
    if (!client.active && !isConnected.value) {
        client.activate();
    }

    const componentSubscriptions = []; // Local to the component using this composable

    const subscribeTopic = (topic, callback) => {
        const sub = { topic, callback, stompSubscription: null };
        
        if (client.connected) {
            sub.stompSubscription = client.subscribe(topic, (message) => {
                if (message.body) {
                    callback(JSON.parse(message.body));
                }
            });
        }
        
        subscriptions.push(sub);
        componentSubscriptions.push(sub);
    };

    onUnmounted(() => {
        // Clean up subscriptions created by this component
        componentSubscriptions.forEach(sub => {
            if (sub.stompSubscription) {
                sub.stompSubscription.unsubscribe();
            }
            const index = subscriptions.indexOf(sub);
            if (index > -1) {
                subscriptions.splice(index, 1);
            }
        });
    });

    return {
        isConnected,
        subscribeTopic
    };
}
