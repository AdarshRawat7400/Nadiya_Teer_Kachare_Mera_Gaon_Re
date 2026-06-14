const fs = require('fs');
const path = require('path');

const baseDir = 'Nadiya_Teer_Kachare_Mera_Gaon_Re';

// Function to process Markdown tables into normal text
function processMarkdownTables(text) {
    if (!text) return text;
    let lines = text.split('\n');
    let out = [];
    let inTable = false;
    
    for (let i = 0; i < lines.length; i++) {
        let line = lines[i];
        if (line.trim().startsWith('|')) {
            if (line.includes('---')) continue; // Skip separator
            let parts = line.split('|').map(s => s.trim()).filter(s => s.length > 0);
            
            if (!inTable) { // First row is headers
                inTable = true;
                continue;
            } else {
                // Determine format based on structure
                if (parts.length === 2 && parts[1].match(/^\d+$/)) {
                    out.push(`${parts[0]} (पृ. ${parts[1]})`);
                } else if (parts[0] === 'शब्द') {
                     continue;
                } else if (parts.length >= 2) {
                    let shabad = parts[0].replace(/\*\*/g, '');
                    let arth = parts[1];
                    let mool = parts.length > 2 ? parts[2] : "";
                    let kavita = parts.length > 3 ? parts[3] : "";
                    let pankti = parts.length > 4 ? ` (${parts[4]})` : "";
                    
                    let formattedLine = `**${shabad}** — ${arth} [${mool}]`;
                    if (kavita) formattedLine += `\n  > ${kavita}${pankti}`;
                    out.push(formattedLine);
                } else {
                    out.push(parts.join(' - '));
                }
            }
        } else {
            inTable = false;
            out.push(line);
        }
    }
    return out.join('\n');
}

function readMarkdownFiles(dirPath) {
    if (!fs.existsSync(dirPath)) return [];
    
    return fs.readdirSync(dirPath)
        .filter(f => f.endsWith('.md'))
        .sort((a, b) => {
            const numA = parseInt(a.split('_')[0]) || 0;
            const numB = parseInt(b.split('_')[0]) || 0;
            return numA - numB;
        })
        .map(file => {
            const filePath = path.join(dirPath, file);
            let content = fs.readFileSync(filePath, 'utf8');
            
            // Remove frontmatter
            content = content.replace(/^---\n[\s\S]*?\n---\n/, '');
            
            content = processMarkdownTables(content);
            
            const lines = content.split('\n');
            let title = '';
            for (let line of lines) {
                if (line.startsWith('# ')) {
                    title = line.replace('# ', '').trim();
                    break;
                }
            }
            // Strip out header from content
            if (title) {
                content = content.replace('# ' + title, '').trim();
            }

            // Parse stanzas
            const stanzas = [];
            let currentStanza = [];
            const contentLines = content.split('\n');
            for (let line of contentLines) {
                line = line.trim();
                if (line === '') {
                    if (currentStanza.length > 0) {
                        stanzas.push(currentStanza.join('\n'));
                        currentStanza = [];
                    }
                } else {
                    line = line.replace(/\[\d+\]\*?$/, '').trim();
                    if (line !== '') {
                        currentStanza.push(line);
                    }
                }
            }
            if (currentStanza.length > 0) {
                stanzas.push(currentStanza.join('\n'));
            }

            // Find matching explainer
            const explainerFileName = file.replace('.md', '_explainer.md');
            const explainerPath = path.join(baseDir, 'Explainers', explainerFileName);
            let explainerContent = null;
            if (fs.existsSync(explainerPath)) {
                explainerContent = fs.readFileSync(explainerPath, 'utf8');
                // Remove frontmatter
                explainerContent = explainerContent.replace(/^---\n[\s\S]*?\n---\n/, '').trim();
                let explainerTitleLine = '';
                const exLines = explainerContent.split('\n');
                for (let exLine of exLines) {
                    if (exLine.startsWith('# ')) {
                        explainerTitleLine = exLine;
                        break;
                    }
                }
                if (explainerTitleLine) {
                  explainerContent = explainerContent.replace(explainerTitleLine, '').trim();
                }
            }
            
            return {
                id: `poem_${file.replace('.md', '').replace(/[\s\(\)-]+/g, '_')}`,
                title: title || file.replace('.md', ''),
                stanzas: stanzas,
                explainer: explainerContent
            };
        });
}

