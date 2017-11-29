package ru.bomber.game.mm;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ru.bomber.game.model.GameSession;
import ru.bomber.game.service.BomberService;

import java.io.IOException;
import java.util.Date;


import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.bomber.game.mm.MmController.MAX_PLAYER_IN_GAME;

public class StartThread extends Thread {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(StartThread.class);
    private Integer gameId;
    private boolean suspendFlag;
    static final int TIMEOUT = 30;
    static final int MAX_TIMEOUTS = 3;
    private boolean isStarted;
    private BomberService bomberService;


    public StartThread(Integer gameId, BomberService bomberService) {
        super("StartThread_gameId=" + gameId);
        suspendFlag = false;
        this.gameId = gameId;
        isStarted = false;
    }

    @Override
    public void run() {

        while (suspendFlag) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.info("Wait of thread={} interrupted", currentThread());
            }
        }
        int tryCounter = 0;
        while (tryCounter++ < MAX_TIMEOUTS
                && !isStarted) {
            try {
                if (Integer.parseInt(Requests.checkStatus().body().string()) == MAX_PLAYER_IN_GAME) {
                    bomberService.addTodb(gameId, ConnectionQueue.getInstance(), new Date());
                    log.info("Sending a request to start the game, gameID = {}", gameId);
                    Requests.start(this.gameId.toString());
                    isStarted = true;
                } else {
                    log.info("Timeout for {} SECONDS, waiting for players to CONNECT. {} TIMEOUTS left",
                            TIMEOUT, MAX_TIMEOUTS - tryCounter);
                    sleep(SECONDS.toMillis(TIMEOUT));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.info("Sleep of thread={} interrupted", currentThread());
            }
        }
        if (!isStarted)
            log.info("failed to start the game");
        MmController.clear();
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public synchronized void suspendThread() throws InterruptedException {
        suspendFlag = true;
    }

    public synchronized void resumeThread() throws InterruptedException {
        suspendFlag = false;
        notify();
    }
}