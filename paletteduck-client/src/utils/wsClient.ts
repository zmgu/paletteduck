// src/utils/wsClient.ts
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketClient {
  private client: Client | null = null;

  connect(onConnect: () => void) {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8083/ws'),
      onConnect,
      onStompError: (frame) => console.error('STOMP error:', frame),
    });
    this.client.activate();
  }

  subscribe(destination: string, callback: (message: any) => void) {
    return this.client?.subscribe(destination, (msg) => {
      callback(JSON.parse(msg.body));
    });
  }

  send(destination: string, body: any = {}) {
    this.client?.publish({ destination, body: JSON.stringify(body) });
  }

  disconnect() {
    this.client?.deactivate();
  }
}

export const wsClient = new WebSocketClient();