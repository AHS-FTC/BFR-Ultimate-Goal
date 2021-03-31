package com.bfr.util;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.bfr.control.path.Position;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AutoTransitioner {
    public static Position readJSON() throws IOException {
        double x = 0, y = 0, h = Math.toRadians(-90);

        JsonReader reader = new JsonReader(new FileReader(FTCUtilities.getLogDirectory()+"/transfer.json"));

        reader.beginObject();
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
                    break;
                case "isBlue":
                    boolean isBlue = reader.nextBoolean();
                    if (isBlue){
                        FTCUtilities.setAllianceColor(AllianceColor.BLUE);
                    } else {
                        FTCUtilities.setAllianceColor(AllianceColor.RED);
                    }
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        reader.close();
        return new Position(x,y,h);
    }

    public static void writeJSON(Position position) throws IOException{
        JsonWriter writer = new JsonWriter(new FileWriter(FTCUtilities.getLogDirectory()+"/transfer.json"));
        writer.beginObject();
        writer.name("x").value(position.x);
        writer.name("y").value(position.y);
        writer.name("h").value(position.heading);

        boolean isBlue = FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE);
        writer.name("isBlue").value(isBlue);

        writer.endObject();

        writer.flush();
        writer.close();
    }
}
