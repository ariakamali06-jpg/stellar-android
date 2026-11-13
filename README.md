# ✦ Stellar — Manga Typesetting Editor for Android

<p align="center">
  <img src="app/src/main/res/drawable/ic_stellar_logo.xml" width="96" height="96" alt="Stellar Logo" />
</p>

<p align="center">
  <b>A modern, lightweight, and native Android application for typesetting manga and comics on mobile devices.</b><br/>
  نرم‌افزار مدرن، سبک و نیتیو اندروید برای تایپ، بالون‌گذاری و حروف‌چینی مانگا و کمیک روی گوشی.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen.svg" alt="Platform" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Design-Material%203-blue.svg" alt="Material 3" />
</p>

---

## 🌐 Language / زبان
- [English](#-english)
- [فارسی (Persian)](#-فارسی)

---

# 🇬🇧 English

## ✨ Features

- **📱 Touch-Optimized Canvas:**
  - **Pinch-to-Zoom & Pan:** Smooth two-finger zoom (50% to 400%) and navigation across high-resolution manga pages.
  - **Quick Reset (Fit Screen):** One-tap floating button to return to 100% view with current zoom level indicator.
  - **Freeform Move & Resize:** Drag anywhere inside a bubble to move, or grab the bottom-right corner handle to resize width and height.

- **💬 Speech Bubbles & Balloons:**
  - Multiple balloon styles: Ellipse (بیضی), Rectangle (مستطیل), Rounded Rect (گوشه‌گرد), Cloud (ابر), Explosion (انفجار).
  - Customizable stroke width, outline colors, and background presets (White, Black, Transparent/No-bubble).
  - Bubble shadow effects with offset support.

- **✏️ Rich Typography Controls:**
  - Full multiline text editing with real-time preview.
  - Font size slider (12sp to 60sp).
  - Bold (`B`) and Italic (`I`) styling.
  - Text alignment: Left, Center, Right (RTL/LTR compatible).
  - Text color presets (Black, White, Red, Indigo).
  - **3D Text Extrusion:** Adds comic-style depth to sound effects and dramatic dialogue.
  - **Rotation:** Smooth angle adjustment from -180° to +180° with quick-reset.

- **📄 Multi-Page Project Management:**
  - Add, navigate, and manage multiple manga pages in a single project.
  - Select high-res manga raw scans directly from device gallery.
  - **Local Persistence:** Images are copied safely to the app's internal sandbox to prevent URI expiration.
  - **Auto-Save & Load:** Project data is automatically serialized to JSON and reloaded upon opening.

- **↩️ Full Undo / Redo:**
  - Dedicated history stack (up to 50 operations) for error-free editing.

- **📤 High-Quality Export:**
  - Direct export of all edited pages to high-resolution PNG files (`01.png`, `02.png`, ...) preserved in app storage.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose + Material 3 (100% declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
- **State Management:** Kotlin Coroutines & `StateFlow`
- **Graphics Engine:** Compose `Canvas` + Android native `android.graphics.Canvas` for pixel-perfect bitmap rendering
- **Image Handling:** Coil Compose

---

## 🚀 How to Build & Run

### Prerequisites
1. **JDK:** Java 17 or higher
2. **Android SDK:** API Level 34 (Compile) / API Level 26+ (Minimum Android 8.0)
3. **Android Studio:** Ladybug / Koala or newer

### Building from Command Line

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ariakamali06-jpg/stellar-android.git
   cd stellar-android
   ```

2. **Set up SDK path:**
   Create a `local.properties` file in the root directory:
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```

3. **Build Debug APK:**
   - On Windows:
     ```bash
     .\gradlew.bat assembleDebug
     ```
   - On Linux / macOS:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Locate the APK:**
   The output APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

# 🇮🇷 فارسی

## معرفی پروژه

**استلار (Stellar)** یک نرم‌افزار نیتیو، سبک و تخصصی برای سیستم‌عامل اندروید است که برای حروف‌چینی، بالون‌گذاری و تایپ مانگا، مانهوا و کمیک طراحی شده است. این نسخه با هدف انتقال تجربه کاربری روان نسخه دسکتاپ به محیط لمسی گوشی‌های هوشمند پیاده‌سازی شده است.

---

## 🌟 قابلیت‌های کلیدی

- **🔍 کنواس لمسی و پیشرفته:**
  - **بزرگ‌نمایی دو انگشتی (Pinch-to-Zoom):** امکان زوم روان بین ۵۰٪ تا ۴۰۰٪ برای تنظیم دقیق جزئیات متن روی صفحات باکیفیت.
  - **دکمه بازگشت به حالت اولیه (Fit Screen):** دکمه شناور نمایش درصد زوم به همراه ریست با یک لمس.
  - **تغییر سایز و جابه‌جایی آزاد:** لمس بدنه کادر برای جابه‌جایی و کشیدن هندل دایره‌ای گوشه برای تغییر آسان ابعاد کادر.

- **💬 حباب‌ها و کادرهای متنوع:**
  - پشتیبانی از ۵ شکل حباب: **بیضی، مستطیل، گوشه‌گرد، ابر و انفجار**.
  - تنظیم ضخامت خط دور، پالت رنگ‌های پرکاربرد (سفید، سیاه، شفاف/بدون کادر).
  - امکان فعال‌سازی سایه برای حباب‌ها.

- **✏️ ابزارهای جامع ویرایش متن:**
  - ویرایشگر چندخطی متن با پیش‌نمایش زنده.
  - اسلایدر تنظیم سایز قلم (Font Size) از ۱۲ تا ۶۰.
  - حالت‌های ضخیم (**Bold**) و مورب (*Italic*).
  - چیدمان متن: راست‌چین، وسط‌چین و چپ‌چین (پشتیبانی کامل از زبان فارسی).
  - انتخاب رنگ متن (سیاه، سفید، قرمز، بنفش).
  - **افکت سه‌بعدی (3D Extrusion):** مناسب برای متن‌های جلوه صوتی (SFX) و دیالوگ‌های پرهیجان.
  - **چرخش آزاد:** تنظیم زاویه از -۱۸۰ تا +۱۸۰ درجه همراه با دکمه ریست به صفر.

- **📄 مدیریت پروژه‌های چند صفحه‌ای:**
  - افزودن صفحات جدید و ورق زدن بین آن‌ها.
  - انتخاب تصویر خام مانگا از گالری.
  - **ذخیره امن تصاویر:** کپی خودکار تصاویر به حافظه داخلی اپ جهت جلوگیری از ابطال مجوز دسترسی گالری.
  - **ذخیره و بازیابی خودکار:** ثبت آنی تغییرات پروژه در قالب JSON و بازیابی خودکار در زمان اجرای بعدی اپلیکیشن.

- **↩️ قابلیت Undo / Redo:**
  - پشته تاریخچه تا ۵۰ عملیات برای بازگردانی یا اعمال مجدد بدون نگرانی از خطای کاربری.

- **📤 خروجی باکیفیت PNG:**
  - استخراج تمامی صفحات پروژه در قالب فایل‌های مجزای PNG با نام‌گذاری مرتب (`01.png`, `02.png`, ...) در حافظه گوشی.

---

## 🛠️ ساختار فنی و معماری

- **زبان توسعه:** Kotlin 2.0.21
- **رابط کاربری:** Jetpack Compose + Material 3
- **الگوی معماری:** MVVM با جریان داده یک‌طرفه (Unidirectional Data Flow)
- **مدیریت وضعیت:** Kotlin Coroutines و `StateFlow`
- **موتور گرافیکی:** رندر دوگانه در Compose Canvas (پیش‌نمایش تعاملی) و Android Native Canvas (خروجی نهایی با بالاترین رزولوشن)
- **مدیریت تصاویر:** Coil Compose

---

## 📦 نحوه خروجی گرفتن و نصب APK

### ۱. از طریق محیط Android Studio
1. پروژه را در **Android Studio** باز کنید.
2. از منوی بالا به مسیر **Build > Build Bundle(s) / APK(s) > Build APK(s)** بروید.
3. پس از اتمام ساخت، با کلیک روی گزینه **locate** فایل `app-debug.apk` را برداشته و روی گوشی نصب کنید.

### ۲. از طریق خط فرمان (ترمینال)

در پوشه اصلی پروژه دستور زیر را وارد کنید:

- **ویندوز:**
  ```powershell
  .\gradlew.bat assembleDebug
  ```

- **لینوکس / مک:**
  ```bash
  ./gradlew assembleDebug
  ```

فایل خروجی APK در مسیر زیر ساخته می‌شود:
```
app/build/outputs/apk/debug/app-debug.apk
```

کافیست فایل APK را به گوشی منتقل و نصب کنید.

---

## 📄 لایسنس
این پروژه به صورت متن‌باز منتشر شده است.
