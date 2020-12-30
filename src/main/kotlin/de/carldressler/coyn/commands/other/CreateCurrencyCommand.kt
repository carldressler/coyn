package de.carldressler.coyn.commands.other

import de.carldressler.coyn.Bot
import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.database.CurrencyHandler
import de.carldressler.coyn.entities.CoynGuild
import de.carldressler.coyn.handlers.ProcessHandler
import de.carldressler.coyn.handlers.ProcessHandler.engageUser
import de.carldressler.coyn.handlers.ProcessHandler.releaseUser
import de.carldressler.coyn.handlers.ProcessHandler.userMayStartProcess
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.TimeUnit

object CreateCurrencyCommand : Command {
    private lateinit var currencyName: String
    private lateinit var currencySymbol: String
    private var isCompetitive: Boolean = false
    private lateinit var originEvent: GuildMessageReceivedEvent

    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        originEvent = event
        if (!userMayStartProcess(event)) return
        engageUser(event.author.id)

        s1SendEmbed()
    }

    private fun s1SendEmbed() {
        originEvent.channel.sendMessage(setNameEmbed).queue()
        s1AwaitName()
    }

    private fun s1AwaitName() {
        Bot.eventWaiter.waitForEvent(
            GuildMessageReceivedEvent::class.java,
            { caughtEvent ->
                caughtEvent.author.id == originEvent.author.id &&
                        caughtEvent.channel.id == originEvent.channel.id
            },
            { caughtEvent ->
                println(caughtEvent.message.contentStripped)
                val name = caughtEvent.message.contentRaw
                if (!isValidName(name)) {
                    s1AwaitName()
                    return@waitForEvent
                }
                currencyName = name
                s2SendEmbed()
            },
            5, TimeUnit.MINUTES,
            { ProcessHandler.timeout(originEvent) }
        )
    }

    private fun s2SendEmbed() {
        originEvent.channel.sendMessage(setSymbolEmbed).queue()
        s2AwaitSymbol()
    }

    private fun s2AwaitSymbol() {
        Bot.eventWaiter.waitForEvent(
            GuildMessageReceivedEvent::class.java,
            { caughtEvent ->
                caughtEvent.author.id == originEvent.author.id &&
                        caughtEvent.channel.id == originEvent.channel.id
            }, { caughtEvent ->
                val symbol = caughtEvent.message.contentRaw
                if (!isValidSymbol(symbol)) {
                    s2AwaitSymbol()
                    return@waitForEvent
                }
                currencySymbol = symbol
                s3SendEmbed()
            },
            5, TimeUnit.MINUTES,
            { ProcessHandler.timeout(originEvent) }
        )
    }

    private fun s3SendEmbed() {
        originEvent.channel.sendMessage(setCompetitiveEmbed).queue { message ->
            val actions = mutableListOf<RestAction<Void>>()

            actions.add(message.addReaction("\uD83C\uDDF3"))
            actions.add(message.addReaction("\uD83C\uDDE8"))
            RestAction.allOf(actions).queue()
            s3AwaitReaction(message)
        }
    }

    private fun s3AwaitReaction(message: Message) {
        Bot.eventWaiter.waitForEvent(
            GuildMessageReactionAddEvent::class.java,
            { caughtEvent ->
                caughtEvent.user.id == originEvent.author.id &&
                        caughtEvent.messageId == message.id &&
                        (caughtEvent.reactionEmote.name == "\uD83C\uDDF3" ||
                        caughtEvent.reactionEmote.name == "\uD83C\uDDE8")
            }, { caughtEvent ->
                isCompetitive = caughtEvent.reaction.reactionEmote.name == "\uD83C\uDDE8"
                s4SendEmbed()
            },
            5, TimeUnit.MINUTES,
            { ProcessHandler.timeout(originEvent) }
        )
    }

    private fun s4SendEmbed() {
        val summary = EmbedBuilder()
            .setColor(Constants.ACCENT_COLOR)
            .setTitle("This looks promising!")
            .setDescription("""
                You did it. Please check this information for correctness. If everything is correct, you can create your currency with the tick reaction. Alternatively you can cancel the creation with the cross reaction.
            """.trimIndent())
            .addField(
                "Currency name",
                currencyName,
                true
            ).addField(
                "Currency symbol",
                currencySymbol,
                true
            ).addField(
                "Competitive mode",
                if (isCompetitive) "activated" else "deactivated",
                true
            ).build()
        originEvent.channel.sendMessage(summary).queue { message ->
            val actions = mutableListOf<RestAction<Void>>()

            actions.add(message.addReaction("✅"))
            actions.add(message.addReaction("🚫"))
            RestAction.allOf(actions).queue()
            s4AwaitReaction(message)
        }
    }

    private fun s4AwaitReaction(message: Message) {
        Bot.eventWaiter.waitForEvent(
            GuildMessageReactionAddEvent::class.java,
            { caughtEvent ->
                caughtEvent.user.id == originEvent.author.id &&
                        caughtEvent.messageId == message.id &&
                        (caughtEvent.reactionEmote.name == "✅" ||
                        caughtEvent.reactionEmote.name == "🚫")
            }, { caughtEvent ->
                if (caughtEvent.reactionEmote.name == "✅") {
                    val newCurrency = CurrencyHandler.registerCurrency(currencyName, currencySymbol, isCompetitive)
                    CoynGuild(originEvent.guild.id).updateCurrency(newCurrency)
                    originEvent.channel.sendMessage(currencyCreatedEmbed).queue()
                } else {
                    originEvent.channel.sendMessage(abortedEmbed).queue()
                }
                releaseUser(originEvent.author.id)
            },
            5, TimeUnit.MINUTES,
            { ProcessHandler.timeout(originEvent) }
        )
    }

    /*
     * ===== UTILITY FUNCTIONS =====
     */

    private fun isValidName(name: String): Boolean {
        return if (
            name.isEmpty() ||
            name.isBlank() ||
            name.contains("coyn", true) ||
            name.length > 32
        ) {
            originEvent.channel.sendMessage(invalidNameEmbed).queue()
            false
        } else {
            true
        }
    }

    private fun isValidSymbol(symbol: String): Boolean {
        return if (
            symbol.isEmpty() ||
            symbol.isBlank() ||
            symbol.contains("coyn", true) ||
            symbol.length > 8
        ) {
            originEvent.channel.sendMessage(invalidSymbolEmbed).queue()
            false
        } else {
            true
        }
    }

    /*
     * ===== EMBEDS =====
     */

    private val setNameEmbed = EmbedBuilder()
        .setColor(Constants.ACCENT_COLOR)
        .setTitle("Woah, a new currency?")
        .setDescription("""
            Only a few seconds separate you from your own currency. You are about to become the administrator of a new currency, and it is up to you how you want to run it.
            
            The currency is automatically set as this servers currency. If the server has a currency in place right now, it will be removed from the server (but not deleted). It can be reactivated again afterwards.
            
              • 32 characters maximum
                • Regular emoji are supported but count towards the char limit (e.g. \:slight_smile\:)
                • Custom emoji will not be displayed properly but as their name (e.g. \:KEKW\:)
            
            ➡ Please enter your desired name for your new currency now (you can change it later)
        """.trimIndent())
        .build()

    private val setSymbolEmbed = EmbedBuilder()
        .setColor(Constants.ACCENT_COLOR)
        .setTitle("Outstanding name!")
        .setDescription("""
            Please set a currency symbol now. What is a currency symbol you ask? The currency symbol is the character(s) that come after the number, for example 400€, ${'$'}750 or £3000 or the unified versions EUR, USD and GBP.

            • 8 characters maximum
                • Regular emoji are supported but count towards the char limit (e.g. \:slight_smile\:)
                • Custom emoji will not be displayed properly but as their name (e.g. \:KEKW\:)

            ➡ Please specify the currency symbol now (you can change it later)
        """.trimIndent())
        .build()

    private val setCompetitiveEmbed = EmbedBuilder()
        .setColor(Constants.ACCENT_COLOR)
        .setTitle("Competitive mode - choose wisely!")
        .setDescription("""
            Okay, this one is more important and NOT CHANGEABLE WITHOUT CONSEQUENCES, so think it through.

            If you enable the competitive mode for your currency, you can fight for supremacy on global leaderboards and your users can be sure that the numbers have not been manipulated (there is a badge), because: When a currency is competitive, administrators cannot create or take away money or otherwise manipulate it.
            
            This is important! You can always decide to switch from a competitive currency to a non-competitive currency without repercussions. However, if you choose to change a non-competitive currency (where manipulation may have occurred) to a competitive currency, ALL balances of ALL currency users will be reset.
        """.trimIndent())
        .addField(
            "Non-competitive mode", """
            ✅ admins can gift/remove coins
            ✅ admins can change all settings
            🚫 no participation on the leaderboard
            🚫 users don't know about currency integrity
        """.trimIndent(), true)
        .addField(
            "Competitive mode", """
            🚫 admins cannot gift/remove coins
            🚫 admins cannot change some settings
            ✅ fighting on the global leaderboard
            ✅ shiny badge to display integrity
        """.trimIndent(), true)
        .addBlankField(false)
        .addField(
            "Uncertain?",
            "Choose competitive mode for now, you can change to non-competitive without problems (not so vice versa).",
            false)
        .addField(
            "",
            "Choose \uD83C\uDDF3 for the non-competitive mode and \uD83C\uDDE8 for the competitive mode",
            false
        )
        .build()

    private val currencyCreatedEmbed = EmbedBuilder()
        .setColor(Constants.SUCCESS_COLOR)
        .setTitle("Currency created")
        .setDescription("""
            Congratulations! Your currency has just been created and assigned to this server.

            It is already fully functional, but you can customize it further. Some entry points would be these: 

              • Check out the settings using ${Constants.PREFIX}settings
              • Appoint a 2nd administrator in settings
              • Set the starting coins every new user gets in settings

            > Use `${Constants.PREFIX}help` to see all commands

            Welcome to the club, we are thrilled to have you! :)
        """.trimIndent())
        .build()

    private val abortedEmbed = EmbedBuilder()
        .setColor(Constants.ACCENT_COLOR)
        .setTitle("We're taking it slow")
        .setDescription("Currency creation was canceled at the request of the user. You can restart the process anytime.")
        .build()

    private val invalidNameEmbed = EmbedBuilder()
        .setColor(Constants.WARNING_COLOR)
        .setTitle("Invalid name!")
        .setDescription("""
                    The name was not approved because it violates one of these rules:

                      • It is longer than 32 characters
                        • Emoji codes (e.g. \:smile\:) count towards the limit!
                        • Custom emoji will display as their names (e.g. \:KEKW\:)
                      • It contains the term 'coyn'
                      • It is not text (e.g. a picture was sent)

                    ➡ Please adjust your name, then you can send it in and it will be checked again.
                """.trimIndent())
        .build()

    private val invalidSymbolEmbed = EmbedBuilder()
        .setColor(Constants.WARNING_COLOR)
        .setTitle("Invalid symbol!")
        .setDescription("""
                    The name was not approved because it violates one of these rules:

                      • It is longer than 8 characters
                        • Emoji codes (e.g. \:smile\:) count towards the limit!
                        • Custom emoji will display as their names (e.g. \:KEKW\:)
                      • It contains the term 'coyn'
                      • It is not text (e.g. a picture was sent)

                    ➡ Please adjust your symbol, then you can send it in and it will be checked again.
                """.trimIndent())
        .build()
}