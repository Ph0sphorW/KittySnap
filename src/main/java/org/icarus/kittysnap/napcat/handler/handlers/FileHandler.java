package org.icarus.kittysnap.napcat.handler.handlers;

import org.icarus.kittysnap.config.MessagesConfig;
import org.icarus.kittysnap.napcat.onebot.OB11MessageFile;

public class FileHandler {
    public static String fileHandler(OB11MessageFile file,
            MessagesConfig messages){
        return messages.getSegment().getFileText().formatted(file.getFileName());
    }
}
