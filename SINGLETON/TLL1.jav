import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Caso real: auditoría central de accesos en un campus (múltiples entradas y módulos).
public class CampusAccessDemo {
    public static void main(String[] args) {
        GateModule northGate = new GateModule("Entrada Norte");
        GateModule libraryGate = new GateModule("Biblioteca");
        TurnstileModule gymTurnstile = new TurnstileModule("Gimnasio");

        northGate.scanCredential("STU-10293");
        libraryGate.scanCredential("EMP-55210");
        gymTurnstile.allowPass("STU-10293");

        // Reporte centralizado: un solo lugar donde quedó TODO
        AccessAuditLogger logger = AccessAuditLogger.getInstance();
        System.out.println("\n=== Reporte central de auditoría ===");
        logger.getLogs().forEach(System.out::println);
    }
}

// Singleton: un único auditor de accesos para todo el sistema
final class AccessAuditLogger {

    private final List<String> logs = new ArrayList<>();

    private AccessAuditLogger() { } // constructor privado

    // "Holder" carga la instancia solo cuando se llama getInstance()
    private static class Holder {
        private static final AccessAuditLogger INSTANCE = new AccessAuditLogger();
    }

    public static AccessAuditLogger getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized void logAccess(String source, String credentialId, String result) {
        String entry = String.format("[%s] %s | credencial=%s | resultado=%s",
                LocalDateTime.now(), source, credentialId, result);
        logs.add(entry);
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}

// Módulo 1: una entrada con lector QR/NFC
class GateModule {
    private final String gateName;

    public GateModule(String gateName) {
        this.gateName = gateName;
    }

    public void scanCredential(String credentialId) {
        boolean valid = credentialId.startsWith("STU-") || credentialId.startsWith("EMP-");
        String result = valid ? "ACCESO PERMITIDO" : "ACCESO DENEGADO";

        // Todos los módulos registran en EL MISMO logger (Singleton)
        AccessAuditLogger.getInstance().logAccess(gateName, credentialId, result);
    }
}

// Módulo 2: torniquete (otro subsistema distinto)
class TurnstileModule {
    private final String location;

    public TurnstileModule(String location) {
        this.location = location;
    }

    public void allowPass(String credentialId) {
        boolean allowed = credentialId.startsWith("STU-");
        String result = allowed ? "TORNIQUETE ABIERTO" : "TORNIQUETE BLOQUEADO";

        AccessAuditLogger.getInstance().logAccess("Torniquete - " + location, credentialId, result);
    }
}