package com.example

import org.junit.Test
import java.io.File

class GenerateJsonTest {
    @Test
    fun generate() {
        val pathsToTry = listOf(
            "/app/src/test/java/com/example/BookData.txt",
            "src/test/java/com/example/BookData.txt",
            "../src/test/java/com/example/BookData.txt"
        )
        val jsFile = pathsToTry.map { File(it) }.firstOrNull { it.exists() }
            ?: throw java.io.FileNotFoundException("Could not find BookData.txt in any tested path")
        val rawText = jsFile.readText()

        val blocks = rawText.split(Regex("^## ", RegexOption.MULTILINE)).filter { it.trim().isNotEmpty() }
        
        val prefaceTitles = listOf("समर्पण", "मंगल कामना", "आमुख", "दो-शब्द")
        val ignoreTitles = listOf("अनुक्रमणिका")
        val aboutAuthorTitles = listOf("रचनाकार परिचय")
        
        val prefacePoems = mutableListOf<String>()
        val regularPoems = mutableListOf<String>()
        val aboutPoems = mutableListOf<String>()
        
        var poemIndex = 1
        var chapterIndex = 1
        for (block in blocks) {
            val lines = block.split("\n")
            val title = lines[0].trim()
            if (ignoreTitles.contains(title)) continue
            
            val stanzas = mutableListOf<String>()
            var currentStanza = mutableListOf<String>()
            
            for (i in 1 until lines.size) {
                val line = lines[i].trim()
                if (line == "*" || line.startsWith("[") || line.startsWith("*नदिया तीर कछारे") || line.startsWith("----") || line == "***" || line == "---" || line == "****") continue
                
                if (line.isEmpty()) {
                    if (currentStanza.isNotEmpty()) {
                        stanzas.add(currentStanza.joinToString("\n"))
                        currentStanza.clear()
                    }
                } else {
                    currentStanza.add(line)
                }
            }
            if (currentStanza.isNotEmpty()) {
                stanzas.add(currentStanza.joinToString("\n"))
            }

            val stanzasJson = stanzas.joinToString(",") { 
                "\"${it.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")}\""
            }

            val poemJson = """
                {
                  "id": "poem_$poemIndex",
                  "title": "${title.replace("\"", "\\\"")}",
                  "sequenceOrder": $poemIndex,
                  "stanzas": [$stanzasJson]
                }
            """.trimIndent()
            
            if (prefaceTitles.contains(title)) {
                prefacePoems.add(poemJson)
            } else if (aboutAuthorTitles.contains(title)) {
                aboutPoems.add(poemJson)
            } else {
                regularPoems.add(poemJson)
            }
            poemIndex++
        }

        val chaptersJson = """
        [
          {
            "id": "chap_1",
            "title": "The Preface",
            "sequenceOrder": 1,
            "poems": [
              ${prefacePoems.joinToString(",\n              ")}
            ]
          },
          {
            "id": "chap_2",
            "title": "Poems",
            "sequenceOrder": 2,
            "poems": [
              ${regularPoems.joinToString(",\n              ")}
            ]
          },
          {
            "id": "chap_3",
            "title": "About the Author",
            "sequenceOrder": 3,
            "poems": [
              ${aboutPoems.joinToString(",\n              ")}
            ]
          }
        ]
        """.trimIndent()

        val output = """
        {
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
          "chapters": $chaptersJson
        }
        """.trimIndent()

        val outputPathsToTry = listOf(
            "/app/src/main/assets/data/nadiya_teer.json",
            "src/main/assets/data/nadiya_teer.json",
            "../src/main/assets/data/nadiya_teer.json",
            "../../src/main/assets/data/nadiya_teer.json"
        )
        // just write to the one that exists (we want to overwrite it)
        val outFile = outputPathsToTry.map { File(it) }.firstOrNull { it.parentFile?.exists() == true }
            ?: File("src/main/assets/data/nadiya_teer.json")
        outFile.writeText(output)
        println("Generated JSON successfully to ${outFile.absolutePath}")
    }
}
