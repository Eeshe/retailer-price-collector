package me.eeshe.retailerpricecollector.service.scrapers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;

import me.eeshe.retailerpricecollector.model.Product;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class KromiScraper extends Scraper {

  private static final List<String> CATEGORY_IDS = List.of(
      // "VIV",
      // "ANI",
      // "AUT",
      // "CAM",
      // "CAR",
      // "CHA",
      // "CMT",
      "FER"
  // "HOG",
  // "JAR",
  // "JUG",
  // "LIC",
  // "LIM",
  // "NAT",
  // "PES",
  // "QCL",
  // "RYC",
  // "VYH"
  );

  public void run() {
    // Start the futures that will scrape all the products from each category
    ExecutorService executor = Executors.newFixedThreadPool(3);
    List<Future<List<Product>>> futures = new ArrayList<>();
    for (String categoryId : CATEGORY_IDS) {
      Future<List<Product>> future = executor.submit(() -> scrapeProductCategory(categoryId));
      futures.add(future);
    }

    // Collect the data from the futures as they are completed
    List<Product> products = new ArrayList<>();
    for (Future<List<Product>> future : futures) {
      try {
        products.addAll(future.get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    // Write the results to a .csv file
    writeToCsv(products);
  }

  /**
   * Scrapes the passed category's page products.
   *
   * @param categoryId ID of the category page to scrape.
   * @return List of scraped products.
   */
  private List<Product> scrapeProductCategory(String categoryId) {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = launchBrowser(playwright, true);

      // First, get product URLs from the category page
      Page page = browser.newPage();

      page.navigate("https://www.kromionline.com/Products.php?cat=" + categoryId + "&suc=UNI02",
          new NavigateOptions().setTimeout(120000));
      page.waitForSelector("#pasilloDeProductos");

      scrollPage(page, "#loadingMore");
      Set<String> productUrls = extractProductUrls(page);
      page.close();
      browser.close();

      return scrapeProductUrls(categoryId, productUrls);
    }
  }

  /**
   * Iterates all the loaded products and extracts their HREF URLs.
   *
   * @param page Page to extract URLs from.
   * @return List of product URLs.
   */
  private Set<String> extractProductUrls(Page page) {
    Set<String> productUrls = new HashSet<>();
    for (Locator locator : page.locator(".nombre_producto").all()) {
      for (Locator link : locator.getByRole(AriaRole.LINK).all()) {
        productUrls.add("https://www.kromionline.com/" + link.getAttribute("href"));
      }
    }
    return productUrls;
  }

  private List<Product> scrapeProductUrls(String categoryId, Set<String> productUrls) {
    List<Product> products = new ArrayList<>();
    try (Playwright playwright = Playwright.create()) {
      Browser browser = launchBrowser(playwright, true);
      Page page = browser.newPage();
      page.navigate("https://www.kromionline.com/");

      for (String productUrl : productUrls)
        products.add(scrapeProduct(productUrl, categoryId, page));
    }
    return products;
  }

  /**
   * Scrapes the product information corresponding to the passed product URL.
   *
   * @param productUrl URL of the product to scrape.
   * @return Scraped product.
   */
  private Product scrapeProduct(String productUrl, String categoryId, Page page) {
    // System.out.println("PRODUCT URL: " + productUrl);
    page.navigate(productUrl);

    String barCode = page.locator(".bar_code_span").textContent();
    String name = page.locator(".h2.m-0.product_title").textContent();
    double price = Double.parseDouble(page.locator(".tag_precio_producto").textContent()
        .replace("$", ""));

    // System.out.println("BAR CODE: " + barCode);
    // System.out.println("NAME: " + name);
    // System.out.println("PRICE: " + price);
    return new Product(barCode, categoryId, name, price);
  }

  private void writeToCsv(List<Product> products) {
    Table table = Table.create("Products",
        StringColumn.create("barCode"),
        StringColumn.create("categoryId"),
        StringColumn.create("name"),
        DoubleColumn.create("price"));
    for (Product product : products) {
      Row row = table.appendRow();
      row.setString("barCode", product.getBarCode());
      row.setString("categoryId", product.getCategoryId());
      row.setString("name", product.getName());
      row.setDouble("price", product.getPrice());
    }

    table.write().csv("../output/products.csv");
  }
}
