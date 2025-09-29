package org.example.java_cli_app.services;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.facade.IssueServiceFacade;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsIssueService implements IssueServiceFacade {

    private final GoogleSheetsProvider provider;

    private static final String SPREADSHEET_ID = "1C3iDl_MlzykAf0GLINHKCbj-QDFvMBbolgP0vFxTzBk";
    private static final String SHEET_NAME = "Issues";

    public GoogleSheetsIssueService(GoogleSheetsProvider provider) {
        this.provider = provider;
    }

    @Override
    public void createIssue(String description, String parentId) {
        try {
            Sheets sheetsService = provider.getSheetsService();

            // get ids from first column A
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, SHEET_NAME + "!A:A")
                    .execute();

            List<List<Object>> idRows = response.getValues();
            Set<String> existingIds = idRows.stream()
                    .filter(row -> !row.isEmpty())
                    .map(row -> row.get(0).toString())
                    .collect(Collectors.toSet());

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
                if (!existingIds.contains(parentIdStr)) {
                    throw new IllegalArgumentException("Parent ID " + parentIdStr + " does not exist.");
                }
            }

            List<Object> row = List.of(
                    "AD-" + newId,
                    description,
                    parentIdStr != null ? parentId : "",
                    Status.OPEN.name(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                    ""
            );

            ValueRange newIssueData = new ValueRange().setValues(Collections.singletonList(row));

            sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, SHEET_NAME, newIssueData)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("Issue created in Google Sheets: " + description); // print issue detail

        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to create issue in Google Sheets", e);
        }
    }

    @Override
    public void updateIssue(String issueId, Status status) {
        System.out.printf("Updating issue %s to status: %s%n", issueId, status);
    }

    @Override
    public void listIssues(Status status) {
        System.out.printf("List of issues with status: %s%n", status);
    }

}
