package com.bfr.util;

import com.bfr.control.path.Position;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AutoTransitionerTest {

    @BeforeEach
    void setUp(){
        FTCUtilities.startTestMode();
    }

    @Test
    void writeJSON() throws IOException{
        AutoTransitioner.writeJSON(new Position(12, 17, 90));
    }

    @Test
    void readJSON() throws IOException {
        Position position = AutoTransitioner.readJSON();
    }

}