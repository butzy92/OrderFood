package ro.butzi.order.food.processor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.butzi.order.food.model.Result;
import ro.butzi.order.food.sheet.GoogleSheetSaver;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class MenuController {
    private GoogleSheetSaver googleSheetSaver;

    public MenuController(GoogleSheetSaver googleSheetSaver) {
        this.googleSheetSaver = googleSheetSaver;
    }

    @PostMapping("/menu/googleSheet/{spreadSheetId}")
    public List<Result> createMenu(@PathVariable String spreadSheetId,
                                   @RequestParam Optional<String> sheetName,
                                   @RequestParam Optional<String> startingCell) throws IOException {
        return googleSheetSaver.updateCells(spreadSheetId, sheetName.orElse("Menu"), startingCell.orElse("A1"));
    }
}
