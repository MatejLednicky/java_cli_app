package org.example.java_cli_app.unit_tests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.example.java_cli_app.enums.Status;
import org.example.java_cli_app.services.GoogleSheetsIssueService;
import org.example.java_cli_app.services.GoogleSheetsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class GoogleSheetsIssueServiceTest {

    @Mock
    private GoogleSheetsProvider provider;

    @Mock
    private Sheets sheetsService;

    @Mock
    private Sheets.Spreadsheets spreadsheets;

    @Mock
    private Sheets.Spreadsheets.Values values;

    private GoogleSheetsIssueService service;

    @BeforeEach
    void setUp() throws Exception {
        when(provider.getSheetsService()).thenReturn(sheetsService);
        when(sheetsService.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(values);

        service = new GoogleSheetsIssueService(provider);
    }

    @Test
    void createIssueWithoutParentId_Success() throws IOException {
        Sheets.Spreadsheets.Values.Get getRequest = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), anyString())).thenReturn(getRequest);
        ValueRange columnAResponse = new ValueRange().setValues(List.of(List.of("ID")));
        when(getRequest.execute()).thenReturn(columnAResponse);

        Sheets.Spreadsheets.Values.Append appendRequest = mock(Sheets.Spreadsheets.Values.Append.class);
        when(values.append(anyString(), anyString(), any(ValueRange.class)))
                .thenReturn(appendRequest);
        when(appendRequest.setValueInputOption(anyString())).thenReturn(appendRequest);
        when(appendRequest.execute()).thenReturn(new AppendValuesResponse());

        service.createIssue("Test Issue", null);

        ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
        verify(values).append(anyString(), anyString(), captor.capture());

        List<Object> row = captor.getValue().getValues().get(0);
        assertEquals("AD-1", row.get(0));
        assertEquals("Test Issue", row.get(1));
        assertEquals(" ", row.get(2));
        assertEquals("OPEN", row.get(3));

        String createdAt = (String) row.get(4);
        assertDoesNotThrow(() -> LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        assertEquals(" ", row.get(5));
    }


    @Test
    void createIssueWithParentId_Success() throws IOException {
        Sheets.Spreadsheets.Values.Get getRequest = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), anyString())).thenReturn(getRequest);
        ValueRange columnAResponse = new ValueRange().setValues(List.of(List.of("ID"), List.of("AD-1")));
        when(getRequest.execute()).thenReturn(columnAResponse);

        Sheets.Spreadsheets.Values.Update matchUpdate = mock(Sheets.Spreadsheets.Values.Update.class);
        when(values.update(anyString(), eq("Z1"), any(ValueRange.class))).thenReturn(matchUpdate);
        when(matchUpdate.setValueInputOption(anyString())).thenReturn(matchUpdate);
        UpdateValuesResponse updateValuesResponse = new UpdateValuesResponse();
        when(matchUpdate.execute()).thenReturn(updateValuesResponse);

        ValueRange matchResult = new ValueRange().setValues(List.of(List.of("2")));
        Sheets.Spreadsheets.Values.Get matchGet = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), eq("Z1"))).thenReturn(matchGet);
        when(matchGet.setMajorDimension(anyString())).thenReturn(matchGet);
        when(matchGet.execute()).thenReturn(matchResult);

        Sheets.Spreadsheets.Values.Clear clearRequest = mock(Sheets.Spreadsheets.Values.Clear.class);
        when(values.clear(anyString(), anyString(), any(ClearValuesRequest.class))).thenReturn(clearRequest);
        when(clearRequest.execute()).thenReturn(new ClearValuesResponse());

        Sheets.Spreadsheets.Values.Append appendRequest = mock(Sheets.Spreadsheets.Values.Append.class);
        when(values.append(anyString(), anyString(), any(ValueRange.class)))
                .thenReturn(appendRequest);
        when(appendRequest.setValueInputOption(anyString())).thenReturn(appendRequest);
        when(appendRequest.execute()).thenReturn(new AppendValuesResponse());

        service.createIssue("Child Issue", "1");

        ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
        verify(values).append(anyString(), anyString(), captor.capture());

        verify(values, atLeastOnce()).update(anyString(), anyString(), any(ValueRange.class));
        verify(values, atLeastOnce()).get(anyString(), anyString());
        verify(values).append(anyString(), anyString(), any(ValueRange.class));

        List<Object> row = captor.getValue().getValues().get(0);
        assertEquals("AD-2", row.get(0));
        assertEquals("Child Issue", row.get(1));
        assertEquals("1", row.get(2));
        assertEquals("OPEN", row.get(3));

        String createdAt = (String) row.get(4);
        assertDoesNotThrow(() -> LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        assertEquals(" ", row.get(5));
    }

    @Test
    void updateIssue_ShouldUpdateStatusAndUpdatedAt() throws IOException {
        Sheets.Spreadsheets.Values.Update matchUpdate = mock(Sheets.Spreadsheets.Values.Update.class);
        when(values.update(anyString(), eq("Z1"), any(ValueRange.class))).thenReturn(matchUpdate);
        when(matchUpdate.setValueInputOption(anyString())).thenReturn(matchUpdate);
        UpdateValuesResponse updateValuesResponse = new UpdateValuesResponse();
        when(matchUpdate.execute()).thenReturn(updateValuesResponse);

        ValueRange matchResult = new ValueRange().setValues(List.of(List.of("2")));
        Sheets.Spreadsheets.Values.Get matchGet = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), eq("Z1"))).thenReturn(matchGet);
        when(matchGet.setMajorDimension(anyString())).thenReturn(matchGet);
        when(matchGet.execute()).thenReturn(matchResult);

        Sheets.Spreadsheets.Values.Clear clearRequest = mock(Sheets.Spreadsheets.Values.Clear.class);
        when(values.clear(anyString(), anyString(), any(ClearValuesRequest.class))).thenReturn(clearRequest);
        when(clearRequest.execute()).thenReturn(new ClearValuesResponse());

        // Mock batch update
        Sheets.Spreadsheets.Values.BatchUpdate batchUpdate = mock(Sheets.Spreadsheets.Values.BatchUpdate.class);
        when(values.batchUpdate(anyString(), any(BatchUpdateValuesRequest.class)))
                .thenReturn(batchUpdate);
        when(batchUpdate.execute()).thenReturn(new BatchUpdateValuesResponse());

        service.updateIssue("1", Status.CLOSED);

        ArgumentCaptor<BatchUpdateValuesRequest> captor = ArgumentCaptor.forClass(BatchUpdateValuesRequest.class);
        verify(values).batchUpdate(anyString(), captor.capture());

        List<ValueRange> data = captor.getValue().getData();
        assertEquals("CLOSED", data.get(0).getValues().get(0).get(0));
        assertNotNull(data.get(1).getValues().get(0).get(0));
    }

    @Test
    void listIssues_ShouldPrintMatchingRowsAndClearHelper() throws IOException {
        ValueRange filterResult = new ValueRange().setValues(
                List.of(List.of("AD-1", "Issue1", "", "OPEN", "2025-01-01T12:00", ""))
        );

        Sheets.Spreadsheets.Values.Update update = mock(Sheets.Spreadsheets.Values.Update.class);
        when(values.update(anyString(), anyString(), any(ValueRange.class))).thenReturn(update);
        when(update.setValueInputOption(anyString())).thenReturn(update);
        UpdateValuesResponse updateValuesResponse = new UpdateValuesResponse();
        when(update.execute()).thenReturn(updateValuesResponse);

        Sheets.Spreadsheets.Values.Get getRequest = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), anyString())).thenReturn(getRequest);
        when(getRequest.execute()).thenReturn(filterResult);

        Sheets.Spreadsheets.Values.Clear clearRequest = mock(Sheets.Spreadsheets.Values.Clear.class);
        when(values.clear(anyString(), anyString(), any(ClearValuesRequest.class))).thenReturn(clearRequest);
        when(clearRequest.execute()).thenReturn(new ClearValuesResponse());

        service.listIssues(Status.OPEN);

        verify(values).update(anyString(), anyString(), any(ValueRange.class));
        verify(values).get(anyString(), anyString());
        verify(values).clear(anyString(), anyString(), any(ClearValuesRequest.class));
    }

    @Test
    void findRowById_ShouldThrowIfNotFound() throws IOException {
        ValueRange result = new ValueRange().setValues(List.of(List.of("#N/A")));

        Sheets.Spreadsheets.Values.Update matchUpdate = mock(Sheets.Spreadsheets.Values.Update.class);
        when(values.update(anyString(), anyString(), any(ValueRange.class))).thenReturn(matchUpdate);
        when(matchUpdate.setValueInputOption(anyString())).thenReturn(matchUpdate);
        UpdateValuesResponse updateValuesResponse = new UpdateValuesResponse();
        when(matchUpdate.execute()).thenReturn(updateValuesResponse);

        Sheets.Spreadsheets.Values.Get matchGet = mock(Sheets.Spreadsheets.Values.Get.class);
        when(values.get(anyString(), anyString())).thenReturn(matchGet);
        when(matchGet.setMajorDimension(anyString())).thenReturn(matchGet);
        when(matchGet.execute()).thenReturn(result);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.findRowById("999"));
        assertTrue(exception.getMessage().contains("Issue ID not found"));
    }
}

