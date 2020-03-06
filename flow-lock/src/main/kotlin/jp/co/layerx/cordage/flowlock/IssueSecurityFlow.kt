package jp.co.layerx.cordage.flowlock

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import scala.collection.parallel.ParIterableLike

@InitiatingFlow
@StartableByRPC
class IssueSecurityFlow(val securityState: SecurityState) : FlowLogic<SignedTransaction>() {
    companion object {
        fun tracker() = ProgressTracker()
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        println(securityState.owner.owningKey)
        println(securityState.sender.owningKey)
        val requiredSigners = listOf(securityState.owner.owningKey, securityState.sender.owningKey)
        val command = Command(SecurityContract.Commands.IssueSecurity(), requiredSigners)

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(securityState)
                .addCommand(command)

        val partStx = serviceHub.signInitialTransaction(txBuilder)

        val counterpartySession = initiateFlow(securityState.sender)
        val fullyStx = subFlow(CollectSignaturesFlow(partStx, listOf(counterpartySession)))

        return subFlow(FinalityFlow(fullyStx, listOf(counterpartySession)))
    }
}

@InitiatedBy(IssueSecurityFlow::class)
class IssueSecurityFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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