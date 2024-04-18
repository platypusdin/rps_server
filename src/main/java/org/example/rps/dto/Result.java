package org.example.rps.dto;

public enum Result {

    WIN("You win!"),
    LOSE("You lose!"),
    DRAW("It's a draw!"),
    ERROR("Something is going wrong. Please try again.");

    private final String message;

    Result(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
