
import org.jsoup.Jsoup





fun loadData(): List<Pair<String, List<String>>> {
    println("Loading data..")
    val sites = mutableListOf<Pair<String, List<String>>>()
    sites.add(Pair("https://www.novinky.cz/", listOf("a")))
    sites.add(Pair("https://www.parlamentnilisty.cz/", listOf("a")))
    sites.add(Pair("http://forum24.cz/", listOf("a")))
    sites.add(Pair("https://cz.sputniknews.com/", listOf("a")))
    sites.add(Pair("https://www.aktualne.cz", listOf("a")))
    sites.add(Pair("http://www.blesk.cz/zpravy", listOf("a")))
    sites.add(Pair("https://www.idnes.cz/", listOf("a")))
    sites.add(Pair("https://www.seznamzpravy.cz/", listOf("a")))
    sites.add(Pair("https://www.lidovky.cz/", listOf("a")))
    //TODO
    return sites
}

fun processData(sites: List<Pair<String, List<String>>>) : MutableList<Pair<String, List<String>>> {
    val words = mutableListOf<String>()
    val wordsListBySite = mutableListOf<Pair<String, List<String>>>()
    for(site in sites){
        for(selector in site.second) {
            words.addAll(getHeadlinesBySelector(site.first, selector))
        }
        wordsListBySite.add(Pair(site.first, words.toMutableList()))
        words.removeAll(words)
    }
    return wordsListBySite
}

fun getHeadlinesBySelector(site: String, selector: String): List<String> {
    val words = mutableListOf<String>()
    val doc = Jsoup.connect(site).get()
    val newsHeadlines = doc.select(selector)
    for (headline in newsHeadlines) {
        words.addAll(headline.absUrl("href").parseHeadlineFromUrl()
                .split("-")
                .filter { it.isNotEmpty()}
                .filter { it.length > 2 }
                .filter { !it.contains("www") }
                .filter { !it.contains("mailto") }
                .filter { !it.contains("#") }
                .filter { !it.contains("?") }
                .filter { !it.contains("%") }
                .filter { !it.contains(".") }
                .filter { !it.contains("novinky", true) }
                .filter { !it.contains("forum", true) }
                .filter { !it.contains("sez", true) }
                .filter { !it.contains("aktualne", true) }
                .filter { !it.contains("idnes", true) }
                .filter { !it.contains("lidovky", true) }
                .filter { !it.matches(".*\\d+.*".toRegex()) }
                .map { it.toLowerCase() }
                .map { if(it.contains(".html")) it.substringBefore(".html") else it }
        )
    }
    return words
}

private fun String.parseHeadlineFromUrl(): String  {
    if(this.contains("aktualne")){
        return this.substringBeforeLast("/").substringBeforeLast("/").substringAfterLast("/")
    }else if(this.contains("idnes")){
        return this.substringBeforeLast("/").substringAfterLast("/")
    }else if(this.contains("lidovky")){
        return this.substringBeforeLast("/").substringAfterLast("/")
    }else if(this.substringAfterLast("/").isEmpty()
            && this.isNotEmpty()
            && this.contains("-")
            && !this.contains("novinky")){
        return this.substring(0, this.length - 1).substringAfterLast("/")
    }
    return this.substringAfterLast("/")

}

fun main(args: Array<String>){
    val sites = loadData()
    println("Processing..")
    val wordsBySite: MutableList<Pair<String, List<String>>> = processData(sites)
    val siteWordMap = mutableListOf<Pair<String, Map<String, Int>>>()
    for(wordList in wordsBySite){
        val occurrenceMap: Map<String, Int> = wordList.second.groupingBy { it }
                .eachCount()
                .toList()
                .sortedBy { (_, value) -> -value }
                .toMap()
        siteWordMap.add(Pair(wordList.first, occurrenceMap))
    }

    siteWordMap.forEach(::println)
    println("-------------SUMMARY---------------")
    val summary: MutableMap<String, Int> = mutableMapOf()
    for(siteValues in siteWordMap){
        for(wordList in siteValues.second){
            summary[wordList.key] = summary.getOrDefault(wordList.key, 0) + wordList.value
        }
    }
    summary.toList().sortedBy { (_, v) -> -v }
            .toMap()
            .forEach { print(it.toString() + ",") }
}