package com.example.teamcity.api;


import com.example.teamcity.api.models.User;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import io.restassured.RestAssured;
import org.testng.annotations.Test;

public class BuildConfigurationTest extends BaseApiTest{
    //"http://admin:admin@172.18.144.1:8111/authenticationTest.html?crsf"
    @Test
    public void buildconfigurationTest(){
        var user = User.builder()
                .username("admin")
                .password("admin")
                .build();

        var token = RestAssured
                .given()
                .spec(Specifications.getSpec().authSpec(user))
                .get("/authenticationTest.html?crsf")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        System.out.println("My token: " + token);
    }
}
