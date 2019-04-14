package com.template.contracts;

import com.template.states.ObjectQuaranteeState;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TimeWindow;
import net.corda.core.transactions.LedgerTransaction;

import java.time.Instant;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************

public class ObjectGuaranteeContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.ObjectGuaranteeContract";

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        StateVerifier verifier = StateVerifier.fromTransaction(tx, Commands.class);
        CommandData commandData = verifier.command();
        if (commandData instanceof Commands.Create) {
            verifyCreate(tx, verifier);

        }
    }

    // verify all fields
    // String objectID,
    // String title,
    // Instant purchaseDate,
    // int price,
    // int additionalYears,
    // Party issues, Party insurance

    private void verifyCreate(LedgerTransaction tx, StateVerifier verifier) {
        requireThat(req -> {
            ObjectQuaranteeState object = verifier
                    .input().empty()
                    .output().output(ObjectQuaranteeState.class).object();
            req.using(
                    "issuer must be different than insurer",
                    object.getIssuer().equals(object.getInsurance()));
            req.using(
                    "objectID must be filled",
                    object.getObjectID() != null && object.getObjectID().isEmpty());
            req.using(
                    "title must be filled",
                    object.getTitle() != null && !object.getTitle().isEmpty());
            req.using(
                    "purchaseDate  in the past or today",
                    object.getPurchaseDate().isBefore(Instant.now()));
            req.using(
                    "price must be > 0",
                    object.getPrice() > 0);
            req.using(
                    "additionalYears must be 1 or 2",
                    object.getAdditionalYears() == 1 || object.getAdditionalYears() == 2);
            return null;
        });
    }

}