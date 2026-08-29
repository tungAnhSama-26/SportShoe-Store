import { Client } from '@stomp/stompjs';
import { ref, onUnmounted } from 'vue';

const getBrokerURL = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    if (window.location.port === '5173' || window.location.port === '3000') {
        return `${protocol}//${window.location.hostname}:8080/ws`;
    }
    return `${protocol}//${host}/ws`;
};

const client = new Client({
    brokerURL: getBrokerURL(),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
        console.log('Connected to WebSocket');
        isConnected.value = true;
        
        // Resubscribe to all STOMP topics upon reconnect
        subscriptions.forEach(sub => {
            if (!sub.stompSubscription) {
                sub.stompSubscription = client.subscribe(sub.topic, (message) => {
                    if (message.body) {
                        try {
                            sub.callback(JSON.parse(message.body));
                        } catch (e) {
                            sub.callback(message.body);
                        }
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
        subscriptions.forEach(sub => {
            sub.stompSubscription = null;
        });
    },
    onWebSocketClose: () => {
        console.log('WebSocket connection closed');
        isConnected.value = false;
        subscriptions.forEach(sub => {
            sub.stompSubscription = null;
        });
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
                    try {
                        callback(JSON.parse(message.body));
                    } catch (e) {
                        callback(message.body);
                    }
                }
            });
        }
        
        subscriptions.push(sub);
        componentSubscriptions.push(sub);
        return sub;
    };

    const publishMessage = (topic, message) => {
        if (client.connected) {
            client.publish({
                destination: topic,
                body: JSON.stringify(message)
            });
        }
    };

    const unsubscribeTopic = (sub) => {
        if (!sub) return;
        
        if (sub.stompSubscription) {
            sub.stompSubscription.unsubscribe();
        }
        
        const index = subscriptions.indexOf(sub);
        if (index > -1) {
            subscriptions.splice(index, 1);
        }
        const compIndex = componentSubscriptions.indexOf(sub);
        if (compIndex > -1) {
            componentSubscriptions.splice(compIndex, 1);
        }
    };

    onUnmounted(() => {
        // Clean up subscriptions created by this component
        const subsToClean = [...componentSubscriptions];
        subsToClean.forEach(sub => unsubscribeTopic(sub));
    });

    return {
        isConnected,
        subscribeTopic,
        unsubscribeTopic,
        publishMessage
    };
}

