package jp.co.layerx.cordage.flowlock

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

open class SecurityContract: Contract {
    companion object {
        val contractID = "jp.co.layerx.cordage.flowlock.SecurityContract"
    }
    override fun verify(tx: LedgerTransaction) {
        // TODO: implement
    }

    sealed class Commands: TypeOnlyCommandData() {
      class IssueSecurity: Commands()
      class LockSecurity: Commands()
    }
}