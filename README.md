# REDChat — Decentralized Offline BLE Mesh & Onion Messaging

**REDChat** is an enterprise-grade, peer-to-peer, encrypted decentralized communication platform designed for Android. It operates over Bluetooth Low Energy (BLE) multi-hop mesh networks with optional Tor Onion routing fallbacks, ensuring robust, surveillance-resistant messaging without central servers.

Developed by **Llama AI SAS** in collaboration with **Walluu SAS** and **Pulzo Holding SAS**.

---

## Key Features

- **Off-Grid BLE Mesh Network**: Multi-hop routing using flood/managed flooding mesh topology across local Android devices.
- **End-to-End Encryption (E2EE)**: Hybrid cryptography using X25519 key exchange, Ed25519 signing, and AES-256-GCM symmetric encryption. Password hashing via Argon2id.
- **Tor Network Fallback**: Onion routing integration for internet-reachable peers when BLE range is exceeded.
- **Volatile In-Memory Storage**: Zero-trace local RAM chat model with instant Emergency Wipe capabilities.
- **Cover Traffic & Traffic Analysis Countermeasures**: Adjustable background decoy packet generation to obscure metadata and volume analysis.
- **Material 3 Design**: Professional Polish theme with responsive layout, dynamic dark styling, and slash command integration (`/j`, `/m`, `/w`, `/clear`, `/pass`, `/block`).

---

## Architectural Overview

```
+-------------------------------------------------------------------+
|                        Presentation Layer                         |
|  Compose UI (Chats, Channels, Conversation, Settings, Diagnostics) |
+-----------------------------------+-------------------------------+
                                    |
+-----------------------------------v-------------------------------+
|                         Domain & ViewModel                        |
|  MainViewModel / SlashCommandParser / Clean Architecture Models   |
+-----------------------------------+-------------------------------+
                                    |
+-----------------------------------v-------------------------------+
|                         Repository Layer                          |
|    ChatRepository (StateFlow Management & Volatile Memory Cache)  |
+---------+-------------------------+---------------------+---------+
          |                         |                     |
+---------v--------+      +---------v--------+  +---------v--------+
|  Crypto Engine   |      |  BLE Mesh Engine |  |    Tor Manager   |
| (X25519/AES-GCM) |      | (Multi-hop Mesh) |  | (Onion Routing)  |
+------------------+      +------------------+  +------------------+
```

---

## Build & Installation

### Requirements
- Android SDK 34+
- JDK 17+
- Kotlin 1.9+

### Build APK
```bash
./gradlew :app:assembleRelease
```

### Run Unit & Robolectric Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## License & Copyright

Copyright © 2026 **Walluu SAS**.  
Developed by **Llama AI SAS** in collaboration with **Pulzo Holding SAS**. All Rights Reserved.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
