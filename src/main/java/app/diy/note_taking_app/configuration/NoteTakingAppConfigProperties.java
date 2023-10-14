package app.diy.note_taking_app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("note-taking")
public record NoteTakingAppConfigProperties(String decodeSecretKey) {
}
