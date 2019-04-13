package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ObjectGuaranteeContract;
import com.template.states.ObjectQuaranteeState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.time.Instant;

public class CreateObjectGuaranteeFlow {

    @InitiatingFlow
    @StartableByRPC
    public class Initiator extends FlowLogic<Void> {

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

            // Signing the transaction.
            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Creating a session with the other party.
            FlowSession otherPartySession = initiateFlow(this.insuranceParty);

            // We finalise the transaction and then send it to the counterparty.
            subFlow(new FinalityFlow(signedTx, otherPartySession));

            return null;
        }
    }

    // Replace Responder's definition with:
    @InitiatedBy(Initiator.class)
    public class Responder extends FlowLogic<Void> {
        private final FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            subFlow(new ReceiveFinalityFlow(otherPartySession));
            return null;
        }
    }
}
