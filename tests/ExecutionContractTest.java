import br.com.pythoteca.protocol.ExecutionContract;
import br.com.pythoteca.protocol.ExecutionContract.*;
import java.util.*;

public final class ExecutionContractTest {
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    private static void reject(Runnable test) {
        try { test.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected rejection");
    }
    public static void main(String[] args) {
        UUID id = UUID.randomUUID();
        EnumSet<Capability> caps = EnumSet.of(Capability.CONSOLE, Capability.FULL_PROGRAM);
        Request r = new Request(id, Language.PORTUGOL_STUDIO, caps, "programa {}", "", 1000);
        Engine e = new Engine(Language.PORTUGOL_STUDIO, caps);
        caps.clear();
        check(e.supports(r));
        check(r.required.size() == 2);
        check(!new Engine(Language.JAVA_8, r.required).supports(r));
        check(!new Engine(Language.PORTUGOL_STUDIO, EnumSet.of(Capability.CONSOLE)).supports(r));
        reject(() -> new Request(id, r.language, r.required, " ", "", 1000));
        reject(() -> new Request(id, r.language, r.required, "x", "", 0));
        reject(() -> new Request(id, r.language, r.required, "x", "", 120001));
        reject(() -> ExecutionContract.bounded("á".repeat(10), 19));
        check(ExecutionContract.bounded("á".repeat(10), 20).length() == 10);
        reject(() -> new Request(id, r.language, r.required, "x".repeat(65537), "", 1000));
        reject(() -> new Request(id, r.language, r.required, "x", "x".repeat(16385), 1000));
        check(ExecutionContract.mayTransition(State.ACCEPTED, State.RUNNING));
        check(ExecutionContract.mayTransition(State.RUNNING, State.WAITING_INPUT));
        check(ExecutionContract.mayTransition(State.WAITING_INPUT, State.RUNNING));
        check(ExecutionContract.mayTransition(State.RUNNING, State.COMPLETED));
        check(!ExecutionContract.mayTransition(State.ACCEPTED, State.COMPLETED));
        for (State s : State.values()) {
            check(!ExecutionContract.mayTransition(s, s));
            if (ExecutionContract.terminal(s))
                for (State t : State.values()) check(!ExecutionContract.mayTransition(s, t));
        }
        System.out.println("EXECUTION_CONTRACT_OK");
    }
}
