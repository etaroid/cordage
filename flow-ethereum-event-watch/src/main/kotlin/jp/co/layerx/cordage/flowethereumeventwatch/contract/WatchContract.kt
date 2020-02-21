package jp.co.layerx.cordage.flowethereumeventwatch.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

open class WatchContract: Contract {
    companion object {
        const val contractID = "jp.co.layerx.cordage.flowethereumeventwatch.contract.WatchContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // Omitted for the purpose of this sample.
    }

    interface Commands : CommandData {
        class Beat : Commands
    }
}