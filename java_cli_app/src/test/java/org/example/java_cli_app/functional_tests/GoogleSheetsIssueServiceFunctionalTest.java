package org.example.java_cli_app.functional_tests;

import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.services.GoogleSheetsIssueService;
import org.example.java_cli_app.services.GoogleSheetsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class GoogleSheetsIssueServiceFunctionalTest {

    @Autowired
    private GoogleSheetsIssueService service;

    @Autowired
    private GoogleSheetsProvider provider;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.sheet-name}")
    private String sheetName;

    @AfterEach
    void cleanup() throws IOException, GeneralSecurityException {
        clearRows();
    }

    private void clearRows() throws IOException, GeneralSecurityException {
        String range = sheetName + "!A2:F";
        ClearValuesRequest requestBody = new ClearValuesRequest();
        provider.getSheetsService().spreadsheets().values()
                .clear(spreadsheetId, range, requestBody)
                .execute();
    }

    //////////// CREATE ////////////

    @Test
    void testCreateIssue_WithoutParentId_Success() throws GeneralSecurityException, IOException {
        service.createIssue("Functional Test Issue", null);

        ValueRange response = provider.getSheetsService().spreadsheets().values()
                .get(spreadsheetId, sheetName + "!A:F")
                .execute();

        List<List<Object>> rows = response.getValues();
        assertNotNull(rows);

        List<Object> row = rows.get(1);
        assertEquals("AD-1", row.get(0));
        assertEquals("Functional Test Issue", row.get(1));
        assertEquals(" ", row.get(2));
        assertEquals("OPEN", row.get(3));
        assertNotNull(row.get(4));
        assertEquals(" ", row.get(5));
    }

    @Test
    void testCreateIssue_WithParentId_Success() throws GeneralSecurityException, IOException {
        service.createIssue("Parent Test Issue", null);
        service.createIssue("Child Test Issue", String.valueOf(1));

        ValueRange response = provider.getSheetsService().spreadsheets().values()
                .get(spreadsheetId, sheetName + "!A:F")
                .execute();

        List<List<Object>> rows = response.getValues();
        assertNotNull(rows);

        List<Object> row = rows.get(2);
        assertEquals("AD-2", row.get(0));
        assertEquals("Child Test Issue", row.get(1));
        assertEquals("1", row.get(2));
        assertEquals("OPEN", row.get(3));
        assertNotNull(row.get(4));
        assertEquals(" ", row.get(5));
    }

    @Test
    void testCreateIssue_WithParentId_InvalidId() {
        String invalidId = "999";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createIssue("Child Test Issue", invalidId)
        );

        assertEquals("Issue ID not found: " + invalidId, exception.getMessage());
    }

    //////////// UPDATE ////////////

    @Test
    void testUpdateIssue_Success() throws GeneralSecurityException, IOException {
        service.createIssue("Functional Test Issue", null);
        service.updateIssue(String.valueOf(1), Status.IN_PROGRESS);

        ValueRange response = provider.getSheetsService().spreadsheets().values()
                .get(spreadsheetId, sheetName + "!A:F")
                .execute();

        List<List<Object>> rows = response.getValues();
        assertNotNull(rows);

        List<Object> row = rows.get(1);
        assertEquals("AD-1", row.get(0));
        assertEquals("Functional Test Issue", row.get(1));
        assertEquals(" ", row.get(2));
        assertEquals("IN_PROGRESS", row.get(3));
        assertNotNull(row.get(4));
        assertNotNull(row.get(5));
    }

    @Test
    void testUpdateIssue_InvalidId() {
        String invalidId = "999";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateIssue(invalidId, Status.IN_PROGRESS)
        );

        assertEquals("Issue ID not found: " + invalidId, exception.getMessage());
    }

    //////////// LIST ////////////

    @Test
    void testListIssue_Success() throws GeneralSecurityException, IOException {
        service.createIssue("First Test Issue", null);
        service.createIssue("Second Test Issue", String.valueOf(1));
        service.updateIssue(String.valueOf(2), Status.CLOSED);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        service.listIssues(Status.CLOSED);

        String output = out.toString();

        assertTrue(output.contains("Second Test Issue"));
        assertFalse(output.contains("First Test Issue"));
    }

}
