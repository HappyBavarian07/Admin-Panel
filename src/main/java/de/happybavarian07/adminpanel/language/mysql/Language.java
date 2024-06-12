package de.happybavarian07.adminpanel.language.mysql;/*
 * @Author HappyBavarian07
 * @Date 20.02.2024 | 19:26
 */

import java.util.Objects;
import java.util.UUID;

public class Language {
    private final UUID ID;
    private final String languageShort;
    private final String languageName;
    private final String languageVersion;
    private final String languageDescription;
    private final String languageFilePath;

    public Language(UUID ID, String languageShort, String languageName, String languageVersion, String languageDescription, String languageFilePath) {
        this.ID = ID;
        this.languageShort = languageShort;
        this.languageName = languageName;
        this.languageVersion = languageVersion;
        this.languageDescription = languageDescription;
        this.languageFilePath = languageFilePath;
    }

    public UUID getID() {
        return ID;
    }

    public String getLanguageShort() {
        return languageShort;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageVersion() {
        return languageVersion;
    }

    public String getLanguageDescription() {
        return languageDescription;
    }

    public String getLanguageFilePath() {
        return languageFilePath;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Language language = (Language) obj;
        return Objects.equals(ID, language.ID) &&
                Objects.equals(languageShort, language.languageShort) &&
                Objects.equals(languageName, language.languageName) &&
                Objects.equals(languageVersion, language.languageVersion) &&
                Objects.equals(languageDescription, language.languageDescription) &&
                Objects.equals(languageFilePath, language.languageFilePath);
    }
}
