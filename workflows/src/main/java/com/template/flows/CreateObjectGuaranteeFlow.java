package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ObjectGuaranteeContract;
import com.template.states.ObjectQuaranteeState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.time.Instant;
import java.util.Arrays;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CreateObjectGuaranteeFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {

        /**
         * The progress tracker provides checkpoints indicating the progress of the flow to observers.
         */
        private final ProgressTracker progressTracker = new ProgressTracker();
        private final String objectID;
        private final String title;
        private final Instant purchaseDate;
        private final int price;
        private final int additionalYears;
        private final Party insuranceParty;


        public Initiator(String objectID, String title, Instant purchaseDate, int price, int additionalYears, Party insuranceParty) {
            this.objectID = objectID;
            this.title = title;
            this.purchaseDate = purchaseDate;
            this.price = price;
            this.additionalYears = additionalYears;
            this.insuranceParty = insuranceParty;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            // We retrieve the notary identity from the network map.
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            Party me = this.getOurIdentity();

            // We create the transaction components.
            ObjectQuaranteeState outputState = new ObjectQuaranteeState(objectID, title, purchaseDate, price, additionalYears, getOurIdentity(), this.insuranceParty);
            Command command = new Command<>(new ObjectGuaranteeContract.Commands.Create(), getOurIdentity().getOwningKey());

            // We create a transaction builder and add the components.
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(outputState, ObjectGuaranteeContract.ID)
                    .addCommand(command);

            // Verifying the transaction.
            txBuilder.verify(getServiceHub());

            // Signing the transaction.
            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Creating a session with the other party.
            FlowSession otherPartySession = initiateFlow(this.insuranceParty);

            // Obtaining the counterparty's signature.
            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                    signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));

            // We finalise the transaction and then send it to the counterparty.
            subFlow(new FinalityFlow(fullySignedTx, otherPartySession));

            return null;
        }
    }

    // Replace Responder's definition with:
    @InitiatedBy(CreateObjectGuaranteeFlow.Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        private final FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SecureHash expectedTxId = subFlow(new SignTxFlow(otherPartySession)).getId();
            subFlow(new ReceiveFinalityFlow(otherPartySession, expectedTxId));
            return null;
        }
    }

    /*
        implement a behavior on the insurance side to accept only prices of object >= 100 and <= 1000
        this implementation can be overridden later per node
     */

    public static class SignTxFlow extends SignTransactionFlow {
        private SignTxFlow(FlowSession otherPartySession) {
            super(otherPartySession);
        }

        @Override
        protected void checkTransaction(SignedTransaction stx) {
            requireThat(require -> {
                ContractState output = stx.getTx().getOutputs().get(0).getData();
                require.using("This must be an guarantee transaction.", output instanceof ObjectQuaranteeState);
                ObjectQuaranteeState object = (ObjectQuaranteeState) output;
                require.using("The price of the object is too low. Only accepting values >= 100.", object.getPrice() >= 100);
                require.using("The price of the object is too hight. Only accepting values <= 1000.", object.getPrice() < 1000);
                return null;
            });
        }
    }
}
