package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.ui.pages.ProjectPage;
import com.example.teamcity.ui.pages.ProjectsPage;
import com.example.teamcity.ui.pages.admin.CreateProjectPage;
import org.testng.annotations.Test;

import static com.example.teamcity.api.enums.Endpoint.PROJECTS;

@Test(groups = {"Regression"})
public class CreateProjectTest extends BaseUiTest{
    //private static final String REPO_URL = "https://github.com/AlexPshe/spring-core-for-qa";

    @Test(description = "User should be able to create Project", groups = {"Positive"})
    public void userCreatesProject(){
        // подготовка окружения
        loginAs(testData.getUser());

        // взаимодействие с UI
        //"Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)"
        //"Send all project parameters (repository URL)"
        //"Click `Proceed`"
        //"Fix 'Build configuration name: *' values"
        //"Click `Proceed`"
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        // проверка состояния API (корректность отправки данных с UI на API)
        //"Check that all entities (project, build type) was successfully created with correct data on API level"
        var createdProject = superUserCheckRequests.<Project>getRequest(PROJECTS)
                .read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject);

        // add createdProject to TestDataStorage to delete in the end
        TestDataStorage.getStorage().addCreatedEntity(PROJECTS, createdProject);

        // проверка состояния UI (корректность считывания данных и отображение данных на UI)
        //"Check that project is visible on Projects Page (http://localhost:8111/favorite/projects)"
        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));

        var foundProjects = ProjectsPage.open()
                .getProjects().stream()
                .anyMatch(project -> project.getName().text().equals(testData.getProject().getName()));

        softy.assertTrue(foundProjects);
    }

    @Test(description = "User should not be able to craete project without name", groups = {"Negative"})
    public void userCreatesProjectWithoutName() {
/*        // подготовка окружения
        step("Login as user");
        step("Check number of projects");

        // взаимодействие с UI
        step("Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)");
        step("Send all project parameters (repository URL)");
        step("Click `Proceed`");
        step("Set Project Name");
        step("Click `Proceed`");

        // проверка состояния API
        // (корректность отправки данных с UI на API)
        step("Check that number of projects did not change");

        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        step("Check that error appears `Project name must not be empty`");*/
    }
}
