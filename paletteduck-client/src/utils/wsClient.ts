import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketClient {
  private client: Client | null = null;
  private connecting: boolean = false;

  connect(onConnect: () => void) {
    // 이미 연결되었거나 연결 중이면 무시
    if (this.isConnected() || this.connecting) {
      console.log('WebSocket already connected or connecting');
      onConnect();
      return;
    }

    this.connecting = true;

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8083/ws'),
      onConnect: () => {
        this.connecting = false;
        console.log('WebSocket connected');
        onConnect();
      },
      onStompError: (frame) => {
        this.connecting = false;
        console.error('STOMP error:', frame);
      },
      onDisconnect: () => {
        this.connecting = false;
        console.log('WebSocket disconnected');
      },
    });
    this.client.activate();
  }

  subscribe(destination: string, callback: (message: any) => void) {
    if (!this.client) return;
    return this.client.subscribe(destination, (msg) => callback(JSON.parse(msg.body)));
  }

  send(destination: string, body: any = {}) {
    if (!this.isConnected()) {
      console.warn('WebSocket not connected, message not sent');
      return;
    }
    const payload = typeof body === 'string' ? body : JSON.stringify(body);
    this.client?.publish({ destination, body: payload });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connecting = false;
  }

  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

export const wsClient = new WebSocketClient();