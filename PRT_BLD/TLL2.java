package PRT_BLD;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TLL2 extends JFrame {

    private JComboBox<String> cbRole;
    private JTextField txtEvent, txtDate, txtVenue;
    private JTextField txtName, txtEmail;
    private JTextArea txtOut;

    private EventPass templatePass = null;
    private final List<EventPass> createdPasses = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TLL2().setVisible(true));
    }

    public TLL2() {
        setTitle("Event Pass Generator (Builder + Prototype)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        log("Builder + Prototype GUI");
        log("1) Pick role -> Build template");
        log("2) Fill name/email -> Clone from template");
        log("------------------------------------------");
    }

    private JPanel buildTop() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 0, 10));

        cbRole = new JComboBox<>(new String[]{"PROFESSOR", "MONITOR", "STUDENT"});

        txtEvent = new JTextField("Software Engineering Expo");
        txtDate = new JTextField("2026-02-20");
        txtVenue = new JTextField("Main Auditorium");

        txtName = new JTextField();
        txtEmail = new JTextField();

        JPanel row1 = new JPanel(new GridLayout(1, 8, 8, 8));
        row1.add(new JLabel("Role:"));
        row1.add(cbRole);

        row1.add(new JLabel("Event:"));
        row1.add(txtEvent);

        row1.add(new JLabel("Date:"));
        row1.add(txtDate);

        row1.add(new JLabel("Venue:"));
        row1.add(txtVenue);

        JPanel row2 = new JPanel(new GridLayout(1, 6, 8, 8));
        row2.add(new JLabel("Name:"));
        row2.add(txtName);

        row2.add(new JLabel("Email:"));
        row2.add(txtEmail);

        JButton btnBuild = new JButton("Build template");
        btnBuild.addActionListener(e -> onBuildTemplate());

        JButton btnClone = new JButton("Clone pass");
        btnClone.addActionListener(e -> onClonePass());

        row2.add(btnBuild);
        row2.add(btnClone);

        panel.add(row1);
        panel.add(row2);
        return panel;
    }

    private JScrollPane buildCenter() {
        txtOut = new JTextArea(18, 60);
        txtOut.setEditable(false);
        txtOut.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane sp = new JScrollPane(txtOut);
        sp.setBorder(new EmptyBorder(0, 10, 0, 10));
        return sp;
    }

    private JPanel buildBottom() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnShowTemplate = new JButton("Show template");
        btnShowTemplate.addActionListener(e -> showTemplate());

        JButton btnShowAll = new JButton("Show created passes");
        btnShowAll.addActionListener(e -> showAll());

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> txtOut.setText(""));

        panel.add(btnShowTemplate);
        panel.add(btnShowAll);
        panel.add(btnClear);

        return panel;
    }

    private void onBuildTemplate() {
        String role = (String) cbRole.getSelectedItem();

        String event = txtEvent.getText().trim();
        String date = txtDate.getText().trim();
        String venue = txtVenue.getText().trim();

        if (event.isEmpty() || date.isEmpty() || venue.isEmpty()) {
            log("Oops. Event, date and venue cannot be empty.");
            return;
        }

        PassBuilder builder;
        if ("PROFESSOR".equalsIgnoreCase(role)) {
            builder = new ProfessorPassBuilder();
        } else if ("MONITOR".equalsIgnoreCase(role)) {
            builder = new MonitorPassBuilder();
        } else {
            builder = new StudentPassBuilder();
        }

        PassDirector director = new PassDirector(builder);

        log("[BUILDER] Building template for role: " + role);
        director.buildBasePass(event, date, venue, this::log);

        templatePass = builder.getPass();
        log("[BUILDER] Done. Template created with ID: " + templatePass.getPassId());
        log(templatePass.toText());
    }

    private void onClonePass() {
        if (templatePass == null) {
            log("You need to build a template first.");
            return;
        }

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            log("Please fill name and email.");
            return;
        }


        log("[PROTOTYPE] Cloning current template...");
        EventPass newPass = templatePass.clone();

        newPass.setHolderName(name);
        newPass.setEmail(email);


        if ("MONITOR".equalsIgnoreCase(newPass.getRole())) {
            newPass.addPermission("CHECK_ATTENDANCE_LIST");
        }

        newPass.regenerateQrPayload();

        createdPasses.add(newPass);

        log("[PROTOTYPE] New pass created. New ID: " + newPass.getPassId());
        log(newPass.toText());


        txtName.setText("");
        txtEmail.setText("");
    }

    private void showTemplate() {
        if (templatePass == null) {
            log("No template yet.");
            return;
        }
        log("=== TEMPLATE ===");
        log(templatePass.toText());
    }

    private void showAll() {
        log("=== CREATED PASSES (" + createdPasses.size() + ") ===");
        if (createdPasses.isEmpty()) {
            log("(none yet)");
            return;
        }
        for (int i = 0; i < createdPasses.size(); i++) {
            log("#" + (i + 1));
            log(createdPasses.get(i).toText());
        }
    }

    private void log(String msg) {
        txtOut.append(msg + "\n");
        txtOut.setCaretPosition(txtOut.getDocument().getLength());
    }
}


interface PatternLog {
    void log(String msg);
}

interface PassPrototype {
    EventPass clone();
}

