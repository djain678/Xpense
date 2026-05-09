# XpenseAtlas Ultimate Edition — Handover Log

> **Project**: XpenseAtlas — Privacy-First Financial Intelligence Vault  
> **Date**: 2026-05-10  
> **Author**: Antigravity AI  
> **Status**: Code Complete — **Ultimate Edition**  
> **Location**: `c:\Users\divya\.gemini\antigravity\playground\primordial-bohr\XpenseAtlas\`

---

## 1. What Is XpenseAtlas?

XpenseAtlas is a **premium, offline-first financial vault** designed for the privacy-conscious user in India. It automatically transforms bank SMS alerts into a rich, searchable, and secure financial dashboard without ever connecting to the internet.

### Core Philosophy: "Zero-Cloud Fortress"
- 🛡️ **No Internet**: The app is 100% offline. No cloud, no servers, no leaks.
- 🔒 **Biometric Security**: Gated behind Fingerprint/Face ID.
- 📍 **GPS Intelligence**: Automatically tags every transaction with its physical location.
- 🧠 **On-Device AI**: Local OCR for receipts, STT for voice logging, and a learning engine for merchant categories.

---

## 2. Feature Architecture

### 2.1 The Financial Intelligence Core
- **SMS Parser**: Advanced regex engine that detects amounts and currencies from Indian Banks/UPI.
- **Learning Engine**: Remembers when you manually change a merchant's category and applies it to future transactions.
- **Subscription Detective**: Identifies recurring payment patterns to flag hidden subscriptions.

### 2.2 Privacy & Utility
- **🕵️ Shadow Mode**: One-tap toggle to mask all currency amounts in public places.
- **💱 Multi-Currency**: Dynamic detection of $, €, £, and AED.
- **🌍 Travel Mode**: Offline country detection via GPS bounding boxes (Works without Roaming/Internet).
- **📄 Local Export**: Standardized CSV generator for data portability.

### 2.3 Connectivity & Interaction
- **🤝 Partner Sync (P2P)**: Offline sync with a partner's device over local Wi-Fi using Google Nearby Connections.
- **🗣️ Voice Atlas**: On-device Speech-to-Text for logging cash expenses hands-free.
- **📸 Receipt OCR**: ML Kit-powered scanner to digitize paper bills.
- **↩️ U-Turn UPI**: Floating shortcut to launch your payment app of choice instantly.

---

## 3. Technical Stack

| Layer | Technology |
|:------|:-----------|
| **Language** | Kotlin 2.1.0 |
| **UI** | Jetpack Compose (Material 3) |
| **Database** | Room (Offline SQLite) |
| **Security** | Android Biometric Library |
| **Background** | WorkManager (Automated Reports) |
| **Connectivity** | Google Nearby Connections API |
| **Intelligence** | ML Kit (Text Recognition & Entity Extraction) |

---

## 4. File Map & Project Structure

```
XpenseAtlas/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml      # NFC, Biometric, and SMS permissions
│   │   ├── java/com/xpenseatlas/
│   │   │   ├── MainActivity.kt      # Biometric gating + Profile state
│   │   │   ├── data/
│   │   │   │   ├── AppDatabase.kt   # Room DB (v3)
│   │   │   │   └── Transaction.kt   # Core data models
│   │   │   ├── logic/
│   │   │   │   ├── SmsParser.kt     # Multi-currency regex engine
│   │   │   │   ├── TravelManager.kt # Offline GPS bounding boxes
│   │   │   │   ├── ReceiptScanner.kt# ML Kit OCR logic
│   │   │   │   └── PartnerSync.kt   # P2P Nearby Connections
│   │   │   └── ui/screens/
│   │   │       ├── DashboardScreen.kt # The "Fortress" UI
│   │   │       ├── CalendarScreen.kt  # Spend-density calendar
│   │   │       └── SplitDialog.kt     # Bill splitting UI
```

---

## 5. Deployment & Testing

### Installation
The latest build is located at:
`c:\Users\divya\.gemini\antigravity\playground\primordial-bohr\XpenseAtlas\app\build\outputs\apk\debug\app-debug.apk`

### Manual Test Checklist
1. **Biometric**: Launch app → System prompt for Fingerprint/Face ID.
2. **Shadow Mode**: Tap the "Eye" icon → All currency values should show `****`.
3. **Voice**: Tap Mic → Say "Fifty on Coffee" → Transaction should appear.
4. **Partner Sync**: Tap Sync on two devices on same Wi-Fi → P2P transfer of records.
5. **Travel**: Mock location to Dubai (25.2, 55.2) → App should identify "UAE" and "AED" currency.

---

## 6. AI Resumption Prompt

If you are continuing development with an AI assistant, use this prompt:

> *"I am working on XpenseAtlas, a privacy-first offline finance vault. Read the `xpenseatlas_handover_log.md` to understand the architecture, including Nearby Connections for P2P sync and ML Kit for OCR. Current goal: [Insert Goal]"*

---
© 2026 XpenseAtlas Ultimate Edition. Local Data. Absolute Privacy.
