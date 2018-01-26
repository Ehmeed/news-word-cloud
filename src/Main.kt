
import org.jsoup.Jsoup





fun loadData(): List<Pair<String, List<String>>> {
    println("Loading data..")
    val sites = mutableListOf<Pair<String, List<String>>>()
    sites.add(Pair("https://www.novinky.cz/", listOf("a")))
    //TODO
    return sites
}

fun processData(sites: List<Pair<String, List<String>>>) {
    val words = mutableListOf<String>()
    for(site in sites){
        for(selector in site.second) {
            words.addAll(getHeadlinesBySelector(site.first, selector))
        }
    }
    words.forEach(::println)
}

fun getHeadlinesBySelector(site: String, selector: String): List<String> {
    val words = mutableListOf<String>()
    val doc = Jsoup.connect(site).get()
    val newsHeadlines = doc.select(selector)
    for (headline in newsHeadlines) {
        words.addAll(headline.absUrl("href").substringAfterLast("/")
                .split("-")
                .filter { !it.contains("mailto") }
                .filter { it.isNotEmpty()}
        )
    }
    return words
}


fun main(args: Array<String>){
    val sites = loadData()
    println("Processing..")
    processData(sites)


}