package jp.co.layerx.cordage.flowlock

import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.corda.testing.internal.chooseIdentity

class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var node: StartedMockNode
    private lateinit var issuer: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(threadPerNode = true, cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("jp.co.layerx.cordage.flowlock"))))
        node = network.createNode()
        issuer = network.createNode()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `IssueSecurityFlow can issue security`() {
        val party = node.info.chooseIdentity()
        val issuer = issuer.info.legalIdentities.first()
        val receiver = issuer
        val name = "security"

        val securityStates = listOf(
            SecurityState(party, 200, issuer, name),
            SecurityState(party, 200, issuer, name),
            SecurityState(party, 300, issuer, name),
            SecurityState(party, 400, issuer, name)
        )

        securityStates.forEach {securityState ->
            val flow = IssueSecurityFlow(securityState)
            node.startFlow(flow).get()
        }
        val page = node.services.vaultService.queryBy(SecurityState::class.java)
        assert(page.states.size === securityStates.size)
        val sum = page.states.fold(0) { acc, state -> acc + state.state.data.value }
        assert(sum === 1100)

        val lockFlow = LockFlow(500, receiver)
        node.startFlow(lockFlow).get()

        val page2 = node.services.vaultService.queryBy(SecurityState::class.java)
        page2.states.forEach {
            println(it.state.data.toString())
        }
    }
}
