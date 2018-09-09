package ro.butzi.order.food.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.butzi.order.food.model.FoodItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MenuProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuProcessor.class);
    private static final String CLASS_FOR_FOOD_PRICE = "menu-list__item-price";
    private static final String CURRENCY = " RON";
    private static final String CLASS_FOR_FOOD_TITLE = "menu-list__item-title";
    private final String orderFoodLink;
    private final NumberFormat numberInstance;

    public MenuProcessor(
            @Value("${orderFoodLink}")
            String orderFoodLink) {
        this.orderFoodLink = orderFoodLink;
        numberInstance = NumberFormat.getNumberInstance(new Locale("ro"));
    }

    public List<FoodItem> getFoodItems() throws IOException {
        Document document = Jsoup.parse(new URL(orderFoodLink), 10000);
        Elements elementsByClass = document.getElementsByClass("menu-list__item-title");

        Map<String, String> collect = elementsByClass.stream()
                .map(Element::parent)
                .collect(Collectors.toMap(this::extractFoodTitle, this::extractFoodPrice));

        return collect.entrySet()
                .stream()
                .flatMap(this::extractByMultiplePrice)
                .filter(foodItem -> foodItem.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(FoodItem::getTitle))
                .collect(Collectors.toList());
    }

    private String extractFoodPrice(Element e) {
        return e.getElementsByClass(CLASS_FOR_FOOD_PRICE).text()
                .replace(CURRENCY, "").trim();
    }

    private String extractFoodTitle(Element e) {
        return e.getElementsByClass(CLASS_FOR_FOOD_TITLE).text();
    }

    private Stream<FoodItem> extractByMultiplePrice(Map.Entry<String, String> entry) {
        if(entry.getValue().contains("/")){
            return Stream.of(entry.getValue().split("/"))
                    .map(price -> new FoodItem(entry.getKey() + price, extractNumber(price)));

        } else {
            return Stream.of(new FoodItem(entry.getKey(), extractNumber(entry.getValue())));
        }
    }

    private BigDecimal extractNumber(String priceInString){
        try {
            String numberWithoutSpace = priceInString.trim();
            return new BigDecimal(numberInstance.parse(numberWithoutSpace).toString());
        } catch (ParseException e) {
            LOGGER.error(priceInString + " cannot be converted to number", e);
            return BigDecimal.ZERO;
        }
    }

}
