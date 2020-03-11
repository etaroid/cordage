package jp.co.layerx.cordage.flowlock

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class LockFlow(
        val value: Int,
        val sender: Party
) : FlowLogic<SignedTransaction>() {
    companion object {
        fun tracker() = ProgressTracker()
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val page = serviceHub.vaultService.queryBy(SecurityState::class.java)
        var sum = 0
        val inputs = page.states.takeWhile {
            val b = sum < value
            sum += it.state.data.value
            b
        }

        val name = page.states.first().state.data.name
        val owner = serviceHub.myInfo.legalIdentities.first()
        val rest = sum - value
        val lockedSecurity = SecurityState(owner, value, sender, name, true)
        val restSecurity = SecurityState(owner, rest, sender, name)
        val outputs = listOf(lockedSecurity, restSecurity)

        val requiredSigners = listOf(owner.owningKey, sender.owningKey)
        val command = Command(SecurityContract.Commands.LockSecurity(), requiredSigners)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
                .addCommand(command)
        inputs.forEach {input ->
          txBuilder.addInputState(input)
        }
        outputs.forEach{output ->
          txBuilder.addOutputState(output)
        }

        val partStx = serviceHub.signInitialTransaction(txBuilder)

        val counterpartySession = initiateFlow(sender)
        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        return subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
    }
}

@InitiatedBy(LockFlow::class)
class LockFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, txId))
    }
}