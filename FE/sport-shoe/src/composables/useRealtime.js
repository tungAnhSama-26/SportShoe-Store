import { Client } from '@stomp/stompjs';
import { ref, onUnmounted } from 'vue';

const getBrokerURL = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    // Nếu chạy local trực tiếp không qua proxy (ví dụ localhost:5173 kết nối tới localhost:8080)
    if (host.includes('localhost') || host.includes('127.0.0.1')) {
        return `${protocol}//localhost:8080/ws`;
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
            if (!sub.isSSE && !sub.stompSubscription) {
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
        subscriptions.forEach(sub => {
            if (!sub.isSSE) {
                sub.stompSubscription = null;
            }
        });
    }
});

const isConnected = ref(false);
const subscriptions = []; // Global list of all active subscriptions
const sseConnections = {}; // Global map of SSE connections

export function useRealtime() {
    if (!client.active && !isConnected.value) {
        client.activate();
    }

    const componentSubscriptions = []; // Local to the component using this composable

    const subscribeTopic = (topic, callback) => {
        const useSSE = topic === '/topic/admin/san-pham' || topic === '/topic/admin/thuoc-tinh';

        if (useSSE) {
            const sub = { topic, callback, isSSE: true };
            
            if (!sseConnections[topic]) {
                const host = window.location.host;
                let baseUrl = '';
                if (host.includes('localhost') || host.includes('127.0.0.1')) {
                    baseUrl = 'http://localhost:8080';
                }
                const eventSource = new EventSource(`${baseUrl}/api/v1/sse/subscribe?topic=${encodeURIComponent(topic)}`);
                
                sseConnections[topic] = {
                    eventSource,
                    subscribers: []
                };
                
                eventSource.onmessage = (event) => {
                    if (event.data) {
                        try {
                            const data = JSON.parse(event.data);
                            sseConnections[topic].subscribers.forEach(s => s.callback(data));
                        } catch (e) {
                            console.error('Error parsing SSE data', e);
                        }
                    }
                };
                
                eventSource.onerror = (error) => {
                    console.error('SSE Error for topic', topic, error);
                };
            }
            
            sseConnections[topic].subscribers.push(sub);
            subscriptions.push(sub);
            componentSubscriptions.push(sub);
            return sub;
        }

        const sub = { topic, callback, isSSE: false, stompSubscription: null };
        
        if (client.connected) {
            sub.stompSubscription = client.subscribe(topic, (message) => {
                if (message.body) {
                    callback(JSON.parse(message.body));
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
        
        if (sub.isSSE) {
            const topicConn = sseConnections[sub.topic];
            if (topicConn) {
                topicConn.subscribers = topicConn.subscribers.filter(s => s !== sub);
                if (topicConn.subscribers.length === 0) {
                    topicConn.eventSource.close();
                    delete sseConnections[sub.topic];
                }
            }
        } else {
            if (sub.stompSubscription) {
                sub.stompSubscription.unsubscribe();
            }
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

