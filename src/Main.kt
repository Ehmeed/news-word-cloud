
import org.jsoup.Jsoup





fun loadData(): List<Pair<String, List<String>>> {
    println("Loading data..")
    val sites = mutableListOf<Pair<String, List<String>>>()
    sites.add(Pair("https://www.novinky.cz/", listOf("a")))
    sites.add(Pair("https://www.parlamentnilisty.cz/", listOf("a")))
    sites.add(Pair("http://forum24.cz/", listOf("a")))

    //TODO
    return sites
}

fun processData(sites: List<Pair<String, List<String>>>) : MutableList<String>{
    val words = mutableListOf<String>()
    for(site in sites){
        for(selector in site.second) {
            words.addAll(getHeadlinesBySelector(site.first, selector))
        }
    }
    return words
}

fun getHeadlinesBySelector(site: String, selector: String): List<String> {
    val words = mutableListOf<String>()
    val doc = Jsoup.connect(site).get()
    val newsHeadlines = doc.select(selector)
    for (headline in newsHeadlines) {
        words.addAll(headline.absUrl("href").substringAfterLast("/")
                .split("-")
                .filter { it.isNotEmpty()}
                .filter { it.length > 2 }
                .filter { !it.contains("www") }
                .filter { !it.contains("mailto") }
                .filter { !it.contains("#") }
                .filter { !it.contains("?") }
                .filter { !it.contains("novinky", true) }
                .filter { !it.matches("-?\\d+(\\.\\d+)?".toRegex()) }
                .map { if(it.contains(".html")) it.substringBefore(".html") else it }
        )
    }
    return words
}


fun main(args: Array<String>){
    val sites = loadData()
    println("Processing..")
    val words = processData(sites)
    val occurrenceMap: Map<String, Int> = words.groupingBy { it }
            .eachCount()
            .toList()
            .sortedBy { (_, value) -> value }
            .toMap()
    occurrenceMap.forEach(::println)
}