# NovaPOS ✦ — Android SoftPOS Terminal, Offline SAF Queue & ESC/POS Register

A widescreen **Android Point-of-Sale (POS) & SoftPOS Terminal** built with **Kotlin**, **Jetpack Compose (`Canvas`)**, **Clean Architecture (MVI)**, **Room SQLite Store-and-Forward (SAF)**, **80mm ESC/POS Binary Encoding**, and an **AI Cashier Co-Pilot**.

<p align="center">
  <!-- Paste your GitHub issue-uploaded GIF or screenshot URL here -->
  <img src="https://github.com/user-attachments/assets/YOUR-NOVAPOS-GIF-ID-HERE" alt="NovaPOS Register Demo" width="760"/>
</p>

---

## ✨ Key Engineering Highlights

1. **Offline-First Store-and-Forward (SAF) Queue with UUID Idempotency**
   - Point-of-sale registers must keep accepting contactless payments during Wi-Fi outages without risking double-charges on retry.
   - Toggling the terminal to **Offline Mode** routes signed SoftPOS EMV transactions into a local Room `saf_transactions` queue (`isSynced = false`) keyed by deterministic **UUID idempotency keys** and **EMV `ARQC` cryptograms**.
   - Reconnecting automatically flushes pending transactions (`SAF QUEUE (N) -> SYNCED ↗`) back to the acquiring host.

2. **SoftPOS Contactless EMV Reader & ISO 14443 A/B APDU Trace**
   - Features a dedicated right-pane contactless tap target with animated concentric radar rings, 4 standard EMV contactless indicator LEDs, and live ISO-DEP APDU frame telemetry (`SELECT PPSE 2PAY.SYS.DDF01` $\rightarrow$ `GET PROCESSING OPTIONS` $\rightarrow$ `9000 OK`).
   - Supports multi-person **Split-Tender** progression (`PERSON 1 OF 2` $\rightarrow$ `PERSON 2 OF 2`), decrementing catalog stock atomically only after the final split clears.

3. **80mm ESC/POS Thermal Printer Binary Encoder & Serrated Receipt Preview**
   - `EscPosEncoder` builds spec-compliant Epson `ESC/POS` command byte arrays for 42-column 80mm thermal paper:
     - `0x1B 0x40` (`ESC @`) — Hardware initialization
     - `0x1B 0x61` (`ESC a`) — Header/body alignment
     - `0x1B 0x45` (`ESC E`) — Bold typography for totals and split tenders
     - `0x1D 0x56 0x42 0x00` (`GS V B 0`) — Feed & guillotine partial blade cut
   - Renders a live paper preview with a custom Compose `Canvas` serrated cut edge and real-time hex spool telemetry.

4. **AI Cashier Natural-Language Command Engine (`AiCashierAgent`)**
   - Converts rapid spoken/typed orders into structured tickets, kitchen modifiers, tip percentages, and split-bill calculations in one step:
     - *"Add 2 Oat Flat Whites, 1 Wagyu Burger no pickles, split 2 ways"*
     - *"3 Iced Matcha, 2 Truffle Fries extra crispy, tip 20%, split 3 ways"*

---

## 🏗️ POS & Payment Architecture Overview

```mermaid
flowchart LR
    UI["NovaPosScreen\n(3-Pane Tablet Register + Canvas)"] -->|NovaPosIntent| VM["NovaPosViewModel\n(MVI StateFlow + Split Engine)"]
    VM -->|Natural Language Order| AI["AiCashierAgent\n(Item, Modifier & Split Parser)"]
    VM -->|Encode 80mm Receipt| Printer["EscPosEncoder\n(Binary ESC/POS + GS V Cut)"]
    VM <-->|Idempotent SAF Queue| Room["NovaPosDao\n(Room SQLite SAF Ledger)"]
    Room -->|observePendingSafCount()| VM
    VM -->|Immutable NovaPosUiState| UI
```

---

## 🚀 Getting Started

1. **Run on a Landscape Tablet AVD:**
   Open the project in Android Studio and run on a **Pixel Tablet** or **1280x800 Landscape** emulator.
2. **Test the Core POS Workflows:**
   - Tap **`ONLINE / SAF QUEUE (0)`** in the top header to simulate a store Wi-Fi outage, then tap the emerald **SoftPOS Reader** (`$16.82`) twice to complete both halves of the split bill offline and watch the `SAF QUEUE (2)` counter increment.
   - Tap **`SYNC NOW ↻`** (or restore Online mode) to flush the queued transactions.
   - Enter **`3 Iced Matcha, 2 Truffle Fries extra crispy, tip 20%, split 3 ways`** in the top AI Cashier bar and click **`↵`**.
   - Click **`🖨 Print ESC/POS & Cut`** to generate and inspect the raw 80mm ESC/POS binary byte payload.
3. **Run the Unit Test Suite:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
