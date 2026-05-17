/**
 * WebSocket connection manager
 * Provides auto-reconnect, heartbeat, and error handling.
 */

interface WebSocketConfig {
  url: string;
  maxReconnectAttempts?: number;
  reconnectDelay?: number;
  heartbeatInterval?: number;
  connectionTimeout?: number;
  onMessage?: (data: any) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
  onTimeout?: () => void;
}

class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts: number;
  private reconnectDelay: number;
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private connectionTimeout: NodeJS.Timeout | null = null;
  private heartbeatTime: number;
  private connectionTimeoutTime: number;
  private isConnecting = false;
  private isDestroyed = false;
  private config: WebSocketConfig;

  constructor(config: WebSocketConfig) {
    this.config = config;
    this.maxReconnectAttempts = config.maxReconnectAttempts || 5;
    this.reconnectDelay = config.reconnectDelay || 1000;
    this.heartbeatTime = config.heartbeatInterval || 30000;
    this.connectionTimeoutTime = config.connectionTimeout || 10000;
  }

  async connect(): Promise<void> {
    if (this.isConnecting || this.isDestroyed) {
      return;
    }

    if (this.ws?.readyState === WebSocket.OPEN) {
      return;
    }

    if (!this.validateUrl(this.config.url)) {
      console.error('Invalid WebSocket URL:', this.config.url);
      this.isConnecting = false;
      return;
    }

    this.isConnecting = true;
    this.clearConnectionTimeout();

    this.connectionTimeout = setTimeout(() => {
      if (this.isConnecting && this.ws?.readyState === WebSocket.CONNECTING) {
        console.warn('WebSocket connection timed out, closing');
        this.isConnecting = false;
        this.ws?.close(1006, 'Connection timeout');
        this.config.onTimeout?.();
        this.handleReconnect();
      }
    }, this.connectionTimeoutTime);

    try {
      this.ws = new WebSocket(this.config.url);

      this.ws.onopen = () => {
        console.log('WebSocket connected:', this.config.url);
        this.clearConnectionTimeout();
        this.reconnectAttempts = 0;
        this.isConnecting = false;
        this.startHeartbeat();
        this.config.onConnect?.();
      };

      this.ws.onmessage = (event) => {
        if (event.data && this.config.onMessage) {
          try {
            const data = JSON.parse(event.data);
            this.config.onMessage(data);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket connection closed:', event.code, event.reason);
        this.clearConnectionTimeout();
        this.stopHeartbeat();
        this.isConnecting = false;

        this.analyzeCloseCode(event.code, event.reason);
        this.config.onDisconnect?.();

        if (!this.isDestroyed && event.code !== 1000) {
          this.handleReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.clearConnectionTimeout();
        this.isConnecting = false;
        this.config.onError?.(error);
      };

    } catch (error) {
      console.error('WebSocket connection failed:', error);
      this.clearConnectionTimeout();
      this.isConnecting = false;
      this.handleReconnect();
    }
  }

  private handleReconnect(): void {
    if (this.isDestroyed || this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnect attempts reached or destroyed, stopping reconnect');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`WebSocket will reconnect in ${delay}ms (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

    setTimeout(() => {
      if (!this.isDestroyed) {
        this.connect();
      }
    }, delay);
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();

    this.heartbeatInterval = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send('ping');
      } else {
        this.stopHeartbeat();
      }
    }, this.heartbeatTime);
  }

  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private clearConnectionTimeout(): void {
    if (this.connectionTimeout) {
      clearTimeout(this.connectionTimeout);
      this.connectionTimeout = null;
    }
  }

  send(message: string | object): boolean {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const data = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(data);
      return true;
    }
    console.warn('WebSocket is not connected, cannot send message');
    return false;
  }

  getReadyState(): number | null {
    return this.ws?.readyState || null;
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  disconnect(): void {
    this.isDestroyed = true;
    this.stopHeartbeat();
    this.clearConnectionTimeout();

    if (this.ws) {
      this.ws.close(1000, 'Client disconnected');
      this.ws = null;
    }
  }

  /**
   * Reset destroyed state to allow reconnection after destroy().
   */
  resume(): void {
    this.isDestroyed = false
    this.reconnectAttempts = 0
  }

  resetReconnectAttempts(): void {
    this.reconnectAttempts = 0;
  }

  private validateUrl(url: string): boolean {
    if (!url || typeof url !== 'string') {
      return false;
    }

    const isValidProtocol = url.startsWith('ws://') ||
                           url.startsWith('wss://') ||
                           url.startsWith('/');

    if (!isValidProtocol) {
      console.warn('Invalid WebSocket URL protocol, must be ws://, wss://, or a relative path');
      return false;
    }

    return true;
  }

  private analyzeCloseCode(code: number, reason: string): void {
    const closeReasons: { [key: number]: string } = {
      1000: 'Normal closure',
      1001: 'Endpoint leaving',
      1002: 'Protocol error',
      1003: 'Unsupported data type',
      1005: 'No status received',
      1006: 'Abnormal closure (connection never established)',
      1007: 'Invalid data',
      1008: 'Policy violation',
      1009: 'Message too large',
      1010: 'Missing client extension',
      1011: 'Internal server error',
      1012: 'Service restart',
      1013: 'Try again later',
      1014: 'Bad gateway',
      1015: 'TLS handshake failure'
    };

    const reasonText = closeReasons[code] || 'Unknown reason';
    console.log(`WebSocket close: code=${code} (${reasonText}), reason=${reason}`);

    if (code === 1006) {
      console.error('WebSocket closed before connection was established (code 1006)');
      console.error('  URL:', this.config.url);
      console.error('  Close reason:', reason || 'none');
      console.error('  Reconnect attempts:', this.reconnectAttempts);
      console.error('  Possible causes: invalid auth token, network issue, proxy blocking, server down, CORS');

      if (this.reconnectAttempts === 0) {
        console.error('  First-connection failure: check network panel, verify endpoint is reachable, confirm token is valid');
      }
    }
  }
}

export default WebSocketManager;
