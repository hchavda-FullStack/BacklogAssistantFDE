package com.fde.backlogassistant.service;

@FunctionalInterface
interface Sleeper {
    void sleep(long milliseconds) throws InterruptedException;
}
