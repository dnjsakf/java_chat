function _makeURL(url, endpoint, username){
  const madeURL = [];
  const c = '/';
  const n = 1;
  
  // URL
  if( url && url.length > 0 ){
    const len = url.length;
    const end = len - (url.charAt(len-1) === c ? n : 0);
    
    madeURL.push( url.slice(0, end) );
  }
  
  // Endpoint
  if( endpoint && endpoint.length > 0 ){
    const len = endpoint.length;
    const end = len - (endpoint.charAt(len-1) === c ? n : 0);
    
    madeURL.push( endpoint.slice(0, end) );
  }
  
  // URL or Params?
  if( username && username.length > 0 ){
    madeURL.push(username.split(c, n)[0]);
  }
  
  return madeURL.join(c);
}

function ChatWS( _config ){
  
  // Configurations
  const config = Object.assign({}, {
    // For Connection
    url: "ws://localhost:3000/",
    endpoint: "chat",
    username: "anonymous",
    
    // For Render
    root: document.body,
    
    // For Handlers
    onOpen: null,
    onMessage: null,
    onError: null,
  }, _config);
  
  this.setConfig = function(k,v){ config[k] = v; }
  this.getConfig = function(k){ return config[k]; }
}

ChatWS.prototype = (function(){
  function _connect(self, url, endpoint){
    
    // Create WebSocket
    const socket = new WebSocket(_makeURL(
      self.getConfig("url"),
      self.getConfig("endpoint"),
      self.getConfig("username")
    ));

    // Receive Open
    const onOpen = self.getConfig("onOpen");
    if( onOpen && onOpen instanceof Function ){
      socket.onopen = onOpen.bind(self);
    }
  
    // Receive Message
    const onMessage = self.getConfig("onMessage");
    if( onMessage && onMessage instanceof Function ){
      socket.onmessage = onMessage.bind(self);
    } else {
      socket.onmessage = function(event){
        console.log( event.data );
      }
    }

    // Receive Error
    const onError = self.getConfig("onError");
    if( onError && onError instanceof Function ){
      socket.onerror = onError.bind(self);
    }
    
    // Store Socket
    self.setConfig("socket", socket);
  }
  
  function _disconnect(self){
    const socket = self.getConfig("socket");
    
    if( socket && socket.readyState == socket.OPEN ){
      socket.close();
    }
  }
  
  function _getState(self){
    const socket = self.getConfig("socket");
    
    if( socket ){
      return socket.readyState;
    }
    
    return null;
  }
  
  function _send(self, message){
    const socket = self.getConfig("socket");
    
    socket.send(message);
  }
  
  function _sendJson(self, message){
    _send(self, JSON.stringify(message));
  }
  
  return {
    connect: function(url, endpoint){
      return _connect(this, url, endpoint);
    },
    disconnect: function(){
      _disconnect(this);
    },
    send: function(message){
      _send(this, message);
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
  username: "heo",
  onOpen: function(event){
    
  },
  onMessage: function(event){
    console.log( event.data );
  },
  onError: function( error ){
    console.error( error );
  }
});
chat1.connect();


chat2 = new ChatWS({
  url: "ws://localhost:3000",
  endpoint: "chat",
  username: "jeong",
  onOpen: function(event){
    
  },
  onMessage: function(event){
    console.log( event.data );
  },
  onError: function( error ){
    console.error( error );
  }
});
chat2.connect();



chat3 = new ChatWS({
  url: "ws://localhost:3000",
  endpoint: "chat",
  username: "un",
  onOpen: function(event){
    this.sendJson({
      content: "Hello~~~",
    });
  },
  onMessage: function(event){
    console.log( event.data );
  },
  onError: function( error ){
    console.error( error );
  }
});
chat3.connect();