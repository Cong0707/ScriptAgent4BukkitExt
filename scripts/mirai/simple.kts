package mirai

globalEventChannel().subscribeGroupMessages {
    contains("hello", true).reply {
        QuoteReply(message) + "你好" + At(sender)
    }
    content { message.filterIsInstance<PlainText>().any { it.content.contains("欢迎新人") } }
        .reply { message.firstIsInstanceOrNull<At>()?.let { PlainText("欢迎") + it } ?: "欢迎新人!" }
}