package com.example.teamcity.api;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specs;
import org.apache.http.HttpStatus;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.teamcity.api.enums.Endpoint.*;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypTest extends BaseApiTest {
    //1.21 Use TestData
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTestUseTestData() {
        //Create user
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //Create project by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        //Create buildType for project by user
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        //Check buildType was created successfully with correct data
        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES)
                .read("id:" + testData.getBuildType().getId());
        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName()
                , "Build type name is not correct");
    }

    //1.21 Use TestData Негативный тест по созданию билд конфигурации
    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTestUseTestData() {
        //step("Create buildTypeWithTheSameId with same id as buildType for project by user");
        var buildTypeWithTheSameId = generate(Arrays.asList(testData.getProject())
                , BuildType.class, testData.getBuildType().getId());

        //step("Create user");
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        //step("Create project by user");
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        //step("Check buildType2 was not created with bad request code");
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specs.authSpec(testData.getUser()), BUILD_TYPES)
                .create(buildTypeWithTheSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(
                        "The build configuration / template ID \"%s\" is already used by another configuration or template"
                                .formatted(testData.getBuildType().getId())));
    }

    //1.19 Улучшаем тест на билд конфигурацию
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest1() {
        //Create user
        var user = generate(User.class);
        superUserCheckRequests.getRequest(USERS).create(user);

        //Create project by user
        var userCheckRequests = new CheckedRequests(Specs.authSpec(user));
        var project = generate(Project.class);
        project = userCheckRequests.<Project>getRequest(PROJECTS).create(project);

        //Create buildType for project by user
        var buildType = generate(Arrays.asList(project), BuildType.class);
        userCheckRequests.getRequest(BUILD_TYPES).create(buildType);

        //Check buildType was created successfully with correct data
        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read("id:" + buildType.getId());
        softy.assertEquals(buildType.getName(), createdBuildType.getName(), "Build type name is not correct");
    }

    //1.18 Тест на билд конфигурацию
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        var user = generate(User.class);
        /* var user = User.builder()
                    .username(RandomData.getString())
                    .password(RandomData.getString())
                    .build();*/

        step("Create user", () -> {
            var requester = new CheckedBase<User>(Specs.superUserSpec(), USERS);
            requester.create(user);
        });

        var project = generate(Project.class);
        AtomicReference<String> projectId = new AtomicReference<>("");

        step("Create project by user", () -> {
            var requester = new CheckedBase<Project>(Specs.authSpec(user), PROJECTS);
            projectId.set(requester.create(project).getId());
        });

        var buildType = generate(BuildType.class);
        buildType.setProject(Project.builder().id(projectId.get()).locator(null).build());

        var requester = new CheckedBase<BuildType>(Specs.authSpec(user), BUILD_TYPES);
        AtomicReference<String> buildTypeId = new AtomicReference<>("");

        step("Create buildType for project by user", () -> {
            buildTypeId.set(requester.create(buildType).getId());
        });

        step("Check buildType was created successfully with correct data", () -> {
            var createdBuildType = requester.read("id:" + buildTypeId.get());

            softy.assertEquals(buildType.getName(), createdBuildType.getName(), "Build type name is not correct");
        });
    }

    //1.20 Hегативный тест по созданию билд конфигурации
    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        //step("Create user");
        var user = generate(User.class);
        superUserCheckRequests.getRequest(USERS).create(user);

        //step("Create project by user");
        var userCheckRequests = new CheckedRequests(Specs.authSpec(user));
        var project = generate(Project.class);
        project = userCheckRequests.<Project>getRequest(PROJECTS).create(project);

        //step("Create buildType1 for project by user");
        var buildType1 = generate(Arrays.asList(project), BuildType.class);
        //step("Create buildType2 with same id as buildType1 for project by user");
        var buildType2 = generate(Arrays.asList(project), BuildType.class, buildType1.getId());

        //step("Check buildType2 was not created with bad request code");
        userCheckRequests.getRequest(BUILD_TYPES).create(buildType1);
        new UncheckedBase(Specs.authSpec(user), BUILD_TYPES)
                .create(buildType2)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(buildType1.getId())));
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        step("Create user");
        step("Create project");
        step("Grant user PROJECT_ADMIN role in project");

        step("Create buildType for project by user (PROJECT_ADMIN)");
        step("Check buildType was created successfully");
    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
        step("Create user1");
        step("Create project1");
        step("Grant user1 PROJECT_ADMIN role in project1");

        step("Create user2");
        step("Create project2");
        step("Grant user2 PROJECT_ADMIN role in project2");

        step("Create buildType for project1 by user2");
        step("Check buildType was not created with forbidden code");
    }

    //video 1.11
/*    @Test
    public void buildconfigurationTest() {
        var user = User.builder()
                .username("admin")
                .password("admin")
                .build();

        var token = RestAssured
                .given()
                .spec(Specs.authSpec(user))
                .get("/authenticationTest.html?crsf")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        System.out.println("My token: " + token);
    }*/
}
