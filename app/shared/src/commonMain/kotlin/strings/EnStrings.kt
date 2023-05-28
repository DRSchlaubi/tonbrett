package dev.schlaubi.tonbrett.app.strings

import cafe.adriel.lyricist.LyricistStrings

@LyricistStrings(languageTag = "en", default = true)
val EnStrings = Strings(
    reload = "Reload",
    crashedExplainer = "The server connection was closed unexpectedly, this likely means that your internet connection was closed or you opened an additional tab",
    botOfflineExplainer = "The bot is currently not connected to a voice channel, please use /join to make it join your channel",
    wrongChannelExplainer = "The bot is currently in another voice channel, either join that channel or make it join your channel",
    noSounds = "There are no sounds in this sad world :(",
    offline = "You are currently not connected to a voice channel",
    sessionExpiredExplainer = "Your session expired! Please sign in again",
    reAuthorize = "Re-login",
    searchExplainer = "Search for sounds",
    search = "Search",
    onlineMine = "Only mine",
    searchByTag = "Search by tag",
    searchByName = "Search by name",
    searchByDescription = "Search by description",
    searchOptions = "Search options",
    enterToAdd = "Press enter to add"
)
