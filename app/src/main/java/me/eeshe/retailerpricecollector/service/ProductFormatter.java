package me.eeshe.retailerpricecollector.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.eeshe.retailerpricecollector.model.Product;
import me.eeshe.retailerpricecollector.util.TableUtil;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ProductFormatter {

  public void run() {
    Table rawProducts = TableUtil.loadTable(new File("../output/raw_products.csv"));
    if (rawProducts == null) {
      return;
    }
    formatProductNames(rawProducts);
    TableUtil.writeTableFile(rawProducts, new File("../output/products.csv"));
  }

  private void formatProductNames(Table rawProducts) {
    List<Product> products = new ArrayList<>();
    // Load products
    for (int rowIndex = 0; rowIndex < rawProducts.rowCount(); rowIndex++) {
      Row row = rawProducts.row(rowIndex);
      products.add(new Product(
          row.getString("barCode"),
          row.getString("categoryId"),
          row.getString("name"),
          row.getDouble("price")));
    }
    GeminiWrapper geminiWrapper = new GeminiWrapper();
    int productsPerPrompt = 100;
    for (int index = 0; index < products.size(); index += productsPerPrompt) {
      int toIndex = Math.min(products.size() - 1, index + productsPerPrompt);
      List<Product> productsToFormat = products.subList(index, toIndex);
      List<String> productNames = productsToFormat.stream().map(Product::getName).toList();
      String geminiResponse = geminiWrapper.prompt(generatePrompt(productNames));
      if (!geminiResponse.contains("\n")) {
        // Gemini returned an invalid response, retry
        index -= productsPerPrompt;
        continue;
      }
      List<String> formattedNames = new ArrayList<>(Arrays.asList(geminiResponse.split("\n")));

      for (Product product : productsToFormat) {
        product.setName(formattedNames.remove(0));
      }
    }

    StringColumn formattedNameColumn = StringColumn.create("formattedName", rawProducts.rowCount());
    for (int index = 0; index < products.size(); index++) {
      formattedNameColumn.set(index, products.get(index).getName());
    }
    rawProducts.addColumns(formattedNameColumn);
  }

  private String generatePrompt(List<String> productNames) {
    String prompt = """
                Format the following product names following the format <ProductName> <Description> <Brand> <Measurements>. Also capitalize every word as you see fitting. Do not change nor translate anything else, only the order of words and capitalization of the first letter of each word. If the measurement of product isn't a conventional unit, but something lie 'pack' or 'bags', separate it from the number. For example, 10bags -> 10 bags. Don't do this for grams, liters, kilograms or any other conventional unit. Also, if the non-conventional unit is abbreviated, try to write the full name. For example, 'und' is an abbreviation for 'unidad', so 'und' should be replaced with 'unidad'.

        It's important that you DO NOT TRANSLATE ANYTHING. All names should remain in Spanish. You may add missing tildes.

            Return the list of product names, each of them separated with \\n.

                """;
    prompt += String.join("\n", productNames);

    return prompt;
  }
}
