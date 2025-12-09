# 🛫 Airport Resource Allocation Simulator

## Project Overview

This application is a desktop simulator designed to model the complex operations and resource allocation within an international airport, specifically focusing on the synchronization of concurrent processes.

It serves as a practical demonstration of concurrency concepts (like **Semaphores**, **Locks**, and **Monitors**) by managing limited resources such as Runways and Gates for incoming and outgoing aircraft.

### Key Focus

* **Concurrency:** Visually demonstrate how synchronization primitives prevent deadlocks and ensure orderly resource access in a multi-threaded environment.
* **Resource Management:** Track the status and usage of critical airport infrastructure (Runways and Gates).
* **Modern UI:** Features a clean, modern interface inspired by macOS/Apple design principles for a clear and intuitive user experience.

---

## ✨ Features

| Component | Function | Status Indication |
| :--- | :--- | :--- |
| **Control Tower** | Allows users to manually inject new aircraft processes (`Arrival Plane` or `Departure Plane`) into the system queue. | N/A |
| **Queue** | Displays all pending aircraft processes currently waiting for a resource (Runway or Gate). | Shows plane ID and current waiting state (e.g., "Waiting for Runway"). |
| **Runways** | Manages the primary resource for landing and takeoff. | Clearly indicates **Free** (Green) or **Occupied** (e.g., Orange) status. |
| **Gates** | Manages the gates required for aircraft to drop off/pick up passengers. | Clearly indicates **Free** (Green) or **Occupied** (e.g., Orange) status. |
| **Event Logs** | Provides a detailed, chronological record of all system events, including resource allocation, process completion, and synchronization state changes. | Log entries for key events. |

### Synchronization Algorithms

The simulator allows dynamic switching between three core concurrency control methods to observe their behavior:

1.  **Semaphore**
2.  **Lock**
3.  **Monitor**

---

## 💻 Technical Details

| Detail | Value |
| :--- | :--- |
| **Programming Language** | `[INSERT YOUR PROGRAMMING LANGUAGE, e.g., C#, Java, Python]` |
| **Framework/Toolkit** | `[INSERT YOUR UI FRAMEWORK, e.g., WPF, Swing, Tkinter, PySide]` |
| **New UI Style** | Modern, minimalist design with clear visual hierarchy, increased spacing, and subtle color accents (inspired by Apple/macOS). |

---

## 🖼️ User Interface Preview

### Original Interface
!

[Image of the Original Application Interface]
(image_820e95.png)

### Modern Redesign Preview
![Conceptual Image of the Modern Redesign](modern_redesign_preview.png)
*(**Note:** Place the image of your new UI design in the project folder and rename it `modern_redesign_preview.png`)*

---

## 🚀 Getting Started

### Prerequisites

You will need the following software installed on your machine to run the project:

* `[REQUIRED SOFTWARE 1, e.g., Java Development Kit (JDK) 17 or newer]`
* `[REQUIRED SOFTWARE 2, e.g., Python 3.x]`

### Installation and Run

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/](https://github.com/)[Your-Username]/Airport-Management-System.git
    cd Airport-Management-System
    ```

2.  **Build the Project (If Applicable)**
    ```bash
    [INSERT BUILD COMMAND HERE, e.g., javac *.java or dotnet build]
    ```

3.  **Run the Application**
    ```bash
    [INSERT RUN COMMAND HERE, e.g., java MainApp or python main.py]
    ```

---

## 🤝 Contribution

This project is currently for personal learning and demonstration purposes, specifically to practice synchronization and modern UI implementation. Feedback and suggestions for improving the logic or design are always welcome!
