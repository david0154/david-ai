# GitHub Wiki Setup Guide

## 📚 How to Enable and Setup Wiki

This guide explains how to set up the GitHub Wiki for D.A.V.I.D AI repository.

---

## Step 1: Enable Wiki

1. Go to your repository: https://github.com/david0154/david-ai
2. Click **Settings** tab
3. Scroll to **Features** section
4. Check ☑️ **Wikis** checkbox
5. Click **Save**

---

## Step 2: Access Wiki

1. Go to repository homepage
2. Click **Wiki** tab (top navigation)
3. You'll see the wiki page

---

## Step 3: Create Wiki Pages

The wiki markdown files are in the `wiki/` folder of this repository. To add them to GitHub Wiki:

### Method 1: Clone Wiki Repository

```bash
# Clone the wiki
git clone https://github.com/david0154/david-ai.wiki.git
cd david-ai.wiki

# Copy wiki files from main repository
cp ../wiki/*.md .

# Add and commit
git add .
git commit -m "Add complete wiki documentation"
git push
```

### Method 2: Manual Creation

1. Go to Wiki tab
2. Click **Create the first page**
3. Copy content from `wiki/Home.md`
4. Save as "Home"
5. Repeat for each wiki page

---

## Wiki Pages Structure

### Core Pages (Created):

1. **Home.md** - Wiki homepage with overview
2. **Installation.md** - Installation and setup guide
3. **Voice-Commands.md** - Complete voice commands list
4. **Privacy-Policy.md** - Detailed privacy policy
5. **FAQ.md** - Frequently asked questions
6. **AI-Models.md** - AI models documentation

### Additional Pages (To Create):

7. **Gesture-Control.md** - Gesture commands guide
8. **Languages.md** - Language support details
9. **Troubleshooting.md** - Common issues and fixes
10. **Contributing.md** - How to contribute
11. **Building-from-Source.md** - Developer guide
12. **Architecture.md** - Technical architecture

---

## Page Linking

### Internal Links

Use simple names without `.md` extension:

```markdown
[Installation Guide](Installation)
[Voice Commands](Voice-Commands)
[Privacy Policy](Privacy-Policy)
```

### External Links

```markdown
[GitHub Repository](https://github.com/david0154/david-ai)
[Report Bug](https://github.com/david0154/david-ai/issues)
```

---

## Sidebar (Optional)

Create `_Sidebar.md` for custom sidebar:

```markdown
## Quick Links
- [Home](Home)
- [Installation](Installation)
- [Voice Commands](Voice-Commands)
- [FAQ](FAQ)

## Features
- [Gesture Control](Gesture-Control)
- [AI Models](AI-Models)
- [Languages](Languages)

## Support
- [Troubleshooting](Troubleshooting)
- [Privacy Policy](Privacy-Policy)
```

---

## Footer (Optional)

Create `_Footer.md` for custom footer:

```markdown
**© 2026 Nexuzy Tech Ltd.** | [david@nexuzy.in](mailto:david@nexuzy.in)
```

---

## Wiki Order

### Recommended Page Order:

1. Home
2. Installation
3. Voice Commands
4. Gesture Control
5. AI Models
6. Languages
7. Privacy Policy
8. FAQ
9. Troubleshooting
10. Contributing
11. Building from Source
12. Architecture

---

## Wiki Settings

### Access Control:

**Options**:
- **Public**: Anyone can view and edit (recommended for open-source)
- **Restricted**: Only collaborators can edit
- **Private**: Only you can edit

**To Change**:
1. Go to **Settings** > **Options**
2. Find **Wikis** section
3. Select access level
4. Save

---

## Updating Wiki

### Via Git:

```bash
# Clone wiki
git clone https://github.com/david0154/david-ai.wiki.git

# Edit files
vim Home.md

# Commit and push
git add .
git commit -m "Update wiki"
git push
```

### Via Web Interface:

1. Go to Wiki page
2. Click **Edit**
3. Make changes
4. Click **Save**

---

## Wiki Best Practices

### ✅ Do:
- Keep pages concise
- Use clear headings
- Add navigation links
- Include examples
- Update regularly
- Use images (upload to wiki)

### ❌ Don't:
- Make pages too long
- Duplicate information
- Use broken links
- Forget to update version numbers

---

## Current Wiki Content

All wiki pages are ready in the `wiki/` folder:

```
wiki/
├── Home.md                    ✅ Complete
├── Installation.md             ✅ Complete
├── Voice-Commands.md           ✅ Complete
├── Privacy-Policy.md           ✅ Complete
├── FAQ.md                      ✅ Complete
├── AI-Models.md                ✅ Complete
└── WIKI_SETUP_GUIDE.md         ✅ This file
```

---

## Quick Setup Commands

```bash
# 1. Clone wiki repo
git clone https://github.com/david0154/david-ai.wiki.git
cd david-ai.wiki

# 2. Copy all wiki files
cp ../wiki/Home.md ./Home.md
cp ../wiki/Installation.md ./Installation.md
cp ../wiki/Voice-Commands.md ./Voice-Commands.md
cp ../wiki/Privacy-Policy.md ./Privacy-Policy.md
cp ../wiki/FAQ.md ./FAQ.md
cp ../wiki/AI-Models.md ./AI-Models.md

# 3. Commit and push
git add .
git commit -m "Add complete wiki documentation"
git push origin master
```

---

## Verification

After setup, verify:

1. Visit https://github.com/david0154/david-ai/wiki
2. Check all pages load correctly
3. Test all internal links
4. Verify images display (if any)
5. Check formatting

---

## Support

Need help with wiki setup?

📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)

---

**© 2026 Nexuzy Tech Ltd.**
