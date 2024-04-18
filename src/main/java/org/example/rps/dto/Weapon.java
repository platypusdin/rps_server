package org.example.rps.dto;

public enum Weapon {

    ROCK,
    PAPER,
    SCISSORS;

    public static boolean isValid(String attack) {
        for (Weapon type : Weapon.values()) {
            if (type.name().equalsIgnoreCase(attack)) {
                return true;
            }
        }
        return false;
    }

    public Result battleResult(Weapon opponentWeapon) {
        if (this == opponentWeapon) {
            return Result.DRAW;
        }
        switch (this) {
            case ROCK:
                return (opponentWeapon == SCISSORS ? Result.WIN : Result.LOSE);
            case PAPER:
                return (opponentWeapon == ROCK ? Result.WIN : Result.LOSE);
            case SCISSORS:
                return (opponentWeapon == PAPER ? Result.WIN : Result.LOSE);
        }
        return Result.ERROR;
    }
}
