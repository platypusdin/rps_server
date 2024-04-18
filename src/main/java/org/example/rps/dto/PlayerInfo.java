package org.example.rps.dto;

import org.springframework.util.StringUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerInfo {

    private String playerId;
    private String login;
    private String weapon;

    private final Lock lock = new ReentrantLock();
    private final Condition selectWeapon = lock.newCondition();

    public PlayerInfo(String playerId, String login) {
        this.playerId = playerId;
        this.login = login;

    }

    public String getPlayerId() {
        return playerId;
    }

    public String getLogin() {
        if (login == null) {
            return "";
        }
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        lock.lock();
        try {
            this.weapon = weapon;
            selectWeapon.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String waitForWeapon() throws InterruptedException {
        lock.lock();
        try {
            while (StringUtils.isEmpty(this.weapon)) {
                selectWeapon.await();
            }
            return weapon;
        } finally {
            lock.unlock();
        }
    }

}