class EventPass implements PassPrototype {

    private String passId;

    private String eventName;
    private String eventDate;
    private String venue;

    private String holderName;
    private String email;

    private String role;
    private String accessLevel;

    private List<String> permissions;
    private String qrPayload;

    public EventPass() {
        permissions = new ArrayList<>();
        passId = UUID.randomUUID().toString();
        regenerateQrPayload();
    }


    @Override
    public EventPass clone() {
        EventPass copy = new EventPass();


        copy.eventName = this.eventName;
        copy.eventDate = this.eventDate;
        copy.venue = this.venue;

        copy.holderName = this.holderName;
        copy.email = this.email;


        copy.role = this.role;
        copy.accessLevel = this.accessLevel;

        copy.permissions = new ArrayList<>(this.permissions);

        copy.regenerateQrPayload();
        return copy;
    }

    public void regenerateQrPayload() {
        
        qrPayload = "PASS|" + safe(passId) + "|" + safe(eventName) + "|" + safe(email) + "|" + safe(role);
    }

    private String safe(String s) {
        return s == null ? "-" : s.replace("|", "/");
    }

    public void addPermission(String p) {
        if (p != null && !p.isBlank()) permissions.add(p);
    }

    public String toText() {
        return ""
                + "ID=" + passId + "\n"
                + "Event=" + eventName + " | Date=" + eventDate + " | Venue=" + venue + "\n"
                + "Holder=" + holderName + " (" + email + ")\n"
                + "Role=" + role + " | Access=" + accessLevel + "\n"
                + "Permissions=" + (permissions.isEmpty() ? "none" : permissions) + "\n"
                + "QR=" + qrPayload + "\n"
                + "------------------------------";
    }

   
    public String getPassId() { return passId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setVenue(String venue) { this.venue = venue; }

    public void setHolderName(String holderName) { this.holderName = holderName; }
    public void setEmail(String email) { this.email = email; }
}

interface PassBuilder {
    void buildEventInfo(String name, String date, String venue);
    void buildRoleAndAccess();
    void buildDefaultPermissions();
    void buildTemplateHolder();
    EventPass getPass();
}

class ProfessorPassBuilder implements PassBuilder {

    private final EventPass pass = new EventPass();

    @Override
    public void buildEventInfo(String name, String date, String venue) {
        pass.setEventName(name);
        pass.setEventDate(date);
        pass.setVenue(venue);
    }

    @Override
    public void buildRoleAndAccess() {
        pass.setRole("PROFESSOR");
        pass.setAccessLevel("HIGH");
    }

    @Override
    public void buildDefaultPermissions() {
        pass.addPermission("CREATE_EVENT");
        pass.addPermission("EXPORT_ATTENDANCE");
    }

    @Override
    public void buildTemplateHolder() {
        pass.setHolderName("TEMPLATE_PROF");
        pass.setEmail("template.prof@uni.edu");
        pass.regenerateQrPayload();
    }

    @Override
    public EventPass getPass() {
        return pass;
    }
}

class MonitorPassBuilder implements PassBuilder {

    private final EventPass pass = new EventPass();

    @Override
    public void buildEventInfo(String name, String date, String venue) {
        pass.setEventName(name);
        pass.setEventDate(date);
        pass.setVenue(venue);
    }

    @Override
    public void buildRoleAndAccess() {
        pass.setRole("MONITOR");
        pass.setAccessLevel("MEDIUM");
    }

    @Override
    public void buildDefaultPermissions() {
        pass.addPermission("SCAN_QR");
        pass.addPermission("REGISTER_ENTRY_EXIT");
    }

    @Override
    public void buildTemplateHolder() {
        pass.setHolderName("TEMPLATE_MONITOR");
        pass.setEmail("template.monitor@uni.edu");
        pass.regenerateQrPayload();
    }

    @Override
    public EventPass getPass() {
        return pass;
    }
}

class StudentPassBuilder implements PassBuilder {

    private final EventPass pass = new EventPass();

    @Override
    public void buildEventInfo(String name, String date, String venue) {
        pass.setEventName(name);
        pass.setEventDate(date);
        pass.setVenue(venue);
    }

    @Override
    public void buildRoleAndAccess() {
        pass.setRole("STUDENT");
        pass.setAccessLevel("BASIC");
    }

    @Override
    public void buildDefaultPermissions() {
        pass.addPermission("ENTER_EVENT");
    }

    @Override
    public void buildTemplateHolder() {
        pass.setHolderName("TEMPLATE_STUDENT");
        pass.setEmail("template.student@uni.edu");
        pass.regenerateQrPayload();
    }

    @Override
    public EventPass getPass() {
        return pass;
    }
}

class PassDirector {

    private final PassBuilder builder;

    public PassDirector(PassBuilder builder) {
        this.builder = builder;
    }


    public void buildBasePass(String eventName, String eventDate, String venue, PatternLog logger) {
        if (logger != null) logger.log("  - set event info");
        builder.buildEventInfo(eventName, eventDate, venue);

        if (logger != null) logger.log("  - set role + access");
        builder.buildRoleAndAccess();

        if (logger != null) logger.log("  - add default permissions");
        builder.buildDefaultPermissions();

        if (logger != null) logger.log("  - set template holder");
        builder.buildTemplateHolder();
    }
}