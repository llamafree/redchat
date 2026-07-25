package com.llama.redchat.command

sealed class CommandResult {
    data class JoinChannel(val channelName: String) : CommandResult()
    data class SendPrivateMessage(val targetUser: String, val messageText: String) : CommandResult()
    object ListWho : CommandResult()
    object ListChannels : CommandResult()
    data class BlockUser(val targetUser: String) : CommandResult()
    data class UnblockUser(val targetUser: String) : CommandResult()
    object ClearChat : CommandResult()
    data class SetChannelPassword(val password: String) : CommandResult()
    data class TransferOwnership(val targetUser: String) : CommandResult()
    object SaveRetention : CommandResult()
    data class Error(val message: String) : CommandResult()
    object NotACommand : CommandResult()
}

object SlashCommandParser {

    fun parse(input: String): CommandResult {
        val trimmed = input.trim()
        if (!trimmed.startsWith("/")) return CommandResult.NotACommand

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()

        return when (cmd) {
            "/j", "/join" -> {
                val channel = parts.getOrNull(1)?.removePrefix("#")
                if (channel.isNullOrBlank()) CommandResult.Error("Uso: /j #canal")
                else CommandResult.JoinChannel(channel)
            }
            "/m", "/msg" -> {
                if (parts.size < 3) CommandResult.Error("Uso: /m @usuario mensaje")
                else {
                    val user = parts[1].removePrefix("@")
                    val text = parts.subList(2, parts.size).joinToString(" ")
                    CommandResult.SendPrivateMessage(user, text)
                }
            }
            "/w", "/who" -> CommandResult.ListWho
            "/channels" -> CommandResult.ListChannels
            "/block" -> {
                val user = parts.getOrNull(1)?.removePrefix("@")
                if (user.isNullOrBlank()) CommandResult.Error("Uso: /block @usuario")
                else CommandResult.BlockUser(user)
            }
            "/unblock" -> {
                val user = parts.getOrNull(1)?.removePrefix("@")
                if (user.isNullOrBlank()) CommandResult.Error("Uso: /unblock @usuario")
                else CommandResult.UnblockUser(user)
            }
            "/clear" -> CommandResult.ClearChat
            "/pass" -> {
                val pass = parts.getOrNull(1)
                if (pass.isNullOrBlank()) CommandResult.Error("Uso: /pass contraseña")
                else CommandResult.SetChannelPassword(pass)
            }
            "/transfer" -> {
                val user = parts.getOrNull(1)?.removePrefix("@")
                if (user.isNullOrBlank()) CommandResult.Error("Uso: /transfer @usuario")
                else CommandResult.TransferOwnership(user)
            }
            "/save" -> CommandResult.SaveRetention
            else -> CommandResult.Error("Comando desconocido: $cmd. Escribe / para ver comandos válidos.")
        }
    }
}
