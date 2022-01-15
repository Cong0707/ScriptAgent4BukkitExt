package coreLibrary.lib.util

import coreLibrary.lib.PlaceHoldString
import coreLibrary.lib.with
import kotlin.math.ceil


fun calPage(page: Int, prePage: Int, size: Int): Pair<Int, Int> {
    val totalPage = ceil(size / prePage.toDouble()).toInt()
    //note: totalPage may be 0 (less than 1), so can't use coerceIn
    val newPage = page.coerceAtMost(totalPage).coerceAtLeast(1)
    return newPage to totalPage
}

fun <E> menu(title: String, list: List<E>, page: Int, prePage: Int, handle: (E) -> PlaceHoldString): PlaceHoldString {
    val (newPage, totalPage) = calPage(page, prePage, list.size)
    val list2 = list.subList((newPage - 1) * prePage, (newPage * prePage).coerceAtMost(list.size))
        .map(handle)
    return """
            | [green]==== [white]{title}[green] ====
            | {list:${"\n"}}
            | [green]==== [white]{page}/{total}[green] ====
            """.trimMargin().with("title" to title, "list" to list2, "page" to newPage, "total" to totalPage)
}