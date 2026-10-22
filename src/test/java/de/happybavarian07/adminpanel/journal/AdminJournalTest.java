package de.happybavarian07.adminpanel.journal;

import de.happybavarian07.adminpanel.data.AdminAction;
import de.happybavarian07.adminpanel.data.AdminActionRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminJournalTest {
    private final List<AdminAction> saved = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private AdminActionRepository repository(boolean failing) {
        return (AdminActionRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{AdminActionRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        if (failing) throw new IllegalStateException("database down");
                        saved.add((AdminAction) args[0]);
                        return args[0];
                    }
                    return null;
                });
    }

    @Test
    void uuidTextsOfActorAndTargetAreTurnedIntoNames() {
        UUID staff = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        AdminJournal journal = new AdminJournal(repository(false), Runnable::run, () -> 1000L, (m, e) -> errors.add(m),
                id -> id.equals(staff) ? "Steve" : "Alex");

        journal.record(staff.toString(), "", "WARN", player.toString(), "spam");

        AdminAction row = saved.get(0);
        assertEquals("Steve", row.getActor());
        assertEquals(staff.toString(), row.getActorUuid());
        assertEquals("Alex", row.getTarget());
        assertEquals("WARN", row.getAction());
        assertEquals(1000L, row.getCreatedAt());
    }

    @Test
    void theConsoleAndPlainNamesStayAsTheyAre() {
        AdminJournal journal = new AdminJournal(repository(false), Runnable::run, () -> 5L, (m, e) -> errors.add(m), id -> "X");

        journal.record("", "", "OP", "Alex", null);
        journal.record(AdminJournal.CONSOLE, "", "KICK", "Alex", "bye");

        assertEquals("", saved.get(0).getActor());
        assertEquals("Alex", saved.get(0).getTarget());
        assertEquals("", saved.get(0).getDetails());
        assertEquals(AdminJournal.CONSOLE, saved.get(1).getActor());
    }

    @Test
    void aFailingDatabaseIsReportedAndDoesNotThrow() {
        AdminJournal journal = new AdminJournal(repository(true), Runnable::run, () -> 1L, (m, e) -> errors.add(m));

        journal.record("Steve", "", "BAN", "Alex", "");

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("BAN"));
    }

    @Test
    void csvQuotesSpecialCharactersAndGuardsAgainstFormulas() {
        assertEquals("plain", AdminJournal.csv("plain"));
        assertEquals("\"a,b\"", AdminJournal.csv("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", AdminJournal.csv("say \"hi\""));
        assertEquals("'=SUM(A1)", AdminJournal.csv("=SUM(A1)"));
        assertEquals("", AdminJournal.csv(null));
    }

    @Test
    void aCsvLineHasTheSevenColumns() {
        AdminAction row = new AdminAction(0L, "Steve", "id-1", "BAN", "Alex", "7d, spam");
        assertEquals("0,1970-01-01T00:00:00Z,Steve,id-1,BAN,Alex,\"7d, spam\"", AdminJournal.csvLine(row));
    }

    @Test
    void disabledCategoriesAreNotRecorded() {
        AdminJournal journal = new AdminJournal(repository(false), Runnable::run, () -> 1L, (m, e) -> errors.add(m));
        journal.setCategoryFilter(category -> !category.equals("Kicks"));

        journal.record("Steve", "", "KICK", "Alex", "");
        journal.record("Steve", "", "BAN", "Alex", "");

        assertEquals(1, saved.size());
        assertEquals("BAN", saved.get(0).getAction());
    }

    @Test
    void everyActionBelongsToACategory() {
        assertEquals("Bans", AdminJournal.category("BAN_IP"));
        assertEquals("Bans", AdminJournal.category("UNBAN"));
        assertEquals("Warnings", AdminJournal.category("WARN_END_ALL"));
        assertEquals("Operators", AdminJournal.category("DEOP"));
        assertEquals("Permissions", AdminJournal.category("PERMISSION_SET"));
        assertEquals("Permissions", AdminJournal.category("GROUP_CREATE"));
        assertEquals("Server", AdminJournal.category("SERVER_STOP"));
        assertEquals("Other", AdminJournal.category("SOMETHING"));
    }
}