const prefacePoems = readMarkdownFiles(path.join(baseDir, 'Preface'));
const mainPoems = readMarkdownFiles(path.join(baseDir, 'Poems'));
const postfacePoems = readMarkdownFiles(path.join(baseDir, 'Postface'));

let dictionaryContent = null;
let dictionaryList = [];
const dictPath = path.join(baseDir, 'Word_Dictionary', 'shabdakosh.md');
if (fs.existsSync(dictPath)) {
    dictionaryContent = fs.readFileSync(dictPath, 'utf8');
} else {
    // try Source folder
    const sourceDict = path.join(baseDir, 'Source', 'shabdakosh.md');
    if (fs.existsSync(sourceDict)) {
        dictionaryContent = fs.readFileSync(sourceDict, 'utf8');
    }
}

if (!dictionaryContent) {
    // Fallback
} else {
    // Parse list into structured objects
    let lines = dictionaryContent.split('\n');
    let currentItem = null;
    
    for (let i = 0; i < lines.length; i++) {
        let line = lines[i];
        if (line.startsWith('- **')) {
            let match = line.match(/- \*\*([^*]+)\*\*: (.*)/);
            if (match) {
                if (currentItem) dictionaryList.push(currentItem);
                currentItem = { word: match[1].trim(), meaning: match[2].trim(), origin: "", poem: "", line: "" };
            }
        } else if (line.trim().startsWith('- *मूल*:') && currentItem) {
            currentItem.origin = line.replace('- *मूल*:', '').trim();
        } else if (line.trim().startsWith('- *कविता*:') && currentItem) {
            currentItem.poem = line.replace('- *कविता*:', '').trim();
        } else if (line.trim().startsWith('- *पंक्ति*:') && currentItem) {
            currentItem.line = line.replace('- *पंक्ति*:', '').trim();
        }
    }
    if (currentItem) {
        dictionaryList.push(currentItem);
    }
}

const output = {
  "author": {
    "id": "author_1",
    "name": "अवध बिहारी रावत",
    "bio": "कवि अवध बिहारी रावत नगर की वरिष्ठ पीढ़ी के रचनाकार हैं। इनकी रचनाओं में गाँव, प्रकृति, देश-प्रेम और जीवन के विविध रंग देखने को मिलते हैं।",
    "birthDate": "10 अगस्त 1934",
    "birthPlace": "छतरपुर"
  },
  "book": {
    "id": "book_1",
    "title": "नदिया तीर कछारे मेरा गाँव रे",
    "description": "यह एक अनुपम कविता संग्रह है जो गांव की माटी, नदी के किनारे और ग्रामीण जीवन की सादगी को संजोए हुए है। पढ़ते और सुनते हुए जड़ों की ओर लौटें।",
    "coverImageUrl": "https://images.unsplash.com/photo-1549646549-b59dd1a45700?w=800&q=80"
  },
  "dictionary": dictionaryList,
  "chapters": [
    {
      "id": "chapter_1_preface",
      "title": "प्राक्कथन (Preface)",
      "poems": prefacePoems
    },
    {
      "id": "chapter_2_poems",
      "title": "कविताएँ (Poems)",
      "poems": mainPoems
    },
    {
      "id": "chapter_3_author",
      "title": "लेखक के बारे में (About the Author)",
      "poems": postfacePoems
    }
  ]
};

fs.mkdirSync('app/src/main/assets/data', { recursive: true });
fs.writeFileSync('app/src/main/assets/data/nadiya_teer.json', JSON.stringify(output, null, 2));
console.log('JSON written successfully with multiple chapters and explainers.');
