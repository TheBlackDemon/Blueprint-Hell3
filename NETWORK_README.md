# BlueprintHell Network Multiplayer

This document explains how to use the network multiplayer features of BlueprintHell.

## Overview

The game now supports both online multiplayer and offline play modes:

- **Online Mode**: Connect to a server to play with other players, view global leaderboards, and sync game progress
- **Offline Mode**: Play locally with local leaderboards and progress saved for later synchronization

## Architecture

The network implementation follows a client-server architecture:

- **Server** (`server.ServerMain`): Manages multiple client connections, handles game state synchronization, and maintains global leaderboards
- **Client** (`client.ClientMain`): Connects to server, sends user input, receives game updates, and manages local state
- **Network Protocol**: Uses TCP sockets with JSON message format for reliable communication

## Quick Start

### 1. Build the Project

```bash
./gradlew build
```

This will create three JAR files:
- `client.jar` - Client application
- `server.jar` - Server application  
- `BlueprintHell-5-1.0-SNAPSHOT.jar` - Original offline game

### 2. Start the Server

```bash
# Windows
launch_server.bat

# Or directly
java -jar build/libs/server.jar
```

The server will start on port 8888 by default.

### 3. Start the Client

```bash
# Windows
launch_client.bat

# Or directly
java -jar build/libs/client.jar
```

The client will attempt to connect to localhost:8888 by default.

### 4. Play Offline

```bash
# Windows
launch_offline.bat

# Or directly
java -jar build/libs/BlueprintHell-5-1.0-SNAPSHOT.jar
```

## Network Features

### Connection Management

- **Auto-Connect**: Client automatically attempts to connect to server on startup
- **Connection Status**: Real-time display of connection status in the main menu
- **Retry Mechanism**: Easy retry button if connection fails
- **Graceful Fallback**: Automatically switches to offline mode if server unavailable

### Game State Synchronization

- **Real-time Updates**: Game state changes are synchronized between all connected clients
- **User Input Broadcasting**: Player actions are shared with other players
- **State Consistency**: Server ensures all clients have consistent game state

### Leaderboard System

- **Global Leaderboards**: Server maintains global leaderboards across all players
- **Local Caching**: Client caches leaderboard data for offline viewing
- **Automatic Sync**: Offline game results are queued for synchronization when connection is restored
- **Multiple Metrics**: Tracks completion time, XP, coins, and level progress

### Offline Support

- **Local Storage**: Game progress and results are saved locally when offline
- **Sync Queue**: Offline results are automatically synchronized when connection is restored
- **Hybrid Mode**: Can switch between online and offline modes seamlessly

## Configuration

### Server Configuration

The server can be configured by modifying `ServerMain.java`:

```java
private static final int SERVER_PORT = 8888;        // Server port
private static final int MAX_CLIENTS = 10;          // Maximum concurrent clients
private static final long HEARTBEAT_INTERVAL = 5000; // Heartbeat interval (ms)
private static final long CLIENT_TIMEOUT = 30000;   // Client timeout (ms)
```

### Client Configuration

The client can be configured by modifying `ClientMain.java`:

```java
private static final String DEFAULT_SERVER_HOST = "localhost"; // Default server host
private static final int DEFAULT_SERVER_PORT = 8888;          // Default server port
private static final int CONNECTION_TIMEOUT = 5000;           // Connection timeout (ms)
private static final long HEARTBEAT_INTERVAL = 10000;         // Heartbeat interval (ms)
```

## Network Protocol

### Message Types

- `CONNECT`: Client connection request
- `DISCONNECT`: Client disconnection
- `GAME_STATE`: Game state update
- `USER_INPUT`: User input from client
- `LEADERBOARD`: Leaderboard data request/response
- `GAME_RESULT`: Game completion result
- `HEARTBEAT`: Keep-alive message
- `ERROR`: Error message

### Message Format

All messages use JSON format:

```json
{
  "type": "GAME_STATE",
  "data": "{...game state data...}",
  "clientId": "client_1",
  "timestamp": 1234567890,
  "sessionId": "session_123"
}
```

## File Structure

```
src/main/java/
├── network/                    # Network protocol and data structures
│   ├── NetworkMessage.java     # Base network message class
│   ├── GameStateData.java      # Serializable game state
│   ├── LeaderboardEntry.java   # Leaderboard entry data
│   ├── OfflineGameManager.java # Offline game management
│   └── GameStateConverter.java # State conversion utilities
├── server/                     # Server implementation
│   ├── ServerMain.java         # Main server class
│   └── ClientHandler.java      # Individual client handler
├── client/                     # Client implementation
│   ├── ClientMain.java         # Main client class
│   └── NetworkManager.java     # Network communication manager
└── view/                       # UI components
    ├── NetworkStatusPanel.java # Network status display
    └── LeaderboardPanel.java   # Leaderboard display
```

## Troubleshooting

### Common Issues

1. **Connection Refused**: Server is not running or port is blocked
2. **Timeout**: Network latency or server overload
3. **Sync Issues**: Check network stability and server logs

### Debug Mode

Enable debug logging by modifying `logback.xml`:

```xml
<logger name="network" level="DEBUG"/>
<logger name="server" level="DEBUG"/>
<logger name="client" level="DEBUG"/>
```

### Port Configuration

If port 8888 is unavailable, modify the port in both server and client configuration files.

## Performance Considerations

- **Bandwidth**: Game state updates are sent frequently, consider network bandwidth
- **Latency**: High latency may affect real-time gameplay experience
- **Server Load**: Each connected client increases server resource usage
- **Memory**: Leaderboard data is kept in memory, consider cleanup policies

## Security Notes

- **No Authentication**: Current implementation has no user authentication
- **No Encryption**: Network traffic is not encrypted
- **Input Validation**: Server validates all incoming messages
- **Rate Limiting**: Consider implementing rate limiting for production use

## Future Enhancements

- **User Authentication**: Add user accounts and authentication
- **Encryption**: Implement TLS/SSL for secure communication
- **Database Storage**: Replace file-based storage with database
- **Matchmaking**: Add automatic matchmaking system
- **Spectator Mode**: Allow players to spectate ongoing games
- **Replay System**: Save and replay game sessions
