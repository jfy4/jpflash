# Japanese Flashcard App 🇯🇵

An Android app for practicing Japanese vocabulary and verbs — including **dictionary forms**, **masu forms**, and **English meanings**.  
Built with **Kotlin** and **Jetpack Compose**, this project aims to make it easy to learn and review common verbs interactively.

---

## 📱 Features

- 🔤 Displays verbs with their:
  - Dictionary form (行く)
  - Hiragana (いく)
  - English meaning (“to go”)
  - Polite conjugations:
    - Present (行きます)
    - Past (行きました)
    - Negative (行きません)
- 🗂️ Reads data from a local CSV file (e.g. `verbs.csv`)
- 💾 Simple local storage for tracking known words (coming soon)
- 🎨 Built using modern Android development practices with Kotlin and Jetpack Compose
- 🌙 Dark mode support (optional)

---

## 📂 Project Structure

```

app/
├── src/
│   ├── main/
│   │   ├── java/com/example/flashcards/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   └── data/
│   │   │       └── verbs.csv
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
├── build.gradle
└── settings.gradle

````

---

## 🧠 Example Data (verbs.csv)

```csv
dictionary,hiragana,meaningEn,masu,masuHiragana,mashita,mashitaHiragana,masen,masenHiragana
行く,いく,to go,行きます,いきます,行きました,いきました,行きません,いきません
来る,くる,to come,来ます,きます,来ました,きました,来ません,きません
する,する,to do,します,します,しました,しました,しません,しません
食べる,たべる,to eat,食べます,たべます,食べました,たべました,食べません,たべません
読む,よむ,to read,読みます,よみます,読みました,よみました,読みません,よみません
````

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Framework:** Jetpack Compose
* **Build Tool:** Gradle
* **IDE:** Android Studio
* **Version Control:** Git + GitHub

---

## 🚀 Getting Started

### Prerequisites

* Android Studio (latest version)
* JDK 17+
* A device or emulator running Android 8.0 (API 26) or higher

### Installation

1. Clone the repo:

   ```bash
   git clone https://github.com/jfy4/jpflash.git
   ```
2. Open in Android Studio
3. Build and run on your device

---

## 📜 License

This project is open-source under the [MIT License](LICENSE).

---

## 👤 Author

**Judah Unmuth-Yockey**
Physicist, developer, and language learner.
Building tools that make learning more intuitive and fun.

---

*もし日本語を勉強しているなら、がんばってください！* 💪🇯🇵
