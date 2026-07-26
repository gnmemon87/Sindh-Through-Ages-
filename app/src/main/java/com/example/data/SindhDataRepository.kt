package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SindhDataRepository(private val progressDao: ProgressDao) {

    val userProgressFlow: Flow<UserProgressData> = progressDao.getUserProgress().map { entity ->
        if (entity == null) {
            UserProgressData()
        } else {
            val unlockedEras = entity.unlockedEraIdsString.split(",").filter { it.isNotBlank() }.toSet()
            val completedNodes = entity.completedNodeIdsString.split(",").filter { it.isNotBlank() }.toSet()
            val unlockedArtifacts = entity.unlockedArtifactIdsString.split(",").filter { it.isNotBlank() }.toSet()
            val answeredQuizzes = entity.answeredQuizIdsString.split(",").filter { it.isNotBlank() }.toSet()
            val badgesFromDb = entity.unlockedBadgeIdsString.split(",").filter { it.isNotBlank() }.toSet()

            val calculatedBadges = calculateBadges(
                points = entity.knowledgePoints,
                completedNodes = completedNodes,
                unlockedArtifacts = unlockedArtifacts,
                unlockedEras = unlockedEras
            ) + badgesFromDb

            UserProgressData(
                knowledgePoints = entity.knowledgePoints,
                rankTitle = calculateRankTitle(entity.knowledgePoints),
                dailyStreak = entity.dailyStreak,
                unlockedEraIds = if (unlockedEras.isEmpty()) setOf("era_indus_valley") else unlockedEras,
                completedNodeIds = completedNodes,
                unlockedArtifactIds = unlockedArtifacts,
                answeredQuizIds = answeredQuizzes,
                unlockedBadgeIds = calculatedBadges
            )
        }
    }

    suspend fun addKnowledgePoints(points: Int, unlockArtifactId: String? = null, completeNodeId: String? = null) {
        val current = progressDao.getUserProgressDirect() ?: ProgressEntity()
        val newPoints = current.knowledgePoints + points
        
        var unlockedEras = current.unlockedEraIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (unlockedEras.isEmpty()) unlockedEras.add("era_indus_valley")

        // Check if points milestone unlocks new eras
        if (newPoints >= 100 && !unlockedEras.contains("era_soomra")) unlockedEras.add("era_soomra")
        if (newPoints >= 250 && !unlockedEras.contains("era_talpur")) unlockedEras.add("era_talpur")
        if (newPoints >= 450 && !unlockedEras.contains("era_british")) unlockedEras.add("era_british")
        if (newPoints >= 700 && !unlockedEras.contains("era_modern")) unlockedEras.add("era_modern")

        val completedNodes = current.completedNodeIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (completeNodeId != null) completedNodes.add(completeNodeId)

        val unlockedArtifacts = current.unlockedArtifactIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (unlockArtifactId != null) unlockedArtifacts.add(unlockArtifactId)

        val updatedBadges = calculateBadges(newPoints, completedNodes, unlockedArtifacts, unlockedEras)

        val updatedEntity = current.copy(
            knowledgePoints = newPoints,
            rankTitle = calculateRankTitle(newPoints),
            unlockedEraIdsString = unlockedEras.joinToString(","),
            completedNodeIdsString = completedNodes.joinToString(","),
            unlockedArtifactIdsString = unlockedArtifacts.joinToString(","),
            unlockedBadgeIdsString = updatedBadges.joinToString(",")
        )

        progressDao.saveUserProgress(updatedEntity)
    }

    suspend fun recordQuizCompleted(quizId: String, rewardPoints: Int, artifactId: String) {
        val current = progressDao.getUserProgressDirect() ?: ProgressEntity()
        val answeredQuizzes = current.answeredQuizIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
        
        if (!answeredQuizzes.contains(quizId)) {
            answeredQuizzes.add(quizId)
            val newPoints = current.knowledgePoints + rewardPoints

            val unlockedEras = current.unlockedEraIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (newPoints >= 100 && !unlockedEras.contains("era_soomra")) unlockedEras.add("era_soomra")
            if (newPoints >= 250 && !unlockedEras.contains("era_talpur")) unlockedEras.add("era_talpur")
            if (newPoints >= 450 && !unlockedEras.contains("era_british")) unlockedEras.add("era_british")
            if (newPoints >= 700 && !unlockedEras.contains("era_modern")) unlockedEras.add("era_modern")

            val unlockedArtifacts = current.unlockedArtifactIdsString.split(",").filter { it.isNotBlank() }.toMutableSet()
            unlockedArtifacts.add(artifactId)

            val completedNodes = current.completedNodeIdsString.split(",").filter { it.isNotBlank() }.toSet()
            val updatedBadges = calculateBadges(newPoints, completedNodes, unlockedArtifacts, unlockedEras)

            val updated = current.copy(
                knowledgePoints = newPoints,
                rankTitle = calculateRankTitle(newPoints),
                unlockedEraIdsString = unlockedEras.joinToString(","),
                unlockedArtifactIdsString = unlockedArtifacts.joinToString(","),
                answeredQuizIdsString = answeredQuizzes.joinToString(","),
                unlockedBadgeIdsString = updatedBadges.joinToString(",")
            )
            progressDao.saveUserProgress(updated)
        }
    }

    private fun calculateBadges(
        points: Int,
        completedNodes: Set<String>,
        unlockedArtifacts: Set<String>,
        unlockedEras: Set<String>
    ): Set<String> {
        val badges = mutableSetOf("badge_indus_explorer")
        if (unlockedEras.contains("era_modern") || completedNodes.contains("choice_sufi_risalo") || unlockedArtifacts.contains("art_alghoza_music")) {
            badges.add("badge_sufi_scholar")
        }
        if (unlockedEras.contains("era_talpur") || completedNodes.contains("choice_talpur_fort") || unlockedArtifacts.contains("art_pacco_qillo")) {
            badges.add("badge_fortress_master")
        }
        if (completedNodes.contains("choice_mohenjo_weights") || unlockedArtifacts.contains("art_standard_weights") || unlockedArtifacts.contains("art_banbhore_pottery")) {
            badges.add("badge_silk_road_merchant")
        }
        if (points >= 350 || completedNodes.size >= 4) {
            badges.add("badge_master_historian")
        }
        return badges
    }

    suspend fun resetProgress() {
        progressDao.resetUserProgress()
        progressDao.saveUserProgress(ProgressEntity())
    }

    private fun calculateRankTitle(points: Int): String {
        return when {
            points >= 1200 -> "Grand Historian of Sindh"
            points >= 800 -> "Scholar of the Indus"
            points >= 500 -> "Heritage Master"
            points >= 250 -> "Sindh Traveler"
            points >= 100 -> "History Explorer"
            else -> "Sindh Novice Explorer"
        }
    }

    // Static Historical Content Collections
    fun getAllEras(): List<HistoricalEra> = listOf(
        HistoricalEra(
            id = "era_indus_valley",
            title = "Indus Valley Civilization",
            subtitle = "Mohenjo-Daro & Urban Dawn",
            dateRange = "2500 BCE – 1900 BCE",
            location = "Larkana District, Sindh",
            description = "One of the world's earliest major urban sanctuaries, famous for standardized baked brick architecture, sophisticated subterranean drainage, grid planning, and trade with Mesopotamia.",
            primaryColorHex = 0xFFC85A32, // Terracotta Rust
            keyTopics = listOf("Great Bath", "Priest-King", "Standard Weights", "Grid Urbanism"),
            requiredKnowledgePoints = 0,
            eraIndex = 0
        ),
        HistoricalEra(
            id = "era_soomra",
            title = "Soomra Dynasty",
            subtitle = "Sufi Origins & Maritime Ports",
            dateRange = "1024 CE – 1351 CE",
            location = "Banbhore & Mansura, Sindh",
            description = "An era of indigenous Sindhi statehood, flourishing sea trade along the Indian Ocean, literary romantic epics like Umar Marvi, and early Islamic glazed ceramic craft.",
            primaryColorHex = 0xFF2B5C8F, // Lapis Blue
            keyTopics = listOf("Banbhore Port", "Umar Marvi Epic", "Silk Road Maritime Route", "Sindhi Poetry"),
            requiredKnowledgePoints = 100,
            eraIndex = 1
        ),
        HistoricalEra(
            id = "era_talpur",
            title = "Kalhora & Talpur Era",
            subtitle = "Fortresses & Necropolises",
            dateRange = "1701 CE – 1843 CE",
            location = "Hyderabad Fort & Makli, Sindh",
            description = "A golden age of monumental architecture, hand-carved stone mausoleums at Makli Necropolis, royal Pacco Qillo citadel, and traditional block-printed Ajrak patronage.",
            primaryColorHex = 0xFF8C2D3A, // Crimson Ajrak Red
            keyTopics = listOf("Makli Necropolis", "Pacco Qillo Citadel", "Ajrak Block-Printing", "Khudabad Mosque"),
            requiredKnowledgePoints = 250,
            eraIndex = 2
        ),
        HistoricalEra(
            id = "era_british",
            title = "British Colonial Era",
            subtitle = "Sukkur Barrage & Railways",
            dateRange = "1843 CE – 1947 CE",
            location = "Sukkur & Karachi, Sindh",
            description = "Transformation of the Indus River with the world's largest irrigation system, establishment of Karachi as a premier port city, and the Scinde Railway.",
            primaryColorHex = 0xFF4A6B53, // Canal Emerald
            keyTopics = listOf("Sukkur Barrage", "Karachi Empress Market", "Scinde Railway", "Indus Canal Network"),
            requiredKnowledgePoints = 450,
            eraIndex = 3
        ),
        HistoricalEra(
            id = "era_modern",
            title = "Modern Sindh",
            subtitle = "Sufi Heritage & Living Arts",
            dateRange = "1947 CE – Present",
            location = "Sehwan Sharif, Bhit Shah & Thar",
            description = "A vibrant tapestry of living Sufi music (Shah Abdul Latif Bhittai), Tharparkar desert culture, ecological preservation of the Indus Blind River Dolphin, and modern crafts.",
            primaryColorHex = 0xFFDAA520, // Golden Sunset
            keyTopics = listOf("Shah Abdul Latif Bhittai", "Alghoza & Ektara", "Indus Dolphin Conservation", "Thar Folk Art"),
            requiredKnowledgePoints = 700,
            eraIndex = 4
        )
    )

    fun getStoryNodesForEra(eraId: String): List<StoryNode> {
        return when (eraId) {
            "era_indus_valley" -> listOf(
                StoryNode(
                    id = "node_mohenjo_1",
                    eraId = "era_indus_valley",
                    title = "Arrival at the Great Granary",
                    locationName = "Upper Citadel, Mohenjo-Daro",
                    speakerTitle = "High Priest Architect",
                    narrativeText = "You step into the baked-brick avenues of Mohenjo-Daro in 2200 BCE. The sun shines on a vast brick citadel. A master builder holding stone measuring rods approaches you. 'Welcome traveler! A trade merchant from Sumeria has brought lapis lazuli, but demands our famous standardized clay seals and grain measurements. How shall we negotiate?'",
                    traderNarrative = "💰 [Trader's View]: As a Silk Road & Maritime Merchant, you immediately calculate the exchange rate between Sumerian silver, lapis lazuli, and Mohenjo-Daro's cubical chert weights. Fair weights mean huge profits across Persian Gulf ports!",
                    poetNarrative = "🌸 [Artisan's View]: You admire the flawless symmetry of the baked bricks, the delicate trefoil cloak carvings on priest busts, and the fine clay seals stamped with magical script.",
                    rulerNarrative = "👑 [Ruler's View]: You inspect the civic order of the citadel. With no military walls or weapons needed, your authority rests on public sanitation, well water, and honest trade standards.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_mohenjo_weights",
                            text = "Offer standard chert weights (binary ratio 1, 2, 4, 8) to prove trade accuracy.",
                            consequenceText = "The Sumerian merchant is astonished by the absolute precision of your standardized cubical chert weights! Trade flourishes smoothly.",
                            knowledgePointsReward = 50,
                            nextNodeId = "node_mohenjo_2",
                            artifactUnlockedId = "art_standard_weights",
                            historicalInsight = "Indus Valley people possessed an amazingly uniform decimal and binary system of weights and measures across thousands of miles.",
                            historiansCorner = HistoriansCorner(
                                historicalNotes = "Cubical chert weights discovered across Mohenjo-Daro, Harappa, and Lothal follow a standardized binary ratio (1, 2, 4, 8, 16) for small items and decimal for larger masses, with less than 1.5% variance across 1,000 miles.",
                                museumLocation = "National Museum of Pakistan, Karachi (Gallery of Ancient History)",
                                primarySourceCitation = "Marshall, Sir John (1931). 'Mohenjo-daro and the Indus Civilization', Vol. II, Probsthain."
                            )
                        ),
                        StoryChoice(
                            id = "choice_mohenjo_bath",
                            text = "Guide the merchant to the Great Bath to inspect our waterproof bitumen engineering.",
                            consequenceText = "Inspecting the Great Bath's tar-sealed bricks and drainage system convinces the traveler of Mohenjo-Daro's advanced civic mastery.",
                            knowledgePointsReward = 40,
                            nextNodeId = "node_mohenjo_2",
                            artifactUnlockedId = "art_great_bath",
                            historicalInsight = "The Great Bath is considered one of the earliest public water tanks in the ancient world, featuring fitted bricks with natural tar (bitumen) waterproofing.",
                            historiansCorner = HistoriansCorner(
                                historicalNotes = "The Great Bath measures 11.88 x 7.01 meters with a depth of 2.43 meters. Two wide staircases lead down to the floor sealed with natural asphalt bitumen between double brick walls.",
                                museumLocation = "Mohenjo-Daro Site Museum, Larkana, Sindh & On-Site Citadel Ruin",
                                primarySourceCitation = "Kenoyer, J. Mark (1998). 'Ancient Cities of the Indus Valley Civilization', Oxford University Press."
                            )
                        )
                    )
                ),
                StoryNode(
                    id = "node_mohenjo_2",
                    eraId = "era_indus_valley",
                    title = "The Priest-King's Sanctuary",
                    locationName = "Priestly Citadel Complex",
                    speakerTitle = "Ruler of the Citadel",
                    narrativeText = "The High Priest wearing a trefoil-patterned shawl looks over town blueprints. 'Our city has no weapons or violent carvings. Instead, our strength is subterranean covered drains, clean water wells, and peaceful governance. How do you suggest we allocate our bricklayers today?'",
                    traderNarrative = "💰 [Trader's View]: Sealing trade bales with steatite unicorn seals will guarantee authenticity when exported down the Indus River to Arabian Sea ships.",
                    poetNarrative = "🌸 [Artisan's View]: You observe how the steatite seals are carved with intaglio precision, recording symbols that hold ancient spiritual power.",
                    rulerNarrative = "👑 [Ruler's View]: Public drains must be swept daily! Cleanliness keeps the city healthy and united without needing a standing army.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_mohenjo_drains",
                            text = "Prioritize expanding the covered street drainage network for public sanitation.",
                            consequenceText = "Sanitation improves immensely! Mohenjo-Daro remains free of waterborne plagues, setting a benchmark for ancient public health.",
                            knowledgePointsReward = 60,
                            nextNodeId = null,
                            artifactUnlockedId = "art_priest_king",
                            historicalInsight = "Unlike ancient Mesopotamia or Egypt, Indus Valley cities prioritized urban sanitation and private brick bathrooms in almost every home.",
                            historiansCorner = HistoriansCorner(
                                historicalNotes = "Street drains were covered with loose limestone bricks or burnt slabs that could be lifted for maintenance. Soak pits at regular intervals trapped solid debris.",
                                museumLocation = "Mohenjo-Daro Archaeological Site & Museum, Larkana, Sindh",
                                primarySourceCitation = "Jarrige, J.F. (1986). 'Excavations at Mehrgarh and the Indus Tradition', South Asian Archaeology."
                            )
                        ),
                        StoryChoice(
                            id = "choice_mohenjo_seals",
                            text = "Commission terracotta seals depicting the Unicorn and Bull for overseas shipping.",
                            consequenceText = "Your seals travel across the Arabian Sea, certifying authentic Sindhi cotton goods in ancient Mesopotamian ports!",
                            knowledgePointsReward = 50,
                            nextNodeId = null,
                            artifactUnlockedId = "art_terracotta_seals",
                            historicalInsight = "Steatite and terracotta seals were used to stamp clay tags on trade bales sent across ancient sea routes.",
                            historiansCorner = HistoriansCorner(
                                historicalNotes = "Steatite seals stamped into clay seals ('bulla') certifying cotton and timber cargo were uncovered in Ur, Kish, and Nippur in ancient Mesopotamia.",
                                museumLocation = "National Museum of Pakistan, Karachi & British Museum, London",
                                primarySourceCitation = "Parpola, Asko (1994). 'Deciphering the Indus Script', Cambridge University Press."
                            )
                        )
                    )
                )
            )

            "era_soomra" -> listOf(
                StoryNode(
                    id = "node_soomra_1",
                    eraId = "era_soomra",
                    title = "The Port of Banbhore",
                    locationName = "Banbhore Harbor, Indian Ocean Coast",
                    speakerTitle = "Captain of the Dhow Fleet",
                    narrativeText = "It is 1180 CE. Dhow ships laden with ivory, spices, and Chinese celadon ceramic ride the tide at Banbhore port. A Soomra governor asks: 'Pirates threaten our trade route near the Indus delta. Shall we send naval patrols or negotiate alliance through trade incentives?'",
                    traderNarrative = "💰 [Trader's View]: Your ship's cargo of glass vessels and ivory bangles depends on calm seas. Protecting the sea lanes to Siraf and Canton guarantees wealth!",
                    poetNarrative = "🌸 [Artisan's View]: You compose verses celebrating the bravery of Soomra heroines like Sassui and Marvi, embedding moral strength into Sindhi folklore.",
                    rulerNarrative = "👑 [Ruler's View]: As a Soomra sovereign, you balance maritime defenses with diplomatic ties across the Indian Ocean basin.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_soomra_navy",
                            text = "Deploy fast Sindhi rowed patrols to safeguard merchant ships.",
                            consequenceText = "The harbor remains safe! Banbhore becomes the undisputed maritime gateway connecting China, Arabia, and Persia.",
                            knowledgePointsReward = 60,
                            nextNodeId = "node_soomra_2",
                            artifactUnlockedId = "art_banbhore_pottery",
                            historicalInsight = "Banbhore (historically identified by many as Debal) was a major international port with thousands of glass and ceramic artifacts unearthed."
                        ),
                        StoryChoice(
                            id = "choice_soomra_epic",
                            text = "Sponsor the court poets to sing ballads of Marvi's loyalty to her homeland.",
                            consequenceText = "The legendary folk tale of Marvi choosing her village desert over royal luxury inspires patriotism across the entire realm!",
                            knowledgePointsReward = 50,
                            nextNodeId = "node_soomra_2",
                            artifactUnlockedId = "art_marvi_tower",
                            historicalInsight = "The story of Umar Marvi is one of Sindh's seven classic romantic tragedies, symbolizing unyielding love for one's native soil."
                        )
                    )
                ),
                StoryNode(
                    id = "node_soomra_2",
                    eraId = "era_soomra",
                    title = "Guild of Glazed Ceramics",
                    locationName = "Mansura Artisan District",
                    speakerTitle = "Master Potter of Sindh",
                    narrativeText = "Inside a ceramic kiln workshop, master artisans prepare vibrant turquoise lead-glazed pottery. 'Our blue and green tile recipes require specialized copper oxides. Which technique should we pass down to future generations?'",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_soomra_kashi",
                            text = "Refine Kashi blue tilework to adorn mosques and shrines.",
                            consequenceText = "Your blue tile traditions endure for centuries, defining the iconic aesthetic of Sindhi architecture!",
                            knowledgePointsReward = 60,
                            nextNodeId = null,
                            artifactUnlockedId = "art_sindhi_kashi",
                            historicalInsight = "Sindhi Kashi tile work is world-famous for its distinct cobalt blue and turquoise glazes baked at high kiln temperatures."
                        )
                    )
                )
            )

            "era_talpur" -> listOf(
                StoryNode(
                    id = "node_talpur_1",
                    eraId = "era_talpur",
                    title = "Carvings of Makli Hill",
                    locationName = "Makli Necropolis, Thatta",
                    speakerTitle = "Royal Stone Mason",
                    narrativeText = "You stand amid Makli Hill in 1780 CE, one of the largest funerary sites in the world. Masons are carving floral lace patterns into golden sandstone. 'The Mir of Talpur wishes his mausoleum to reflect Sindhi Ajrak motifs in stone. Which pattern should we etch?'",
                    traderNarrative = "💰 [Trader's View]: Ajrak block-printed textiles exported to East Africa generate steady revenue for the Talpur court.",
                    poetNarrative = "🌸 [Artisan's View]: You carefully chisel rosette stars and lace tendrils into sandstone, turning stone into immortal poetry.",
                    rulerNarrative = "👑 [Ruler's View]: Grand tombs honor our forefathers while demonstrating Talpur prestige to visiting Persian and Mughal envoys.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_talpur_carving",
                            text = "Inscribe geometric trefoil rosette and calligraphy patterns.",
                            consequenceText = "The resulting sandstone carvings look like woven lace in stone, mesmerizing visitors for centuries to come!",
                            knowledgePointsReward = 60,
                            nextNodeId = "node_talpur_2",
                            artifactUnlockedId = "art_makli_carvings",
                            historicalInsight = "Makli Necropolis covers over 10 square kilometers and houses half a million tombs decorated with delicate stone relief carvings."
                        ),
                        StoryChoice(
                            id = "choice_talpur_ajrak",
                            text = "Commission indigo and madder-red Ajrak cloth for royal gifts.",
                            consequenceText = "The royal court adopts Ajrak as the official symbol of hospitality and honor across all diplomatic envoys.",
                            knowledgePointsReward = 50,
                            nextNodeId = "node_talpur_2",
                            artifactUnlockedId = "art_ajrak_craft",
                            historicalInsight = "Ajrak block printing involves 16 painstaking natural dyeing and washing steps using carved wooden blocks and river water."
                        )
                    )
                ),
                StoryNode(
                    id = "node_talpur_2",
                    eraId = "era_talpur",
                    title = "Defending Pacco Qillo Citadel",
                    locationName = "Hyderabad Citadel (Pacco Qillo)",
                    speakerTitle = "Mir of Hyderabad",
                    narrativeText = "High on the red clay hill of Hyderabad, the Talpur rulers hold court inside Pacco Qillo (The Sturdy Fort). 'Foreign powers send envoys to inspect our artillery and river tolls. How shall Sindh assert its independence?'",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_talpur_fort",
                            text = "Demonstrate Sindh's proud swordcraft and fort garrison readiness.",
                            consequenceText = "The envoys depart impressed by the high mud-brick ramparts and fierce independence of the Sindhi Amirs.",
                            knowledgePointsReward = 60,
                            nextNodeId = null,
                            artifactUnlockedId = "art_pacco_qillo",
                            historicalInsight = "Pacco Qillo was constructed in 1768 by Mian Ghulam Shah Kalhoro as a formidable fortified capital."
                        )
                    )
                )
            )

            "era_british" -> listOf(
                StoryNode(
                    id = "node_british_1",
                    eraId = "era_british",
                    title = "The Taming of the Mighty River",
                    locationName = "Sukkur Indus River Gorge, 1928",
                    speakerTitle = "Chief Irrigation Engineer Sir Arnold Musto",
                    narrativeText = "The Indus River floods furiously every monsoon, leaving fields dry during winter. Engineers gather at Sukkur gorge. 'We plan to build a 1,500-meter barrage with 66 span gates to feed seven giant canals. How should we ensure long-term stability?'",
                    traderNarrative = "💰 [Trader's View]: Year-round canal water will quadruple cotton and wheat yields, boosting Karachi Port export traffic!",
                    poetNarrative = "🌸 [Artisan's View]: You admire the architectural arches of the barrage reflecting in the sunset waters of the Indus River.",
                    rulerNarrative = "👑 [Ruler's View]: Engineering must respect the river's ecological power while preventing catastrophic seasonal droughts.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_sukkur_gates",
                            text = "Construct heavy steel radial sluice gates anchored in stone piers.",
                            consequenceText = "The Sukkur Barrage becomes an engineering marvel, turning millions of acres of desert into fertile wheat and cotton farmland!",
                            knowledgePointsReward = 70,
                            nextNodeId = "node_british_2",
                            artifactUnlockedId = "art_sukkur_barrage",
                            historicalInsight = "Opened in 1932, the Lloyd/Sukkur Barrage is one of the largest irrigation networks in the world, irrigating over 5 million acres."
                        ),
                        StoryChoice(
                            id = "choice_scinde_railway",
                            text = "Build the Scinde Railway line connecting Karachi Port directly to Kotri.",
                            consequenceText = "Locomotives transform travel time from days down to hours, accelerating agricultural trade to the world!",
                            knowledgePointsReward = 50,
                            nextNodeId = "node_british_2",
                            artifactUnlockedId = "art_scinde_railway",
                            historicalInsight = "The Scinde Railway line opened in 1861 as the very first railway line in modern-day Pakistan."
                        )
                    )
                ),
                StoryNode(
                    id = "node_british_2",
                    eraId = "era_british",
                    title = "Empress Market & Karachi City",
                    locationName = "Saddar District, Karachi",
                    speakerTitle = "City Architect James Strachan",
                    narrativeText = "In 1889, Karachi's Neo-Gothic clock tower market stands tall. Local traders bring fruits, brassware, and Sindhi carpets. 'How do we design civic markets that respect both local climate and global architecture?'",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_empress_market",
                            text = "Incorporate high vaulted arches and central courtyard ventilation.",
                            consequenceText = "Empress Market stays cool in summer heat, becoming Karachi's vibrant urban landmark for over a century!",
                            knowledgePointsReward = 60,
                            nextNodeId = null,
                            artifactUnlockedId = "art_empress_market",
                            historicalInsight = "Empress Market's 140-foot central clock tower and Gizri limestone arches remain an architectural icon of Karachi."
                        )
                    )
                )
            )

            "era_modern" -> listOf(
                StoryNode(
                    id = "node_modern_1",
                    eraId = "era_modern",
                    title = "The Sufi Melody of Bhit Shah",
                    locationName = "Shrine of Shah Abdul Latif Bhittai",
                    speakerTitle = "Faqir Singer with Tambooro",
                    narrativeText = "At dusk in Bhit Shah, Sufi singers gather playing the 5-stringed Tambooro instrument created by Shah Latif himself. The air vibrates with divine poetry. 'Which message of Shah Latif's Shah Jo Risalo should we share with travelers?'",
                    traderNarrative = "💰 [Trader's View]: Artisans selling glazed Kashi tiles and Ajrak shawls at shrine festivals generate vibrant local economy.",
                    poetNarrative = "🌸 [Artisan's View]: You listen intently as Faqir singers strike the 5-stringed Tambooro, weaving human yearning with spiritual peace.",
                    rulerNarrative = "👑 [Ruler's View]: Sufi message of universal tolerance ('Sur Sarang') is Sindh's true moral armor against division.",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_sufi_risalo",
                            text = "Sing 'Sur Sarang' - praying for rain and universal peace across all nations.",
                            consequenceText = "The sublime melody touches every soul present, spreading Sindh's message of peace, love, and human unity!",
                            knowledgePointsReward = 70,
                            nextNodeId = "node_modern_2",
                            artifactUnlockedId = "art_alghoza_music",
                            historicalInsight = "Shah Jo Risalo is a monumental compilation of Sindhi Sufi poetry divided into 'Surs' (musical modes) depicting human resilience and divine love."
                        ),
                        StoryChoice(
                            id = "choice_indus_dolphin",
                            text = "Join ecological conservators protecting the rare Indus Blind River Dolphin.",
                            consequenceText = "Protected river sanctuaries are established! The playful 'Bhulan' dolphin leaps happily in the silted Indus currents.",
                            knowledgePointsReward = 60,
                            nextNodeId = "node_modern_2",
                            artifactUnlockedId = "art_indus_dolphin",
                            historicalInsight = "The Indus River Dolphin (Bhulan) is an endangered freshwater species that uses echolocation to navigate muddy river waters."
                        )
                    )
                ),
                StoryNode(
                    id = "node_modern_2",
                    eraId = "era_modern",
                    title = "Tharparkar Desert Heritage",
                    locationName = "Mithi & Nagarparkar, Thar Desert",
                    speakerTitle = "Master Artisan of Thar",
                    narrativeText = "In the golden sands of Tharparkar near Karoonjhar mountains, peacocks dance near whitewashed circular 'Chunra' mud homes decorated with mirrorwork. 'How do we preserve our desert folk heritage in the modern era?'",
                    choices = listOf(
                        StoryChoice(
                            id = "choice_thar_craft",
                            text = "Empower women artisans through fair-trade embroidery and mirrorwork cooperatives.",
                            consequenceText = "Thar mirrorwork textiles win international acclaim, providing sustainable livelihoods to desert families!",
                            knowledgePointsReward = 70,
                            nextNodeId = null,
                            artifactUnlockedId = "art_thar_embroidery",
                            historicalInsight = "Tharparkar is world-renowned for its vibrant hand embroidery, mirrorwork, and harmonious co-existence of desert communities."
                        )
                    )
                )
            )

            else -> emptyList()
        }
    }

    fun getAllCulturalArtifacts(): List<CulturalArtifact> = listOf(
        CulturalArtifact(
            id = "art_priest_king",
            name = "The Priest-King Statue",
            eraId = "era_indus_valley",
            eraName = "Indus Valley Civilization",
            category = "Stone Sculpture",
            briefSummary = "A iconic 17.5 cm soapstone (steatite) bust carved around 2000 BCE at Mohenjo-Daro.",
            microLessonContent = "Discovered at Mohenjo-Daro in 1927, the Priest-King figurine wears a trefoil-patterned cloak, an armlet, and a forehead fillet. The trefoil motif was filled with red pigment originally. His calm expression and closed eyes suggest meditative royalty or spiritual leadership in a society that valued peace over militarism.",
            keyFacts = listOf(
                "Material: Soft steatite soapstone carved with bronze tools.",
                "Trefoil Motif: Similar trefoil patterns appear in ancient Mesopotamian and Minoan art.",
                "No Weapons: Demonstrates that Indus Valley society prioritized civic order without grand military monuments."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_priest_king",
                questionText = "What distinctive pattern is carved on the cloak worn by the Priest-King figurine?",
                options = listOf("Trefoil (three-leaved clover)", "Stripes", "Checkerboard", "Floral Lotus"),
                correctAnswerIndex = 0,
                explanation = "The cloak features carved trefoil (three-petaled rosette) motifs originally inlaid with red pigment."
            ),
            defaultUnlocked = true,
            hotspots = listOf(
                ArtifactHotspot(
                    id = "hp_trefoil",
                    title = "Trefoil Patterned Cloak",
                    xPercent = 0.35f,
                    yPercent = 0.55f,
                    significance = "Spiritual & Royal Motif",
                    detailNote = "Carved rosettes originally inlaid with red pigment. The trefoil symbol represented sacred authority across ancient trade routes from the Indus to Mesopotamia."
                ),
                ArtifactHotspot(
                    id = "hp_headband",
                    title = "Forehead Fillet & Hair Bun",
                    xPercent = 0.48f,
                    yPercent = 0.22f,
                    significance = "Ceremonial Headband",
                    detailNote = "A circular gold-colored ribbon tied neatly around a combed hair bun, holding a central circular ornament over his forehead."
                ),
                ArtifactHotspot(
                    id = "hp_armlet",
                    title = "Carved Armlet",
                    xPercent = 0.22f,
                    yPercent = 0.65f,
                    significance = "Elite Insignia",
                    detailNote = "A metal armlet worn high on the right arm, matching the forehead ornament design."
                )
            )
        ),
        CulturalArtifact(
            id = "art_great_bath",
            name = "Mohenjo-Daro Great Bath",
            eraId = "era_indus_valley",
            eraName = "Indus Valley Civilization",
            category = "Urban Architecture",
            briefSummary = "A 12x7 meter public pool engineered with fitted bitumen-waterproofed bricks.",
            microLessonContent = "Built atop the Citadel hill of Mohenjo-Daro, the Great Bath features two wide staircases, a recessed floor lined with tightly fitted fine bricks on edge, and a thick layer of natural bitumen (tar) between brick layers to prevent water leakage. It reflects early ritual bathing traditions.",
            keyFacts = listOf(
                "Waterproofing: Sealed with natural asphalt bitumen.",
                "Drainage: Connected to a corbelled brick drain high enough for a human to walk through.",
                "Public Health: Surroundings included private dressing rooms and clean well-water supply."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_great_bath",
                questionText = "What natural material did Indus Valley architects use to make the Great Bath waterproof?",
                options = listOf("Bitumen (natural asphalt tar)", "Pine Resin", "Volcanic Ash", "Animal Wax"),
                correctAnswerIndex = 0,
                explanation = "Engineers applied a layer of natural bitumen between brick walls to render the bath completely watertight."
            ),
            defaultUnlocked = true,
            hotspots = listOf(
                ArtifactHotspot(
                    id = "hp_bitumen",
                    title = "Bitumen Waterproofing Layer",
                    xPercent = 0.50f,
                    yPercent = 0.50f,
                    significance = "Early Chemical Engineering",
                    detailNote = "Natural asphalt bitumen was sandwiched between inner and outer brick walls, preventing a single drop of water from leaking into surrounding foundations."
                ),
                ArtifactHotspot(
                    id = "hp_staircase",
                    title = "Bathing Staircases",
                    xPercent = 0.20f,
                    yPercent = 0.40f,
                    significance = "Ritual Bathing Access",
                    detailNote = "Symmetrical north and south staircases with timber treads allowed citizens or priests to step down into the ceremonial waters."
                ),
                ArtifactHotspot(
                    id = "hp_drain",
                    title = "Corbelled Brick Drain",
                    xPercent = 0.80f,
                    yPercent = 0.70f,
                    significance = "Advanced Drainage",
                    detailNote = "A massive arched corbelled brick outlet allowed quick draining and refilling of clean well water."
                )
            )
        ),
        CulturalArtifact(
            id = "art_standard_weights",
            name = "Cubical Chert Trade Weights",
            eraId = "era_indus_valley",
            eraName = "Indus Valley Civilization",
            category = "Commerce & Science",
            briefSummary = "Uniform polished stone cubes used for precise merchant measurements across thousands of miles.",
            microLessonContent = "Indus Valley weights were polished cubes cut from chert stone. The lower weights followed a binary system (1, 2, 4, 8, 16, 32 up to 64), while higher weights followed a decimal system. Astonishingly, weights found in Mohenjo-Daro matched those in Harappa and Lothal down to fractions of a gram!",
            keyFacts = listOf(
                "Standardization: Identical weight ratios across a region of over 1 million sq km.",
                "Binary & Decimal: Used binary ratios for small goods and decimal for large quantities.",
                "Honest Trade: Proves a strong central standard of fair trade and taxation."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_standard_weights",
                questionText = "What numerical system ratio was used for smaller Indus Valley trade weights?",
                options = listOf("Binary ratio (1, 2, 4, 8, 16)", "Roman Numerals", "Duodecimal (base 12)", "Random sizing"),
                correctAnswerIndex = 0,
                explanation = "Small chert weights followed a strict binary progression (1, 2, 4, 8, 16, 32, 64) for precise trade."
            )
        ),
        CulturalArtifact(
            id = "art_banbhore_pottery",
            name = "Banbhore Glazed Ceramics",
            eraId = "era_soomra",
            eraName = "Soomra Dynasty",
            category = "Craft & Maritime Trade",
            briefSummary = "Polychrome glazed tableware and oil lamps excavated at ancient Banbhore port.",
            microLessonContent = "Banbhore was a bustling port city on the Indus delta. Excavations revealed thousands of glazed ceramic shards, ivory armlets, glass perfumes, and Chinese porcelain, showing Sindh's role as a vital nexus on the Maritime Silk Road.",
            keyFacts = listOf(
                "Silk Road Gateway: Linked sea traders from Guangzhou to Siraf and Basra.",
                "Glass & Pottery Kilns: Local workshops crafted blue lead-glazed jars.",
                "Ivory Trade: Unworked elephant tusks were imported and carved into delicate bangles."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_banbhore_pottery",
                questionText = "Which port city served as Sindh's primary Maritime Silk Road hub during the Soomra era?",
                options = listOf("Banbhore", "Gwadar", "Peshawar", "Multan"),
                correctAnswerIndex = 0,
                explanation = "Banbhore on the Indus delta was a thriving maritime trade port linking Asia, Arabia, and China."
            )
        ),
        CulturalArtifact(
            id = "art_ajrak_craft",
            name = "Sindhi Ajrak Block Printing",
            eraId = "era_talpur",
            eraName = "Kalhora & Talpur Era",
            category = "Textile & Craft",
            briefSummary = "A 16-step natural dyeing cloth tradition featuring geometric indigo, madder-red, and white star motifs.",
            microLessonContent = "Ajrak is a symbol of Sindhi identity, respect, and hospitality. Made from natural indigo, madder root, tamarind seed paste, and acacia gum, wooden blocks are stamped onto cotton cloth in a mirror-image geometric symmetry on both sides. The name originates from 'Azrak' (Arabic for blue).",
            keyFacts = listOf(
                "16 Steps: Requires washing, resist-stamping, indigo dipping, and sun baking over several days.",
                "Double-Sided: Master block-printers stamp both sides so patterns align perfectly.",
                "Cultural Gift: Presented to guests as a token of highest honor and respect."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_ajrak_craft",
                questionText = "What two primary natural dye colors give traditional Sindhi Ajrak its iconic palette?",
                options = listOf("Indigo Blue and Madder Red", "Yellow and Purple", "Green and Black", "Pink and Gold"),
                correctAnswerIndex = 0,
                explanation = "Traditional Ajrak gets its distinctive colors from deep Indigo Blue (Azrak) and rich Madder Red dyes."
            ),
            hotspots = listOf(
                ArtifactHotspot(
                    id = "hp_star_motifs",
                    title = "Geometric Star Motifs",
                    xPercent = 0.50f,
                    yPercent = 0.35f,
                    significance = "Astral Geometry",
                    detailNote = "Carved teakhazari wooden blocks align rosettes and eight-pointed stars representing the clear desert cosmos."
                ),
                ArtifactHotspot(
                    id = "hp_indigo_dye",
                    title = "Natural Indigo Dye",
                    xPercent = 0.30f,
                    yPercent = 0.70f,
                    significance = "Fermented Plant Dye",
                    detailNote = "Submerged in stone fermentation vats filled with Indigofera tinctoria, resulting in intense dark blue hues that never fade."
                ),
                ArtifactHotspot(
                    id = "hp_madder_red",
                    title = "Madder Root Crimson",
                    xPercent = 0.70f,
                    yPercent = 0.70f,
                    significance = "Rubia tinctorum Root",
                    detailNote = "Boiled with tamarind seed extract and camel dung mordant to fix the deep earthy crimson red into cotton fibers."
                )
            )
        ),
        CulturalArtifact(
            id = "art_makli_carvings",
            name = "Makli Hill Stone Reliefs",
            eraId = "era_talpur",
            eraName = "Kalhora & Talpur Era",
            category = "Architecture & Carving",
            briefSummary = "Intricate sandstone floral and calligraphic stone carvings at Makli UNESCO World Heritage Site.",
            microLessonContent = "Makli Hill near Thatta contains over 500,000 tombs constructed between the 14th and 18th centuries. Yellow sandstone mausoleums feature lace-like relief carvings depicting floral vines, geometric stars, armor, and Sindhi calligraphic verses without applying mortar.",
            keyFacts = listOf(
                "UNESCO World Heritage Site: One of the largest necropolises in the entire world.",
                "Lace in Stone: Stone masons carved intricate patterns up to several inches deep.",
                "Fusion Architecture: Blends Hindu, Islamic, and Gujarati structural motifs."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_makli_carvings",
                questionText = "What type of local stone was primarily used for the lace-like mausoleums at Makli Necropolis?",
                options = listOf("Yellow Sandstone", "Black Basalt", "White Marble", "Red Brick"),
                correctAnswerIndex = 0,
                explanation = "Masons hand-carved golden-yellow sandstone quarried nearby into intricate floral and geometric reliefs."
            ),
            hotspots = listOf(
                ArtifactHotspot(
                    id = "hp_floral_vine",
                    title = "Floral Tendril Carving",
                    xPercent = 0.45f,
                    yPercent = 0.45f,
                    significance = "Lace in Stone",
                    detailNote = "Master stone masons used fine iron chisels to undercut lotus vines and arabesques 3 inches deep into sandstone."
                ),
                ArtifactHotspot(
                    id = "hp_thuluth_script",
                    title = "Calligraphic Inscriptions",
                    xPercent = 0.50f,
                    yPercent = 0.15f,
                    significance = "Epigraphic Masterwork",
                    detailNote = "Verses of Quranic Thuluth and Persian poetry carved in raised relief framing entry arches."
                )
            )
        ),
        CulturalArtifact(
            id = "art_sukkur_barrage",
            name = "Sukkur Barrage Canal Model",
            eraId = "era_british",
            eraName = "British Colonial Era",
            category = "Civil Engineering",
            briefSummary = "A monumental 66-arch Indus River barrage controlling the world's largest contiguous canal system.",
            microLessonContent = "Completed in 1932 at the Sukkur gorge, this yellow limestone barrage spans 1,524 meters. Seven massive main canals divert river water across millions of acres, transforming arid desert plains into fertile agricultural heartlands.",
            keyFacts = listOf(
                "66 Sluice Gates: Each gate weighs 50 tons and controls river velocity.",
                "7 Canals: Canals like the Nara Canal are wider than the Suez Canal.",
                "Agricultural Engine: Created Sindh's breadbasket for wheat, rice, and cotton."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_sukkur_barrage",
                questionText = "How many main sluice gate arches comprise the main Sukkur Barrage across the Indus?",
                options = listOf("66 Arches", "12 Arches", "100 Arches", "30 Arches"),
                correctAnswerIndex = 0,
                explanation = "The Sukkur Barrage features 66 steel radial sluice gate arches spanning 1,524 meters across the Indus."
            )
        ),
        CulturalArtifact(
            id = "art_alghoza_music",
            name = "Alghoza & Tambooro Instruments",
            eraId = "era_modern",
            eraName = "Modern Sindh",
            category = "Sufi Music & Heritage",
            briefSummary = "The twin-flute Alghoza and 5-stringed Tambooro used in Sindhi Sufi folk music.",
            microLessonContent = "The Alghoza is a double flute played simultaneously: one pipe provides a continuous drone sound while the other plays intricate melodies using circular breathing techniques. Shah Abdul Latif Bhittai modified the 5-stringed Tambooro to accompany Sindhi Sufi poetry.",
            keyFacts = listOf(
                "Circular Breathing: Musicians inhale through nose while expelling air through cheeks endlessly.",
                "Sufi Sacred Music: Played nightly at the shrine of Shah Abdul Latif Bhittai in Bhit Shah.",
                "Woodcraft: Handmade from local rosewood and brass fittings."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_alghoza_music",
                questionText = "What specialized breathing technique enables Alghoza flute masters to play non-stop melodies?",
                options = listOf("Circular Breathing", "Hyperventilation", "Pranayama", "Rhythmic Sighing"),
                correctAnswerIndex = 0,
                explanation = "Circular breathing allows the performer to maintain continuous sound by inhaling through the nose while playing."
            ),
            hotspots = listOf(
                ArtifactHotspot(
                    id = "hp_drone_pipe",
                    title = "Male Drone Flute",
                    xPercent = 0.35f,
                    yPercent = 0.40f,
                    significance = "Continuous Harmonic Base",
                    detailNote = "Emits a sustained low pitch drone that provides the tonal anchor for Sindhi folk Surs."
                ),
                ArtifactHotspot(
                    id = "hp_melody_pipe",
                    title = "Female Melody Flute",
                    xPercent = 0.65f,
                    yPercent = 0.40f,
                    significance = "Virtuoso Finger Holes",
                    detailNote = "Equipped with 6 precision-spaced finger holes for executing rapid microtonal trills."
                )
            )
        ),
        CulturalArtifact(
            id = "art_indus_dolphin",
            name = "Indus Blind River Dolphin (Bhulan)",
            eraId = "era_modern",
            eraName = "Modern Sindh",
            category = "Ecological Heritage",
            briefSummary = "An ancient blind freshwater dolphin species unique to the Indus River basin.",
            microLessonContent = "Locally known as 'Bhulan', the Indus River Dolphin has lived in the Indus waters for millions of years. Having adapted to murky, silt-laden river currents, it is functionally blind and navigates exclusively using advanced echolocation. Protected sanctuaries exist between Sukkur and Guddu Barrages.",
            keyFacts = listOf(
                "Echolocation Master: Uses high-frequency sound pulses to navigate turbid river waters.",
                "Side-Swimmer: Swims on its side in shallow river channels.",
                "Endangered Treasure: Protected by Sindhi wildlife conservators and local fishing communities."
            ),
            quizQuestion = QuizQuestion(
                id = "quiz_indus_dolphin",
                questionText = "How does the Indus Blind River Dolphin (Bhulan) navigate muddy river waters?",
                options = listOf("Advanced Echolocation (Sonar)", "Infrared Vision", "Magnetic Compass", "Thermal Sensing"),
                correctAnswerIndex = 0,
                explanation = "The dolphin emits continuous ultrasound clicks and reads returning echoes to hunt and navigate in muddy waters."
            )
        )
    )

    fun getAllPronunciationTerms(): List<PronunciationTerm> = listOf(
        PronunciationTerm(
            id = "term_ajrak",
            term = "Ajrak",
            phonetic = "Uhj-RUHK (أجرڪ)",
            translation = "Block-printed Indigo & Crimson Shawl",
            culturalContext = "From Arabic 'Azrak' (Blue). Symbol of honor, warmth, and Sindhi cultural identity."
        ),
        PronunciationTerm(
            id = "term_kashi",
            term = "Kashi",
            phonetic = "KAH-shee (ڪاشي)",
            translation = "Turquoise Glazed Ceramic Tiles",
            culturalContext = "Traditional glazed blue tile architecture adorning historical mosques, tombs, and shrines."
        ),
        PronunciationTerm(
            id = "term_ranikot",
            term = "Ranikot",
            phonetic = "RAH-nee-kote (رني ڪوٽ)",
            translation = "The Great Wall of Sindh",
            culturalContext = "Vast stone fortress with a 32-kilometer perimeter in the Kirthar Mountains."
        ),
        PronunciationTerm(
            id = "term_alghoza",
            term = "Alghoza",
            phonetic = "Ahl-GHO-zah (الغوزو)",
            translation = "Twin-Pipe Folk Flute",
            culturalContext = "Double wooden flute played simultaneously using circular breathing techniques."
        ),
        PronunciationTerm(
            id = "term_mohaana",
            term = "Mohaana",
            phonetic = "Mo-HAH-nah (موهاڻا)",
            translation = "Indus Riverboat People",
            culturalContext = "Indigenous boat dwellers of Manchar Lake, descendants of ancient Indus river mariners."
        ),
        PronunciationTerm(
            id = "term_sufi_kalaam",
            term = "Shah Jo Raag",
            phonetic = "SHAH-jo-RAAG (شاهه جو راڳ)",
            translation = "Sufi Musical Recitation",
            culturalContext = "Poetic verses of Shah Abdul Latif Bhittai performed at dusk with 5-stringed Tambooro."
        )
    )

    fun getAllLandmarks(): List<Landmark> = listOf(
        Landmark(
            id = "lm_mohenjo",
            name = "Mohenjo-Daro Citadel",
            eraId = "era_indus_valley",
            region = "Larkana District",
            shortSummary = "UNESCO site featuring Great Bath, Stupa Mound, and grid layout.",
            mapX = 0.32f,
            mapY = 0.28f
        ),
        Landmark(
            id = "lm_sukkur",
            name = "Sukkur Indus Gorge & Barrage",
            eraId = "era_british",
            region = "Sukkur District",
            shortSummary = "Engineering masterpiece & Sadh Belo river island shrine.",
            mapX = 0.52f,
            mapY = 0.22f
        ),
        Landmark(
            id = "lm_sehwan",
            name = "Sehwan Sharif",
            eraId = "era_modern",
            region = "Jamshoro District",
            shortSummary = "Historic shrine of Lal Shahbaz Qalandar and ancient fort ruins.",
            mapX = 0.38f,
            mapY = 0.48f
        ),
        Landmark(
            id = "lm_hyderabad",
            name = "Hyderabad Citadel (Pacco Qillo)",
            eraId = "era_talpur",
            region = "Hyderabad District",
            shortSummary = "Historic capital citadel built by Kalhora and Talpur rulers.",
            mapX = 0.45f,
            mapY = 0.68f
        ),
        Landmark(
            id = "lm_makli",
            name = "Makli Hill & Thatta",
            eraId = "era_talpur",
            region = "Thatta District",
            shortSummary = "Vast sandstone necropolis and Shah Jahan Mosque tilework.",
            mapX = 0.36f,
            mapY = 0.82f
        ),
        Landmark(
            id = "lm_banbhore",
            name = "Banbhore Maritime Port",
            eraId = "era_soomra",
            region = "Indus Delta, Thatta",
            shortSummary = "Ancient coastal port city and Silk Road maritime gateway.",
            mapX = 0.28f,
            mapY = 0.88f
        ),
        Landmark(
            id = "lm_karachi",
            name = "Karachi Port & Saddar",
            eraId = "era_british",
            region = "Karachi Division",
            shortSummary = "Empress Market, Frere Hall, and Arabian Sea harbor.",
            mapX = 0.18f,
            mapY = 0.86f
        ),
        Landmark(
            id = "lm_thar",
            name = "Tharparkar Desert & Nagarparkar",
            eraId = "era_modern",
            region = "Tharparkar District",
            shortSummary = "Karoonjhar mountains, Jain temples, and colorful mirrorwork crafts.",
            mapX = 0.82f,
            mapY = 0.78f
        )
    )
}
