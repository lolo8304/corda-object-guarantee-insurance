package com.template;

import com.template.contracts.ObjectGuaranteeContract;
import com.template.states.ObjectQuaranteeState;
import net.corda.testing.node.MockServices;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests extends BaseTests {
    @Before
    public void setup() {
        this.setup(false);
    }


    @Test
    public void testValidObjectGuarantee() {
        transaction(ledgerServices, tx -> {


            ObjectQuaranteeState object = new ObjectQuaranteeState(
                    "4711", "iPhone",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    100,
                    2,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void failsWithMissingFields() {

        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "4711", "iPhone",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    100,
                    2,
                    this.bobTheOnlineShop,
                    this.bobTheOnlineShop
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("issuer must be different than insurer");
            return null;
        });

        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "", "iPhone",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    100,
                    2,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("objectID must be filled");
            return null;
        });
        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "4711", "",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    100,
                    2,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("title must be filled");
            return null;
        });

        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "4711", "iPhoneX",
                    Instant.parse("2029-03-01T00:00:00Z"),
                    100,
                    2,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("purchaseDate must be in the past or today");
            return null;
        });


        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "4711", "iPhoneX",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    0,
                    2,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("price must be > 0");
            return null;
        });

        transaction(ledgerServices, tx -> {
            ObjectQuaranteeState object1 = new ObjectQuaranteeState(
                    "4711", "iPhoneX",
                    Instant.parse("2019-03-01T00:00:00Z"),
                    100,
                    0,
                    this.bobTheOnlineShop,
                    this.cesarTheInsurer
            );

            tx.output(ObjectGuaranteeContract.class.getName(), object1);
            tx.command(bobTheOnlineShop.getOwningKey(), new ObjectGuaranteeContract.Commands.Create());
            tx.failsWith("additionalYears must be 1 or 2");
            return null;
        });
    }

}