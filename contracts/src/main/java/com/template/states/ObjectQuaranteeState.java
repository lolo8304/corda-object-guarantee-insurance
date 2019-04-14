package com.template.states;

import com.template.contracts.ObjectGuaranteeContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// *********
// * State *
// *********
@BelongsToContract(ObjectGuaranteeContract.class)
@CordaSerializable
public class ObjectQuaranteeState implements LinearState {

    private final String objectID;
    private final String title;
    private Instant purchaseDate;
    private int price;
    private int additionalYears;
    private Party issuer;
    private Party insurance;

    public ObjectQuaranteeState(String objectID, String title, Instant purchaseDate, int price, int additionalYears, Party issuer, Party insurance) {

        this.objectID = objectID;
        this.title = title;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.additionalYears = additionalYears;
        this.issuer = issuer;
        this.insurance = insurance;
    }


    public String getObjectID() {
        return objectID;
    }

    public String getTitle() {
        return title;
    }

    public Instant getPurchaseDate() {
        return purchaseDate;
    }

    public int getPrice() {
        return price;
    }

    public int getAdditionalYears() {
        return additionalYears;
    }

    public Party getIssuer() {
        return issuer;
    }

    public Party getInsurance() {
        return insurance;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.getIssuer(), this.getInsurance());
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier(objectID);
    }
}