package com.glez.tmo_service.utils;

import com.glez.tmo_service.functions.TMOChapterProcessor;
import jakarta.annotation.PreDestroy;

public class TMOFans extends TMOChapterProcessor {

    public TMOFans() {
        super();
        login();
    }

    @PreDestroy
    public void destroy() {
        logout();
    }

}

