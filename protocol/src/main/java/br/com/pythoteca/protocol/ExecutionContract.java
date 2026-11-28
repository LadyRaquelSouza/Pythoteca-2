package br.com.pythoteca.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Candidate domain contract only. No Android transport or engine implementation. */
public final class ExecutionContract {
    private ExecutionContract() {}
    public static final int VERSION = 1;
    public static final int MAX_SOURCE_BYTES = 64 * 1024;
    public static final int MAX_INPUT_BYTES = 16 * 1024;
    public static final int MAX_OUTPUT_BYTES = 64 * 1024;
    public static final long MAX_DURATION_MS = 120_000;
    public enum Language { PYTHON, PORTUGOL_STUDIO, JAVA_8 }
    public enum Capability { CONSOLE, INTERACTIVE_INPUT, FULL_PROGRAM, METHOD, GUI_EVENTS }
    public enum State { ACCEPTED, RUNNING, WAITING_INPUT, COMPLETED, FAILED, CANCELLED, TIMED_OUT }
    public enum Diagnostic { SYNTAX, COMPILE, RUNTIME, RESOURCE_LIMIT, UNSUPPORTED, TRANSPORT }
    private static Set<Capability> immutable(Set<Capability> values) {
        Objects.requireNonNull(values);
        EnumSet<Capability> copy = EnumSet.noneOf(Capability.class);
        copy.addAll(values);
        return Collections.unmodifiableSet(copy);
    }
    public static String bounded(String value, int limit) {
        Objects.requireNonNull(value);
        // Bound UTF-16 length before allocating the UTF-8 representation.
        if (limit < 0 || value.length() > limit || value.getBytes(StandardCharsets.UTF_8).length > limit)
            throw new IllegalArgumentException("Text exceeds limit");
        return value;
    }
    public static final class Engine {
        public final Language language;
        public final Set<Capability> capabilities;
        public Engine(Language language, Set<Capability> capabilities) {
            this.language = Objects.requireNonNull(language);
            this.capabilities = immutable(capabilities);
        }
        public boolean supports(Request request) {
            return language == request.language && capabilities.containsAll(request.required);
        }
    }
    public static final class Request {
        public final UUID session;
        public final Language language;
        public final Set<Capability> required;
        public final String source;
        public final String initialInput;
        public final long durationMs;
        public Request(UUID session, Language language, Set<Capability> required, String source,
                       String initialInput, long durationMs) {
            this.session = Objects.requireNonNull(session);
            this.language = Objects.requireNonNull(language);
            this.required = immutable(required);
            if (this.required.isEmpty()) throw new IllegalArgumentException("Missing capabilities");
            this.source = bounded(source, MAX_SOURCE_BYTES);
            if (this.source.trim().isEmpty()) throw new IllegalArgumentException("Empty source");
            this.initialInput = bounded(initialInput, MAX_INPUT_BYTES);
            if (durationMs <= 0 || durationMs > MAX_DURATION_MS) throw new IllegalArgumentException("Invalid duration");
            this.durationMs = durationMs;
        }
    }
    public static boolean terminal(State state) {
        return state == State.COMPLETED || state == State.FAILED || state == State.CANCELLED || state == State.TIMED_OUT;
    }
    public static boolean mayTransition(State from, State to) {
        if (from == null || to == null || terminal(from) || from == to) return false;
        if (to == State.FAILED || to == State.CANCELLED || to == State.TIMED_OUT) return true;
        switch (from) {
            case ACCEPTED: return to == State.RUNNING;
            case RUNNING: return to == State.WAITING_INPUT || to == State.COMPLETED;
            case WAITING_INPUT: return to == State.RUNNING;
            default: return false;
        }
    }
}
