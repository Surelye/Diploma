package sgu.borodin.nas.enums;

import lombok.Getter;

@Getter
public enum Operation {
    MOVE("move"),
    COPY("copy");

    private final String value;

    Operation(String value) {
        this.value = value;
    }
}
