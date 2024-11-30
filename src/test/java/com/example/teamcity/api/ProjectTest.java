package com.example.teamcity.api;

import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specs;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.teamcity.api.enums.Endpoint.PROJECTS;
import static com.example.teamcity.api.enums.Endpoint.USERS;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class ProjectTest extends BaseApiTest {

    @Test(description = "User should be able to create project",
            groups = {"Positive", "CRUD"})
    public void userCreateProject() {
        //Create user
        step("Create user", () -> {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        });

        //Create project by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));

        step("Create project by user", () -> {
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        });

        //Check project was created successfully with correct data
        step("Check project was created successfully with correct data", () -> {
        var createdProject = userCheckRequests.<Project>getRequest(PROJECTS).read("id:" + testData.getProject().getId());
        softy.assertEquals(testData.getProject().getName(), createdProject.getName()
                , "Project name is not correct");
        });
    }

    @Test(description = "User should not be able to create two project with the same ID",
            groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithTheSameId() {
        //Generate projectWithTheSameId
        var projectWithTheSameId = generate(Project.class, testData.getProject().getId());
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        //Create one project by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        //Check project was not created successfully with the same ID
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .create(projectWithTheSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(
                        "Project ID \"%s\" is already used by another project"
                                .formatted(testData.getProject().getId())));
    }

    @Test(description = "User should not be able to create two project with the same Name",
            groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithTheSameName() {
        //Generate projectWithTheSameName
        var projectWithTheSameName = generate(Project.class, RandomData.getString(10), testData.getProject().getName());
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        //Create one project by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        //Check project was not created successfully with the same Name
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .create(projectWithTheSameName)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(
                        "Project with this name already exists: %s".formatted(testData.getProject().getName())));
    }

//Требования для ID
//ID should start with a latin letter and contain only latin letters, digits and underscores (at most 225 characters)

    @DataProvider(name = "validIds")
    public Iterator<Object[]> validIds() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"Project1"});                  // Валидный ID с буквой и цифрой
        data.add(new Object[]{"project_123"});               // Валидный ID с подчеркиванием
        data.add(new Object[]{"P"});                         // Короткий валидный ID из одной буквы
        data.add(new Object[]{"A".repeat(225)});       // Максимально допустимая длина ID

        return data.iterator();
    }

    @Test(description = "User should be able to create project with correct ID",
            groups = {"Positive", "CRUD"},
            dataProvider = "validIds")
    public void userCreateProjectWithValidId(String validId) {
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //Authorize by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));

        //Create project by user
        var project = testData.getProject();
        project.setId(validId);
        userCheckRequests.<Project>getRequest(PROJECTS).create(project);

        //Check project was created successfully with correct data
        var createdProject = userCheckRequests.<Project>getRequest(PROJECTS).read("id:" + testData.getProject().getId());
        softy.assertEquals(testData.getProject().getName(), createdProject.getName()
                , "Project name is not correct");

        System.out.println("====================Next test======================");
    }

    @Test(description = "User should not be able to create project with empty ID",
            groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyId() {
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //Change id project
        var project = testData.getProject();
        project.setId("");

        //Check project was not created successfully with incorrect ID
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .create(project)
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID must not be empty"));
    }

    @DataProvider(name = "invalidIds")
    public Iterator<Object[]> invalidIds() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{"111"});                // Начинается не с латинской буквы
        data.add(new Object[]{"_project"});           // Начинается с подчеркивания
        data.add(new Object[]{"project@123"});        // Содержит недопустимые символы
        data.add(new Object[]{"прjсt123"});           // Содержит нелатинские символы
        data.add(new Object[]{"project!"});           // Содержит спецсимвол
        data.add(new Object[]{"a".repeat(226)});      // Превышает 225 символов

        return data.iterator();
    }

    @Test(description = "User should not be able to create project with incorrect ID",
            groups = {"Negative", "CRUD"},
            dataProvider = "invalidIds")
    public void userCannotCreateProjectWithIncorrectId(String invalidId) {
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //Change id project
        var project = testData.getProject();
        project.setId(invalidId);

        //Check project was not created successfully with incorrect ID
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .create(project)
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID \"%s\" is invalid".formatted(project.getId())));

        System.out.println("====================Next test======================");
    }


    @Test(description = "User should not be able to create project with empty Name",
            groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyName() {
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //Change id project
        var project = testData.getProject();
        project.setName("");

        //Check project was not created successfully with incorrect ID
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .create(project)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project name cannot be empty"));
    }

}

