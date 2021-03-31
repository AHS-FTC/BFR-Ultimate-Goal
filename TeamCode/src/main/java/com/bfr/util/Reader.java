package com.bfr.util;

import android.util.JsonReader;

import com.bfr.control.path.Position;

import java.io.FileReader;
import java.io.IOException;

public class Reader {
    public static Position readJSON() throws IOException {
        double x = 0, y = 0, h = Math.toRadians(-90);

        JsonReader reader = new JsonReader(new FileReader(FTCUtilities.getLogDirectory()+"/yeet.json"));

        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "x":
                    x = reader.nextDouble();
                    break;
                case "y":
                    y = reader.nextDouble();
                    break;
                case "h":
                    h = reader.nextDouble();
                case "color":
                    String color = reader.nextString();
                    if (color.equals("b")){
                        FTCUtilities.setAllianceColor(AllianceColor.BLUE);
                    } else {
                        FTCUtilities.setAllianceColor(AllianceColor.RED);
                    }
                    break;
            }
        }
        return new Position(x,y,h);
    }
}
