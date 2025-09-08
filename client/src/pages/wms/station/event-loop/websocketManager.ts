/**
 * WebSocket连接管理器
 * 提供自动重连、智能心跳和错误处理功能
 */

interface WebSocketConfig {
  url: string;
  maxReconnectAttempts?: number;
  reconnectDelay?: number;
  heartbeatInterval?: number;
  onMessage?: (data: any) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: Event) => void;
}

class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts: number;
  private reconnectDelay: number;
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private heartbeatTime: number;
  private isConnecting = false;
  private isDestroyed = false;
  private config: WebSocketConfig;

  constructor(config: WebSocketConfig) {
    this.config = config;
    this.maxReconnectAttempts = config.maxReconnectAttempts || 5;
    this.reconnectDelay = config.reconnectDelay || 1000;
    this.heartbeatTime = config.heartbeatInterval || 30000;
  }

  /**
   * 建立WebSocket连接
   */
  async connect(): Promise<void> {
    if (this.isConnecting || this.isDestroyed) {
      return;
    }

    if (this.ws?.readyState === WebSocket.OPEN) {
      return;
    }

    this.isConnecting = true;
    
    try {
      this.ws = new WebSocket(this.config.url);
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功:', this.config.url);
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
            console.error('解析WebSocket消息失败:', error);
          }
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭:', event.code, event.reason);
        this.stopHeartbeat();
        this.isConnecting = false;
        this.config.onDisconnect?.();
        
        // 如果不是主动关闭，则尝试重连
        if (!this.isDestroyed && event.code !== 1000) {
          this.handleReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.isConnecting = false;
        this.config.onError?.(error);
      };

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.isConnecting = false;
      this.handleReconnect();
    }
  }

  /**
   * 处理重连逻辑
   */
  private handleReconnect(): void {
    if (this.isDestroyed || this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('达到最大重连次数或已销毁，停止重连');
      return;
    }

    this.reconnectAttempts++;
    // 指数退避算法
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
    
    console.log(`WebSocket将在${delay}ms后尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      if (!this.isDestroyed) {
        this.connect();
      }
    }, delay);
  }

  /**
   * 开始心跳检测
   */
  private startHeartbeat(): void {
    this.stopHeartbeat(); // 先停止之前的心跳
    
    this.heartbeatInterval = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send('ping');
      } else {
        this.stopHeartbeat();
      }
    }, this.heartbeatTime);
  }

  /**
   * 停止心跳检测
   */
  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  /**
   * 发送消息
   * @param message 要发送的消息
   */
  send(message: string | object): boolean {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const data = typeof message === 'string' ? message : JSON.stringify(message);
      this.ws.send(data);
      return true;
    }
    console.warn('WebSocket未连接，无法发送消息');
    return false;
  }

  /**
   * 获取连接状态
   */
  getReadyState(): number | null {
    return this.ws?.readyState || null;
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isDestroyed = true;
    this.stopHeartbeat();
    
    if (this.ws) {
      this.ws.close(1000, '主动断开连接');
      this.ws = null;
    }
  }

  /**
   * 重置重连计数
   */
  resetReconnectAttempts(): void {
    this.reconnectAttempts = 0;
  }
}

export default WebSocketManager;
