package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specs;
import com.example.teamcity.ui.pages.BuildConfigurationPage;
import com.example.teamcity.ui.pages.admin.CreateBuildTypePage;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static com.example.teamcity.api.enums.Endpoint.PROJECTS;

@Test(groups = {"Regression"})
public class CreateBuildTypeTest  extends BaseUiTest{

    @Test(description = "User should be able to create Build Type", groups = {"Positive"})
    public void userCreatesBuildType(){
        // подготовка окружения
        loginAs(testData.getUser());

        //Create project via API
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        // взаимодействие с UI
/*        step("Open `Create Build Configuration` (http://localhost:8111/admin/createObjectMenu.html)");
        step("Fill in 'Repository URL*' field ");
        step("Click `Proceed`");
        step("Fix Project Name and Build Type name values");
        step("Click `Proceed`");*/

        CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(REPO_URL)
                .setupBuildType(testData.getBuildType().getName());

        // проверка состояния API (корректность отправки данных с UI на API)
        var createdBuildType = superUserCheckRequests.<BuildType>getRequest(Endpoint.BUILD_TYPES)
                .read("name:" + testData.getBuildType().getName());
        softy.assertNotNull(createdBuildType);

        // проверка состояния UI (корректность считывания данных и отображение данных на UI)
        //"Check that Build Type is visible on Build Configuration Page (http://localhost:8111/buildConfiguration/{buildTypeId})"
        BuildConfigurationPage.open(createdBuildType.getId())
                .title.shouldHave(Condition.exactText(testData.getBuildType().getName()));
    }

    @Test(description = "User should not be able to create Build Type with empty RepoUrl", groups = {"Negative"})
    public void userCanNotCreatesBuildTypeWithEmptyUrl(){
        // подготовка окружения
        loginAs(testData.getUser());

        //Create project via API
        var userCheckRequests = new CheckedRequests(Specs.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        //Try to create Build Type with empty repo Url
        CreateBuildTypePage.open(testData.getProject().getId())
                .getErrorEmptyRepoUrl().errorUrl.shouldHave(Condition.exactText("URL must not be empty"));

        //Check that list of BuildTypes of project is empty /app/rest/projects/<projectLocator>/buildTypes
        new UncheckedBase(Specs.authSpec(testData.getUser()), PROJECTS)
                .read(testData.getProject().getId() + "/buildTypes")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .body("count", Matchers.equalTo(0));

    }

}
