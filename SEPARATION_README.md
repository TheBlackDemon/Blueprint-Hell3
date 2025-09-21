# BlueprintHell - Separated Client/Server Architecture

This document explains the new separated architecture of the BlueprintHell multiplayer game.

## Project Structure

The project has been reorganized into separate client and server components:

```
src/
├── main/java/           # Original offline game (shared components)
├── client/java/         # Client-specific components
│   ├── client/          # Client main classes
│   └── view/            # UI components (client-only)
├── server/java/         # Server-specific components
│   └── server/          # Server main classes
└── shared/java/         # Shared components
    ├── Game/            # Game logic (shared)
    ├── network/         # Network protocol (shared)
    └── controller/      # Controllers (shared)
```

## Build Configuration

The project now uses Gradle source sets to build separate JARs:

- **client.jar**: Client application with UI
- **server.jar**: Server application
- **BlueprintHell-5-1.0-SNAPSHOT.jar**: Original offline game

## Quick Start

### 1. Build All Components

```bash
gradlew buildAll
```

This will create all three JAR files in `build/libs/`.

### 2. Start the Server

```bash
# Windows
launch_server_separated.bat

# Or directly
java -jar build/libs/server.jar
```

The server will start on port 8888.

### 3. Start the Client

```bash
# Windows
launch_client_separated.bat

# Or directly
java -jar build/libs/client.jar
```

The client will attempt to connect to localhost:8888.

### 4. Play Offline

```bash
# Windows
launch_offline_separated.bat

# Or directly
java -jar build/libs/BlueprintHell-5-1.0-SNAPSHOT.jar
```

## Component Separation

### Client Components (`src/client/java/`)

- **ClientMain.java**: Main client application entry point
- **NetworkManager.java**: Handles client-side network communication
- **view/**: All UI components (Window, panels, etc.)

### Server Components (`src/server/java/`)

- **ServerMain.java**: Main server application entry point
- **ClientHandler.java**: Handles individual client connections

### Shared Components (`src/shared/java/`)

- **Game/**: Core game logic, packet mechanics, and game state
- **network/**: Network protocol definitions and data structures
- **controller/**: User management and audio controllers

### Original Components (`src/main/java/`)

- **Main.java**: Original offline game entry point
- **Game/**: Enhanced packet mechanics (moved to shared)
- **view/**: UI components (moved to client)
- **server/**: Server components (moved to server)
- **client/**: Client components (moved to client)

## Benefits of Separation

1. **Clear Architecture**: Client and server are now clearly separated
2. **Independent Deployment**: Each component can be deployed separately
3. **Reduced Dependencies**: Client doesn't include server code
4. **Better Maintainability**: Easier to maintain and update individual components
5. **Scalability**: Server can be deployed on different machines

## Development Workflow

### Adding New Features

1. **Game Logic**: Add to `src/shared/java/Game/`
2. **Client UI**: Add to `src/client/java/view/`
3. **Server Logic**: Add to `src/server/java/server/`
4. **Network Protocol**: Add to `src/shared/java/network/`

### Building Specific Components

```bash
# Build only client
gradlew clientJar

# Build only server
gradlew serverJar

# Build only offline game
gradlew offlineJar

# Build all components
gradlew buildAll
```

## Network Architecture

The separated architecture maintains the same network protocol:

- **Client**: Connects to server, sends user input, receives game updates
- **Server**: Manages multiple clients, handles game state synchronization
- **Shared Protocol**: Uses the same NetworkMessage format for communication

## Enhanced Features

The separated architecture includes all the enhanced packet mechanics:

- **Trojan Packet Conversion**: Type 1 ↔ Type 2 packet conversion
- **Confidential Packet Restrictions**: Only through uncontrollable systems
- **Feedback Loop**: Successful packets generate counter-packets
- **Automated Wave System**: Hierarchical packet spawning
- **Reflection-Based Discovery**: Automatic packet type management

## Troubleshooting

### Common Issues

1. **Build Failures**: Ensure all dependencies are available
2. **Connection Issues**: Check server is running before starting client
3. **Package Errors**: Verify package declarations match directory structure

### Debug Mode

Enable debug logging by modifying `logback.xml`:

```xml
<logger name="network" level="DEBUG"/>
<logger name="server" level="DEBUG"/>
<logger name="client" level="DEBUG"/>
```

## Migration from Original

The original game files remain in `src/main/java/` for backward compatibility. The separated components use the same core logic but with improved architecture.

## Future Enhancements

- **Docker Support**: Containerize client and server separately
- **Load Balancing**: Multiple server instances
- **Microservices**: Further break down server components
- **Web Interface**: Web-based client option
