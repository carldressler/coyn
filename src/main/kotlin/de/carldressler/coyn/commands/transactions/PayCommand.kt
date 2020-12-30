package de.carldressler.coyn.commands.transactions

import de.carldressler.coyn.commands.Command
import de.carldressler.coyn.entities.CoynBalance
import de.carldressler.coyn.entities.CoynGuild
import de.carldressler.coyn.entities.CoynUser
import de.carldressler.coyn.handlers.ErrorHandler
import de.carldressler.coyn.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object PayCommand : Command {
    class Context(val event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        private val silentCommands = setOf("silentpay", "spay", "sp")

        val currency = CoynGuild(event.guild.id).currency
        val invokerMember = event.member ?: throw Exception("PayCommand/Context")
        val targetMember = event.message.mentionedMembers[0]
        val invokerBalance = CoynBalance(invokerMember.id, currency.id)
        val targetBalance = CoynBalance(targetMember.id, currency.id)
        val amountToPay = args[1].toInt()
        val reason = if (args.size > 2) args.drop(2).joinToString(" ") else null
        val doNotify = !silentCommands.contains(command)
    }

    override fun run(event: GuildMessageReceivedEvent, args: List<String>, command: String) {
        val user = CoynUser(event.author.id)
        if (user.isBanned) return ErrorHandler.sendBannedProhibited(event.channel)
        if (event.member == null) return
        if (
            args.size < 2 ||
            event.message.mentionedMembers.isEmpty() ||
            !args[1].matches(Regex("^[1-9]|[1-9][0-9]{1,9}\$"))
        ) return invalidUsage(event, command)

        val ctxt = Context(event, args, command)
        if (ctxt.invokerMember.id == ctxt.targetMember.id) return sendCannotPayYourself(ctxt)
        if (ctxt.invokerBalance.amount < ctxt.amountToPay) return sendInsufficientFunds(ctxt)

        ctxt.invokerBalance.removeCoins(ctxt.amountToPay)
        ctxt.targetBalance.addCoins(ctxt.amountToPay)
        val embed = EmbedBuilder()
            .setColor(Constants.SUCCESS_COLOR)
            .setTitle("Payment sent")
            .setDescription("""
                The payment was confirmed and ${ctxt.targetMember.effectiveName} has received ${ctxt.amountToPay} ${ctxt.currency.symbol}.
                
                ${if (ctxt.reason != null) "Reason: ${ctxt.reason}\n" else "A reason was not specified."}
                ${if (ctxt.doNotify) "We will _attempt_ to notify the user about the payment. If we cannot notify the user, there will be a follow-up in this channel." else "The user will not be notified about the payment."}
                """.trimIndent())
            .build()
        ctxt.event.channel.sendMessage(embed).queue()

        if (ctxt.doNotify) notify(ctxt)
    }

    private fun notify(ctxt: Context) {
        val paymentNotification = EmbedBuilder()
            .setColor(Constants.ACCENT_COLOR)
            .setTitle("You received ${ctxt.amountToPay} ${ctxt.currency.symbol} from ${ctxt.targetMember.effectiveName}")
            .setDescription("""
                ${ctxt.targetMember.effectiveName} has paid you ${ctxt.amountToPay} ${ctxt.currency.symbol} (Guild: ${ctxt.event.guild.name}).
                
                ${if (ctxt.reason != null) "Reason: ${ctxt.reason}" else "The user did not specify a reason."}
                """.trimIndent())
            .build()

        ctxt.targetMember.user.openPrivateChannel()
            .flatMap { channel -> channel.sendMessage(paymentNotification) }
            .queue({}, { ErrorHandler.sendDMsClosed(ctxt.event.channel, ctxt.targetMember.effectiveName) })
    }


    private fun sendCannotPayYourself(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ERROR_COLOR)
            .setTitle("Payment declined")
            .setDescription("It is not possible to send money to yourself as this would not alter the balance.")
            .build()
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun sendInsufficientFunds(ctxt: Context) {
        val embed = EmbedBuilder()
            .setColor(Constants.ERROR_COLOR)
            .setTitle("Payment declined")
            .setDescription("The payment was rejected because the payment sender has insufficient ${ctxt.currency.symbol}. The payment sender can view his ${ctxt.currency.symbol} with the `${Constants.PREFIX}balance` command.")
            .build()
        ctxt.event.channel.sendMessage(embed).queue()
    }

    private fun invalidUsage(event: GuildMessageReceivedEvent, command: String) {
        ErrorHandler.sendInvalidArgumentsError(event.channel, command, """
            (1) @mention of payment recipient 
            (2) payment amount (max one hundred million - 1) 
        """.trimIndent())
    }
}