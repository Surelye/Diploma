package sgu.borodin.nas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class PatchContext {
    @NotNull(message = "Patch operation is required")
    private Action operation;

    @NotNull(message = "Update field is required")
    private UpdateField field;

    private String value;

    public enum Action {
        UPDATE, RESET
    }

    @Getter
    public enum UpdateField {
        USERNAME("username"),
        PASSWORD("password"),
        NOTES("notes");

        private final String value;

        UpdateField(String value) {
            this.value = value;
        }
    }
}
