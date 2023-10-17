package api.tests;

import api.basetest.BaseTest;
import api.endpoints.BookStoreEndPoints;
import api.payloads.DataProviders;
import api.payloads.RequestBodyBuilder;
import api.utils.RandomDataGenerator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


import java.io.IOException;
import java.util.*;


public class ValidateBooks {
    String authToken;
    String userName;
    String userId;
    String bookId;
    RandomDataGenerator data = new RandomDataGenerator();
    private String username = data.generateRandomUsername();
    private String password = data.generateRandomPassword();

    @BeforeClass         //initial step to get auth token, username, and userId ( to uitlize on other test cases )
    public void createUserAndAuthorize() {
        System.out.println(username + "   " + password);
        Map<String, String> userMap = BaseTest.createUser(userName, password);
        authToken = userMap.get("authToken");
        userName = userMap.get("username");
        userId = userMap.get("userId");
    }


    @Test(priority = 0)
    public void validateAllBooks() throws JSONException {
        Response response = BookStoreEndPoints.getAllBooks(authToken);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals( response.getHeader("content-type"),"application/json; charset=utf-8");
        String jsonString = response.asString();
        JSONAssert.assertEquals(RequestBodyBuilder.allBookAPIResponse, jsonString, true);

        List<Map<String, String>> books = JsonPath.from(jsonString).get("books");
        Assert.assertTrue(books.size() > 0);
        bookId = books.get(1).get("isbn");
      /*  List<Map<String, Object>> bookList = new ArrayList<>();*/

    }

    @Test(priority = 1)
    public void validateAddBook() {
        System.out.println(userId + "    auth token   " + authToken);
        String requestBody = RequestBodyBuilder.buildAddBookRequestBody(userId, bookId);

        Response response = BookStoreEndPoints.addBook(authToken, requestBody);
        System.out.println(response.getStatusCode() + "  " + response.getHeaders());
        Assert.assertEquals(response.getStatusCode(), 201);
        Assert.assertEquals(response.jsonPath().get("books[0].isbn"), bookId);

    }

    @Test(priority = 2)
    public void validateGetBook() {
        Response response = BookStoreEndPoints.getBookData(authToken, bookId);
        Assert.assertEquals(response.getStatusCode(), 200);

        String[] fields = {"isbn", "title", "subTitle", "author", "website"};
        Map<String, String> resposeMap = new LinkedHashMap<>();
        for (String field : fields) {
            String value = response.getBody().jsonPath().get(field);
            resposeMap.put(field, value);
        }
    }

    @Test(dataProvider = "userISBN", dataProviderClass = DataProviders.class, priority = 3)
    public void updateBook(String user, String ISBN) {
        String requestBody = "{ \"userId\": \"" + userId + "\", \"isbn\": \"" + ISBN + "\" }";
     //   System.out.println(requestBody);
        Response response = BookStoreEndPoints.updateBook(authToken, bookId, requestBody);
       // System.out.println(response.getBody().asString());
        Assert.assertEquals(response.getStatusCode(), 200);
        String isbnno = response.jsonPath().get("books[0].isbn");
        Assert.assertEquals(isbnno, "9781593277574");

    }


}
