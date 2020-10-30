function _makeURL(host, uri, endpoint, params){
  const builder = [];
  const separator = '/';
  
  // HOST
  if( host && host.length > 0 ){
    if( host.charAt(host.length-1) === separator ){
      host = host.slice(0, host.length-1);
    }
    builder.push( host );
  }
  
  // URI
  if( uri && uri.length > 0 ){
    if( uri.charAt(0) === separator ){
      uri = uri.slice(1, uri.length);
    } 
    builder.push( separator );
    builder.push( uri );
  }
  
  // Endpoint
  if( endpoint && endpoint.length > 0 ){
    if( endpoint.charAt(0) === separator ){
      endpoint = endpoint.slice(1, endpoint.length);
    }
    builder.push( separator );
    builder.push( endpoint );
  }
  
  // Last Character
  if( builder.length === 1 ){
    const atLast = builder[builder.length-1];
    
    if( atLast && atLast.charAt(atLast.length-1) !== separator ){
      builder.push(separator);
    }
  }
  
  // Params
  if( params ){    
    // Query String
    builder.push('?');
    builder.push(
      Object.keys(params).map(function(key){
        return key+'='+encodeURIComponent(params[key]);
      }).join('&')
    );
  }
  
  // Made URL
  return builder.join('');
}

function ChatWS( _config ){
  
  let socket = null;
  
  // Configurations
  const config = Object.assign({}, {
    // For Connection
    url: "ws://localhost:3000/",
    endpoint: "chat",
    room: "lobby",
    protocol: null,
    params: null,
    
    // For Render
    root: document.body,
    
    // For Handlers
    onOpen: null,
    onMessage: null,
    onError: null,
  }, _config);
  
  
  this.setSocket = function(v){ socket = v; }
  this.getSocket = function(){ return socket||null; }
  this.clearSocket = function(){ socket = null; }
  
  this.setConfig = function(k,v){ config[k] = v; }
  this.getConfig = function(k,d){ return config[k]||d; }
}

ChatWS.prototype = (function(){
  
  // Connection
  function _connect(self, url, endpoint){
    const URL = _makeURL(
      self.getConfig("url", null),
      self.getConfig("endpoint", null),
      self.getConfig("room", null),
      self.getConfig("params", null),
    );
    const protocol = self.getConfig("protocol", null);    
    
    // Create WebSocket
    const socket = new WebSocket(URL, protocol);
    
    // Set Configurations
    socket.binaryType = self.getConfig("binaryType", "arraybuffer");
    socket.bufferedAmount = self.getConfig("bufferedAmount", 0);

    // Receive Open
    const onOpen = self.getConfig("onOpen", null);
    if( onOpen && onOpen instanceof Function ){
      socket.onopen = onOpen.bind(self);
    }
  
    // Receive Message
    const onMessage = self.getConfig("onMessage", null);
    if( onMessage && onMessage instanceof Function ){
      socket.onmessage = onMessage.bind(self);
    }

    // Receive Close
    const onClose = self.getConfig("onClose", null);
    if( onClose && onClose instanceof Function ){
      socket.onClose = onClose.bind(self);
    }

    // Receive Error
    const onError = self.getConfig("onError", null);
    if( onError && onError instanceof Function ){
      socket.onerror = onError.bind(self);
    }
    
    // Store Socket
    self.setSocket(socket);
  }
  
  // Disconnection
  function _disconnect(self){
    const socket = self.getSocket();
    
    if( socket && socket.readyState == socket.OPEN ){
      socket.close();
    }
  }
  
  // Get Socket State
  function _getState(self){
    const codeMap = [
      {
        code: 0,
        name: "CONNECTING",
        desc: "연결이 수립되지 않은 상태입니다."
      },
      {
        code: 1,
        name: "OPEN",
        desc: "연결이 수립되어 데이터가 오고갈 수 있는 상태입니다.",
      },
      {
        code: 2,
        name: "CLOSING",
        desc: "연결이 닫히는 중 입니다.",
      },
      {
        code: 3,
        name: "CLOSED",
        desc: "연결이 종료되었거나, 연결에 실패한 경우입니다.",
      }
    ];
    
    const socket = self.getSocket();
    if( !socket ){
      return null;
    }
    return codeMap.filter( data => data.code === socket.readyState )[0]||null;
  }
  
  // Message Handlers
  function _send(self, message){
    const socket = self.getSocket();
    
    socket.send(message);
  }
  function _sendJson(self, message){
    _send(self, JSON.stringify(message));
  }
  function _sendMessage(self, message){
    _send(self, JSON.stringify({
      content: message
    }));
  }
  
  // Mathods
  return {
    connect: function(url, endpoint){
      return _connect(this, url, endpoint);
    },
    disconnect: function(){
      _disconnect(this);
    },
    send: function(message){
      //_send(this, message);
      _sendMessage(this, message);
    },
    sendMessage: function(message){
      _sendMessage(this, message);
    },
    sendJson: function(message){
      _sendJson(this, message);
    },
    getState: function(){
      return _getState(this);
    },
  };
})();


chat1 = new ChatWS({
  url: "ws://localhost:3000",
  endpoint: "chat",
  room: "Dochi's Room",
  protocol: [
    "chat-ws"
  ],
  params: {
    username: "heo",
  },
  //binaryType: "blob",
  onOpen: function(event){
    console.log( this.getConfig("params").username, ":", "open" );
    
    this.send("Hi, i'm "+this.getConfig("params").username);
  },
  onMessage: function(event){
    console.log( this.getConfig("params").username, ":", event.data );
  },
  onClose: function(event){
    console.log( this.getConfig("params").username, ":", "close" );
  },
  onError: function( error ){
    console.error( this.getConfig("params").username, ":", error );
    this.disconnect();
  },
});


chat2 = new ChatWS({
  url: "ws://localhost:3000",
  endpoint: "chat",
  room: "Dochi's Room",
  protocol: [
    "chat-ws"
  ],
  params: {
    username: "chat2",
  },
  //binaryType: "blob",
  onOpen: function(event){
    console.log( this.getConfig("params").username, ":", "open" );
    
    this.send("Hi, i'm "+this.getConfig("params").username);
  },
  onMessage: function(event){
    console.log( this.getConfig("params").username, ":", event.data );
  },
  onClose: function(event){
    console.log( this.getConfig("params").username, ":", "close" );
  },
  onError: function( error ){
    console.error( this.getConfig("params").username, ":", error );
    this.disconnect();
  },
});


chat3 = new ChatWS({
  url: "ws://localhost:3000",
  endpoint: "chat",
  room: "Dochi's Room",
  protocol: [
    "chat-ws"
  ],
  params: {
    username: "chat3",
  },
  //binaryType: "blob",
  onOpen: function(event){
    console.log( this.getConfig("params").username, ":", "open" );
    
    this.send("Hi, i'm "+this.getConfig("params").username);
  },
  onMessage: function(event){
    console.log( this.getConfig("params").username, ":", event.data );
  },
  onClose: function(event){
    console.log( this.getConfig("params").username, ":", "close" );
  },
  onError: function( error ){
    console.error( this.getConfig("params").username, ":", error );
    this.disconnect();
  },
});


chat1.connect();
chat2.connect();
chat3.connect();