/**
 * WebSocket连接管理器
 * 提供自动重连、智能心跳和错误处理功能
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

    // 验证URL
    if (!this.validateUrl(this.config.url)) {
      console.error('WebSocket URL无效:', this.config.url);
      this.isConnecting = false;
      return;
    }

    this.isConnecting = true;
    this.clearConnectionTimeout();

    // 详细连接信息记录
    console.log('🚀 开始建立WebSocket连接...');
    console.log('📍 连接URL:', this.config.url);
    console.log('🔧 连接配置:', {
      maxReconnectAttempts: this.maxReconnectAttempts,
      reconnectDelay: this.reconnectDelay,
      connectionTimeout: this.connectionTimeoutTime,
      heartbeatInterval: this.heartbeatTime
    });

    // 设置连接超时
    this.connectionTimeout = setTimeout(() => {
      if (this.isConnecting && this.ws?.readyState === WebSocket.CONNECTING) {
        console.warn('WebSocket连接超时，关闭连接');
        this.isConnecting = false;
        this.ws?.close(1006, 'Connection timeout');
        this.config.onTimeout?.();
        this.handleReconnect();
      }
    }, this.connectionTimeoutTime);

    try {
      this.ws = new WebSocket(this.config.url);

      this.ws.onopen = () => {
        console.log('WebSocket连接成功:', this.config.url);
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
            console.error('解析WebSocket消息失败:', error);
          }
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭:', event.code, event.reason);
        this.clearConnectionTimeout();
        this.stopHeartbeat();
        this.isConnecting = false;

        // 详细的关闭原因分析
        this.analyzeCloseCode(event.code, event.reason);
        this.config.onDisconnect?.();

        // 如果不是主动关闭，则尝试重连
        if (!this.isDestroyed && event.code !== 1000) {
          this.handleReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.clearConnectionTimeout();
        this.isConnecting = false;
        this.config.onError?.(error);
      };

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.clearConnectionTimeout();
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
   * 清除连接超时定时器
   */
  private clearConnectionTimeout(): void {
    if (this.connectionTimeout) {
      clearTimeout(this.connectionTimeout);
      this.connectionTimeout = null;
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
    this.clearConnectionTimeout();

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

  /**
   * 验证WebSocket URL
   */
  private validateUrl(url: string): boolean {
    if (!url || typeof url !== 'string') {
      return false;
    }

    // 检查是否以ws://或wss://开头，或者相对路径
    const isValidProtocol = url.startsWith('ws://') ||
                           url.startsWith('wss://') ||
                           url.startsWith('/');

    if (!isValidProtocol) {
      console.warn('WebSocket URL协议无效，应为 ws://, wss:// 或相对路径');
      return false;
    }

    return true;
  }

  /**
   * 分析WebSocket关闭代码
   */
  private analyzeCloseCode(code: number, reason: string): void {
    const closeReasons: { [key: number]: string } = {
      1000: '正常关闭',
      1001: '端点离开',
      1002: '协议错误',
      1003: '不支持的数据类型',
      1005: '无状态',
      1006: '异常关闭（连接未建立）',
      1007: '无效数据',
      1008: '策略违反',
      1009: '消息过大',
      1010: '客户端扩展缺失',
      1011: '服务器错误',
      1012: '服务重启',
      1013: '尝试重连',
      1014: '网关错误',
      1015: 'TLS握手失败'
    };

    const reasonText = closeReasons[code] || '未知原因';
    console.log(`WebSocket关闭分析: 代码=${code} (${reasonText}), 原因=${reason}`);

    // 特别处理连接未建立就关闭的情况
    if (code === 1006) {
      console.error('🚨 WebSocket在连接建立前就关闭了 (代码1006)');
      console.error('🔍 详细诊断信息：');
      console.error('  - 连接URL:', this.config.url);
      console.error('  - 关闭原因:', reason || '无具体原因');
      console.error('  - 重连次数:', this.reconnectAttempts);
      console.error('🔧 可能的原因和解决方案：');
      console.error('  1. 服务器拒绝连接 - 检查认证令牌是否有效');
      console.error('  2. 网络连接问题 - 检查网络连接和防火墙设置');
      console.error('  3. 代理阻止连接 - 检查代理配置');
      console.error('  4. 服务器未运行 - 确认WebSocket服务正在运行');
      console.error('  5. 跨域问题 - 检查CORS配置');

      // 如果是第一次连接就失败，提供更具体的建议
      if (this.reconnectAttempts === 0) {
        console.error('💡 首次连接失败建议：');
        console.error('  - 检查浏览器控制台的网络面板');
        console.error('  - 验证WebSocket服务器端点是否可访问');
        console.error('  - 确认认证令牌未过期');
      }
    }
  }
}

export default WebSocketManager;
