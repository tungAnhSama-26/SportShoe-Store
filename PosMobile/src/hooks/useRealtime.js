import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import { Platform } from 'react-native';

// Giao thức ws:// cho WebSocket thuần thay vì http:// của SockJS
// Lưu ý: Nếu chạy trên thiết bị thật, hãy thay '10.0.2.2' hoặc 'localhost' thành IP mạng LAN của máy tính chạy backend (ví dụ: '192.168.1.5:8080')
const WS_BROKER_URL = Platform.OS === 'android' ? 'ws://10.0.2.2:8080/ws' : 'ws://localhost:8080/ws';

// Global variables for singleton client
let stompClient = null;
let globalIsConnected = false;
let globalSubscriptions = [];

function getClient() {
  if (!stompClient) {
    stompClient = new Client({
      brokerURL: WS_BROKER_URL,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Connected to WebSocket (React Native)');
        globalIsConnected = true;
        // Resubscribe to all topics upon reconnect
        globalSubscriptions.forEach(sub => {
          if (!sub.stompSubscription) {
            sub.stompSubscription = stompClient.subscribe(sub.topic, (message) => {
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
        globalIsConnected = false;
        globalSubscriptions.forEach(sub => sub.stompSubscription = null);
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed (React Native)');
        globalIsConnected = false;
        globalSubscriptions.forEach(sub => sub.stompSubscription = null);
      }
    });
  }
  return stompClient;
}

export function useRealtime() {
  const [isConnected, setIsConnected] = useState(globalIsConnected);
  const client = getClient();

  useEffect(() => {
    if (!client.active && !globalIsConnected) {
      client.activate();
    }
    const interval = setInterval(() => {
      setIsConnected(globalIsConnected);
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const componentSubscriptions = useRef([]);

  const subscribeTopic = useCallback((topic, callback) => {
    const sub = { topic, callback, stompSubscription: null };
    
    if (client.connected) {
      sub.stompSubscription = client.subscribe(topic, (message) => {
        if (message.body) {
          callback(JSON.parse(message.body));
        }
      });
    }
    
    globalSubscriptions.push(sub);
    componentSubscriptions.current.push(sub);
    return sub;
  }, []);

  const publishMessage = useCallback((topic, message) => {
    if (client.connected) {
      client.publish({
        destination: topic,
        body: JSON.stringify(message)
      });
    }
  }, []);

  const unsubscribeTopic = useCallback((sub) => {
    if (!sub) return;
    if (sub.stompSubscription) {
      sub.stompSubscription.unsubscribe();
    }
    const index = globalSubscriptions.indexOf(sub);
    if (index > -1) {
      globalSubscriptions.splice(index, 1);
    }
    const compIndex = componentSubscriptions.current.indexOf(sub);
    if (compIndex > -1) {
      componentSubscriptions.current.splice(compIndex, 1);
    }
  }, []);

  useEffect(() => {
    return () => {
      // Clean up subscriptions created by this component
      componentSubscriptions.current.forEach(sub => {
        if (sub.stompSubscription) {
          sub.stompSubscription.unsubscribe();
        }
        const index = globalSubscriptions.indexOf(sub);
        if (index > -1) {
          globalSubscriptions.splice(index, 1);
        }
      });
      componentSubscriptions.current = [];
    };
  }, []);

  return {
    isConnected,
    subscribeTopic,
    unsubscribeTopic,
    publishMessage
  };
}
