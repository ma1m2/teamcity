package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class BuildConfigurationPage extends BasePage{
    private static final String BUILD_CONFIGURATION_URL = "/buildConfiguration/%s";

    public SelenideElement title = $("h1[class*='BuildTypePageHeader']");

    public static BuildConfigurationPage open(String buildTypeId) {
        return Selenide.open(BUILD_CONFIGURATION_URL.formatted(buildTypeId), BuildConfigurationPage.class);
    }
}
