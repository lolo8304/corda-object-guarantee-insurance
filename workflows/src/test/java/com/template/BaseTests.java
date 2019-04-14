package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockServices;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;

import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

abstract public class BaseTests {
    protected final Instant start = Instant.now();

    protected final TestIdentity aliceID = new TestIdentity(new CordaX500Name("Alice", "Uster", "CH"));
    protected final TestIdentity bobID = new TestIdentity(new CordaX500Name("Bob", "Zurich", "CH"));
    protected final TestIdentity cesarID = new TestIdentity(new CordaX500Name("Cesar Insurance AG", "Winterthur", "CH"));
    protected MockNetwork network;
    protected StartedMockNode aliceTheCustomerNode;
    protected Party aliceTheCustomer;
    protected StartedMockNode bobTheOnlineShopNode;
    protected Party bobTheOnlineShop;
    protected StartedMockNode cesarTheInsurerNode;
    protected Party cesarTheInsurer;

    protected MockServices ledgerServices = null;
    protected MockServices ledgerServicesBrokerOnlineShop = null;
    protected MockServices ledgerServicesInsurer = null;

    // must be called to initialize using setup(true | false) and annotate with @Before
    public abstract void setup();

    public void setup(boolean withNodes) {

        if (withNodes) {
            network = new MockNetwork(ImmutableList.of("com.template.contracts", "com.template.flows"));
            aliceTheCustomerNode = network.createPartyNode(aliceID.getName());
            bobTheOnlineShopNode = network.createPartyNode(bobID.getName());
            cesarTheInsurerNode = network.createPartyNode(cesarID.getName());

            aliceTheCustomer = aliceTheCustomerNode.getInfo().getLegalIdentities().get(0);
            bobTheOnlineShop = bobTheOnlineShopNode.getInfo().getLegalIdentities().get(0);
            cesarTheInsurer = cesarTheInsurerNode.getInfo().getLegalIdentities().get(0);

            network.runNetwork();
        } else {
            aliceTheCustomer = aliceID.getParty();
            bobTheOnlineShop = bobID.getParty();
            cesarTheInsurer = cesarID.getParty();
        }


        ledgerServices = new MockServices(
                ImmutableList.of("com.template.contracts", "com.template.flows"),
                aliceTheCustomer.getName()
        );
        ledgerServicesBrokerOnlineShop = new MockServices(
                ImmutableList.of("com.template.contracts", "com.template.flows"),
                bobTheOnlineShop.getName()
        );
        ledgerServicesInsurer = new MockServices(
                ImmutableList.of("com.template.contracts", "com.template.flows"),
                cesarTheInsurer.getName()
        );

    }

    @After
    public void tearDown() {
        if (network != null) network.stopNodes();
    }


    protected List<PublicKey> getPublicKeys(Party... parties) {
        ImmutableList<Party> list = ImmutableList.copyOf(parties);
        return list.stream().map(party -> party.getOwningKey()).collect(Collectors.toList());
    }

    protected Party getParty(StartedMockNode node) {
        return node.getInfo().getLegalIdentities().get(0);
    }


    protected LocalDate get1stDayOfNextMonth() {
        YearMonth yearMonth = YearMonth.from(Instant.now().atZone(ZoneId.systemDefault()));
        return yearMonth.atEndOfMonth().plus(1, ChronoUnit.DAYS);
    }

    protected Instant get1stDayOfNextMonth_Instant() {
        return this.get1stDayOfNextMonth().atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
