package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class CreateBuildTypePage  extends CreateBasePage{

    private static final String BUILD_TYPE_SHOW_MODE = "createBuildTypeMenu";

    private SelenideElement successfulMessage = $("#unprocessed_objectsCreated");
    public SelenideElement errorUrl = $("#error_url");


    public static CreateBuildTypePage open(String projectId) {
        return Selenide.open(CREATE_URL.formatted(projectId, BUILD_TYPE_SHOW_MODE), CreateBuildTypePage.class);
    }

    public CreateBuildTypePage createForm(String url) {
        baseCreateForm(url);
        return this;
    }

    public BuildTypePage setupBuildType(String buildTypeName) {
        buildTypeNameInput.val(buildTypeName);
        submitButton.click();
        successfulMessage.shouldBe(Condition.visible, BASE_WAITING);
        return page(BuildTypePage.class);
    }

    public CreateBuildTypePage getErrorEmptyRepoUrl(){
        submitButton.click();
        errorUrl.shouldBe(Condition.visible,BASE_WAITING);
        return page(CreateBuildTypePage.class);
    }

}
