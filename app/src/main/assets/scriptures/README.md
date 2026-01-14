# Hindu Scriptures Database

## 📚 Complete Scripture Collection

This folder should contain JSON files with complete Hindu scriptures.

### Required Files:

1. **bhagavad_gita.json** (700 verses, ~500KB)
2. **ramayana.json** (Key verses, ~200KB)
3. **puranas.json** (Selected wisdom, ~300KB)

---

## 📥 DOWNLOAD LINKS:

### Bhagavad Gita (Complete 700 Verses)

**Option 1: Gita Society (Public Domain)**
- Website: https://www.gita-society.com/
- Format: JSON/Text
- Languages: English, Hindi, Sanskrit
- License: Public Domain

**Option 2: Bhagavad Gita API**
- API: https://bhagavadgita.io/api/
- Documentation: https://rapidapi.com/bhagavad-gita-bhagavad-gita-default/api/bhagavad-gita3
- Format: JSON
- Free tier available

**Option 3: GitHub Repositories**
- Repo: https://github.com/gita/gita
- Repo: https://github.com/vedicscriptures/bhagavad-gita
- Format: JSON/XML

**Manual Download Instructions:**
```bash
# Using curl (if available online)
curl -O https://bhagavadgita.io/api/v1/chapters

# Or download manually and place here
```

---

### Ramayana (Valmiki Ramayana)

**Option 1: Valmiki Ramayana Net**
- Website: http://www.valmikiramayan.net/
- Format: HTML (convert to JSON)
- All 7 Kandas available

**Option 2: Internet Archive**
- Link: https://archive.org/details/SrimadValmikiRamayanaSanskritHindiEnglish
- Format: PDF/Text
- License: Public Domain

**Option 3: Sacred Texts**
- Website: https://www.sacred-texts.com/hin/rama/
- Format: HTML
- Translation: Ralph T.H. Griffith

---

### Puranas

**Option 1: Internet Archive (Gita Press)**
- Vishnu Purana: https://archive.org/details/vishnu-puran-gita-press
- Shiva Purana: https://archive.org/details/in.ernet.dli.2015.459093
- Bhagavata Purana: https://archive.org/details/in.ernet.dli.2015.280851

**Option 2: Wisdom Library**
- Website: https://www.wisdomlib.org/hinduism/book/vishnu-purana
- Format: HTML
- Multiple Puranas

**Option 3: Sacred Texts**
- Website: https://www.sacred-texts.com/hin/index.htm
- Multiple Puranas available

---

## 🔨 JSON FORMAT:

### bhagavad_gita.json
```json
{
  "metadata": {
    "name": "Bhagavad Gita",
    "total_chapters": 18,
    "total_verses": 700,
    "language": "Sanskrit"
  },
  "chapters": [
    {
      "chapter_number": 1,
      "name": "Arjuna Vishada Yoga",
      "verses": [
        {
          "verse_number": 1,
          "sanskrit": "धृतराष्ट्र उवाच...",
          "transliteration": "Dhritarashtra uvaacha...",
          "english": "Dhritarashtra said...",
          "hindi": "धृतराष्ट्र ने कहा...",
          "theme": "Introduction"
        }
      ]
    }
  ]
}
```

### ramayana.json
```json
{
  "metadata": {
    "name": "Valmiki Ramayana",
    "total_kandas": 7,
    "author": "Valmiki"
  },
  "kandas": [
    {
      "kanda_number": 1,
      "name": "Bala Kanda",
      "verses": [
        {
          "sarga": 1,
          "verse_number": 1,
          "sanskrit": "तपः स्वाध्याय निरतं...",
          "transliteration": "Tapah swadhyaya niratam...",
          "english": "Devoted to austerity and study...",
          "theme": "Valmiki's tapas"
        }
      ]
    }
  ]
}
```

### puranas.json
```json
{
  "metadata": {
    "total_puranas": 18,
    "category": "Major Puranas"
  },
  "puranas": [
    {
      "name": "Vishnu Purana",
      "verses": [
        {
          "chapter": "1.1",
          "sanskrit": "श्रीपराशर उवाच...",
          "transliteration": "Shri Parashara uvaacha...",
          "english": "Parashara said...",
          "theme": "Creation"
        }
      ]
    }
  ]
}
```

---

## 📝 HOW TO USE:

1. **Download JSON files** from above links
2. **Place in this folder**: `app/src/main/assets/scriptures/`
3. **App will auto-load** on startup
4. **Fallback**: If files not found, app uses built-in sample verses

---

## ⚠️ COPYRIGHT & LICENSE:

- **Bhagavad Gita**: Public Domain (ancient text)
- **Ramayana**: Public Domain (ancient text)
- **Puranas**: Public Domain (ancient text)
- **Translations**: Check specific translator's license
- **Gita Society**: Explicitly allows free distribution
- **Archive.org**: Public Domain collections

**NOTE**: Original Sanskrit texts are public domain.
Modern translations may have copyright.
Use translations explicitly marked as public domain
or Creative Commons.

---

## 🔄 UPDATE PROCESS:

1. Download latest JSON from sources
2. Validate JSON format
3. Replace old files
4. Restart app
5. Quotes updated automatically

---

## 📊 FILE SIZES:

- bhagavad_gita.json: ~500KB (700 verses)
- ramayana.json: ~200KB (selected verses)
- puranas.json: ~300KB (wisdom verses)
- **Total**: ~1MB

---

## 🆘 SUPPORT:

If files are missing or corrupted:
1. App will show error message
2. Falls back to built-in sample verses
3. Check logcat for details:
   ```
   adb logcat | grep ScriptureLoader
   ```

---

## 📱 QUICK START:

Don't want to download manually?

**Use built-in samples** (15-20 verses)
- Already included in app
- No download needed
- Basic functionality works

**Download complete texts later** for full experience
- 850+ verses
- All themes covered
- Multi-language support