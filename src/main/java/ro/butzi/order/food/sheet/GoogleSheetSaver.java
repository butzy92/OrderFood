package ro.butzi.order.food.sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.stereotype.Service;
import ro.butzi.order.food.model.Result;
import ro.butzi.order.food.model.FoodItem;
import ro.butzi.order.food.processor.MenuProcessor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoogleSheetSaver {
    private final Sheets.Spreadsheets spreadsheets;
    private final MenuProcessor menuProcessor;

    public GoogleSheetSaver(Sheets.Spreadsheets spreadsheets, MenuProcessor menuProcessor) {
        this.spreadsheets = spreadsheets;
        this.menuProcessor = menuProcessor;
    }

    public List<Result> updateCells(String sheetId, String sheetName, String startingCell) throws IOException {
        List<Result> results = new ArrayList<>();

        getRequestForCreateSheetIfNotExists(sheetId, sheetName, startingCell)
                .map(req -> this.createNewSheet(sheetId, req))
                .ifPresent(result -> results.add(new Result("sheet added", "1")));


        List<FoodItem> foodItems = menuProcessor.getFoodItems();
        List list = new ArrayList();
        list.add(foodItems.stream().map(FoodItem::getTitle).collect(Collectors.toList()));
        list.add(foodItems.stream().map(FoodItem::getPrice).collect(Collectors.toList()));

        ValueRange valueRange = new ValueRange()
                .setValues(list)
                .setRange(getRange(sheetName, startingCell))
                .setMajorDimension("COLUMNS");

        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(Collections.singletonList(valueRange));

        BatchUpdateValuesResponse execute = spreadsheets.values().batchUpdate(sheetId, body).execute();
        results.add(new Result("menu items added", String.valueOf(execute.getTotalUpdatedCells())));

        return results;
    }

    private BatchUpdateSpreadsheetResponse createNewSheet(String spreadSheetId, Request request) {
        try {
            return spreadsheets.batchUpdate(spreadSheetId, new BatchUpdateSpreadsheetRequest()
                    .setRequests(Collections.singletonList(request)))
                    .execute();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Request> getRequestForCreateSheetIfNotExists(String sheetId, String sheetName, String startingCell) throws IOException {
        Spreadsheet spreadsheet = spreadsheets.get(sheetId).execute();

        Optional<Sheet> sheetOpt = spreadsheet.getSheets().stream()
                .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
                .findAny();
        if (!sheetOpt.isPresent()) {
            return Optional.of(getRequestForCreateSheet(sheetId, sheetName));
        } else {
            return Optional.empty();
        }
    }

    private Request getRequestForCreateSheet(String sheetId, String sheetName) {
        return new Request()
                .setAddSheet(new AddSheetRequest()
                        .setProperties(new SheetProperties()
                                .setTitle(sheetName)));
    }

    private String getRange(String sheetName, String startingCell) {
        return sheetName + "!" + startingCell;
    }
}
