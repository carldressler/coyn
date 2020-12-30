package de.carldressler.coyn.commands.transactions

import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.entities.CoynBalance
import de.carldressler.coyn.entities.CoynGuild
import de.carldressler.coyn.handlers.ErrorHandler
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object AdminTransaction : Command {
    class Context(val event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        private val giveCommands = setOf("give", "g", "silentgive", "sg")
        private val setCommands = setOf("set", "s", "silentset", "s")
        private val silentCommands = setOf("silentgive", "sg", "silentset", "sset", "ss", "silentremove", "srem", "sr")

        val targetMember: Member = event.message.mentionedMembers[0]
        val targetCurrency = CoynGuild(event.guild.id).currency
        val targetBalance = CoynBalance(targetMember.id, targetCurrency.id)
        val amountToShift = args[1].toInt()
        var intent = if (giveCommands.contains(command)) AdminIntent.ADD else if (setCommands.contains(command)) AdminIntent.SET else if (targetBalance.amount < amountToShift) AdminIntent.ZERO else AdminIntent.REMOVE
        val reason = if (args.size > 2) args.drop(2).joinToString(" ") else null
        val doNotify = !silentCommands.contains(command)
    }

    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        // TODO: Check whether user is allowed to issue command (missing currency_admins table)
        if (
            args.size < 2 ||
            event.message.mentionedUsers.isEmpty() ||
            !args[1].matches(Regex("^[1-9]|[1-9][0-9]{1,9}$"))
        ) return invalidUsage(event, command)

        val ctxt = Context(event, args, command)

        when (ctxt.intent) {
            AdminIntent.ADD -> addCoins(ctxt)
            AdminIntent.SET -> setCoins(ctxt)
            AdminIntent.ZERO -> setToZeroCoins(ctxt)
            AdminIntent.REMOVE -> removeCoins(ctxt)
        }

        if (ctxt.doNotify) notifyTarget(ctxt)
    }

    private fun addCoins(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ADMIN_COLOR)
            .setTitle("Added ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} to ${ctxt.targetMember.effectiveName}'s balance")
            .setDescription("""
                New balance: ${ctxt.targetBalance.amount + ctxt.amountToShift} ${ctxt.targetCurrency.symbol}
                Old balance: ${ctxt.targetBalance.amount} ${ctxt.targetCurrency.symbol}
                User ID: ${ctxt.targetMember.id}
                Reason: ${ctxt.reason ?: "None specified"}
                                
                ${if (ctxt.doNotify) "We will _attempt_ to notify the user about the event. If we cannot notify the user, there will be a follow-up in this channel." else "The user will not be notified about the event."}
            """.trimIndent())
            .setFooter(Constants.ADMIN_TOOLS)
            .build()

        ctxt.targetBalance.addCoins(ctxt.amountToShift)
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun setCoins(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ADMIN_COLOR)
            .setTitle("Set ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} as ${ctxt.targetMember.effectiveName}'s balance")
            .setDescription("""
                New balance: $ctxt.amountToShift ${ctxt.targetCurrency.symbol}
                Old balance: ${ctxt.targetBalance.amount} ${ctxt.targetCurrency.symbol}
                User ID: ${ctxt.targetMember.id}
                Reason: ${ctxt.reason ?: "None specified"}
                                
                ${if (ctxt.doNotify) "We will _attempt_ to notify the user about the event. If we cannot notify the user, there will be a follow-up in this channel." else "The user will not be notified about the event."}
            """.trimIndent())
            .setFooter(Constants.ADMIN_TOOLS)
            .build()

        ctxt.targetBalance.setCoins(ctxt.amountToShift)
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun setToZeroCoins(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ADMIN_COLOR)
            .setTitle("Set ${ctxt.targetMember.effectiveName}'s balance to 0 ${ctxt.targetCurrency.symbol}")
            .setDescription("""
                New balance: 0 ${ctxt.targetCurrency.symbol}
                Old balance: ${ctxt.targetBalance.amount} ${ctxt.targetCurrency.symbol}
                User ID: ${ctxt.targetMember.id}
                Reason: ${ctxt.reason ?: "None specified"}
                                
                ${if (ctxt.doNotify) "We will _attempt_ to notify the user about the event. If we cannot notify the user, there will be a follow-up in this channel." else "The user will not be notified about the event."}
            """.trimIndent())
            .setFooter(Constants.ADMIN_TOOLS)
            .build()

        ctxt.targetBalance.setCoins(0)
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun removeCoins(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ADMIN_COLOR)
            .setTitle("Removed ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} from ${ctxt.targetMember.effectiveName}'s balance")
            .setDescription("""
                New balance: ${ctxt.targetBalance.amount - ctxt.amountToShift} ${ctxt.targetCurrency.symbol}
                Old balance: ${ctxt.targetBalance.amount} ${ctxt.targetCurrency.symbol}
                User ID: ${ctxt.targetMember.id}
                Reason: ${ctxt.reason ?: "None specified"}
                
                ${if (ctxt.doNotify) "We will _attempt_ to notify the user about the event. If we cannot notify the user, there will be a follow-up in this channel." else "The user will not be notified about the event."}
            """.trimIndent())
            .setFooter(Constants.ADMIN_TOOLS)
            .build()

        ctxt.targetBalance.removeCoins(ctxt.amountToShift)
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun notifyTarget(ctxt: Context) {
        val embed = when (ctxt.intent) {
            AdminIntent.ADD -> {
                EmbedBuilder()
                    .setColor(Constants.ADMIN_COLOR)
                    .setTitle("An admin added ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} to your balance")
                    .setDescription("""
                        A currency admin of **${ctxt.targetCurrency.name}** (Guild: ${ctxt.event.guild.name}) added **${ctxt.amountToShift} ${ctxt.targetCurrency.symbol}** to your balance.
                        
                        ${if (ctxt.reason != null) "Reason: ${ctxt.reason}" else "A reason was not specified."}
                        
                        The coins are freely available to you and can be used on any server that uses **${ctxt.targetCurrency.name}**.
                    """.trimIndent())
                    .setFooter(Constants.ADMIN_TOOLS)
                    .build()
            }
            AdminIntent.SET -> {
                EmbedBuilder()
                    .setColor(Constants.ADMIN_COLOR)
                    .setTitle("An admin set ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} as your balance")
                    .setDescription("""
                        A currency admin of **${ctxt.targetCurrency.name}** (Guild: ${ctxt.event.guild.name}) changed your balance to **${ctxt.amountToShift} ${ctxt.targetCurrency.symbol}**.
                        
                        ${if (ctxt.reason != null) "Reason: ${ctxt.reason}" else "A reason was not specified."}
                        
                        The coins are freely available to you and can be used on any server that uses **${ctxt.targetCurrency.name}**.
                    """.trimIndent())
                    .setFooter(Constants.ADMIN_TOOLS)
                    .build()
            }
            AdminIntent.ZERO -> {
                EmbedBuilder()
                    .setColor(Constants.ADMIN_COLOR)
                    .setTitle("An admin set your ${ctxt.targetCurrency.symbol} balance to zero")
                    .setDescription("""
                        A currency admin of **${ctxt.targetCurrency.name}** (Guild: ${ctxt.event.guild.name}) set your ${ctxt.targetCurrency.symbol} balance to zero.
                        
                        ${if (ctxt.reason != null) "Reason: ${ctxt.reason}" else "A reason was not specified."}
                    """.trimIndent())
                    .setFooter(Constants.ADMIN_TOOLS)
                    .build()
            }
            AdminIntent.REMOVE -> {
                EmbedBuilder()
                    .setColor(Constants.ADMIN_COLOR)
                    .setTitle("An admin removed ${ctxt.amountToShift} ${ctxt.targetCurrency.symbol} from your balance")
                    .setDescription("""
                        A currency admin of **${ctxt.targetCurrency.name}** (Guild: ${ctxt.event.guild.name}) removed **${ctxt.amountToShift} ${ctxt.targetCurrency.symbol}** from your balance.
                        
                        ${if (ctxt.reason != null) "Reason: ${ctxt.reason}" else "A reason was not specified."}
                    """.trimIndent())
                    .setFooter(Constants.ADMIN_TOOLS)
                    .build()
            }
        }

        ctxt.targetMember.user.openPrivateChannel()
            .flatMap { channel -> channel.sendMessage(embed) }
            .queue({}, { ErrorHandler.sendDMsClosed(ctxt.event.channel, ctxt.targetMember.effectiveName) })
    }


    private fun invalidUsage(event: GuildMessageReceivedEvent, command: String) {
        ErrorHandler.sendInvalidArgumentsError(event.channel, command, """
            (1) @mention of payment recipient 
            (2) payment amount (max one hundred million - 1) 
        """.trimIndent())
    }
}