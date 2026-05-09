# XpenseAtlas — Forest 8-Bit Edition Handbook

> **Project**: XpenseAtlas — Privacy-First Financial Intelligence Vault  
> **Edition**: Forest 8-Bit Overhaul (v1.1)  
> **Date**: 2026-05-10  
> **Author**: Antigravity AI  
> **Status**: **STABLE & COMPLETE**

---

## 1. Design Philosophy: "The Digital Forest"

XpenseAtlas has been redesigned with a **Forest 8-bit** aesthetic, moving away from generic modern UI toward a unique, premium "RPG-style" experience.

### 🎨 Visual Language
- **8-Bit Art Style**: Sharp 4dp corners, pixel-perfect borders (2.0dp), and monospace typography.
- **Forest Palette**: 
    - `ForestBlack` (#0A1A0A) for deep backgrounds.
    - `GlowGreen` (#7FFF5F) for highlights and income.
    - `PixelRed` (#D93025) for debit alerts.
- **Zero Clipping**: All UI components are built with dynamic heights and overflow protection to ensure text is never cut off on any device.

---

## 2. Core Feature Overhaul (v1.1)

### 📊 Monthly Spending Engine (The "RPG Card")
The dashboard now centers around a **Monthly Spend Card** that tracks your journey month-over-month.
- **◀ ▶ Month Navigation**: Quickly cycle through past months to compare spending habits.
- **Month Picker**: Tap the month name to open a pixel-art grid to jump to any month in any year.
- **Debit vs Credit**: Tracking is now split into "↓ SPENT" and "↑ RECEIVED" chips, ensuring income and expenses are never muddled.

### 📱 Historical SMS Scanner
The most powerful tool for new users. Instead of starting from zero, XpenseAtlas can retroactively build your financial history.
- **Scan Past SMS**: One-tap deep scan of your entire Android SMS inbox.
- **Intelligence**: Uses the `SmsParser` engine to identify old bank/UPI messages from months before the app was installed.
- **On-Device Only**: This scan happens purely in RAM and local SQLite; no data ever leaves your device.

---

## 3. Technical Architecture

| Layer | Component | Purpose |
|:------|:----------|:--------|
| **UI** | DashboardScreen.kt | The main "Forest" interface with 8-bit styling and month navigation. |
| **Logic** | SmsScanner.kt | New background service for bulk-parsing historical SMS messages. |
| **Data** | TransactionDao.kt | Updated with monthly filtering (`getTransactionsForMonth`) and bulk-insert capability. |
| **Theme** | Color.kt | Defines the Moss, Bark, and GlowGreen tokens. |

---

## 4. Operational Checklist

### Syncing to GitHub
To push your changes, use the dedicated folder:
`C:\Users\divya\.gemini\antigravity\playground\primordial-bohr\XpenseAtlas_Complete`

### Manual Verification
1. **The Forest Look**: Launch app → background should be a dark forest gradient.
2. **History Scan**: Tap "Scan Past SMS" → Watch the progress bar as it finds your old bank transactions.
3. **Time Travel**: Use ◀ ▶ on the top card to verify spending totals update correctly for previous months.
4. **Vault Entry**: Biometric gating remains mandatory for entry.

---

## 5. Deployment Info
- **Source**: `Source/` folder in the root.
- **APK**: `APK/XpenseAtlas_v1.1_Forest.apk`.

---
© 2026 XpenseAtlas Ultimate Edition. **Local Data. Forest Aesthetics. Absolute Privacy.**
