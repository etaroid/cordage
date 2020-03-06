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
        val value = 100
        val name = "security1"
        val party = node.info.chooseIdentity()
        val issuer = issuer.info.legalIdentities.first()
        // For simplicity, sender is the same as owner
        val securityState = SecurityState(party, value, issuer, name)
        val flow = IssueSecurityFlow(securityState)
        node.startFlow(flow).get()

        val page = node.services.vaultService.queryBy(SecurityState::class.java)
        val data = page.states.first().state.data
        assert(data == securityState)
    }
}
