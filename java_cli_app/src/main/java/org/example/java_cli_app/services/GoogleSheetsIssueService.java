package org.example.java_cli_app.services;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsIssueService implements IssueServiceFacade {

    private final GoogleSheetsProvider provider;

    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    @Value("${google.sheets.sheet-name}")
    private String sheetName;

    public GoogleSheetsIssueService(GoogleSheetsProvider provider) {
        this.provider = provider;
    }

    @Override
    public void createIssue(String description, String parentId) {
        try {
            Sheets sheetsService = provider.getSheetsService();

            // get ids from first column A
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!A:A")
                    .execute();

            List<List<Object>> idRows = response.getValues();

            int newId;
            if (idRows.size() == 1) {
                // no rows other than headers, start at 1
                newId = 1;
            } else {
                // last id row
                List<Object> lastRow = idRows.get(idRows.size() - 1);
                if (!lastRow.isEmpty()) {
                    String[] idParts = lastRow.get(0).toString().split("-");
                    newId = Integer.parseInt(idParts[1]) + 1;
                } else {
                    newId = idRows.size();
                }
            }

            String parentIdStr = null;
            if (parentId != null) {
                parentIdStr = "AD-" + parentId;
                findRowById(parentId);
            }

            List<Object> row = List.of(
                    "AD-" + newId,
                    description,
                    parentIdStr != null ? parentId : " ",
                    Status.OPEN.name(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                    " "
            );
            ValueRange newIssueData = new ValueRange().setValues(Collections.singletonList(row));

            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, sheetName, newIssueData)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("Issue "+ newId + " created in Google Sheets: " + description);

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to create issue in Google Sheets", e);
        }
    }

    @Override
    public void updateIssue(String issueId, Status status) {
        try {
            Sheets sheetsService = provider.getSheetsService();

            int rowNumber = findRowById(issueId);

            List<ValueRange> updates = List.of(
                    new ValueRange()
                            .setRange(String.format("%s!D%d", sheetName, rowNumber))
                            .setValues(List.of(List.of(status.name()))),
                    new ValueRange()
                            .setRange(String.format("%s!F%d", sheetName, rowNumber))
                            .setValues(List.of(List.of(
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                            )))
            );

            sheetsService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, new BatchUpdateValuesRequest()
                            .setValueInputOption("RAW")
                            .setData(updates))
                    .execute();

            System.out.println("Issue " + issueId + " updated to " + status);

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to update issue in Google Sheets", e);
        }
    }

    @Override
    public void listIssues(Status status) {
        try {
            Sheets service = provider.getSheetsService();

            String helperCell = sheetName + "!Z1";
            String helperRange = sheetName + "!Z1:AE";
            String filterFormula = String.format("=FILTER(A:F; D:D=\"%s\")", status.name());

            ValueRange formulaBody = new ValueRange().setValues(List.of(List.of(filterFormula)));
            service.spreadsheets().values()
                    .update(spreadsheetId, helperCell, formulaBody)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, helperRange)
                    .execute();

            List<List<Object>> rows = response.getValues();
            if (rows.get(0).get(0).toString().equals("#N/A")) {
                System.out.println("No issues found with status: " + status);
            } else {
                System.out.println("ID | Description | Parent ID | Status | Created at | Updated at");
                System.out.println("---------------------------------------------------------------");

                for (List<Object> row : rows) {
                    System.out.printf("%s | %s | %s | %s | %s | %s%n",
                            row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
                }
            }

            // clear used helper cells
            service.spreadsheets().values()
                    .clear(spreadsheetId, helperCell, new ClearValuesRequest())
                    .execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to list issues from Google Sheets", e);
        }
    }

    public int findRowById(String issueId) throws IOException, GeneralSecurityException {
        Sheets sheetsService = provider.getSheetsService();

        // unused column for match function
        String helperCell = "Z1";
        String matchFormula = String.format("=MATCH(\"%s\"; A:A; 0)", "AD-"+issueId);

        ValueRange formulaBody = new ValueRange().setValues(List.of(List.of(matchFormula)));
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, helperCell, formulaBody)
                .setValueInputOption("USER_ENTERED")
                .execute();

        ValueRange matchResult = sheetsService.spreadsheets().values()
                .get(spreadsheetId, helperCell)
                .setMajorDimension("ROWS")
                .execute();

        List<List<Object>> values = matchResult.getValues();
        if (values.get(0).get(0).toString().equals("#N/A")) {
            throw new IllegalArgumentException("Issue ID not found: " + issueId);
        }

        // clear used helper cells
        sheetsService.spreadsheets().values()
                .clear(spreadsheetId, helperCell, new ClearValuesRequest())
                .execute();

        return Integer.parseInt(values.get(0).get(0).toString());
    }


}
