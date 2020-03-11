package jp.co.layerx.cordage.flowlock

import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.Party
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

@BelongsToContract(SecurityContract::class)
data class SecurityState(
    val owner: Party,
    val value: Int,
    val sender: Party,
    val name: String,
    val locked: Boolean = false,
    override val participants: List<AbstractParty> = listOf(owner, sender)
) : ContractState
