import { Client, type StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketClient {
  private client: Client | null = null;
  private connecting: boolean = false;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 5;
  private isReady: boolean = false;
  private pendingSubscriptions: Array<() => void> = [];

  connect(onConnect: () => void) {
    if (this.isConnected()) {
      console.log('WebSocket already connected');
      onConnect();
      return;
    }

    if (this.connecting) {
      console.log('WebSocket connection in progress');
      return;
    }

    this.connecting = true;
    this.isReady = false;

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8083/ws'),
      onConnect: () => {
        this.connecting = false;
        this.reconnectAttempts = 0;
        this.isReady = true;
        console.log('WebSocket connected');
        
        // 대기 중이던 구독 실행
        this.pendingSubscriptions.forEach(sub => sub());
        this.pendingSubscriptions = [];
        
        onConnect();
      },
      onStompError: (frame) => {
        this.connecting = false;
        this.isReady = false;
        console.error('STOMP error:', frame);
        this.handleReconnect(onConnect);
      },
      onDisconnect: () => {
        this.connecting = false;
        this.isReady = false;
        console.log('WebSocket disconnected');
      },
      onWebSocketClose: () => {
        this.connecting = false;
        this.isReady = false;
        console.log('WebSocket connection closed');
        this.handleReconnect(onConnect);
      },
      debug: (_str) => {
        // console.log('STOMP Debug:', _str);
      }
    });
    
    this.client.activate();
  }

  private handleReconnect(onConnect: () => void) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Reconnecting... Attempt ${this.reconnectAttempts}`);
      setTimeout(() => {
        this.connect(onConnect);
      }, 2000 * this.reconnectAttempts);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  // ✅ 수정: unsubscribe 함수를 반환하도록
  subscribe(destination: string, callback: (message: any) => void): () => void {
    let subscription: StompSubscription | null = null;

    const doSubscribe = () => {
      if (!this.client || !this.isReady) {
        this.pendingSubscriptions.push(doSubscribe);
        return;
      }

      try {
        subscription = this.client.subscribe(destination, (msg) => {
          try {
            callback(JSON.parse(msg.body));
          } catch (e) {
            callback(msg.body);
          }
        });
      } catch (error) {
        console.error('Subscribe error:', error);
        this.pendingSubscriptions.push(doSubscribe);
      }
    };

    doSubscribe();

    // ✅ unsubscribe 함수 반환
    return () => {
      if (subscription) {
        subscription.unsubscribe();
        subscription = null;
      }
    };
  }

  send(destination: string, body: any = {}) {
    if (!this.isConnected() || !this.isReady) {
      console.warn('WebSocket not ready, message not sent');
      return;
    }
    
    try {
      const payload = typeof body === 'string' ? body : JSON.stringify(body);
      this.client?.publish({ destination, body: payload });
    } catch (error) {
      console.error('Send error:', error);
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connecting = false;
    this.reconnectAttempts = 0;
    this.isReady = false;
    this.pendingSubscriptions = [];
  }

  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

export const wsClient = new WebSocketClient();