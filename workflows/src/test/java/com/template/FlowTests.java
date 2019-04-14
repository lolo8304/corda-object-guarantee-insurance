package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.CreateObjectGuaranteeFlow;
import com.template.states.ObjectQuaranteeState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    public FlowTests() {
        //a.registerInitiatedFlow(CreateObjectGuaranteeFlow.Responder.class);
        //b.registerInitiatedFlow(CreateObjectGuaranteeFlow.Responder.class);
    }

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void standardTest() throws ExecutionException, InterruptedException {

        CreateObjectGuaranteeFlow.Initiator flow = new CreateObjectGuaranteeFlow.Initiator(
                "4711", "iPhoneX",
                Instant.parse("2019-03-01T00:00:00Z"),
                100,
                1,
                b.getInfo().getLegalIdentities().get(0)
        );
        CordaFuture<Void> future = a.startFlow(flow);
        network.runNetwork();
        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void failedWithEmptyPrice() throws ExecutionException, InterruptedException {

        CreateObjectGuaranteeFlow.Initiator flow = new CreateObjectGuaranteeFlow.Initiator(
                "4711", "iPhoneX",
                Instant.parse("2019-03-01T00:00:00Z"),
                0,
                1,
                b.getInfo().getLegalIdentities().get(0)
        );
        CordaFuture<Void> future = a.startFlow(flow);
        network.runNetwork();
        future.get();
    }


    @Test
    public void responseFlowFailedWithPriceHigherThan1000() throws ExecutionException, InterruptedException {

        CreateObjectGuaranteeFlow.Initiator flow = new CreateObjectGuaranteeFlow.Initiator(
                "4711", "iPhoneX",
                Instant.parse("2019-03-01T00:00:00Z"),
                4000,
                1,
                b.getInfo().getLegalIdentities().get(0)
        );
        CordaFuture<Void> future = a.startFlow(flow);
        network.runNetwork();
        future.get();
    }
}
