package com.template;

import com.template.flows.CreateObjectGuaranteeFlow;
import net.corda.core.concurrent.CordaFuture;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class FlowTests extends BaseTests {

    @Before
    public void setup() {
        this.setup(true);
    }

    @Test
    public void standardTest() throws ExecutionException, InterruptedException {

        CreateObjectGuaranteeFlow.Initiator flow = new CreateObjectGuaranteeFlow.Initiator(
                "4711", "iPhoneX",
                Instant.parse("2019-03-01T00:00:00Z"),
                100,
                1,
                this.cesarTheInsurer
        );
        CordaFuture<Void> future = this.bobTheOnlineShopNode.startFlow(flow);
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
                this.cesarTheInsurer
        );
        CordaFuture<Void> future = this.bobTheOnlineShopNode.startFlow(flow);
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
                this.cesarTheInsurer
        );
        CordaFuture<Void> future = this.bobTheOnlineShopNode.startFlow(flow);
        network.runNetwork();
        future.get();
    }
}
