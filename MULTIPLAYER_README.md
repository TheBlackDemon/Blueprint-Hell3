# BlueprintHell Multiplayer & Data Integrity Features

## Overview

This document describes the implementation of Data Integrity Validation (DIV) and Multiplayer Operator vs Operator functionality in the BlueprintHell game.

## Data Integrity Validation (DIV)

### Features

1. **Server-side Data Storage**: All user progress, game results, and achievements are stored on the server
2. **MAC Address Identification**: Users are identified by their MAC address for persistent data storage
3. **Data Hash Validation**: All game results are validated using SHA-256 hashes to prevent cheating
4. **Anti-cheat Measures**: 
   - Level progression validation
   - Completion time validation
   - Packet loss validation
   - Coins earned validation

### Implementation

- **DataIntegrityValidator**: Main class handling data validation and storage
- **UserData**: Persistent user data structure
- **GameData**: Game configuration and constants storage
- **GameResultData**: Game result validation structure

### Data Storage

- User data stored in `user_data/` directory as JSON files
- Game data stored in `game_data/` directory
- Each user identified by MAC address
- Automatic data backup and recovery

## Multiplayer Operator vs Operator

### Features

1. **Two-Player Competition**: Two operators compete to deliver more packets successfully
2. **Packet Color System**: Packets are colored differently for each player (Blue vs Red)
3. **Network Setup Phase**: 30-second initial setup time with penalty system
4. **Wrath Effects**: Penalty system for overtime:
   - **Wrath of Penia**: Random packets added to opponent's systems
   - **Wrath of Aergia**: Increased cooldowns (1% per second)
   - **Wrath of Penia Speed**: Increased packet speed (3% per second)

### Game Flow

1. **Lobby Phase**: Players create or join games
2. **Network Setup Phase**: 30 seconds to create connections
3. **Extended Time**: Additional 30 seconds with penalties
4. **Game Phase**: Actual packet delivery competition
5. **Results Phase**: Winner determination and score display

### Implementation

- **MultiplayerGameState**: Manages multiplayer game sessions
- **MultiplayerGamePanel**: UI for multiplayer gameplay
- **MultiplayerLobbyPanel**: Lobby for creating/joining games
- **ServerMain**: Enhanced with multiplayer game management
- **ClientHandler**: Handles multiplayer-specific messages

## Network Architecture

### Message Types

- `CREATE_GAME`: Create new multiplayer game
- `JOIN_GAME`: Join existing game
- `PLAYER_READY`: Player ready status
- `WRATH_EFFECT`: Apply wrath effects
- `GAME_LIST`: List available games
- `PLAYER_CONNECTION`: Network setup data
- `PACKET_UPDATE`: Packet state updates

### Server Features

- Game session management
- Player matching
- Real-time synchronization
- Data integrity validation
- Anti-cheat enforcement

## UI Components

### MultiplayerLobbyPanel

- Game creation and joining
- Available games list
- Player status display
- Connection management

### MultiplayerGamePanel

- Network setup timer
- Player score display
- Wrath effects visualization
- Packet color differentiation
- Ready button functionality

## Penalty System (Wrath Effects)

### Wrath of Penia (First 10 seconds of extended time)
- Adds random packets to opponent's controllable systems
- Occurs every 2 seconds
- Makes opponent's task more difficult

### Wrath of Aergia (Second 10 seconds of extended time)
- Increases all cooldowns by 1% per second
- Affects ability usage and system operations
- Cumulative penalty effect

### Wrath of Penia Speed (Third 10 seconds of extended time)
- Increases packet speed by 3% per second
- Makes packet delivery more challenging
- Affects all packets in the network

## Data Validation Process

1. **Client Submission**: Game result submitted to server
2. **MAC Address Verification**: Client identified by MAC address
3. **Data Hash Generation**: Server generates expected hash
4. **Hash Comparison**: Validates submitted data integrity
5. **Bounds Checking**: Validates reasonable game parameters
6. **Progress Validation**: Ensures legitimate progression
7. **Data Storage**: Updates user progress if valid

## Security Features

- **Hash-based Validation**: SHA-256 hashing prevents data tampering
- **Server-side Storage**: All critical data stored on server
- **MAC Address Binding**: User identification tied to hardware
- **Bounds Validation**: Prevents impossible game results
- **Time Validation**: Prevents unrealistic completion times

## Usage

### Starting a Multiplayer Game

1. Launch the client
2. Connect to server
3. Access multiplayer lobby
4. Create new game or join existing
5. Wait for opponent
6. Begin network setup phase
7. Compete in packet delivery

### Data Integrity

- All game results automatically validated
- User progress persistently stored
- Anti-cheat measures active
- Leaderboard integrity maintained

## File Structure

```
src/main/java/
├── network/
│   ├── DataIntegrityValidator.java
│   ├── MultiplayerGameState.java
│   ├── GameStateData.java
│   └── NetworkMessage.java
├── server/
│   ├── ServerMain.java
│   └── ClientHandler.java
├── view/
│   ├── MultiplayerGamePanel.java
│   └── MultiplayerLobbyPanel.java
└── Game/
    ├── Packet.java (enhanced)
    └── PacketRenderer.java (enhanced)
```

## Configuration

### Server Configuration
- Port: 8888
- Max clients: 10
- Heartbeat interval: 5 seconds
- Client timeout: 30 seconds

### Game Configuration
- Network setup time: 30 seconds
- Extended time: 30 seconds
- Wrath effect durations: 10 seconds each
- Packet color differentiation: Blue vs Red

## Future Enhancements

- Spectator mode
- Tournament system
- Advanced anti-cheat measures
- Replay system
- Custom game modes
- Team-based multiplayer

## Troubleshooting

### Common Issues

1. **Connection Failed**: Check server status and network connectivity
2. **Data Validation Failed**: Ensure legitimate gameplay, check for cheating
3. **Game Not Found**: Refresh game list or create new game
4. **MAC Address Issues**: Check network interface configuration

### Debug Information

- Server logs in `logs/` directory
- Client connection status in UI
- Game state synchronization logs
- Data validation logs

## Conclusion

The implementation provides a robust multiplayer experience with strong data integrity validation, ensuring fair competition and preventing cheating while maintaining an engaging gameplay experience.
