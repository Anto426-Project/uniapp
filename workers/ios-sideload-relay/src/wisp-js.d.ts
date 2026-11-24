declare module '@mercuryworkshop/wisp-js/server' {
  export interface WispSocketAdapter {
    connect(): Promise<void>;
    recv(): Promise<Uint8Array | null>;
    send(data: Uint8Array | ArrayBuffer | ArrayBufferView): Promise<void>;
    close(): Promise<void>;
    pause?(): void;
    resume?(): void;
  }

  export type WispSocketConstructor = new (hostname: string, port: number) => WispSocketAdapter;

  export const server: {
    options: {
      hostname_whitelist?: RegExp[];
      port_whitelist?: number[];
      allow_direct_ip?: boolean;
      allow_private_ips?: boolean;
      allow_loopback_ips?: boolean;
      allow_udp_streams?: boolean;
      allow_tcp_streams?: boolean;
      wisp_version?: number;
    };
    ServerConnection: new (
      ws: WebSocket,
      path: string,
      opts: {
        TCPSocket: WispSocketConstructor;
        UDPSocket: WispSocketConstructor;
        ping_interval?: number;
        wisp_version?: number;
      }
    ) => {
      setup(): Promise<void>;
      run(): Promise<void>;
    };
  };
}
